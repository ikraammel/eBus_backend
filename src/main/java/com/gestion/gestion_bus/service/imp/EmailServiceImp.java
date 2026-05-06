package com.gestion.gestion_bus.service.imp;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;


@Service
@RequiredArgsConstructor
public class EmailServiceImp {

    private final JavaMailSender mailSender;

    public void sendEmail(String to, String subject, String html) {

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            helper.setFrom("tonemail@gmail.com");

            mailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException("Erreur email: " + e.getMessage());
        }
    }
}