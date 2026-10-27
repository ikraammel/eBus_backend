package com.gestion.gestion_bus;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcPrint;
import org.springframework.transaction.annotation.Transactional;

/** Fixed disposable database only: never inherits DB_URL or the production profile. */
@SpringBootTest(properties={
    "spring.datasource.url=jdbc:postgresql://127.0.0.1:55433/ebus_integration",
    "spring.datasource.username=ebus_integration", "spring.datasource.password=integration-only",
    "spring.jpa.hibernate.ddl-auto=update", "spring.jpa.show-sql=false",
    "spring.mail.host=127.0.0.1", "spring.mail.username=qa@example.invalid", "spring.mail.password=unused",
    "stripe.api.key=sk_test_disabled", "stripe.webhook.secret=whsec_disabled",
    "jwt.secret=ZWJ1cy1xYS1vbmx5LXN5bnRoZXRpYy1rZXktMzItYnl0ZXMtbWluaW11bQ==",
    "debug=false", "trace=false", "logging.level.root=WARN",
    "logging.level.org.springframework=WARN", "logging.level.org.hibernate=ERROR"
})
@AutoConfigureMockMvc(print=MockMvcPrint.NONE)
@Transactional
public abstract class AbstractDatabaseTest {}
