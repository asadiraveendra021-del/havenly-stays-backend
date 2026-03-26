package com.asadi.havenly_stays.service;

import com.asadi.havenly_stays.dto.RoomTypeCreateRequest;
import com.asadi.havenly_stays.dto.RoomTypeResponse;
import com.asadi.havenly_stays.dto.RoomTypeUpdateRequest;
import java.util.List;

public interface RoomTypeService {
    RoomTypeResponse createRoomType(Long hotelId, RoomTypeCreateRequest request);
    RoomTypeResponse updateRoomType(Long roomTypeId, RoomTypeUpdateRequest request);
    void deleteRoomType(Long roomTypeId);
    List<RoomTypeResponse> getRoomTypesByHotel(Long hotelId);
    RoomTypeResponse getRoomTypeById(Long roomTypeId);
}
