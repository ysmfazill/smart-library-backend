package com.smartlibrary.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInterestRequestDTO {

    @NotBlank(message = "Interest name is required")
    @Size(max = 100, message = "Interest name must not exceed 100 characters")
    private String interestName;
}
