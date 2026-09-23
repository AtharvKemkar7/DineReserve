import { Component, OnInit } from '@angular/core';
import { NgIf } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { Restaurant } from '../../models';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [NgIf, RouterLink],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css'
})
export class HomeComponent implements OnInit {
  restaurant?: Restaurant;
  error = '';

  constructor(private api: ApiService) {}

  ngOnInit(): void {
    this.api.getRestaurant().subscribe({
      next: data => this.restaurant = data,
      error: () => this.error = 'Unable to load restaurant information'
    });
  }
}
