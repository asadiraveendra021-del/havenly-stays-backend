package com.asadi.havenly_stays.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
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
public class RoomTypeCreateRequest {

    @NotBlank
    @Size(max = 200)
    private String name;

    @Size(max = 5000)
    private String description;

    @NotNull
    @Min(1)
    private Integer maxGuests;

    @Min(0)
    private Integer maxAdults;

    @Min(0)
    private Integer maxChildren;

    @Size(max = 50)
    private String bedType;

    @Min(0)
    private Integer bedCount;

    @DecimalMin(value = "0.0", inclusive = true)
    private Double roomSize;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    private Double basePrice;

    @Size(max = 10)
    private String currency;

    @NotNull
    @Min(0)
    private Integer totalRooms;

    private Boolean isActive;
}
