package com.asadi.havenly_stays.service.impl;

import com.asadi.havenly_stays.dto.HotelReviewRequest;
import com.asadi.havenly_stays.dto.HotelReviewResponse;
import com.asadi.havenly_stays.entity.Hotel;
import com.asadi.havenly_stays.entity.HotelReview;
import com.asadi.havenly_stays.exception.ResourceNotFoundException;
import com.asadi.havenly_stays.mapper.ReviewMapper;
import com.asadi.havenly_stays.repository.HotelRepository;
import com.asadi.havenly_stays.repository.HotelReviewRepository;
import com.asadi.havenly_stays.service.ReviewService;
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
