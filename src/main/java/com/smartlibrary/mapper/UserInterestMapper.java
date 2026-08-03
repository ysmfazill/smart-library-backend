package com.smartlibrary.mapper;

import com.smartlibrary.dto.UserInterestRequestDTO;
import com.smartlibrary.dto.UserInterestResponseDTO;
import com.smartlibrary.entity.UserInterest;
import org.springframework.stereotype.Component;

@Component
public class UserInterestMapper {

    public UserInterestResponseDTO toResponseDTO(UserInterest entity) {
        if (entity == null) {
            return null;
        }
        return UserInterestResponseDTO.builder()
                .id(entity.getId())
                .interestName(entity.getInterestName())
                .build();
    }

    public UserInterest toEntity(UserInterestRequestDTO dto) {
        if (dto == null) {
            return null;
        }
        return UserInterest.builder()
                .interestName(dto.getInterestName())
                .build();
    }
}
