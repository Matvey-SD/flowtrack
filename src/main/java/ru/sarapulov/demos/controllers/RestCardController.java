package ru.sarapulov.demos.controllers;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.sarapulov.demos.entities.CardAddingRequestDTO;
import ru.sarapulov.demos.entities.CardChangeDTO;
import ru.sarapulov.demos.entities.CardDTO;
import ru.sarapulov.demos.entities.CardDeletingRequestDTO;
import ru.sarapulov.demos.entities.CardPositionUpdateDTO;
import ru.sarapulov.demos.entities.CardRequestDTO;
import ru.sarapulov.demos.entities.CommentAddingDTO;
import ru.sarapulov.demos.models.Card;
import ru.sarapulov.demos.models.User;
import ru.sarapulov.demos.services.CardService;
import ru.sarapulov.demos.services.UserService;

import java.util.UUID;

@RestController
@RequestMapping("/api")
@Slf4j
@AllArgsConstructor
public class RestCardController {

    private UserService usersService;

    private CardService cardService;

    @PostMapping("/add-card")
    public ResponseEntity<UUID> addCard(@AuthenticationPrincipal User user,
                                        @RequestBody CardAddingRequestDTO cardAddingRequestDTO) {
        user = usersService.updateUser(user);
        log.info("Trying to add card with desc {} to team with id {}, column with id {}",
                 cardAddingRequestDTO.getCardDesc(),
                 cardAddingRequestDTO.getTeamId(),
                 cardAddingRequestDTO.getColumnId());
        UUID savedId = cardService.addCardToColumn(user, cardAddingRequestDTO);
        return ResponseEntity.ok(savedId);
    }

    @PostMapping("/get-card")
    public ResponseEntity<CardDTO> getCard(@AuthenticationPrincipal User user, @RequestBody CardRequestDTO cardRequestDTO) {
        user = usersService.updateUser(user);
        log.info("Trying to get card with id {} from team with id {}", cardRequestDTO.getCardId(), cardRequestDTO.getTeamId());
        Card card = cardService.getCardFromTeam(user, cardRequestDTO);
        CardDTO cardDTO = CardDTO.getDTOFrom(card);
        return ResponseEntity.ok(cardDTO);
    }


    @PostMapping("/update-card")
    public ResponseEntity<Boolean> updateCard(@AuthenticationPrincipal User user, @RequestBody CardChangeDTO cardChangeDTO) {
        user = usersService.updateUser(user);
        log.info("Trying to update card with id {} from team with id {}", cardChangeDTO.getCardId(), cardChangeDTO.getTeamId());
        cardService.updateCardFromTeam(user, cardChangeDTO);
        return ResponseEntity.ok(true);
    }

    @PostMapping("/update-card-position")
    public ResponseEntity<Boolean> updateCardPosition(@AuthenticationPrincipal User user,
                                                      @RequestBody CardPositionUpdateDTO cardPositionUpdateDTO) {
        user = usersService.updateUser(user);
        log.info("Trying to update position of card with id {} from team with id {} to column {}",
                 cardPositionUpdateDTO.getCardId(),
                 cardPositionUpdateDTO.getTeamId(),
                 cardPositionUpdateDTO.getColumnId());
        cardService.updateCardPosition(user, cardPositionUpdateDTO);
        return ResponseEntity.ok(true);
    }

    @PostMapping("/save-comment")
    public ResponseEntity<Boolean> saveComment(@AuthenticationPrincipal User user,
                                               @RequestBody CommentAddingDTO commentAddingDTO) {
        user = usersService.updateUser(user);
        log.info("Trying to save comment of user with login {} in card with id {}",
                 user.getLogin(),
                 commentAddingDTO.getCardId());
        cardService.saveComment(user, commentAddingDTO);
        return ResponseEntity.ok(true);
    }

    @PostMapping("delete-card")
    public ResponseEntity<Boolean> deleteCard(@AuthenticationPrincipal User user,
                                              @RequestBody CardDeletingRequestDTO cardDeletingDTO) {
        user = usersService.updateUser(user);
        log.info("Trying to delete card with id {} from team with id {}",
                 cardDeletingDTO.getCardId(),
                 cardDeletingDTO.getTeamId());
        cardService.deleteCardIfAvailable(user, cardDeletingDTO);

        return ResponseEntity.ok(true);
    }

}
