package com.dining.reservation.controller;

import com.dining.reservation.dto.RestaurantDtos.RestaurantResponse;
import com.dining.reservation.service.RestaurantService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/restaurant")
public class RestaurantController {

    private final RestaurantService restaurantService;

    public RestaurantController(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
    }

    @GetMapping
    public RestaurantResponse get() {
        return RestaurantResponse.from(restaurantService.get());
    }
}
