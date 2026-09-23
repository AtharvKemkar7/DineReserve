package com.dining.reservation.dto;

import com.dining.reservation.domain.RestaurantInfo;
import jakarta.validation.constraints.NotBlank;

public class RestaurantDtos {

    public record RestaurantResponse(
            Long id,
            String name,
            String description,
            String address,
            String phone,
            String openingHours,
            String reservationPolicy,
            int defaultDurationMinutes,
            int cancellationHoursNotice,
            String openTime,
            String closeTime
    ) {
        public static RestaurantResponse from(RestaurantInfo info) {
            return new RestaurantResponse(
                    info.getId(),
                    info.getName(),
                    info.getDescription(),
                    info.getAddress(),
                    info.getPhone(),
                    info.getOpeningHours(),
                    info.getReservationPolicy(),
                    info.getDefaultDurationMinutes(),
                    info.getCancellationHoursNotice(),
                    info.getOpenTime(),
                    info.getCloseTime()
            );
        }
    }

    public record UpdateRestaurantRequest(
            @NotBlank String name,
            String description,
            String address,
            String phone,
            String openingHours,
            String reservationPolicy,
            Integer defaultDurationMinutes,
            Integer cancellationHoursNotice,
            String openTime,
            String closeTime
    ) {}
}
