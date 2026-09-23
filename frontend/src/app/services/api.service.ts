import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import {
  AuditLog, AvailabilityResponse, DashboardStats, DiningTable, Feedback,
  MenuCategory, MenuItem, MenuResponse, NotificationItem, Reservation, Restaurant, User
} from '../models';

@Injectable({ providedIn: 'root' })
export class ApiService {
  constructor(private http: HttpClient) {}

  getRestaurant() {
    return this.http.get<Restaurant>('/api/restaurant');
  }

  getMenu() {
    return this.http.get<MenuResponse>('/api/menu');
  }

  availability(date: string, startTime?: string, guestCount?: number) {
    let params = new HttpParams().set('date', date);
    if (startTime) params = params.set('startTime', startTime);
    if (guestCount) params = params.set('guestCount', String(guestCount));
    return this.http.get<AvailabilityResponse>('/api/tables/availability', { params });
  }

  createReservation(body: object) {
    return this.http.post<Reservation>('/api/reservations', body);
  }

  myReservations() {
    return this.http.get<Reservation[]>('/api/reservations/my');
  }

  getReservation(id: number) {
    return this.http.get<Reservation>(`/api/reservations/${id}`);
  }

  cancelReservation(id: number) {
    return this.http.post<Reservation>(`/api/reservations/${id}/cancel`, {});
  }

  reschedule(id: number, body: object) {
    return this.http.post<Reservation>(`/api/reservations/${id}/reschedule`, body);
  }

  submitFeedback(id: number, rating: number, comment: string) {
    return this.http.post<Feedback>(`/api/reservations/${id}/feedback`, { rating, comment });
  }

  notifications() {
    return this.http.get<NotificationItem[]>('/api/notifications');
  }

  unreadCount() {
    return this.http.get<{ count: number }>('/api/notifications/unread-count');
  }

  markRead(id: number) {
    return this.http.post<NotificationItem>(`/api/notifications/${id}/read`, {});
  }

  managerDashboard() {
    return this.http.get<DashboardStats>('/api/manager/dashboard');
  }

  managerToday() {
    return this.http.get<Reservation[]>('/api/manager/reservations/today');
  }

  managerUpcoming() {
    return this.http.get<Reservation[]>('/api/manager/reservations');
  }

  managerReservation(id: number) {
    return this.http.get<Reservation>(`/api/manager/reservations/${id}`);
  }

  managerTables() {
    return this.http.get<DiningTable[]>('/api/manager/tables');
  }

  updateTableStatus(id: number, status: string) {
    return this.http.patch<DiningTable>(`/api/manager/tables/${id}/status`, { status });
  }

  assignTable(reservationId: number, tableId: number) {
    return this.http.post<Reservation>(`/api/manager/reservations/${reservationId}/assign-table`, { tableId });
  }

  checkIn(id: number) {
    return this.http.post<Reservation>(`/api/manager/reservations/${id}/check-in`, {});
  }

  complete(id: number) {
    return this.http.post<Reservation>(`/api/manager/reservations/${id}/complete`, {});
  }

  noShow(id: number) {
    return this.http.post<Reservation>(`/api/manager/reservations/${id}/no-show`, {});
  }

  managerCancel(id: number) {
    return this.http.post<Reservation>(`/api/manager/reservations/${id}/cancel`, {});
  }

  addNote(id: number, note: string) {
    return this.http.post<Reservation>(`/api/manager/reservations/${id}/notes`, { note });
  }

  adminUsers() {
    return this.http.get<User[]>('/api/admin/users');
  }

  createUser(body: object) {
    return this.http.post<User>('/api/admin/users', body);
  }

  updateUser(id: number, body: object) {
    return this.http.put<User>(`/api/admin/users/${id}`, body);
  }

  managers() {
    return this.http.get<User[]>('/api/admin/manager');
  }

  upsertManager(body: object) {
    return this.http.put<User>('/api/admin/manager', body);
  }

  updateRestaurant(body: object) {
    return this.http.put<Restaurant>('/api/admin/restaurant', body);
  }

  adminCategories() {
    return this.http.get<MenuCategory[]>('/api/admin/menu/categories');
  }

  createCategory(body: object) {
    return this.http.post<MenuCategory>('/api/admin/menu/categories', body);
  }

  updateCategory(id: number, body: object) {
    return this.http.put<MenuCategory>(`/api/admin/menu/categories/${id}`, body);
  }

  deleteCategory(id: number) {
    return this.http.delete(`/api/admin/menu/categories/${id}`);
  }

  adminItems() {
    return this.http.get<MenuItem[]>('/api/admin/menu/items');
  }

  createItem(body: object) {
    return this.http.post<MenuItem>('/api/admin/menu/items', body);
  }

  updateItem(id: number, body: object) {
    return this.http.put<MenuItem>(`/api/admin/menu/items/${id}`, body);
  }

  deleteItem(id: number) {
    return this.http.delete(`/api/admin/menu/items/${id}`);
  }

  adminTables() {
    return this.http.get<DiningTable[]>('/api/admin/tables');
  }

  createTable(body: object) {
    return this.http.post<DiningTable>('/api/admin/tables', body);
  }

  updateTable(id: number, body: object) {
    return this.http.put<DiningTable>(`/api/admin/tables/${id}`, body);
  }

  deleteTable(id: number) {
    return this.http.delete(`/api/admin/tables/${id}`);
  }

  adminReservations() {
    return this.http.get<Reservation[]>('/api/admin/reservations');
  }

  auditLogs() {
    return this.http.get<AuditLog[]>('/api/admin/audit-logs');
  }
}
