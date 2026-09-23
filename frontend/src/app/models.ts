export type Role = 'CUSTOMER' | 'MANAGER' | 'ADMIN';
export type ReservationStatus = 'PENDING' | 'CONFIRMED' | 'CHECKED_IN' | 'COMPLETED' | 'CANCELLED' | 'NO_SHOW';
export type TableStatus = 'AVAILABLE' | 'OCCUPIED' | 'MAINTENANCE' | 'INACTIVE';

export interface AuthResponse {
  token: string;
  userId: number;
  email: string;
  fullName: string;
  role: Role;
}

export interface User {
  id: number;
  email: string;
  fullName: string;
  phone?: string;
  role: Role;
  enabled: boolean;
  createdAt?: string;
}

export interface Restaurant {
  id: number;
  name: string;
  description: string;
  address: string;
  phone: string;
  openingHours: string;
  reservationPolicy: string;
  defaultDurationMinutes: number;
  cancellationHoursNotice: number;
  openTime: string;
  closeTime: string;
}

export interface MenuItem {
  id: number;
  categoryId: number;
  categoryName: string;
  name: string;
  description: string;
  price: number;
  available: boolean;
  displayOrder: number;
}

export interface MenuCategory {
  id: number;
  name: string;
  description: string;
  displayOrder: number;
  active: boolean;
  items?: MenuItem[];
}

export interface MenuResponse {
  categories: MenuCategory[];
}

export interface DiningTable {
  id: number;
  tableNumber: string;
  name: string;
  capacity: number;
  section: string;
  status: TableStatus;
  active: boolean;
}

export interface Reservation {
  id: number;
  reservationNumber: string;
  customerId: number;
  customerName: string;
  customerEmail: string;
  reservationDate: string;
  startTime: string;
  endTime: string;
  guestCount: number;
  status: ReservationStatus;
  tableId?: number;
  tableNumber?: string;
  specialRequest?: string;
  managerNote?: string;
  createdAt?: string;
  updatedAt?: string;
  checkedInAt?: string;
  completedAt?: string;
}

export interface AvailabilitySlot {
  startTime: string;
  endTime: string;
  availableTableCount: number;
  available: boolean;
}

export interface AvailabilityResponse {
  date: string;
  guestCount: number;
  availableTables: DiningTable[];
  slots: AvailabilitySlot[];
}

export interface NotificationItem {
  id: number;
  title: string;
  message: string;
  type: string;
  reservationId?: number;
  read: boolean;
  createdAt: string;
}

export interface Feedback {
  id: number;
  reservationId: number;
  rating: number;
  comment: string;
  createdAt: string;
}

export interface AuditLog {
  id: number;
  action: string;
  entityType: string;
  entityId?: number;
  actorId?: number;
  actorEmail: string;
  details: string;
  createdAt: string;
}

export interface DashboardStats {
  todayReservations: number;
  upcomingConfirmed: number;
  pendingCount: number;
  completedToday: number;
  noShowToday: number;
  cancelledToday: number;
}
