package com.asadi.havenly_stays.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "room_types")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "hotel_id", nullable = false)
    private Long hotelId;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 5000)
    private String description;

    @Column(nullable = false)
    private Integer maxGuests;

    private Integer maxAdults;

    private Integer maxChildren;

    private String bedType;

    private Integer bedCount;

    private Double roomSize;

    @Column(nullable = false)
    @DecimalMin(value = "0.0", inclusive = true)
    private Double basePrice;

    private String currency;

    @Column(nullable = false)
    @Min(0)
    private Integer totalRooms;

    private Boolean isActive;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (currency == null || currency.isBlank()) {
            currency = "INR";
        }
        if (isActive == null) {
            isActive = true;
        }
        if (basePrice == null) {
            basePrice = 0.0;
        }
        if (totalRooms == null) {
            totalRooms = 0;
        }
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
