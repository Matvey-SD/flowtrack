package ru.sarapulov.demos.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sarapulov.demos.entities.CardAddingRequestDTO;
import ru.sarapulov.demos.entities.CardChangeDTO;
import ru.sarapulov.demos.entities.CardDeletingRequestDTO;
import ru.sarapulov.demos.entities.CardPositionUpdateDTO;
import ru.sarapulov.demos.entities.CardRequestDTO;
import ru.sarapulov.demos.entities.CommentAddingDTO;
import ru.sarapulov.demos.exceptions.UnauthorisedAccessException;
import ru.sarapulov.demos.models.Card;
import ru.sarapulov.demos.models.CardDocument;
import ru.sarapulov.demos.models.Column;
import ru.sarapulov.demos.models.Comment;
import ru.sarapulov.demos.models.Role;
import ru.sarapulov.demos.models.Team;
import ru.sarapulov.demos.models.TeamMember;
import ru.sarapulov.demos.models.User;
import ru.sarapulov.demos.repositories.CardDocumentsRepository;
import ru.sarapulov.demos.repositories.CardsRepository;
import ru.sarapulov.demos.repositories.TeamsRepository;
import ru.sarapulov.demos.repositories.UsersRepository;
import ru.sarapulov.demos.utils.UserUtils;

import java.io.File;
import java.time.Instant;
import java.util.Collection;
import java.util.UUID;

@Service
@AllArgsConstructor
@Slf4j
public class CardService {

    private TeamsRepository teamsRepository;

    private CardsRepository cardsRepository;

    private CardDocumentsRepository cardDocumentsRepository;

    private UsersRepository usersRepository;

    private MailSendService mailSendService;

    public UUID addCardToColumn(User requester, CardAddingRequestDTO cardDto) {
        Role requesterRole = UserUtils.getUserRoleInTeam(requester, cardDto.getTeamId());
        Team team = teamsRepository.findTeamById(cardDto.getTeamId());
        Column column = team.getColumns()
                            .stream()
                            .filter(col -> col.getId()
                                              .compareTo(cardDto.getColumnId()) == 0)
                            .findFirst()
                            .orElseThrow();

        if (!requesterRole.isCardEditInColumnAvailable(column)) {
            throw new UnauthorisedAccessException();
        }

        UUID savedId = UUID.randomUUID();
        Card cardToAdd = Card.builder()
                             .id(savedId)
                             .cardName(cardDto.getCardDesc())
                             .column(column)
                             .doingTime(0L)
                             .build();

        if (column.getColumnType() == 1) {
            cardToAdd.setTimerStart(Instant.now());
        }

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
        if (card.getDoer() != null) {
            mailSendService.sendMessageToUserIfPossible(card.getDoer(),
                                                        String.format("Изменения в карточке %s", card.getCardName()),
                                                        String.format(
                                                            "В карточке %s, в которой вы являетесь исполнителем, произошли изменения",
                                                            card.getCardName()));
        }
        if (card.getChecker() != null && (card.getDoer() == null || card.getDoer()
                                                                        .getLogin()
                                                                        .equals(card.getChecker()
                                                                                    .getLogin()))) {
            mailSendService.sendMessageToUserIfPossible(card.getChecker(),
                                                        String.format("Изменения в карточке %s", card.getCardName()),
                                                        String.format(
                                                            "В карточке %s, в которой вы являетесь проверяющим, произошли изменения",
                                                            card.getCardName()));
        }
    }

    public void updateCardPosition(User user, CardPositionUpdateDTO cardPositionUpdate) {
        Card card = getCardIfPermittedOrThrow(user, cardPositionUpdate.getTeamId(), cardPositionUpdate.getCardId());
        Column prevColumn = card.getColumn();
        if (prevColumn.getId()
                      .compareTo(cardPositionUpdate.getColumnId()) == 0) {
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
        if (newColumn.getColumnType() == 1 && prevColumn.getColumnType() != 1) {
            card.setTimerStart(Instant.now());
        }

        if (newColumn.getColumnType() != 1 && prevColumn.getColumnType() == 1) {
            card.setDoingTime((card.getDoingTime() == null ? 0 : card.getDoingTime()) + (Instant.now()
                                                                                                .getEpochSecond()
                                                                                             - card.getTimerStart()
                                                                                                   .getEpochSecond()));

            card.setTimerStart(null);
        }

        cardsRepository.save(card);

        if (newColumn.getColumnType() == 2 && card.getChecker() != null) {
            mailSendService.sendMessageToUserIfPossible(card.getChecker(),
                                                        String.format("Задача %s назначена на проверку", card.getCardName()),
                                                        String.format(
                                                            "Задача %s, в которой вы числитесь проверяющим, перемещена в колонку %s, выделенной для проверки",
                                                            card.getCardName(),
                                                            newColumn.getName()));
        }
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

    public void deleteCardIfAvailable(User requester, CardDeletingRequestDTO cardDeletingDTO) {
        Card card = getCardIfPermittedOrThrow(requester, cardDeletingDTO.getTeamId(), cardDeletingDTO.getCardId());
        TeamMember requesterMembership = UserUtils.getUserMembershipInTeam(requester, cardDeletingDTO.getTeamId());

        if (!requesterMembership.getRole()
                                .isCardEditInColumnAvailable(card.getColumn())) {
            return;
        }

        deleteCard(card);
    }

    public void deleteCards(Collection<Card> cards) {
        cards.forEach(this::deleteCard);
    }

    public void deleteCard(Card card) {
        card.getDocuments()
            .forEach(this::deleteDocument);
        cardsRepository.delete(card);
    }

    private void deleteDocument(CardDocument document) {
        new File(document.getPath()).delete();
        cardDocumentsRepository.delete(document);
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
