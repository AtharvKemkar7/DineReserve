import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { NgFor, NgIf } from '@angular/common';
import { Router } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { AvailabilityResponse } from '../../models';

@Component({
  selector: 'app-reserve',
  standalone: true,
  imports: [ReactiveFormsModule, NgIf, NgFor],
  templateUrl: './reserve.component.html',
  styleUrl: './reserve.component.css'
})
export class ReserveComponent {
  private fb = inject(FormBuilder);
  private api = inject(ApiService);
  private router = inject(Router);
  availability?: AvailabilityResponse;
  error = '';
  success = '';
  loading = false;
  form = this.fb.group({
    reservationDate: ['', Validators.required],
    startTime: ['18:00', Validators.required],
    guestCount: [2, [Validators.required, Validators.min(1)]],
    preferredTableId: [''],
    specialRequest: ['']
  });

  check(): void {
    const { reservationDate, startTime, guestCount } = this.form.getRawValue();
    if (!reservationDate) return;
    this.api.availability(reservationDate!, startTime || undefined, guestCount || 2).subscribe({
      next: data => this.availability = data,
      error: err => this.error = err.error?.message || 'Unable to load tables'
    });
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    if (!confirm('Confirm this reservation?')) return;
    this.loading = true;
    this.error = '';
    const value = this.form.getRawValue();
    const body: any = {
      reservationDate: value.reservationDate,
      startTime: value.startTime,
      guestCount: value.guestCount,
      specialRequest: value.specialRequest
    };
    if (value.preferredTableId) {
      body.preferredTableId = Number(value.preferredTableId);
    }
    this.api.createReservation(body).subscribe({
      next: res => this.router.navigate(['/reservations', res.id]),
      error: err => {
        this.loading = false;
        this.error = err.error?.message || 'Unable to create reservation';
      }
    });
  }
}
