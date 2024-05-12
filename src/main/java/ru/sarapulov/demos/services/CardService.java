package ru.sarapulov.demos.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sarapulov.demos.entities.CardAddingRequestDTO;
import ru.sarapulov.demos.entities.CardChangeDTO;
import ru.sarapulov.demos.entities.CardPositionUpdateDTO;
import ru.sarapulov.demos.entities.CardRequestDTO;
import ru.sarapulov.demos.entities.CommentAddingDTO;
import ru.sarapulov.demos.exceptions.UnauthorisedAccessException;
import ru.sarapulov.demos.models.Card;
import ru.sarapulov.demos.models.Column;
import ru.sarapulov.demos.models.Comment;
import ru.sarapulov.demos.models.Role;
import ru.sarapulov.demos.models.Team;
import ru.sarapulov.demos.models.User;
import ru.sarapulov.demos.repositories.CardsRepository;
import ru.sarapulov.demos.repositories.TeamsRepository;
import ru.sarapulov.demos.repositories.UsersRepository;
import ru.sarapulov.demos.utils.UserUtils;

import java.time.Instant;
import java.util.UUID;

@Service
@AllArgsConstructor
@Slf4j
public class CardService {

    private TeamsRepository teamsRepository;

    private CardsRepository cardsRepository;

    private UsersRepository usersRepository;

    public UUID addCardToColumn(User requester, CardAddingRequestDTO cardDto) {
        Role requesterRole = UserUtils.getUserRoleInTeam(requester, cardDto.getTeamId());
        Team team = teamsRepository.findTeamById(cardDto.getTeamId());
        Column column = team.getColumns()
                            .stream()
                            .filter(col -> col.getId()
                                              .compareTo(cardDto.getColumnId()) == 0)
                            .findFirst()
                            .orElseThrow();

        if (!requesterRole.isCardCreationAvailableInColumn(column)) {
            throw new UnauthorisedAccessException();
        }

        UUID savedId = UUID.randomUUID();
        Card cardToAdd = Card.builder()
                             .id(savedId)
                             .cardName(cardDto.getCardDesc())
                             .column(column)
                             .build();
        column.getCards()
              .add(cardToAdd);
        teamsRepository.save(team);

        return savedId;
    }

    public Card getCardFromTeam(User requester, CardRequestDTO cardRequest) {
        return getCardIfPermittedOrThrow(requester, cardRequest.getTeamId(), cardRequest.getCardId());
    }

    public void updateCardFromTeam(User requester, CardChangeDTO cardChange) {
        Card card = getCardIfPermittedOrThrow(requester, cardChange.getTeamId(), cardChange.getCardId());
        card.setCardName(cardChange.getCardName());
        card.setCardDescription(cardChange.getCardDesc());
        card.setTimeToDo(cardChange.getTimeToDo());

        if (UserUtils.getUserRoleInTeam(requester, cardChange.getTeamId())
                     .isUserAssignationAvailable()) {
            if (cardChange.getDoer()
                          .isEmpty()) {
                card.setDoer(null);
            } else {
                User doer = usersRepository.findUserByLogin(cardChange.getDoer());
                if (UserUtils.isUserMemberOfTeam(doer, cardChange.getTeamId())) {
                    card.setDoer(doer);
                }
            }

            if (cardChange.getChecker()
                          .isEmpty()) {
                card.setChecker(null);
            } else {
                User checker = usersRepository.findUserByLogin(cardChange.getChecker());
                if (UserUtils.isUserMemberOfTeam(checker, cardChange.getTeamId())) {
                    card.setChecker(checker);
                }
            }
        }

        cardsRepository.save(card);
    }

    public void updateCardPosition(User user, CardPositionUpdateDTO cardPositionUpdate) {
        Card card = getCardIfPermittedOrThrow(user, cardPositionUpdate.getTeamId(), cardPositionUpdate.getCardId());
        Column prevColumn = card.getColumn();
        if (prevColumn.getId()
                      .compareTo(cardPositionUpdate.getCardId()) == 0) {
            return;
        }

        Column newColumn = prevColumn.getTeam()
                                     .getColumns()
                                     .stream()
                                     .filter(column -> column.getId()
                                                             .compareTo(cardPositionUpdate.getColumnId()) == 0)
                                     .findFirst()
                                     .orElseThrow();

        card.setColumn(newColumn);

        cardsRepository.save(card);
    }

    public void saveComment(User requester, CommentAddingDTO commentAddingDTO) {
        Card card = getCardIfPermittedOrThrow(requester, commentAddingDTO.getTeamId(), commentAddingDTO.getCardId());
        Comment commentToAdd = Comment.builder()
                                      .time(Instant.now())
                                      .sender(UserUtils.getUserMembershipInTeam(requester, commentAddingDTO.getTeamId()))
                                      .commentText(commentAddingDTO.getComment())
                                      .card(card)
                                      .build();

        card.getComments()
            .add(commentToAdd);
        cardsRepository.save(card);
    }

    private Card getCardIfPermittedOrThrow(User requester, UUID teamId, UUID cardId) {
        if (!UserUtils.isUserMemberOfTeam(requester, teamId)) {
            throw new UnauthorisedAccessException();
        }

        Team team = teamsRepository.findTeamById(teamId);
        Card card = cardsRepository.findById(cardId)
                                   .orElseThrow();

        if (team.getColumns()
                .stream()
                .noneMatch(column -> column.getId()
                                           .compareTo(card.getColumn()
                                                          .getId()) == 0)) {
            throw new UnauthorisedAccessException();
        }

        return card;
    }

}