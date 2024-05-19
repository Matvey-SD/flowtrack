package ru.sarapulov.demos.properitest;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "flowtrack.mail")
@Getter
@Setter
public class MailProperties {
    private String sender;
}
