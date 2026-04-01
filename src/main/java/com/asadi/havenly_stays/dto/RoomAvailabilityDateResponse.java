package com.asadi.havenly_stays.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
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
public class RoomAvailabilityDateResponse {

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;
    private Double price;
    private Integer availableRooms;
}
