package com.dining.reservation.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "restaurant_info")
public class RestaurantInfo {

    @Id
    private Long id = 1L;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(length = 300)
    private String address;

    @Column(length = 30)
    private String phone;

    @Column(length = 500)
    private String openingHours;

    @Column(length = 1000)
    private String reservationPolicy;

    @Column(nullable = false)
    private int defaultDurationMinutes = 90;

    @Column(nullable = false)
    private int cancellationHoursNotice = 2;

    @Column(nullable = false)
    private String openTime = "11:00";

    @Column(nullable = false)
    private String closeTime = "22:00";

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getOpeningHours() { return openingHours; }
    public void setOpeningHours(String openingHours) { this.openingHours = openingHours; }
    public String getReservationPolicy() { return reservationPolicy; }
    public void setReservationPolicy(String reservationPolicy) { this.reservationPolicy = reservationPolicy; }
    public int getDefaultDurationMinutes() { return defaultDurationMinutes; }
    public void setDefaultDurationMinutes(int defaultDurationMinutes) { this.defaultDurationMinutes = defaultDurationMinutes; }
    public int getCancellationHoursNotice() { return cancellationHoursNotice; }
    public void setCancellationHoursNotice(int cancellationHoursNotice) { this.cancellationHoursNotice = cancellationHoursNotice; }
    public String getOpenTime() { return openTime; }
    public void setOpenTime(String openTime) { this.openTime = openTime; }
    public String getCloseTime() { return closeTime; }
    public void setCloseTime(String closeTime) { this.closeTime = closeTime; }
}
