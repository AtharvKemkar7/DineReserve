package com.dining.reservation.service;

import com.dining.reservation.domain.RestaurantInfo;
import com.dining.reservation.dto.RestaurantDtos.UpdateRestaurantRequest;
import com.dining.reservation.repository.RestaurantInfoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RestaurantService {

    private final RestaurantInfoRepository restaurantInfoRepository;
    private final AuditService auditService;

    public RestaurantService(RestaurantInfoRepository restaurantInfoRepository, AuditService auditService) {
        this.restaurantInfoRepository = restaurantInfoRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public RestaurantInfo get() {
        return restaurantInfoRepository.findById(1L).orElseGet(this::defaultInfo);
    }

    @Transactional
    public RestaurantInfo update(UpdateRestaurantRequest request) {
        RestaurantInfo info = restaurantInfoRepository.findById(1L).orElseGet(this::defaultInfo);
        info.setName(request.name());
        info.setDescription(request.description());
        info.setAddress(request.address());
        info.setPhone(request.phone());
        info.setOpeningHours(request.openingHours());
        info.setReservationPolicy(request.reservationPolicy());
        if (request.defaultDurationMinutes() != null) {
            info.setDefaultDurationMinutes(request.defaultDurationMinutes());
        }
        if (request.cancellationHoursNotice() != null) {
            info.setCancellationHoursNotice(request.cancellationHoursNotice());
        }
        if (request.openTime() != null) {
            info.setOpenTime(request.openTime());
        }
        if (request.closeTime() != null) {
            info.setCloseTime(request.closeTime());
        }
        info = restaurantInfoRepository.save(info);
        auditService.record("RESTAURANT_UPDATED", "RestaurantInfo", info.getId(), "Restaurant settings updated");
        return info;
    }

    private RestaurantInfo defaultInfo() {
        RestaurantInfo info = new RestaurantInfo();
        info.setId(1L);
        info.setName("Harbour & Herb");
        info.setDescription("A neighborhood bistro serving seasonal menus and relaxed table dining.");
        info.setAddress("18 Maple Street, Downtown");
        info.setPhone("+1-555-0148");
        info.setOpeningHours("Tue-Sun 11:00-22:00. Closed Mondays.");
        info.setReservationPolicy("Reservations last 90 minutes. Cancel at least 2 hours in advance.");
        info.setDefaultDurationMinutes(90);
        info.setCancellationHoursNotice(2);
        info.setOpenTime("11:00");
        info.setCloseTime("22:00");
        return info;
    }
}
