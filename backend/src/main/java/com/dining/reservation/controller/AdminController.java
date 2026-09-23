package com.dining.reservation.controller;

import com.dining.reservation.dto.AuditDtos.AuditLogResponse;
import com.dining.reservation.dto.MenuDtos;
import com.dining.reservation.dto.ReservationDtos.ReservationResponse;
import com.dining.reservation.dto.RestaurantDtos.RestaurantResponse;
import com.dining.reservation.dto.RestaurantDtos.UpdateRestaurantRequest;
import com.dining.reservation.dto.TableDtos;
import com.dining.reservation.dto.UserDtos;
import com.dining.reservation.service.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserService userService;
    private final RestaurantService restaurantService;
    private final MenuService menuService;
    private final TableService tableService;
    private final ReservationService reservationService;
    private final AuditService auditService;

    public AdminController(
            UserService userService,
            RestaurantService restaurantService,
            MenuService menuService,
            TableService tableService,
            ReservationService reservationService,
            AuditService auditService) {
        this.userService = userService;
        this.restaurantService = restaurantService;
        this.menuService = menuService;
        this.tableService = tableService;
        this.reservationService = reservationService;
        this.auditService = auditService;
    }

    @GetMapping("/users")
    public List<UserDtos.UserResponse> users() {
        return userService.findAll().stream().map(UserDtos.UserResponse::from).toList();
    }

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDtos.UserResponse createUser(@Valid @RequestBody UserDtos.CreateUserRequest request) {
        return UserDtos.UserResponse.from(userService.createUser(request));
    }

    @PutMapping("/users/{id}")
    public UserDtos.UserResponse updateUser(@PathVariable Long id, @RequestBody UserDtos.UpdateUserRequest request) {
        return UserDtos.UserResponse.from(userService.updateUser(id, request));
    }

    @GetMapping("/manager")
    public List<UserDtos.UserResponse> managers() {
        return userService.findByRole(com.dining.reservation.domain.Role.MANAGER).stream()
                .map(UserDtos.UserResponse::from)
                .toList();
    }

    @PutMapping("/manager")
    public UserDtos.UserResponse upsertManager(@Valid @RequestBody UserDtos.CreateUserRequest request) {
        return UserDtos.UserResponse.from(userService.createOrReplaceManager(request));
    }

    @PutMapping("/restaurant")
    public RestaurantResponse updateRestaurant(@Valid @RequestBody UpdateRestaurantRequest request) {
        return RestaurantResponse.from(restaurantService.update(request));
    }

    @GetMapping("/menu/categories")
    public List<MenuDtos.CategoryResponse> categories() {
        return menuService.allCategories().stream().map(MenuDtos.CategoryResponse::from).toList();
    }

    @PostMapping("/menu/categories")
    @ResponseStatus(HttpStatus.CREATED)
    public MenuDtos.CategoryResponse createCategory(@Valid @RequestBody MenuDtos.CategoryRequest request) {
        return MenuDtos.CategoryResponse.from(menuService.createCategory(request));
    }

    @PutMapping("/menu/categories/{id}")
    public MenuDtos.CategoryResponse updateCategory(
            @PathVariable Long id, @Valid @RequestBody MenuDtos.CategoryRequest request) {
        return MenuDtos.CategoryResponse.from(menuService.updateCategory(id, request));
    }

    @DeleteMapping("/menu/categories/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable Long id) {
        menuService.deleteCategory(id);
    }

    @GetMapping("/menu/items")
    public List<MenuDtos.ItemResponse> items() {
        return menuService.allItems().stream().map(MenuDtos.ItemResponse::from).toList();
    }

    @PostMapping("/menu/items")
    @ResponseStatus(HttpStatus.CREATED)
    public MenuDtos.ItemResponse createItem(@Valid @RequestBody MenuDtos.ItemRequest request) {
        return MenuDtos.ItemResponse.from(menuService.createItem(request));
    }

    @PutMapping("/menu/items/{id}")
    public MenuDtos.ItemResponse updateItem(@PathVariable Long id, @Valid @RequestBody MenuDtos.ItemRequest request) {
        return MenuDtos.ItemResponse.from(menuService.updateItem(id, request));
    }

    @DeleteMapping("/menu/items/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteItem(@PathVariable Long id) {
        menuService.deleteItem(id);
    }

    @GetMapping("/tables")
    public List<TableDtos.TableResponse> tables() {
        return tableService.findAll().stream().map(TableDtos.TableResponse::from).toList();
    }

    @PostMapping("/tables")
    @ResponseStatus(HttpStatus.CREATED)
    public TableDtos.TableResponse createTable(@Valid @RequestBody TableDtos.TableRequest request) {
        return TableDtos.TableResponse.from(tableService.create(request));
    }

    @PutMapping("/tables/{id}")
    public TableDtos.TableResponse updateTable(@PathVariable Long id, @Valid @RequestBody TableDtos.TableRequest request) {
        return TableDtos.TableResponse.from(tableService.update(id, request));
    }

    @DeleteMapping("/tables/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTable(@PathVariable Long id) {
        tableService.delete(id);
    }

    @GetMapping("/reservations")
    public List<ReservationResponse> reservations() {
        return reservationService.allReservations().stream().map(ReservationResponse::forStaff).toList();
    }

    @GetMapping("/audit-logs")
    public List<AuditLogResponse> auditLogs() {
        return auditService.findAll().stream().map(AuditLogResponse::from).toList();
    }
}
