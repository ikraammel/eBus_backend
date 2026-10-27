"""Three read/login browser journeys; no traces, screenshots, credentials or JWT in reports."""
import json
import os
from pathlib import Path
import re
from urllib.parse import urlsplit
from playwright.sync_api import sync_playwright, expect

API = 'http://127.0.0.1:18081'
UI = 'http://127.0.0.1:18082'
ALLOWED_ORIGINS = (API, 'https://ebus-backend-ao2u.onrender.com')


def permitted(method, path):
    return method in ('GET','HEAD','OPTIONS') or (method == 'POST' and path == '/users/login')


def journey(browser, role):
    context = browser.new_context(service_workers='block', viewport={'width': 1280, 'height': 1000})
    page = context.new_page()
    observed = {'responses': 0, 'login': False, 'lines': False}

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
            response = route.fetch(url=target, max_redirects=0, timeout=45000)
            observed['responses'] += 1
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
            # Suppress event-handler traceback text that might contain session headers.
            try:
                route.abort('failed')
            except Exception:
                pass

    page.route('**/*', guarded_route)
    try:
        page.goto(UI, wait_until='domcontentloaded', timeout=60000)
        page.locator('flt-semantics-placeholder, flt-semantics[aria-label]').first.wait_for(state='attached', timeout=90000)
        placeholder = page.locator('flt-semantics-placeholder')
        if placeholder.count():
            placeholder.first.evaluate('(element)=>element.click()')
        expect(page.get_by_text('Bienvenue sur eBus', exact=False)).to_be_visible(timeout=90000)
        if role == 'GUEST':
            page.get_by_text("Continuer en tant qu'invité", exact=True).last.click()
        else:
            if role == 'ADMIN':
                page.get_by_text('Accès administrateur', exact=True).last.click()
            email = page.get_by_role('textbox').first
            email.click()
            email.press_sequentially(os.environ['EBUS_' + role + '_EMAIL'])
            password = page.locator('input[type="password"]')
            password.click()
            password.press_sequentially(os.environ['EBUS_' + role + '_PASSWORD'])
            with page.expect_response(lambda response: urlsplit(response.url).path == '/users/login' and response.request.method == 'POST', timeout=60000):
                page.get_by_text('Connexion Admin' if role == 'ADMIN' else 'Se Connecter', exact=True).last.click()
            if not observed['login']:
                raise AssertionError('Login role/status mismatch')
        if role == 'ADMIN':
            expect(page.get_by_text(re.compile('Dossiers|Gestion des dossiers')).first).to_be_visible(timeout=60000)
        else:
            page.get_by_text('Lignes', exact=True).last.click()
            expect(page.get_by_text('Lignes de bus', exact=True).last).to_be_visible(timeout=60000)
            if not observed['lines']:
                raise AssertionError('No successful real-backend lines response observed')
        if not observed['responses']:
            raise AssertionError('No response from backend image observed')
        return {'name': role + ' journey', 'pass': True}
    except Exception as error:
        # Playwright exception text can include values typed into fields; omit it.
        return {'name': role + ' journey', 'pass': False, 'error_type': type(error).__name__}
    finally:
        context.close()


def main():
    with sync_playwright() as playwright:
        browser = playwright.chromium.launch(headless=True)
        try:
            checks = [journey(browser, role) for role in ('GUEST','USER','ADMIN')]
        finally:
            browser.close()
    folder = Path('ci-reports')
    folder.mkdir(exist_ok=True)
    (folder / 'playwright.json').write_text(json.dumps({'checks': checks}, indent=2))
    for item in checks:
        print(item['name'] + ': ' + ('PASS' if item['pass'] else 'FAIL (' + item['error_type'] + ')'))
    return int(any(not item['pass'] for item in checks))


if __name__ == '__main__':
    raise SystemExit(main())
