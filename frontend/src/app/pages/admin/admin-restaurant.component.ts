import { Component, OnInit, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { NgIf } from '@angular/common';
import { ApiService } from '../../services/api.service';

@Component({
  selector: 'app-admin-restaurant',
  standalone: true,
  imports: [ReactiveFormsModule, NgIf],
  templateUrl: './admin-restaurant.component.html',
  styleUrl: './admin-restaurant.component.css'
})
export class AdminRestaurantComponent implements OnInit {
  private api = inject(ApiService);
  private fb = inject(FormBuilder);
  message = '';
  form = this.fb.nonNullable.group({
    name: ['', Validators.required],
    description: [''],
    address: [''],
    phone: [''],
    openingHours: [''],
    reservationPolicy: [''],
    defaultDurationMinutes: [90],
    cancellationHoursNotice: [2],
    openTime: ['11:00'],
    closeTime: ['22:00']
  });
  ngOnInit(): void {
    this.api.getRestaurant().subscribe(data => this.form.patchValue(data));
  }
  save(): void {
    this.api.updateRestaurant(this.form.getRawValue()).subscribe(() => this.message = 'Restaurant settings saved');
  }
}
