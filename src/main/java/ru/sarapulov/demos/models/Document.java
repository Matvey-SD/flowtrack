package ru.sarapulov.demos.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.sarapulov.demos.utils.FileUtils;

import java.util.UUID;

@Entity
@Table(name = "documents")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Document {

    @Id
    private UUID id;

    private String fileName;

    private String path;

    private Long size;

    @ManyToOne
    private Team team;

    public String getHumanReadableSize() {
        return FileUtils.humanReadableByteCountBin(this.size);
    }

}
