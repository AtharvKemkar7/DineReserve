import { Component, OnInit } from '@angular/core';
import { NgFor, NgIf } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { Reservation } from '../../models';

@Component({
  selector: 'app-manager-reservations',
  standalone: true,
  imports: [NgIf, NgFor, RouterLink],
  templateUrl: './manager-reservations.component.html',
  styleUrl: './manager-reservations.component.css'
})
export class ManagerReservationsComponent implements OnInit {
  title = 'Reservations';
  reservations: Reservation[] = [];
  constructor(private api: ApiService, private route: ActivatedRoute) {}
  ngOnInit(): void {
    const today = this.route.snapshot.data['today'];
    this.title = today ? "Today's reservations" : 'Upcoming reservations';
    const req = today ? this.api.managerToday() : this.api.managerUpcoming();
    req.subscribe(data => this.reservations = data);
  }
}
