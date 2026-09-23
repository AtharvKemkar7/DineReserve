package com.dining.reservation.dto;

import com.dining.reservation.domain.MenuCategory;
import com.dining.reservation.domain.MenuItem;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;

public class MenuDtos {

    public record CategoryRequest(
            @NotBlank String name,
            String description,
            Integer displayOrder,
            Boolean active
    ) {}

    public record CategoryResponse(Long id, String name, String description, int displayOrder, boolean active) {
        public static CategoryResponse from(MenuCategory c) {
            return new CategoryResponse(c.getId(), c.getName(), c.getDescription(), c.getDisplayOrder(), c.isActive());
        }
    }

    public record ItemRequest(
            @NotNull Long categoryId,
            @NotBlank String name,
            String description,
            @NotNull @Positive BigDecimal price,
            Boolean available,
            Integer displayOrder
    ) {}

    public record ItemResponse(
            Long id,
            Long categoryId,
            String categoryName,
            String name,
            String description,
            BigDecimal price,
            boolean available,
            int displayOrder
    ) {
        public static ItemResponse from(MenuItem item) {
            return new ItemResponse(
                    item.getId(),
                    item.getCategory().getId(),
                    item.getCategory().getName(),
                    item.getName(),
                    item.getDescription(),
                    item.getPrice(),
                    item.isAvailable(),
                    item.getDisplayOrder()
            );
        }
    }

    public record MenuResponse(List<CategoryWithItems> categories) {}

    public record CategoryWithItems(Long id, String name, String description, List<ItemResponse> items) {}
}
