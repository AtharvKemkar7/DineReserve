import { Component, OnInit } from '@angular/core';
import { NgFor, NgIf } from '@angular/common';
import { ApiService } from '../../services/api.service';
import { Reservation } from '../../models';

@Component({
  selector: 'app-admin-reservations',
  standalone: true,
  imports: [NgIf, NgFor],
  templateUrl: './admin-reservations.component.html',
  styleUrl: './admin-reservations.component.css'
})
export class AdminReservationsComponent implements OnInit {
  reservations: Reservation[] = [];
  constructor(private api: ApiService) {}
  ngOnInit(): void { this.api.adminReservations().subscribe(data => this.reservations = data); }
}
