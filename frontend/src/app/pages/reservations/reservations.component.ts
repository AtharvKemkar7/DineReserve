import { Component, OnInit } from '@angular/core';
import { DatePipe, NgFor, NgIf } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { Reservation } from '../../models';

@Component({
  selector: 'app-reservations',
  standalone: true,
  imports: [NgIf, NgFor, RouterLink, DatePipe],
  templateUrl: './reservations.component.html',
  styleUrl: './reservations.component.css'
})
export class ReservationsComponent implements OnInit {
  reservations: Reservation[] = [];
  loading = true;
  error = '';

  constructor(private api: ApiService) {}

  ngOnInit(): void {
    this.api.myReservations().subscribe({
      next: data => {
        this.reservations = data;
        this.loading = false;
      },
      error: () => {
        this.error = 'Unable to load reservations';
        this.loading = false;
      }
    });
  }
}
