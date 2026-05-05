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
        message.setSubject("Your access code");
        message.setText("""
                Your access code is: %s
                
                This code expires in 10 minutes.
                
                If you did not request this code, please ignore this message.
                """.formatted(otp));
        mailSender.send(message);
    }
}
