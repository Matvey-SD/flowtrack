package ru.sarapulov.demos.controllers;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.sarapulov.demos.entities.FileRequestingDTO;
import ru.sarapulov.demos.models.User;
import ru.sarapulov.demos.services.DocumentService;
import ru.sarapulov.demos.services.UserService;

import java.util.UUID;

@RestController
@RequestMapping("/api")
@Slf4j
@AllArgsConstructor
public class RestFileController {

    private DocumentService documentService;
    private UserService usersService;

    @PostMapping("/upload-file")
    public ResponseEntity<UUID> handleFileUpload(@AuthenticationPrincipal User user,
                                                 @RequestParam("file") MultipartFile file,
                                                 @RequestParam("team-id") UUID teamId) {
        user = usersService.updateUser(user);
        UUID id = documentService.saveDocument(user, file, teamId);

        return ResponseEntity.ok(id);
    }


    @PostMapping("/upload-file-card")
    public ResponseEntity<UUID> handleCardFileUpload(@AuthenticationPrincipal User user,
                                                     @RequestParam("file") MultipartFile file,
                                                     @RequestParam("team-id") UUID teamId,
                                                     @RequestParam("card-id") UUID cardId) {
        user = usersService.updateUser(user);
        UUID id = documentService.saveCardDocument(user, file, teamId, cardId);

        return ResponseEntity.ok(id);
    }

    @PostMapping("/download-file")
    public ResponseEntity<Resource> downloadFile(@AuthenticationPrincipal User user,
                                                 @RequestBody FileRequestingDTO fileRequestingDTO) {
        user = usersService.updateUser(user);
        Resource resource = documentService.loadDocument(user, fileRequestingDTO.getDocumentId(), fileRequestingDTO.getTeamId());

        return ResponseEntity.ok()
                             .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                             .body(resource);
    }

}
