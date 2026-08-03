package com.smartlibrary.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "import_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImportHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "imported_by", nullable = false)
    private User importedBy;

    @Column(name = "import_date", nullable = false)
    private LocalDateTime importDate;

    @Column(name = "filename", nullable = false)
    private String filename;

    @Column(name = "books_imported")
    private Integer booksImported;

    @Column(name = "duplicates_skipped")
    private Integer duplicatesSkipped;

    @Column(name = "invalid_rows")
    private Integer invalidRows;

    @Column(name = "import_duration_ms")
    private Long importDurationMs;

    @Column(name = "status", nullable = false)
    private String status;

    @PrePersist
    public void prePersist() {
        if (importDate == null) {
            importDate = LocalDateTime.now();
        }
    }
}
