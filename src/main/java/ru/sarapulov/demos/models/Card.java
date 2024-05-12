package ru.sarapulov.demos.models;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "cards")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Card {

    @Id
    private UUID id;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Column column;

    private String cardName;

    private String cardDescription;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "card", fetch = FetchType.EAGER, orphanRemoval = true)
    private List<Comment> comments;

    @ManyToMany
    private List<Tag> tags;

    @ManyToOne
    private User doer;

    @ManyToOne(cascade = CascadeType.ALL)
    private User checker;

    private Double timeToDo;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "card", fetch = FetchType.EAGER, orphanRemoval = true)
    private List<CardDocument> documents;

    public String getDoerLogin() {
        return doer == null ? "" : doer.getLogin();
    }

    public String getCheckerLogin() {
        return checker == null ? "" : checker.getLogin();
    }

    public boolean containsFile(UUID fileId) {
        return documents.stream()
                        .anyMatch(document -> document.getId()
                                                      .compareTo(fileId) == 0);
    }

    public CardDocument getFile(UUID fileId) {
        return documents.stream()
                        .filter(document -> document.getId()
                                                    .compareTo(fileId) == 0)
                        .findFirst()
                        .orElseThrow();
    }

}
