package com.asadi.havenly_stays.service;

import com.asadi.havenly_stays.dto.RoomImageResponse;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface RoomImageService {
    RoomImageResponse uploadRoomImage(Long roomTypeId, MultipartFile file, String title, String description,
                                      Boolean isMainImage, Integer displayOrder);
    RoomImageResponse updateRoomImage(Long roomTypeId, Long imageId, MultipartFile file, String title,
                                      String description, Boolean isMainImage, Integer displayOrder);
    void deleteRoomImage(Long roomTypeId, Long imageId);
    List<RoomImageResponse> getImagesByRoomType(Long roomTypeId);
}
