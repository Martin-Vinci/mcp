package com.greybox.mediums.utils;

import com.greybox.mediums.models.EmailRequest;
import com.greybox.mediums.services.MediumsControllerService;
import org.springframework.stereotype.Component;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

import static com.greybox.mediums.utils.Logger.logError;

@Component
public class EmailService {

    public static void sendEmail(EmailRequest request) {
        Properties props = new Properties();
        props.put("mail.smtp.host", MediumsControllerService.schemaConfig.getSmtpServer());
        props.put("mail.smtp.socketFactory.port", MediumsControllerService.schemaConfig.getSmtpPort());
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.from", MediumsControllerService.schemaConfig.getEmailAddress());
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", MediumsControllerService.schemaConfig.getSmtpPort());

        Session session = Session.getDefaultInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(MediumsControllerService.schemaConfig.getEmailUserName(), MediumsControllerService.schemaConfig.getEmailPassword());
                    }
                });
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(MediumsControllerService.schemaConfig.getEmailAddress()));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(request.getEmailReceipient()));
            message.setSubject(request.getEmailSubject());
            message.setText(request.getMessageBody());
            Transport.send(message);
            System.out.println("sent " + MediumsControllerService.schemaConfig.getEmailAddress() + " to " + request.getEmailReceipient());
        } catch (MessagingException e) {
            logError(e);
        }
    }
}
