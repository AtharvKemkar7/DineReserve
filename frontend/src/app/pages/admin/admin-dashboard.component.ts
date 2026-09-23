import { Component, OnInit } from '@angular/core';
import { NgIf } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { DiningTable, Reservation, User } from '../../models';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [NgIf, RouterLink],
  templateUrl: './admin-dashboard.component.html',
  styleUrl: './admin-dashboard.component.css'
})
export class AdminDashboardComponent implements OnInit {
  users = 0;
  tables = 0;
  reservations = 0;
  constructor(private api: ApiService) {}
  ngOnInit(): void {
    this.api.adminUsers().subscribe((data: User[]) => this.users = data.length);
    this.api.adminTables().subscribe((data: DiningTable[]) => this.tables = data.length);
    this.api.adminReservations().subscribe((data: Reservation[]) => this.reservations = data.length);
  }
}
