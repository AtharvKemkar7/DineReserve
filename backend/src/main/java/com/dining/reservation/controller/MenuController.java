package com.dining.reservation.controller;

import com.dining.reservation.dto.MenuDtos;
import com.dining.reservation.service.MenuService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/menu")
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @GetMapping
    public MenuDtos.MenuResponse menu() {
        return menuService.publicMenu();
    }

    @GetMapping("/categories")
    public List<MenuDtos.CategoryResponse> categories() {
        return menuService.allCategories().stream()
                .filter(c -> c.isActive())
                .map(MenuDtos.CategoryResponse::from)
                .toList();
    }
}
