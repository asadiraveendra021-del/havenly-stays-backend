package com.hotelbooking.service;

import com.hotelbooking.dto.HotelCreateRequest;
import com.hotelbooking.dto.HotelImageResponse;
import com.hotelbooking.dto.HotelPetPolicyRequest;
import com.hotelbooking.dto.HotelPetPolicyResponse;
import com.hotelbooking.dto.HotelResponse;
import com.hotelbooking.dto.HotelUpdateRequest;
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
}
