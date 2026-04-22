package com.tranhuudat.prms.service.impl;

import com.tranhuudat.prms.service.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import jakarta.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailServiceImpl implements MailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.enabled:false}")
    private boolean enabled;

    @Value("${app.mail.from:}")
    private String from;

    @Override
    @Async
    public void sendHtml(String to, String subject, String html) {
        if (!enabled) {
            return;
        }
        if (!StringUtils.hasText(to) || !StringUtils.hasText(subject)) {
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, StandardCharsets.UTF_8.name());
            helper.setTo(to);
            if (StringUtils.hasText(from)) {
                helper.setFrom(from);
            }
            helper.setSubject(subject);
            helper.setText(html != null ? html : "", true);
            mailSender.send(message);
        } catch (Exception ex) {
            log.warn("Send mail failed to {}", to, ex);
        }
    }
}

