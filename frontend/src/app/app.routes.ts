import { Routes } from '@angular/router';
import { authGuard, guestGuard, roleGuard } from './guards/auth.guard';
import { ShellComponent } from './layout/shell.component';
import { LoginComponent } from './pages/login/login.component';
import { RegisterComponent } from './pages/register/register.component';
import { HomeComponent } from './pages/home/home.component';
import { MenuComponent } from './pages/menu/menu.component';
import { AvailabilityComponent } from './pages/availability/availability.component';
import { ReserveComponent } from './pages/reserve/reserve.component';
import { ReservationsComponent } from './pages/reservations/reservations.component';
import { ReservationDetailComponent } from './pages/reservation-detail/reservation-detail.component';
import { NotificationsComponent } from './pages/notifications/notifications.component';
import { ProfileComponent } from './pages/profile/profile.component';
import { ManagerDashboardComponent } from './pages/manager/manager-dashboard.component';
import { ManagerReservationsComponent } from './pages/manager/manager-reservations.component';
import { ManagerReservationDetailComponent } from './pages/manager/manager-reservation-detail.component';
import { ManagerTablesComponent } from './pages/manager/manager-tables.component';
import { AdminDashboardComponent } from './pages/admin/admin-dashboard.component';
import { AdminUsersComponent } from './pages/admin/admin-users.component';
import { AdminManagerComponent } from './pages/admin/admin-manager.component';
import { AdminRestaurantComponent } from './pages/admin/admin-restaurant.component';
import { AdminCategoriesComponent } from './pages/admin/admin-categories.component';
import { AdminItemsComponent } from './pages/admin/admin-items.component';
import { AdminTablesComponent } from './pages/admin/admin-tables.component';
import { AdminReservationsComponent } from './pages/admin/admin-reservations.component';
import { AdminAuditComponent } from './pages/admin/admin-audit.component';

export const routes: Routes = [
  { path: 'login', component: LoginComponent, canActivate: [guestGuard] },
  { path: 'register', component: RegisterComponent, canActivate: [guestGuard] },
  {
    path: '',
    component: ShellComponent,
    canActivate: [authGuard],
    children: [
      { path: '', component: HomeComponent, canActivate: [roleGuard(['CUSTOMER'])] },
      { path: 'menu', component: MenuComponent, canActivate: [roleGuard(['CUSTOMER'])] },
      { path: 'availability', component: AvailabilityComponent, canActivate: [roleGuard(['CUSTOMER'])] },
      { path: 'reserve', component: ReserveComponent, canActivate: [roleGuard(['CUSTOMER'])] },
      { path: 'reservations', component: ReservationsComponent, canActivate: [roleGuard(['CUSTOMER'])] },
      { path: 'reservations/:id', component: ReservationDetailComponent, canActivate: [roleGuard(['CUSTOMER'])] },
      { path: 'notifications', component: NotificationsComponent, canActivate: [roleGuard(['CUSTOMER'])] },
      { path: 'profile', component: ProfileComponent },
      { path: 'manager', component: ManagerDashboardComponent, canActivate: [roleGuard(['MANAGER'])] },
      { path: 'manager/today', component: ManagerReservationsComponent, data: { today: true }, canActivate: [roleGuard(['MANAGER'])] },
      { path: 'manager/upcoming', component: ManagerReservationsComponent, data: { today: false }, canActivate: [roleGuard(['MANAGER'])] },
      { path: 'manager/reservations/:id', component: ManagerReservationDetailComponent, canActivate: [roleGuard(['MANAGER'])] },
      { path: 'manager/tables', component: ManagerTablesComponent, canActivate: [roleGuard(['MANAGER'])] },
      { path: 'admin', component: AdminDashboardComponent, canActivate: [roleGuard(['ADMIN'])] },
      { path: 'admin/users', component: AdminUsersComponent, canActivate: [roleGuard(['ADMIN'])] },
      { path: 'admin/manager', component: AdminManagerComponent, canActivate: [roleGuard(['ADMIN'])] },
      { path: 'admin/restaurant', component: AdminRestaurantComponent, canActivate: [roleGuard(['ADMIN'])] },
      { path: 'admin/categories', component: AdminCategoriesComponent, canActivate: [roleGuard(['ADMIN'])] },
      { path: 'admin/items', component: AdminItemsComponent, canActivate: [roleGuard(['ADMIN'])] },
      { path: 'admin/tables', component: AdminTablesComponent, canActivate: [roleGuard(['ADMIN'])] },
      { path: 'admin/reservations', component: AdminReservationsComponent, canActivate: [roleGuard(['ADMIN'])] },
      { path: 'admin/audit', component: AdminAuditComponent, canActivate: [roleGuard(['ADMIN'])] }
    ]
  }
];
