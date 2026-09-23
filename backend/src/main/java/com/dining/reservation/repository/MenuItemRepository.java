package com.dining.reservation.repository;

import com.dining.reservation.domain.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    List<MenuItem> findByCategoryIdOrderByDisplayOrderAsc(Long categoryId);
    List<MenuItem> findByAvailableTrueOrderByDisplayOrderAsc();
    List<MenuItem> findAllByOrderByDisplayOrderAsc();
}
