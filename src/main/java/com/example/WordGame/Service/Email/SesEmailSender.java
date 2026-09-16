package com.example.WordGame.Service.Email;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.Body;
import software.amazon.awssdk.services.ses.model.Content;
import software.amazon.awssdk.services.ses.model.Destination;
import software.amazon.awssdk.services.ses.model.Message;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.mail.provider", havingValue = "ses")
public class SesEmailSender implements EmailSender {

    private final SesClient sesClient;

    @Override
    public void send(String to, String subject, String htmlBody, String from, String fromName, String replyTo) {
        String source = fromName == null || fromName.isBlank()
                ? from
                : fromName + " <" + from + ">";

        SendEmailRequest.Builder requestBuilder = SendEmailRequest.builder()
                .source(source)
                .destination(Destination.builder().toAddresses(to).build())
                .message(Message.builder()
                        .subject(Content.builder().data(subject).charset("UTF-8").build())
                        .body(Body.builder()
                                .html(Content.builder().data(htmlBody).charset("UTF-8").build())
                                .build())
                        .build());

        if (replyTo != null && !replyTo.isBlank()) {
            requestBuilder.replyToAddresses(replyTo);
        }

        sesClient.sendEmail(requestBuilder.build());
    }
}