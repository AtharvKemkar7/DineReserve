package com.dining.reservation.repository;

import com.dining.reservation.domain.MenuCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MenuCategoryRepository extends JpaRepository<MenuCategory, Long> {
    List<MenuCategory> findByActiveTrueOrderByDisplayOrderAsc();
    List<MenuCategory> findAllByOrderByDisplayOrderAsc();
}
