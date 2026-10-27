"""Offline checks; never authenticate against the real database."""
import contextlib
import importlib.util
import io
import os
from pathlib import Path
import sys
import unittest
from unittest.mock import patch
import xml.etree.ElementTree as ET

HERE = Path(__file__).resolve().parent
sys.path.insert(0, str(HERE))
import api
import browser
import runtime


class Guards(unittest.TestCase):
    def test_api_blocks_business_writes(self):
        for method in ('POST','PUT','PATCH','DELETE'):
            for path in ('/users/register','/lignes/1','/reclamations','/stripe/webhook'):
                self.assertFalse(api.allowed(method, path))
        self.assertTrue(api.allowed('POST','/users/login'))

    def test_rejected_write_does_not_reach_network(self):
        client = api.Api()
        with patch.object(client.http, 'request') as request:
            with self.assertRaises(AssertionError):
                client.fetch('DELETE','/lignes/1')
            request.assert_not_called()

    def test_browser_blocks_mutations(self):
        self.assertTrue(browser.permitted('POST','/users/login'))
        self.assertFalse(browser.permitted('POST','/users/register'))
        self.assertFalse(browser.permitted('DELETE','/lignes/1'))
        self.assertFalse(browser.permitted('PATCH','/users/1'))

    def test_missing_secrets_stop_before_container_start(self):
        with patch.dict(os.environ, {}, clear=True), patch.object(runtime, 'docker') as docker:
            with self.assertRaisesRegex(ValueError, 'Missing GitHub Secrets'):
                runtime.start()
            docker.assert_not_called()

    def test_start_injects_secrets_by_name_and_disables_ddl_mail_payments(self):
        values = {key: 'synthetic-private-value' for key in runtime.REQUIRED}
        values['DB_URL'] = 'jdbc:postgresql://example.invalid/test'
        with patch.dict(os.environ, values, clear=True), patch.object(runtime, 'docker') as docker, contextlib.redirect_stdout(io.StringIO()):
            runtime.start()
            args = docker.call_args_list[0].args
            self.assertIn('DB_PASSWORD', args)
            self.assertNotIn('DB_PASSWORD=synthetic-private-value', args)
            self.assertIn('SPRING_JPA_HIBERNATE_DDL_AUTO=validate', args)
            self.assertIn('SPRING_DATASOURCE_HIKARI_READ_ONLY=true', args)
            self.assertIn('STRIPE_SECRET_KEY=sk_test_disabled_ci', args)
            self.assertIn('SPRING_MAIL_HOST=127.0.0.1', args)

    def test_jmeter_only_has_fixed_seven_local_read_login_requests(self):
        root = ET.parse(HERE / 'smoke.jmx').getroot()
        samplers = list(root.iter('HTTPSamplerProxy'))
        self.assertEqual(7,len(samplers))
        for sampler in samplers:
            value = lambda key: sampler.find("stringProp[@name='" + key + "']").text
            self.assertEqual('127.0.0.1', value('HTTPSampler.domain'))
            self.assertEqual('18081', value('HTTPSampler.port'))
            self.assertTrue(api.allowed(value('HTTPSampler.method'), value('HTTPSampler.path')))
        text = (HERE / 'smoke.jmx').read_text(encoding='utf-8')
        self.assertIn("System.getenv('EBUS_USER_PASSWORD')", text)
        self.assertNotIn('CSVDataSet', text)

    def test_diagnostics_keep_cause_without_exposing_secrets(self):
        password = 'private-test-password'
        logs = ('Caused by: org.postgresql.util.PSQLException: password=' + password +
                ' jdbc:postgresql://private-host.invalid:5432/db user=private-test-user\n'
                'Caused by: java.net.UnknownHostException: private-host.invalid\n')
        details = '\n'.join(runtime.diagnostic_details(logs, [password, 'private-test-user', 'private-host.invalid']))
        self.assertIn('PSQLException', details)
        self.assertIn('UnknownHostException', details)
        self.assertNotIn(password, details)
        self.assertNotIn('private-test-user', details)
        self.assertNotIn('private-host.invalid', details)
        self.assertNotIn('jdbc:postgresql://', details)

    def test_diagnostics_suppress_tokens_and_email(self):
        value = runtime.redact('Bearer eyJabc.abc.def email=qa@example.invalid secret=some-private-value')
        self.assertNotIn('eyJabc.abc.def', value)
        self.assertNotIn('qa@example.invalid', value)
        self.assertNotIn('some-private-value', value)


if __name__ == '__main__':
    unittest.main()
