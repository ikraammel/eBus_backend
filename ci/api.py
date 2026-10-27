"""Read/login-only API checks against the image under test, never Render directly."""
import os
import requests
from robot.api.deco import keyword, library

BASE = 'http://127.0.0.1:18081'


def allowed(method, path):
    return method == 'GET' or (method == 'POST' and path == '/users/login')


@library(scope='SUITE', auto_keywords=False)
class Api:
    def __init__(self):
        self.http = requests.Session()
        self.token = None
        self.user_id = None

    def fetch(self, method, path, authenticated=False, payload=None):
        if not allowed(method, path) or not path.startswith('/') or '://' in path:
            raise AssertionError('Only reads and login on the CI backend are allowed')
        try:
            return self.http.request(method, BASE + path, json=payload, timeout=(5, 30),
                headers={'Authorization': 'Bearer ' + self.token} if authenticated else {}, allow_redirects=False)
        except Exception:
            raise AssertionError('Backend transport unavailable; private details omitted') from None

    @staticmethod
    def body(response):
        try:
            return response.json()
        except Exception:
            raise AssertionError('Invalid JSON; response omitted') from None

    @keyword
    def connect_existing_user(self):
        email, password = os.getenv('EBUS_USER_EMAIL'), os.getenv('EBUS_USER_PASSWORD')
        if not email or not password:
            raise AssertionError('Missing EBUS_USER_EMAIL / EBUS_USER_PASSWORD')
        response = self.fetch('POST', '/users/login', payload={'email': email, 'password': password})
        if response.status_code != 200:
            raise AssertionError('USER login HTTP ' + str(response.status_code))
        data = self.body(response)
        if data.get('user', {}).get('role') != 'USER' or not data.get('token'):
            raise AssertionError('USER role/token invalid; response omitted')
        self.user_id, self.token = data['user']['id'], data['token']

    @keyword
    def check_existing_account(self, name):
        routes = {
            'health': ('/api/health', False, 'health'),
            'bus': ('/bus', False, 'array'), 'lines': ('/lignes', False, 'array'),
            'tickets': ('/tickets', False, 'array'), 'anonymous-types': ('/types-abonnement', False, 'array'),
            'profile': (f'/users/{self.user_id}', True, 'owner'),
            'dossier': (f'/users/me/dossier?id={self.user_id}', True, 'dossier'),
            'subscription': (f'/abonnements/user/{self.user_id}', True, 'subscription'),
            'history': (f'/abonnements/user/{self.user_id}/historique', True, 'array'),
            'cards': (f'/cards/user/{self.user_id}', True, 'array'),
            'activities': (f'/activities/{self.user_id}', True, 'array'),
            'authenticated-types': ('/types-abonnement', True, 'array'),
        }
        path, auth, shape = routes[name]
        response = self.fetch('GET', path, auth)
        if shape == 'subscription' and response.status_code == 204:
            return
        if response.status_code != 200:
            raise AssertionError('HTTP ' + str(response.status_code) + '; expected 200' + (' or 204' if shape == 'subscription' else ''))
        if shape == 'health':
            if response.text.strip() != 'UP':
                raise AssertionError('Expected UP')
            return
        body = self.body(response)
        if shape == 'array' and not isinstance(body, list):
            raise AssertionError('Expected JSON array')
        if shape == 'owner' and (not isinstance(body, dict) or body.get('id') != self.user_id or 'password' in body):
            raise AssertionError('Profile ownership/privacy mismatch')
        if shape == 'dossier' and body.get('statusDossier') not in ('VALIDE', 'EN_ATTENTE', 'REJETE'):
            raise AssertionError('Invalid dossier state')
        if shape == 'subscription' and (body.get('userId') != self.user_id or body.get('status') not in ('ACTIF','EXPIRE','EN_ATTENTE','ANNULE')):
            raise AssertionError('Subscription identity/state mismatch')

    @keyword
    def close_existing_session(self):
        self.http.close()
        self.token = None
