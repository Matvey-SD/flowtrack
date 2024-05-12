package ru.sarapulov.demos.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.sarapulov.demos.models.Card;
import ru.sarapulov.demos.models.Tag;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class CardDTO {

    private String cardName;

    private String cardDescription;

    private List<CommentDTO> comments;

    private List<CardFileDTO> files;

    private List<Tag> tags;

    private String doer;

    private String checker;

    private Double timeToDo;

    public static CardDTO getDTOFrom(Card card) {
        return CardDTO.builder()
                      .cardName(card.getCardName())
                      .cardDescription(card.getCardDescription())
                      .comments(card.getComments()
                                    .stream()
                                    .map(CommentDTO::getDTOFrom)
                                    .toList())
                      .files(card.getDocuments()
                                 .stream()
                                 .map(CardFileDTO::getDtoFrom)
                                 .toList())
                      .tags(card.getTags())
                      .doer(card.getDoerLogin())
                      .checker(card.getCheckerLogin())
                      .timeToDo(card.getTimeToDo())
                      .build();
    }

}
