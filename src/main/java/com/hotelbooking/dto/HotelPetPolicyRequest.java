package com.hotelbooking.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class HotelPetPolicyRequest {
    @NotNull
    private Boolean petsAllowed;

    @Size(max = 50)
    private String petFeeType;

    private Double petFee;

    private Integer maxPets;

    @Size(max = 500)
    private String description;
}
