package com.smartlibrary.dto;

import com.smartlibrary.entity.AchievementType;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AchievementDTO {
    private Long id;
    private AchievementType type;
    private String name;
    private String description;
    private String icon;
    private LocalDateTime unlockedAt;
}
