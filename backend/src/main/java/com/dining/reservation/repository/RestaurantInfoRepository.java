package com.dining.reservation.repository;

import com.dining.reservation.domain.RestaurantInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RestaurantInfoRepository extends JpaRepository<RestaurantInfo, Long> {
}
