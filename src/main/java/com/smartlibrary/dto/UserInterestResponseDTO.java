package com.smartlibrary.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInterestResponseDTO {

    private Long id;
    private String interestName;
}
