package com.asadi.havenly_stays.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "room_availabilities",
        uniqueConstraints = @UniqueConstraint(columnNames = {"room_type_id", "date"}))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomAvailability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_type_id", nullable = false)
    private Long roomTypeId;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    @DecimalMin(value = "0.0", inclusive = true)
    private Double price;

    @Column(nullable = false)
    @Min(0)
    private Integer availableRooms;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
