package com.smartlibrary.dto;

import com.smartlibrary.entity.Role;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponseDTO {

    private String token;

    @Builder.Default
    private String tokenType = "Bearer";

    private Long id;
    private String fullName;
    private String email;
    private Role role;
}
