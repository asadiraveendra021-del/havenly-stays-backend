package com.asadi.havenly_stays.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HotelPetPolicyResponse {
    private Long id;
    private Boolean petsAllowed;
    private String petFeeType;
    private Double petFee;
    private Integer maxPets;
    private String description;
}
