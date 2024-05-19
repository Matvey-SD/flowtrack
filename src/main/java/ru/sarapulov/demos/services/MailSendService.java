package ru.sarapulov.demos.services;

import lombok.AllArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ru.sarapulov.demos.models.User;
import ru.sarapulov.demos.properitest.MailProperties;

@Service
@AllArgsConstructor
public class MailSendService {

    private JavaMailSender mailSender;

    private MailProperties mailProperties;

    @Async
    public void sendMessage(String receiver, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailProperties.getSender());
        message.setTo(receiver);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }

    @Async
    public void sendMessageToUserIfPossible(User user, String subject, String text) {
        if (user.getMail() == null) {
            return;
        }

        String greetings = user.getFullName()
                               .isEmpty() ? user.getLogin() + "!\n" : "Уважаемый(ая) " + user.getFullName() + "!\n";
        String fullText = greetings + text;

        sendMessage(user.getMail(), subject, fullText);
    }

}
