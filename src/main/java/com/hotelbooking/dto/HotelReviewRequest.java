package com.hotelbooking.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class HotelReviewRequest {

    @NotNull
    private Long userId;

    @NotNull
    @Min(1)
    @Max(5)
    private Integer rating;

    @Size(max = 200)
    private String title;

    @Size(max = 5000)
    private String reviewText;

    private List<String> imageUrls;
}
