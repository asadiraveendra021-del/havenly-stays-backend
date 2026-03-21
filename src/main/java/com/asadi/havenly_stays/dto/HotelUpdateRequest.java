package com.asadi.havenly_stays.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.time.LocalTime;
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
public class HotelUpdateRequest {

    @Size(max = 200)
    private String name;

    @Size(max = 5000)
    private String description;

    @Size(max = 500)
    private String shortDescription;

    @Min(1)
    @Max(5)
    private Integer starRating;

    @Size(max = 500)
    private String address;

    @Size(max = 100)
    private String city;

    @Size(max = 100)
    private String state;

    @Size(max = 100)
    private String country;

    @Size(max = 20)
    private String postalCode;

    private Double latitude;

    private Double longitude;

    @Size(max = 20)
    private String phone;

    @Email
    @Size(max = 150)
    private String email;

    @Size(max = 200)
    private String website;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime checkInTime;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime checkOutTime;

    private Boolean isActive;
}
