"""Local HTTP/browser regression for startup diagnostics; no real API or database."""
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
import json
import os
import threading
import unittest
from unittest.mock import patch
from playwright.sync_api import sync_playwright
import browser


class StartupPage(BaseHTTPRequestHandler):
    def do_GET(self):
        message = 'startup check password=private-bootstrap-sentinel email=qa@example.invalid'
        error = 'new Error(' + json.dumps(message) + ')'
        code = 'Promise.reject(' + error + ')' if self.path == '/promise' else 'throw ' + error
        content = ('<!doctype html><html><body><script>' + code + '</script></body></html>').encode()
        self.send_response(200)
        self.send_header('Content-Type','text/html; charset=utf-8')
        self.send_header('Content-Length',str(len(content)))
        self.end_headers()
        self.wfile.write(content)

    def log_message(self,*args):
        pass


class BootstrapDiagnostics(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.server = ThreadingHTTPServer(('127.0.0.1',0),StartupPage)
        cls.thread = threading.Thread(target=cls.server.serve_forever,daemon=True)
        cls.thread.start()
        cls.playwright = sync_playwright().start()
        cls.chrome = cls.playwright.chromium.launch(headless=True, channel='chrome',
            args=['--use-gl=angle','--use-angle=swiftshader','--enable-unsafe-swiftshader'])

    @classmethod
    def tearDownClass(cls):
        cls.chrome.close()
        cls.playwright.stop()
        cls.server.shutdown()
        cls.server.server_close()
        cls.thread.join(timeout=5)

    def check_failure(self,path):
        url = 'http://127.0.0.1:' + str(self.server.server_port) + path
        with patch.object(browser,'UI',url), patch.dict(os.environ,{'EBUS_USER_PASSWORD':'private-bootstrap-sentinel'}):
            result = browser.journey(self.chrome,'GUEST')
        self.assertFalse(result['pass'])
        self.assertEqual('FrontendBootstrapError',result['error_type'])
        self.assertEqual('enable-flutter-semantics',result['failed_stage'])
        messages = result['diagnostics']['bootstrap_errors_redacted']
        self.assertTrue(messages)
        serialized = json.dumps(result)
        self.assertNotIn('private-bootstrap-sentinel',serialized)
        self.assertNotIn('qa@example.invalid',serialized)
        self.assertFalse(result['diagnostics']['api_requests'])

    def test_javascript_failure_is_reported_and_redacted(self):
        self.check_failure('/')

    def test_rejected_startup_promise_is_reported_and_redacted(self):
        self.check_failure('/promise')


if __name__ == '__main__':
    unittest.main()
