package dev.jotxee.todo.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final String mailFrom;

    public EmailService(
            JavaMailSender mailSender,
            @Value("${app.mail-from}") String mailFrom) {
        this.mailSender = mailSender;
        this.mailFrom = mailFrom;
    }

    @Async("mailExecutor")
    public void sendOtp(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailFrom);
        message.setTo(toEmail);
        message.setSubject("Tu código de acceso");
        message.setText("""
                Tu código de acceso es: %s
                
                Este código expira en 10 minutos.
                
                Si no solicitaste este código, ignora este mensaje.
                """.formatted(otp));
        mailSender.send(message);
    }
}
