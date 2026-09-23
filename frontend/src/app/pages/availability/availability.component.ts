import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { NgFor, NgIf } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { AvailabilityResponse } from '../../models';

@Component({
  selector: 'app-availability',
  standalone: true,
  imports: [ReactiveFormsModule, NgIf, NgFor, RouterLink],
  templateUrl: './availability.component.html',
  styleUrl: './availability.component.css'
})
export class AvailabilityComponent {
  private fb = inject(FormBuilder);
  private api = inject(ApiService);
  result?: AvailabilityResponse;
  error = '';
  loading = false;
  form = this.fb.group({
    date: ['', Validators.required],
    startTime: ['18:00'],
    guestCount: [2, [Validators.required, Validators.min(1)]]
  });

  search(): void {
    if (this.form.invalid) return;
    this.loading = true;
    this.error = '';
    const { date, startTime, guestCount } = this.form.getRawValue();
    this.api.availability(date!, startTime || undefined, guestCount || 2).subscribe({
      next: data => {
        this.result = data;
        this.loading = false;
      },
      error: err => {
        this.loading = false;
        this.error = err.error?.message || 'Unable to check availability';
      }
    });
  }
}
