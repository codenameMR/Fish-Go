package com.fishgo.users.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Autowired
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendVerificationEmail(String to, String code) throws MessagingException {

        log.info("이메일 전송 시도: 수신자 = {}, 코드 = {}", to, code);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        try {
            helper.setFrom("fish-go");
            helper.setTo(to);
            helper.setSubject("Fish-Go 이메일 인증");

            String htmlContent =
                    "<div style='font-family: Arial, sans-serif; padding: 20px; max-width: 600px;'>" +
                            "<h2>Fish-Go 이메일 인증</h2>" +
                            "<p>안녕하세요! Fish-Go 서비스에 가입해 주셔서 감사합니다.</p>" +
                            "<p>아래 인증 코드를 입력하여 이메일 인증을 완료해 주세요:</p>" +
                            "<div style='background-color: #f4f4f4; padding: 10px; font-size: 24px; font-weight: bold; text-align: center; letter-spacing: 5px;'>" +
                            code +
                            "</div>" +
                            "<p>인증 코드는 30분간 유효합니다.</p>" +
                            "</div>";

            helper.setText(htmlContent, true);
            mailSender.send(message);

            log.info("이메일 전송 성공: 수신자 = {}", to);
        } catch (Exception e) {
            log.error("이메일 전송 실패: {}", e.getMessage(), e);
            throw e;
        }


    }

}
