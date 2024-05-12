package ru.sarapulov.demos.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.sarapulov.demos.models.CardDocument;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class CardFileDTO {

    private UUID fileId;

    private String fileName;

    private String fileSize;

    public static CardFileDTO getDtoFrom(CardDocument document) {
        return CardFileDTO.builder()
                          .fileId(document.getId())
                          .fileSize(document.getHumanReadableSize())
                          .fileName(document.getFileName())
                          .build();
    }

}
