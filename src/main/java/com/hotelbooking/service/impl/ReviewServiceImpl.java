package com.hotelbooking.service.impl;

import com.hotelbooking.dto.HotelReviewRequest;
import com.hotelbooking.dto.HotelReviewResponse;
import com.hotelbooking.entity.Hotel;
import com.hotelbooking.entity.HotelReview;
import com.hotelbooking.exception.ResourceNotFoundException;
import com.hotelbooking.mapper.ReviewMapper;
import com.hotelbooking.repository.HotelRepository;
import com.hotelbooking.repository.HotelReviewRepository;
import com.hotelbooking.service.ReviewService;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ReviewServiceImpl implements ReviewService {

    private final HotelRepository hotelRepository;
    private final HotelReviewRepository reviewRepository;
    private final ReviewMapper reviewMapper;

    public ReviewServiceImpl(HotelRepository hotelRepository,
                             HotelReviewRepository reviewRepository,
                             ReviewMapper reviewMapper) {
        this.hotelRepository = hotelRepository;
        this.reviewRepository = reviewRepository;
        this.reviewMapper = reviewMapper;
    }

    @Override
    public HotelReviewResponse addReview(Long hotelId, HotelReviewRequest request) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .filter(h -> Boolean.FALSE.equals(h.getIsDeleted()))
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found: " + hotelId));

        HotelReview review = reviewMapper.toEntity(request, hotel);
        HotelReview saved = reviewRepository.save(review);

        long totalReviews = reviewRepository.countByHotelId(hotelId);
        Double averageRating = reviewRepository.findAverageRatingByHotelId(hotelId);

        hotel.setTotalReviews((int) totalReviews);
        hotel.setAvgRating(averageRating == null ? 0.0 : averageRating);
        hotelRepository.save(hotel);

        return reviewMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HotelReviewResponse> getReviewsByHotel(Long hotelId) {
        hotelRepository.findById(hotelId)
                .filter(h -> Boolean.FALSE.equals(h.getIsDeleted()))
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found: " + hotelId));

        return reviewRepository.findByHotelId(hotelId).stream()
                .map(reviewMapper::toResponse)
                .collect(Collectors.toList());
    }
}
