package com.asadi.havenly_stays.mapper;

import com.asadi.havenly_stays.dto.HotelReviewRequest;
import com.asadi.havenly_stays.dto.HotelReviewResponse;
import com.asadi.havenly_stays.entity.Hotel;
import com.asadi.havenly_stays.entity.HotelReview;
import com.asadi.havenly_stays.entity.ReviewImage;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class ReviewMapper {

    public HotelReview toEntity(HotelReviewRequest request, Hotel hotel) {
        if (request == null) {
            return null;
        }
        HotelReview review = HotelReview.builder()
                .hotel(hotel)
                .userId(request.getUserId())
                .rating(request.getRating())
                .title(request.getTitle())
                .reviewText(request.getReviewText())
                .build();

        if (request.getImageUrls() != null) {
            List<ReviewImage> images = request.getImageUrls().stream()
                    .filter(url -> url != null && !url.isBlank())
                    .map(url -> ReviewImage.builder().review(review).imageUrl(url).build())
                    .collect(Collectors.toList());
            review.setImages(images);
        }

        return review;
    }

    public HotelReviewResponse toResponse(HotelReview review) {
        if (review == null) {
            return null;
        }
        List<String> imageUrls = review.getImages().stream()
                .map(ReviewImage::getImageUrl)
                .collect(Collectors.toList());

        return HotelReviewResponse.builder()
                .id(review.getId())
                .userId(review.getUserId())
                .rating(review.getRating())
                .title(review.getTitle())
                .reviewText(review.getReviewText())
                .createdAt(review.getCreatedAt())
                .imageUrls(imageUrls)
                .build();
    }
}
