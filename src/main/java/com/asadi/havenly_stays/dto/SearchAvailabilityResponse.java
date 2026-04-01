package com.asadi.havenly_stays.dto;

import java.util.List;
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
public class SearchAvailabilityResponse {
    private Long hotelId;
    private String hotelName;
    private String city;
    private Integer starRating;
    private List<RoomAvailabilityResult> rooms;
}
