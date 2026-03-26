package com.asadi.havenly_stays.mapper;

import com.asadi.havenly_stays.dto.RoomFacilityResponse;
import com.asadi.havenly_stays.dto.RoomImageResponse;
import com.asadi.havenly_stays.dto.RoomTypeCreateRequest;
import com.asadi.havenly_stays.dto.RoomTypeResponse;
import com.asadi.havenly_stays.dto.RoomTypeUpdateRequest;
import com.asadi.havenly_stays.entity.RoomType;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class RoomTypeMapper {

    public RoomType toEntity(RoomTypeCreateRequest request, Long hotelId) {
        if (request == null) {
            return null;
        }
        return RoomType.builder()
                .hotelId(hotelId)
                .name(request.getName())
                .description(request.getDescription())
                .maxGuests(request.getMaxGuests())
                .maxAdults(request.getMaxAdults())
                .maxChildren(request.getMaxChildren())
                .bedType(request.getBedType())
                .bedCount(request.getBedCount())
                .roomSize(request.getRoomSize())
                .basePrice(request.getBasePrice())
                .currency(request.getCurrency())
                .totalRooms(request.getTotalRooms())
                .isActive(request.getIsActive())
                .build();
    }

    public void applyUpdate(RoomType roomType, RoomTypeUpdateRequest request) {
        if (request.getName() != null) {
            roomType.setName(request.getName());
        }
        if (request.getDescription() != null) {
            roomType.setDescription(request.getDescription());
        }
        if (request.getMaxGuests() != null) {
            roomType.setMaxGuests(request.getMaxGuests());
        }
        if (request.getMaxAdults() != null) {
            roomType.setMaxAdults(request.getMaxAdults());
        }
        if (request.getMaxChildren() != null) {
            roomType.setMaxChildren(request.getMaxChildren());
        }
        if (request.getBedType() != null) {
            roomType.setBedType(request.getBedType());
        }
        if (request.getBedCount() != null) {
            roomType.setBedCount(request.getBedCount());
        }
        if (request.getRoomSize() != null) {
            roomType.setRoomSize(request.getRoomSize());
        }
        if (request.getBasePrice() != null) {
            roomType.setBasePrice(request.getBasePrice());
        }
        if (request.getCurrency() != null) {
            roomType.setCurrency(request.getCurrency());
        }
        if (request.getTotalRooms() != null) {
            roomType.setTotalRooms(request.getTotalRooms());
        }
        if (request.getIsActive() != null) {
            roomType.setIsActive(request.getIsActive());
        }
    }

    public RoomTypeResponse toResponse(RoomType roomType,
                                       List<RoomImageResponse> images,
                                       List<RoomFacilityResponse> facilities,
                                       Double displayPrice) {
        if (roomType == null) {
            return null;
        }
        Double price = displayPrice != null ? displayPrice : roomType.getBasePrice();
        return RoomTypeResponse.builder()
                .id(roomType.getId())
                .hotelId(roomType.getHotelId())
                .name(roomType.getName())
                .description(roomType.getDescription())
                .maxGuests(roomType.getMaxGuests())
                .maxAdults(roomType.getMaxAdults())
                .maxChildren(roomType.getMaxChildren())
                .bedType(roomType.getBedType())
                .bedCount(roomType.getBedCount())
                .roomSize(roomType.getRoomSize())
                .basePrice(price)
                .currency(roomType.getCurrency())
                .totalRooms(roomType.getTotalRooms())
                .isActive(roomType.getIsActive())
                .createdAt(roomType.getCreatedAt())
                .updatedAt(roomType.getUpdatedAt())
                .images(images)
                .facilities(facilities)
                .build();
    }
}
