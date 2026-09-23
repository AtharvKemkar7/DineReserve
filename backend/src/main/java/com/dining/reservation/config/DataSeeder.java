package com.dining.reservation.config;

import com.dining.reservation.domain.*;
import com.dining.reservation.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;

@Configuration
@Profile("!test")
public class DataSeeder {

    @Bean
    CommandLineRunner seedData(
            UserRepository userRepository,
            RestaurantInfoRepository restaurantInfoRepository,
            MenuCategoryRepository categoryRepository,
            MenuItemRepository itemRepository,
            DiningTableRepository tableRepository,
            PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.findByEmail("admin@harbour.com").isEmpty()) {
                User admin = new User();
                admin.setEmail("admin@harbour.com");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setFullName("Harbour Admin");
                admin.setPhone("+1-555-0100");
                admin.setRole(Role.ADMIN);
                userRepository.save(admin);
            }
            if (userRepository.findByEmail("manager@harbour.com").isEmpty()) {
                User manager = new User();
                manager.setEmail("manager@harbour.com");
                manager.setPassword(passwordEncoder.encode("manager123"));
                manager.setFullName("Maya Chen");
                manager.setPhone("+1-555-0101");
                manager.setRole(Role.MANAGER);
                userRepository.save(manager);
            }
            if (userRepository.findByEmail("guest@harbour.com").isEmpty()) {
                User customer = new User();
                customer.setEmail("guest@harbour.com");
                customer.setPassword(passwordEncoder.encode("guest123"));
                customer.setFullName("Alex Rivera");
                customer.setPhone("+1-555-0199");
                customer.setRole(Role.CUSTOMER);
                userRepository.save(customer);
            }
            if (restaurantInfoRepository.findById(1L).isEmpty()) {
                RestaurantInfo info = new RestaurantInfo();
                info.setId(1L);
                info.setName("Harbour & Herb");
                info.setDescription("A neighborhood bistro serving seasonal menus, coastal produce, and relaxed table dining.");
                info.setAddress("18 Maple Street, Downtown");
                info.setPhone("+1-555-0148");
                info.setOpeningHours("Tue-Sun 11:00-22:00. Closed Mondays.");
                info.setReservationPolicy("Standard seating is 90 minutes. Cancel or reschedule at least 2 hours before your reservation. Special requests such as window seats or birthday notes are welcome.");
                info.setDefaultDurationMinutes(90);
                info.setCancellationHoursNotice(2);
                info.setOpenTime("11:00");
                info.setCloseTime("22:00");
                restaurantInfoRepository.save(info);
            }
            if (categoryRepository.count() == 0) {
                MenuCategory starters = category("Starters", "Small plates to begin", 1);
                MenuCategory mains = category("Mains", "Seasonal entrees", 2);
                MenuCategory desserts = category("Desserts", "House sweets", 3);
                MenuCategory drinks = category("Drinks", "Wine, tea, and sodas", 4);
                categoryRepository.save(starters);
                categoryRepository.save(mains);
                categoryRepository.save(desserts);
                categoryRepository.save(drinks);
                itemRepository.save(item(starters, "Herb Focaccia", "Warm bread with olive oil and sea salt", "8.00", 1));
                itemRepository.save(item(starters, "Tomato Consomme", "Clear tomato broth with basil oil", "11.00", 2));
                itemRepository.save(item(mains, "Seared Salmon", "Citrus butter, fennel, and new potatoes", "28.00", 1));
                itemRepository.save(item(mains, "Mushroom Risotto", "Arborio rice, thyme, and parmesan", "22.00", 2));
                itemRepository.save(item(mains, "Herb Roast Chicken", "Half chicken with pan jus and greens", "24.00", 3));
                itemRepository.save(item(desserts, "Lemon Tart", "Candied zest and whipped cream", "10.00", 1));
                itemRepository.save(item(drinks, "House White", "Glass of coastal sauvignon", "9.00", 1));
            }
            if (tableRepository.count() == 0) {
                tableRepository.save(table("1", "Window Two", 2, "Window", TableStatus.AVAILABLE));
                tableRepository.save(table("2", "Window Four", 4, "Window", TableStatus.AVAILABLE));
                tableRepository.save(table("3", "Garden Two", 2, "Garden", TableStatus.AVAILABLE));
                tableRepository.save(table("4", "Garden Four", 4, "Garden", TableStatus.AVAILABLE));
                tableRepository.save(table("5", "Booth Six", 6, "Booth", TableStatus.AVAILABLE));
                tableRepository.save(table("6", "Private Eight", 8, "Private", TableStatus.AVAILABLE));
            }
        };
    }

    private MenuCategory category(String name, String description, int order) {
        MenuCategory c = new MenuCategory();
        c.setName(name);
        c.setDescription(description);
        c.setDisplayOrder(order);
        c.setActive(true);
        return c;
    }

    private MenuItem item(MenuCategory category, String name, String description, String price, int order) {
        MenuItem item = new MenuItem();
        item.setCategory(category);
        item.setName(name);
        item.setDescription(description);
        item.setPrice(new BigDecimal(price));
        item.setAvailable(true);
        item.setDisplayOrder(order);
        return item;
    }

    private DiningTable table(String number, String name, int capacity, String section, TableStatus status) {
        DiningTable t = new DiningTable();
        t.setTableNumber(number);
        t.setName(name);
        t.setCapacity(capacity);
        t.setSection(section);
        t.setStatus(status);
        t.setActive(true);
        return t;
    }
}
