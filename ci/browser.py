"""Read/login journeys with safe diagnostics; no authenticated screenshots or traces."""
import json
import argparse
import os
from pathlib import Path
import sys
from urllib.parse import urlsplit
from playwright.sync_api import sync_playwright, expect
sys.path.insert(0, str(Path(__file__).resolve().parent))
from runtime import redact

API = 'http://127.0.0.1:18081'
UI = 'http://127.0.0.1:18082'
ALLOWED_ORIGINS = (API, 'https://ebus-backend-ao2u.onrender.com')


class FrontendBootstrapError(RuntimeError):
    pass


def permitted(method, path):
    return method in ('GET','HEAD','OPTIONS') or (method == 'POST' and path == '/users/login')


def ui_label(page, value, exact=True):
    # Flutter may expose a label as DOM text or solely as an accessible name.
    return page.get_by_text(value, exact=exact).or_(page.get_by_label(value, exact=exact)).last


def enter_text(field, value):
    field.click()
    expect(field).to_be_focused()
    field.fill('')
    # Give Flutter's editing controller actual keyboard events at a human cadence.
    field.press_sequentially(value, delay=40)
    expect(field).to_have_value(value)
    field.press('Tab')


def login_payload_checks(payload, role):
    valid = isinstance(payload, dict)
    return {'json_object': valid,
            'email_matches_secret': valid and payload.get('email') == os.environ.get('EBUS_' + role + '_EMAIL'),
            'password_matches_secret': valid and payload.get('password') == os.environ.get('EBUS_' + role + '_PASSWORD')}


def resource_kind(url):
    parsed = urlsplit(url)
    if parsed.scheme + '://' + parsed.netloc in ALLOWED_ORIGINS:
        return 'backend'
    if 'canvaskit' in parsed.path or 'skwasm' in parsed.path:
        return 'flutter-engine'
    if parsed.hostname in ('fonts.googleapis.com', 'fonts.gstatic.com'):
        return 'fonts'
    if parsed.hostname and ('firebase' in parsed.hostname or 'googleapis' in parsed.hostname):
        return 'firebase/google'
    return 'frontend' if parsed.hostname == '127.0.0.1' else 'other'


