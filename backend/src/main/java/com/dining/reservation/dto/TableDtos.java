package com.dining.reservation.dto;

import com.dining.reservation.domain.DiningTable;
import com.dining.reservation.domain.TableStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class TableDtos {

    public record TableRequest(
            @NotBlank String tableNumber,
            String name,
            @NotNull @Min(1) Integer capacity,
            String section,
            TableStatus status,
            Boolean active
    ) {}

    public record TableStatusRequest(@NotNull TableStatus status) {}

    public record TableResponse(
            Long id,
            String tableNumber,
            String name,
            int capacity,
            String section,
            TableStatus status,
            boolean active
    ) {
        public static TableResponse from(DiningTable t) {
            return new TableResponse(
                    t.getId(),
                    t.getTableNumber(),
                    t.getName(),
                    t.getCapacity(),
                    t.getSection(),
                    t.getStatus(),
                    t.isActive()
            );
        }
    }
}
