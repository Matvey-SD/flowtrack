package ru.sarapulov.demos.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.sarapulov.demos.exceptions.UnauthorisedAccessException;
import ru.sarapulov.demos.models.Card;
import ru.sarapulov.demos.models.CardDocument;
import ru.sarapulov.demos.models.Column;
import ru.sarapulov.demos.models.Document;
import ru.sarapulov.demos.models.Role;
import ru.sarapulov.demos.models.Team;
import ru.sarapulov.demos.models.TeamMember;
import ru.sarapulov.demos.models.User;
import ru.sarapulov.demos.repositories.CardsRepository;
import ru.sarapulov.demos.repositories.DocumentsRepository;
import ru.sarapulov.demos.repositories.TeamsRepository;
import ru.sarapulov.demos.utils.UserUtils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
@Slf4j
public class DocumentService {

    private TeamsRepository teamsRepository;

    private CardsRepository cardsRepository;

    private DocumentsRepository documentsRepository;

    public UUID saveDocument(User user, MultipartFile file, UUID teamId) {
        TeamMember teamMember = UserUtils.getUserMembershipInTeam(user, teamId);
        Team team = teamMember.getTeam();
        if (!teamMember.getRole()
                       .isDocumentEditAvailable()) {
            throw new UnauthorisedAccessException();
        }

        UUID docId = UUID.randomUUID();
        String path = "E:/storage/files/" + docId;

        Document docToSave = Document.builder()
                                     .id(docId)
                                     .path(path)
                                     .fileName(file.getOriginalFilename())
                                     .size(file.getSize())
                                     .team(team)
                                     .build();
        team.getDocuments()
            .add(docToSave);
        teamsRepository.save(team);

        saveFile(path, file);
        return docId;
    }

    public UUID saveCardDocument(User user, MultipartFile file, UUID teamId, UUID cardId) {
        Role requesterRole = UserUtils.getUserRoleInTeam(user, teamId);
        Team team = requesterRole.getTeam();
        Card card = team.getColumns()
                        .stream()
                        .filter(column -> column.getCardById(cardId)
                                                .isPresent())
                        .map(column -> column.getCardById(cardId)
                                             .get())
                        .findFirst()
                        .orElseThrow();
        Column column = card.getColumn();
        if (!(requesterRole.isCardEditAvailable() || requesterRole.isCardEditInColumnAvailable(column))) {
            throw new UnauthorisedAccessException();
        }

        UUID docId = UUID.randomUUID();
        String path = "E:/storage/files/" + docId;

        CardDocument docToSave = CardDocument.builder()
                                             .id(docId)
                                             .path(path)
                                             .fileName(file.getOriginalFilename())
                                             .size(file.getSize())
                                             .card(card)
                                             .build();
        card.getDocuments()
            .add(docToSave);
        cardsRepository.save(card);

        saveFile(path, file);
        return docId;
    }

    private void saveFile(String path, MultipartFile file) {
        try {
            File filePath = new File(path);
            file.transferTo(filePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Resource loadDocument(User user, UUID fileId, UUID teamId) {
        TeamMember teamMember = UserUtils.getUserMembershipInTeam(user, teamId);
        Team team = teamMember.getTeam();
        String path;

        Optional<Document> optDocument = team.getDocuments()
                                             .stream()
                                             .filter(doc -> doc.getId()
                                                               .compareTo(fileId) == 0)
                                             .findFirst();
        if (optDocument.isPresent()) {
            path = optDocument.get()
                              .getPath();
        } else {
            CardDocument cardDoc = team.getColumns()
                                       .stream()
                                       .filter(column -> column.getCards()
                                                               .stream()
                                                               .anyMatch(card -> card.containsFile(fileId)))
                                       .map(column -> column.getCards()
                                                            .stream()
                                                            .filter(card -> card.containsFile(fileId))
                                                            .findFirst()
                                                            .map(card -> card.getFile(fileId))
                                                            .orElseThrow())
                                       .findFirst()
                                       .orElseThrow();

            path = cardDoc.getPath();
        }

        Resource resource;

        try {
            resource = new UrlResource(Paths.get(path)
                                            .toUri());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        if (resource.exists() || resource.isReadable()) {
            return resource;
        }

        throw new UnauthorisedAccessException();
    }

    public void deleteAll(Collection<Document> documentsToDelete) {
        documentsToDelete.forEach(this::delete);
    }

    public void delete(Document documentToDelete) {
        new File(documentToDelete.getPath()).delete();
        documentsRepository.delete(documentToDelete);
    }

}
