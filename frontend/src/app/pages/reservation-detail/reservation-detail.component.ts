import { Component, OnInit, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { NgIf } from '@angular/common';
import { ApiService } from '../../services/api.service';
import { Reservation } from '../../models';

@Component({
  selector: 'app-reservation-detail',
  standalone: true,
  imports: [NgIf, RouterLink, ReactiveFormsModule],
  templateUrl: './reservation-detail.component.html',
  styleUrl: './reservation-detail.component.css'
})
export class ReservationDetailComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private api = inject(ApiService);
  private fb = inject(FormBuilder);
  reservation?: Reservation;
  error = '';
  message = '';
  feedbackForm = this.fb.group({
    rating: [5, Validators.required],
    comment: ['']
  });
  rescheduleForm = this.fb.group({
    reservationDate: ['', Validators.required],
    startTime: ['', Validators.required],
    guestCount: [2, Validators.required]
  });

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.api.getReservation(id).subscribe({
      next: data => {
        this.reservation = data;
        this.rescheduleForm.patchValue({
          reservationDate: data.reservationDate,
          startTime: data.startTime?.substring(0, 5),
          guestCount: data.guestCount
        });
      },
      error: err => this.error = err.error?.message || 'Reservation not found'
    });
  }

  cancel(): void {
    if (!this.reservation || !confirm('Cancel this reservation?')) return;
    this.api.cancelReservation(this.reservation.id).subscribe({
      next: data => {
        this.reservation = data;
        this.message = 'Reservation cancelled';
      },
      error: err => this.error = err.error?.message || 'Unable to cancel'
    });
  }

  reschedule(): void {
    if (!this.reservation || this.rescheduleForm.invalid) return;
    this.api.reschedule(this.reservation.id, this.rescheduleForm.getRawValue()).subscribe({
      next: data => {
        this.reservation = data;
        this.message = 'Reservation rescheduled';
      },
      error: err => this.error = err.error?.message || 'Unable to reschedule'
    });
  }

  feedback(): void {
    if (!this.reservation) return;
    const value = this.feedbackForm.getRawValue();
    this.api.submitFeedback(this.reservation.id, Number(value.rating), value.comment || '').subscribe({
      next: () => this.message = 'Thanks for your feedback',
      error: err => this.error = err.error?.message || 'Unable to submit feedback'
    });
  }
}