def journey(browser, role):
    context = browser.new_context(service_workers='block', viewport={'width': 1280, 'height': 1000})
    context.add_init_script("""(() => {
        window.__ebusUIReady = false;
        const failed = () => {
            if (!window.__ebusUIReady && document.documentElement) {
                document.documentElement.setAttribute('data-ebus-bootstrap-failed', 'true');
            }
        };
        addEventListener('error', failed);
        addEventListener('unhandledrejection', failed);
    })();""")
    page = context.new_page()
    observed = {'responses': 0, 'login': False, 'lines': False}
    diagnostics = {'failed_resources': {}, 'javascript_error_types': [], 'api_statuses': {},
                   'api_requests': {}, 'route_errors': 0}
    stage = 'open-frontend'
    private_values = [value for name, value in os.environ.items() if any(
        part in name for part in ('PASSWORD','SECRET','TOKEN','EMAIL','DB_URL','DB_USERNAME'))]

    def failed_request(request):
        kind = resource_kind(request.url)
        diagnostics['failed_resources'][kind] = diagnostics['failed_resources'].get(kind, 0) + 1

    def page_error(error):
        message = str(error)
        kind = next((name for name in ('WebAssembly','CanvasKit','FirebaseError','TypeError','ReferenceError') if name in message), getattr(error, 'name', 'Error'))
        diagnostics['javascript_error_types'].append(kind)
        if stage in ('open-frontend','enable-flutter-semantics','wait-login-screen'):
            diagnostics.setdefault('bootstrap_errors_redacted', []).append(redact(message, private_values)[:2000])

    page.on('requestfailed', failed_request)
    page.on('pageerror', page_error)

    def route_request(route):
        request = route.request
        parsed = urlsplit(request.url)
        origin = parsed.scheme + '://' + parsed.netloc
        if not permitted(request.method, parsed.path):
            route.abort('blockedbyclient')
            return
        if origin in ALLOWED_ORIGINS:
            # Real response from the image under test; preserve the compiled public API path.
            target = API + parsed.path + ('?' + parsed.query if parsed.query else '')
            endpoint = {'/api/health': 'health', '/users/login': 'login', '/lignes': 'lines'}.get(parsed.path, 'other')
            diagnostics['api_requests'][endpoint] = diagnostics['api_requests'].get(endpoint, 0) + 1
            if endpoint == 'login' and request.method == 'POST':
                try:
                    diagnostics['login_payload_checks'] = login_payload_checks(request.post_data_json, role)
                except Exception:
                    diagnostics['login_payload_checks'] = {'json_object': False}
            response = route.fetch(url=target, max_redirects=0, timeout=45000)
            observed['responses'] += 1
            diagnostics['api_statuses'][endpoint] = response.status
            if parsed.path == '/users/login' and request.method == 'POST':
                observed['login'] = response.status == 200 and response.json().get('user', {}).get('role') == role
            if parsed.path == '/lignes' and response.status == 200:
                observed['lines'] = isinstance(response.json(), list)
            route.fulfill(response=response, headers={**response.headers, 'access-control-allow-origin': '*'})
        elif request.method == 'POST':
            route.abort('blockedbyclient')
        else:
            route.continue_()

    def guarded_route(route):
        try:
            route_request(route)
        except Exception:
            diagnostics['route_errors'] += 1
            # Suppress event-handler traceback text that might contain session headers.
            try:
                route.abort('failed')
            except Exception:
                pass

    page.route('**/*', guarded_route)
    try:
        print(role + ': open frontend', flush=True)
        page.goto(UI, wait_until='domcontentloaded', timeout=60000)
        stage = 'enable-flutter-semantics'
        print(role + ': enable Flutter semantics', flush=True)
        page.locator('flt-semantics-placeholder, flt-semantics[aria-label], html[data-ebus-bootstrap-failed]').first.wait_for(state='attached', timeout=90000)
        if page.locator('html[data-ebus-bootstrap-failed]').count() and not page.locator('flt-semantics-placeholder, flt-semantics').count():
            raise FrontendBootstrapError('JavaScript startup failed before Flutter created its view')
        placeholder = page.locator('flt-semantics-placeholder')
        if placeholder.count():
            placeholder.first.evaluate('(element)=>element.click()')
        stage = 'wait-login-screen'
        print(role + ': wait for login screen', flush=True)
        expect(ui_label(page, 'Bienvenue sur eBus', exact=False)).to_be_visible(timeout=90000)
        page.evaluate('window.__ebusUIReady = true')
        if role == 'GUEST':
            stage = 'enter-guest-mode'
            ui_label(page, "Continuer en tant qu'invité").click()
        else:
            if role == 'ADMIN':
                stage = 'open-admin-login'
                ui_label(page, 'Accès administrateur').click()
            stage = 'enter-credentials'
            email = page.get_by_role('textbox').first
            enter_text(email, os.environ['EBUS_' + role + '_EMAIL'])
            password = page.locator('input[type="password"]')
            enter_text(password, os.environ['EBUS_' + role + '_PASSWORD'])
            stage = 'submit-login'
            with page.expect_response(lambda response: urlsplit(response.url).path == '/users/login' and response.request.method == 'POST', timeout=60000):
                ui_label(page, 'Connexion Admin' if role == 'ADMIN' else 'Se Connecter').click()
            if not observed['login']:
                raise AssertionError('Login role/status mismatch')
        if role == 'ADMIN':
            stage = 'wait-admin-home'
            expect(ui_label(page, 'Gestion rapide')).to_be_visible(timeout=60000)
        else:
            stage = 'wait-user-or-guest-home'
            expect(ui_label(page, 'Accès rapide')).to_be_visible(timeout=60000)
            stage = 'load-lines-from-backend'
            if observed['lines']:
                ui_label(page, 'Lignes').click()
            else:
                with page.expect_response(lambda response: urlsplit(response.url).path == '/lignes' and response.request.method == 'GET', timeout=60000) as lines_response:
                    ui_label(page, 'Lignes').click()
                if lines_response.value.status != 200:
                    raise AssertionError('Lines API status mismatch')
            stage = 'wait-lines-page'
            expect(ui_label(page, 'Lignes de bus')).to_be_visible(timeout=60000)
            if not observed['lines']:
                raise AssertionError('No successful real-backend lines response observed')
        if not observed['responses']:
            raise AssertionError('No response from backend image observed')
        return {'name': role + ' journey', 'pass': True, 'diagnostics': diagnostics}
    except Exception as error:
        # Playwright exception text can include values typed into fields; omit it.
        try:
            diagnostics['flutter_dom'] = page.evaluate("""() => ({
                ready: document.readyState,
                flutterViews: document.querySelectorAll('flutter-view,flt-glass-pane').length,
                placeholders: document.querySelectorAll('flt-semantics-placeholder').length,
                semantics: document.querySelectorAll('flt-semantics').length,
                canvas: document.querySelectorAll('canvas').length
            })""")
            diagnostics['known_labels'] = {value: ui_label(page, value).count() for value in
                ('Bienvenue sur eBus', 'Accès rapide', 'Lignes de bus', 'Gestion rapide', 'Connexion requise')}
            diagnostics['lines_heading_nodes'] = page.get_by_text('Lignes de bus', exact=True).or_(page.get_by_label('Lignes de bus', exact=True)).evaluate_all("""items => items.map(element => {
                const rect = element.getBoundingClientRect();
                return {tag: element.tagName, role: element.getAttribute('role'),
                    width: rect.width, height: rect.height, visibility: getComputedStyle(element).visibility};
            })""")
            if role == 'GUEST':
                Path('ci-reports').mkdir(exist_ok=True)
                page.screenshot(path='ci-reports/guest-failure.png')
        except Exception:
            pass
        return {'name': role + ' journey', 'pass': False, 'error_type': type(error).__name__,
                'failed_stage': stage, 'diagnostics': diagnostics}
    finally:
        context.close()


def main(roles=('GUEST','USER','ADMIN')):
    with sync_playwright() as playwright:
        channel = os.environ.get('EBUS_BROWSER_CHANNEL', 'chromium')
        if channel not in ('chrome','chromium'):
            raise ValueError('Unsupported browser channel')
        browser = playwright.chromium.launch(headless=True, channel='chrome' if channel == 'chrome' else None,
            args=['--use-gl=angle', '--use-angle=swiftshader', '--enable-unsafe-swiftshader'])
        browser_version = browser.version
        try:
            checks = [journey(browser, role) for role in roles]
        finally:
            browser.close()
    folder = Path('ci-reports')
    folder.mkdir(exist_ok=True)
    (folder / 'playwright.json').write_text(json.dumps({'browser_channel': channel, 'browser_version': browser_version, 'checks': checks}, indent=2))
    for item in checks:
        print(item['name'] + ': ' + ('PASS' if item['pass'] else 'FAIL (' + item['error_type'] + ', stage=' + item['failed_stage'] + ')'))
        if not item['pass']:
            print(json.dumps(item['diagnostics']))
    return int(any(not item['pass'] for item in checks))


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--role', action='append', choices=('GUEST','USER','ADMIN'))
    arguments = parser.parse_args()
    raise SystemExit(main(tuple(arguments.role) if arguments.role else ('GUEST','USER','ADMIN')))
