package com.hotelbooking.mapper;

import com.hotelbooking.dto.HotelReviewRequest;
import com.hotelbooking.dto.HotelReviewResponse;
import com.hotelbooking.entity.Hotel;
import com.hotelbooking.entity.HotelReview;
import com.hotelbooking.entity.ReviewImage;
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
