package ru.sarapulov.demos.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CardChangeDTO {
    private UUID cardId;
    private String cardName;
    private String cardDesc;
    private UUID teamId;
    private String doer;
    private String checker;
    private Double timeToDo;
}
