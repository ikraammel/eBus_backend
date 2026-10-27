"""Manage CI containers; never print database configuration or raw application logs."""
import base64
import json
import os
from pathlib import Path
import secrets
import subprocess
import sys
import time
from urllib.request import urlopen

REQUIRED = ('DB_URL', 'DB_USERNAME', 'DB_PASSWORD', 'EBUS_USER_EMAIL',
            'EBUS_USER_PASSWORD', 'EBUS_ADMIN_EMAIL', 'EBUS_ADMIN_PASSWORD')
REPORTS = Path('ci-reports')


def docker(*args, check=True, environment=None):
    result = subprocess.run(['docker', *args], env=environment, capture_output=True, text=True)
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


def start():
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
    docker('run', '-d', '--name', 'ebus-ci-frontend', '-p', '127.0.0.1:18082:8080', 'ebus-frontend:ci')
    print('Application containers started; mail and Stripe use disabled test settings.')


def ready():
    pending = {'backend': 'http://127.0.0.1:18081/api/health', 'frontend': 'http://127.0.0.1:18082/'}
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


def diagnose():
    raw = docker('logs', '--tail', '150', 'ebus-ci-backend', check=False)
    text = (raw.stdout + raw.stderr).lower()
    categories = {
        'database authentication': ('password authentication failed',),
        'database network/DNS': ('unknownhostexception', 'connection timed out', 'connection refused', 'network is unreachable'),
        'schema mismatch': ('schema-validation', 'schemamanagementexception'),
        'missing setting': ('could not resolve placeholder',),
    }
    found = [name for name, patterns in categories.items() if any(pattern in text for pattern in patterns)]
    REPORTS.mkdir(exist_ok=True)
    (REPORTS / 'startup.json').write_text(json.dumps({'diagnostic': found or ['inspect application configuration; raw logs withheld']}))
    print('Startup diagnostic: ' + ', '.join(found or ['unclassified failure; raw logs withheld']))


def stop():
    for name in ('ebus-ci-backend', 'ebus-ci-frontend'):
        docker('rm', '-f', name, check=False)
    print('CI application containers removed; remote database untouched by cleanup.')


if __name__ == '__main__':
    try:
        {'validate': validate, 'start': start, 'ready': ready, 'stop': stop}[sys.argv[1]]()
    except Exception as error:
        # Only known local messages are emitted. Docker and network exceptions are hidden.
        print(str(error) if isinstance(error, (ValueError, RuntimeError)) else type(error).__name__)
        raise SystemExit(1)
