"""Manage CI containers; never print database configuration or raw application logs."""
import base64
import json
import os
from pathlib import Path
import re
import secrets
import subprocess
import sys
import time
from urllib.request import urlopen
from urllib.parse import urlsplit

REQUIRED = ('DB_URL', 'DB_USERNAME', 'DB_PASSWORD', 'EBUS_USER_EMAIL',
            'EBUS_USER_PASSWORD', 'EBUS_ADMIN_EMAIL', 'EBUS_ADMIN_PASSWORD')
REPORTS = Path('ci-reports')


def docker(*args, check=True, environment=None):
    try:
        result = subprocess.run(['docker', *args], env=environment, capture_output=True, text=True,
                                encoding='utf-8', errors='replace', timeout=60)
    except subprocess.TimeoutExpired:
        raise RuntimeError('Docker daemon operation timed out; private details omitted') from None
    if check and result.returncode:
        raise RuntimeError('Docker operation failed; private details omitted')
    return result


def validate():
    missing = [name for name in REQUIRED if not os.environ.get(name)]
    if missing:
        raise ValueError('Missing GitHub Secrets: ' + ', '.join(missing))
    if not os.environ['DB_URL'].startswith('jdbc:postgresql://'):
        raise ValueError('DB_URL must be a JDBC PostgreSQL URL')
    if os.environ.get('JWT_SECRET'):
        try:
            key = base64.b64decode(os.environ['JWT_SECRET'], validate=True)
        except Exception:
            raise ValueError('JWT_SECRET must be Base64 encoded; value omitted') from None
        if len(key) < 32:
            raise ValueError('JWT_SECRET must decode to at least 32 bytes')
    print('Seven required secrets are configured; values omitted.')


def start_backend():
    validate()
    environment = os.environ.copy()
    environment['JWT_SECRET'] = environment.get('JWT_SECRET') or base64.b64encode(secrets.token_bytes(48)).decode()
    settings = {
        'PORT': '8080', 'MAIL_USERNAME': 'qa@example.invalid', 'MAIL_PASSWORD': 'unused-ci',
        'SPRING_MAIL_HOST': '127.0.0.1', 'SPRING_MAIL_PORT': '1025',
        'SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH': 'false',
        'SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE': 'false',
        'STRIPE_SECRET_KEY': 'sk_test_disabled_ci', 'STRIPE_WEBHOOK_SECRET': 'whsec_disabled_ci',
        'SPRING_JPA_HIBERNATE_DDL_AUTO': 'validate', 'SPRING_JPA_SHOW_SQL': 'false',
        'SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE': '3',
        'SPRING_DATASOURCE_HIKARI_READ_ONLY': 'true',
    }
    args = ['run', '-d', '--name', 'ebus-ci-backend', '-p', '127.0.0.1:18081:8080']
    for name in ('DB_URL', 'DB_USERNAME', 'DB_PASSWORD', 'JWT_SECRET'):
        args += ['-e', name]
    for name, value in settings.items():
        args += ['-e', name + '=' + value]
    docker(*args, 'ebus-backend:ci', environment=environment)


def start():
    start_backend()
    docker('run', '-d', '--name', 'ebus-ci-frontend', '-p', '127.0.0.1:18082:8080', 'ebus-frontend:ci')
    print('Application containers started; mail and Stripe use disabled test settings.')


def ready(backend_only=False):
    pending = {'backend': 'http://127.0.0.1:18081/api/health', 'frontend': 'http://127.0.0.1:18082/'}
    if backend_only:
        pending.pop('frontend')
    deadline = time.monotonic() + 180
    while pending and time.monotonic() < deadline:
        for name, url in list(pending.items()):
            state = docker('inspect', '--format', '{{.State.Status}}', 'ebus-ci-' + name, check=False)
            if state.returncode == 0 and state.stdout.strip() in ('exited', 'dead'):
                diagnose()
                raise RuntimeError(name + ' exited before readiness')
            try:
                with urlopen(url, timeout=5) as response:
                    body = response.read()
                    if response.status == 200 and (name != 'backend' or body.strip() == b'UP'):
                        del pending[name]
                        print(name + ' readiness: PASS')
            except Exception:
                pass
        if pending:
            time.sleep(3)
    if pending:
        diagnose()
        raise RuntimeError('Readiness timeout: ' + ', '.join(pending))


