package com.asadi.havenly_stays.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
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
public class RoomTypeResponse {
    private Long id;
    private Long hotelId;
    private String name;
    private String description;
    private Integer maxGuests;
    private Integer maxAdults;
    private Integer maxChildren;
    private String bedType;
    private Integer bedCount;
    private Double roomSize;
    private Double basePrice;
    private String currency;
    private Integer totalRooms;
    private Boolean isActive;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    private List<RoomImageResponse> images;
    private List<RoomFacilityResponse> facilities;
}
