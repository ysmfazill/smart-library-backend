package com.smartlibrary.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "reading_history",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "book_id"})
    },
    indexes = {
        @Index(name = "idx_reading_history_last_read", columnList = "user_id, last_read_date")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReadingHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "User is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull(message = "Book is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Min(value = 0, message = "Progress percentage cannot be negative")
    @Max(value = 100, message = "Progress percentage cannot exceed 100")
    @Column(name = "progress_percentage")
    @Builder.Default
    private Double progressPercentage = 0.0;

    @Column(name = "last_read_date")
    private LocalDateTime lastReadDate;

    @Builder.Default
    private Boolean completed = false;

    @PrePersist
    @PreUpdate
    protected void onSave() {
        this.lastReadDate = LocalDateTime.now();
        if (this.progressPercentage != null && this.progressPercentage >= 100.0) {
            this.completed = true;
        }
    }
}
