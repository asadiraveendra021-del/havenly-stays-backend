package com.asadi.havenly_stays.service;

import com.asadi.havenly_stays.dto.HotelCreateRequest;
import com.asadi.havenly_stays.dto.HotelImageResponse;
import com.asadi.havenly_stays.dto.HotelPetPolicyRequest;
import com.asadi.havenly_stays.dto.HotelPetPolicyResponse;
import com.asadi.havenly_stays.dto.HotelResponse;
import com.asadi.havenly_stays.dto.HotelUpdateRequest;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface HotelService {
    HotelResponse createHotel(HotelCreateRequest request);
    HotelResponse updateHotel(Long id, HotelUpdateRequest request);
    void deleteHotel(Long id);
    HotelResponse getHotelById(Long id);
    Page<HotelResponse> getAllHotels(Pageable pageable);
    Page<HotelResponse> searchHotels(String query, Pageable pageable);
    HotelResponse setFacilities(Long hotelId, List<Long> facilityIds);
    HotelPetPolicyResponse setPetPolicy(Long hotelId, HotelPetPolicyRequest request);
    HotelImageResponse uploadHotelImage(Long hotelId, MultipartFile file, String title, String description,
                                        Boolean isMainImage, Integer displayOrder);
    HotelImageResponse updateHotelImage(Long hotelId, Long imageId, MultipartFile file, String title, String description,
                                        Boolean isMainImage, Integer displayOrder);
    void deleteHotelImage(Long hotelId, Long imageId);
}