def redact(message, private_values=()):
    for value in sorted(set(private_values), key=len, reverse=True):
        if value and len(value) >= 3:
            message = message.replace(value, '[REDACTED]')
    message = re.sub(r'(?:jdbc:)?(?:postgresql|postgres|https?)://[^\s\]\)\'\"]+', '[URL REDACTED]', message)
    message = re.sub(r'(?i)((?:password|secret|token|api[-_]?key)\s*[=:]\s*)[^\s,;]+', r'\1[REDACTED]', message)
    message = re.sub(r'\beyJ[A-Za-z0-9_-]+\.[A-Za-z0-9_-]+\.[A-Za-z0-9_-]+\b', '[JWT REDACTED]', message)
    message = re.sub(r'[A-Za-z0-9_.+\-]+@[A-Za-z0-9.\-]+\.[A-Za-z]{2,}', '[EMAIL REDACTED]', message)
    return message


def diagnostic_details(logs, private_values=()):
    lines = logs.splitlines()
    selected = []
    for index, line in enumerate(lines):
        if 'Caused by:' in line or re.search(r'\b(?:[\w$]+\.)+[\w$]*(?:Exception|Error)\b', line):
            selected.append(line.strip())
        elif line.strip() in ('Description:', 'Action:'):
            selected.extend(lines[index:index + 5])
    return [redact(line, private_values)[:1200] for line in selected[-16:] if line.strip()]


def diagnose():
    raw = docker('logs', '--tail', '500', 'ebus-ci-backend', check=False)
    logs = raw.stdout + raw.stderr
    text = logs.lower()
    private_values = [value for key, value in os.environ.items()
                      if any(part in key for part in ('PASSWORD','SECRET','TOKEN','DB_URL','DB_USERNAME','EMAIL'))]
    # Read values privately from the container too: readiness runs in a separate step.
    environment = docker('inspect', '--format', '{{json .Config.Env}}', 'ebus-ci-backend', check=False)
    try:
        for setting in json.loads(environment.stdout):
            key, _, value = setting.partition('=')
            if any(part in key for part in ('PASSWORD','SECRET','TOKEN','DB_URL','DB_USERNAME','MAIL_USERNAME')):
                private_values.append(value)
            if key == 'DB_URL':
                try:
                    parsed = urlsplit(value.removeprefix('jdbc:'))
                    private_values.extend(item for item in (parsed.hostname, parsed.username, parsed.password) if item)
                except ValueError:
                    pass
    except (ValueError, TypeError):
        pass
    state_result = docker('inspect', '--format', '{{json .State}}', 'ebus-ci-backend', check=False)
    try:
        state = json.loads(state_result.stdout)
        state_summary = {key: state.get(key) for key in ('Status', 'ExitCode', 'OOMKilled')}
    except (ValueError, TypeError):
        state_summary = {'Status': 'unavailable'}
    categories = {
        'database authentication': ('password authentication failed', 'tenant or user not found', 'authentication failed', 'role does not exist'),
        'database network/DNS': ('unknownhostexception', 'connection timed out', 'connection refused', 'network is unreachable', 'connectexception', 'the connection attempt failed'),
        'database TLS': ('sslhandshakeexception', 'pkix path building failed', 'ssl error'),
        'schema mismatch': ('schema-validation', 'schemamanagementexception'),
        'missing setting': ('could not resolve placeholder',),
        'read-only transaction': ('read-only transaction', 'read only transaction'),
        'filesystem permissions': ('accessdeniedexception', 'permission denied', 'read-only file system'),
        'application configuration': ('configurationpropertiesbindexception', 'could not bind properties'),
    }
    found = [name for name, patterns in categories.items() if any(pattern in text for pattern in patterns)]
    if state_summary.get('OOMKilled'):
        found.append('container memory limit')
    details = diagnostic_details(logs, private_values)
    report = {'diagnostic': found or ['unclassified failure'], 'container': state_summary,
              'details_redacted': details, 'raw_logs_published': False}
    REPORTS.mkdir(exist_ok=True)
    (REPORTS / 'startup.json').write_text(json.dumps(report, indent=2), encoding='utf-8')
    print('Startup diagnostic: ' + ', '.join(found or ['unclassified failure; raw logs withheld']))
    print('Container state: ' + json.dumps(state_summary))
    for line in details:
        print(line)


def stop():
    for name in ('ebus-ci-backend', 'ebus-ci-frontend'):
        docker('rm', '-f', name, check=False)
    print('CI application containers removed; remote database untouched by cleanup.')


if __name__ == '__main__':
    try:
        {'validate': validate, 'start': start, 'start-backend': start_backend, 'ready': ready,
         'diagnose': diagnose, 'stop': stop}[sys.argv[1]]()
    except Exception as error:
        # Only known local messages are emitted. Docker and network exceptions are hidden.
        print(str(error) if isinstance(error, (ValueError, RuntimeError)) else type(error).__name__)
        raise SystemExit(1)
