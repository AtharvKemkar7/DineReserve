package com.dining.reservation.service;

import com.dining.reservation.domain.MenuCategory;
import com.dining.reservation.domain.MenuItem;
import com.dining.reservation.dto.MenuDtos;
import com.dining.reservation.exception.ApiException;
import com.dining.reservation.repository.MenuCategoryRepository;
import com.dining.reservation.repository.MenuItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class MenuService {

    private final MenuCategoryRepository categoryRepository;
    private final MenuItemRepository itemRepository;
    private final AuditService auditService;

    public MenuService(
            MenuCategoryRepository categoryRepository,
            MenuItemRepository itemRepository,
            AuditService auditService) {
        this.categoryRepository = categoryRepository;
        this.itemRepository = itemRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public MenuDtos.MenuResponse publicMenu() {
        List<MenuDtos.CategoryWithItems> groups = new ArrayList<>();
        for (MenuCategory category : categoryRepository.findByActiveTrueOrderByDisplayOrderAsc()) {
            List<MenuDtos.ItemResponse> items = itemRepository.findByCategoryIdOrderByDisplayOrderAsc(category.getId())
                    .stream()
                    .filter(MenuItem::isAvailable)
                    .map(MenuDtos.ItemResponse::from)
                    .toList();
            groups.add(new MenuDtos.CategoryWithItems(category.getId(), category.getName(), category.getDescription(), items));
        }
        return new MenuDtos.MenuResponse(groups);
    }

    @Transactional(readOnly = true)
    public List<MenuCategory> allCategories() {
        return categoryRepository.findAllByOrderByDisplayOrderAsc();
    }

    @Transactional(readOnly = true)
    public List<MenuItem> allItems() {
        return itemRepository.findAllByOrderByDisplayOrderAsc();
    }

    @Transactional
    public MenuCategory createCategory(MenuDtos.CategoryRequest request) {
        MenuCategory category = new MenuCategory();
        applyCategory(category, request);
        category = categoryRepository.save(category);
        auditService.record("MENU_CATEGORY_CREATED", "MenuCategory", category.getId(), category.getName());
        return category;
    }

    @Transactional
    public MenuCategory updateCategory(Long id, MenuDtos.CategoryRequest request) {
        MenuCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Category not found"));
        applyCategory(category, request);
        category = categoryRepository.save(category);
        auditService.record("MENU_CATEGORY_UPDATED", "MenuCategory", category.getId(), category.getName());
        return category;
    }

    @Transactional
    public void deleteCategory(Long id) {
        MenuCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Category not found"));
        if (!itemRepository.findByCategoryIdOrderByDisplayOrderAsc(id).isEmpty()) {
            throw ApiException.badRequest("Remove menu items in this category first");
        }
        categoryRepository.delete(category);
        auditService.record("MENU_CATEGORY_DELETED", "MenuCategory", id, category.getName());
    }

    @Transactional
    public MenuItem createItem(MenuDtos.ItemRequest request) {
        MenuItem item = new MenuItem();
        applyItem(item, request);
        item = itemRepository.save(item);
        auditService.record("MENU_ITEM_CREATED", "MenuItem", item.getId(), item.getName());
        return item;
    }

    @Transactional
    public MenuItem updateItem(Long id, MenuDtos.ItemRequest request) {
        MenuItem item = itemRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Menu item not found"));
        applyItem(item, request);
        item = itemRepository.save(item);
        auditService.record("MENU_ITEM_UPDATED", "MenuItem", item.getId(), item.getName());
        return item;
    }

    @Transactional
    public void deleteItem(Long id) {
        MenuItem item = itemRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Menu item not found"));
        itemRepository.delete(item);
        auditService.record("MENU_ITEM_DELETED", "MenuItem", id, item.getName());
    }

    private void applyCategory(MenuCategory category, MenuDtos.CategoryRequest request) {
        category.setName(request.name());
        category.setDescription(request.description());
        if (request.displayOrder() != null) {
            category.setDisplayOrder(request.displayOrder());
        }
        if (request.active() != null) {
            category.setActive(request.active());
        }
    }

    private void applyItem(MenuItem item, MenuDtos.ItemRequest request) {
        MenuCategory category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> ApiException.notFound("Category not found"));
        item.setCategory(category);
        item.setName(request.name());
        item.setDescription(request.description());
        item.setPrice(request.price());
        if (request.available() != null) {
            item.setAvailable(request.available());
        }
        if (request.displayOrder() != null) {
            item.setDisplayOrder(request.displayOrder());
        }
    }
}
