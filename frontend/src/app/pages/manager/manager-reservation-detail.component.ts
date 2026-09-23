import { Component, OnInit, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { NgFor, NgIf } from '@angular/common';
import { ApiService } from '../../services/api.service';
import { DiningTable, Reservation } from '../../models';

@Component({
  selector: 'app-manager-reservation-detail',
  standalone: true,
  imports: [NgIf, NgFor, RouterLink, ReactiveFormsModule],
  templateUrl: './manager-reservation-detail.component.html',
  styleUrl: './manager-reservation-detail.component.css'
})
export class ManagerReservationDetailComponent implements OnInit {
  private api = inject(ApiService);
  private route = inject(ActivatedRoute);
  private fb = inject(FormBuilder);
  reservation?: Reservation;
  tables: DiningTable[] = [];
  error = '';
  message = '';
  form = this.fb.group({
    tableId: [''],
    note: ['']
  });

  ngOnInit(): void {
    this.load();
    this.api.managerTables().subscribe(data => this.tables = data);
  }

  load(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.api.managerReservation(id).subscribe({
      next: data => {
        this.reservation = data;
        this.form.patchValue({
          tableId: data.tableId ? String(data.tableId) : '',
          note: data.managerNote || ''
        });
      },
      error: err => this.error = err.error?.message || 'Not found'
    });
  }

  assign(): void {
    if (!this.reservation || !this.form.value.tableId) return;
    this.api.assignTable(this.reservation.id, Number(this.form.value.tableId)).subscribe({
      next: data => { this.reservation = data; this.message = 'Table assigned'; },
      error: err => this.error = err.error?.message || 'Unable to assign table'
    });
  }

  saveNote(): void {
    if (!this.reservation) return;
    this.api.addNote(this.reservation.id, this.form.value.note || '').subscribe({
      next: data => { this.reservation = data; this.message = 'Note saved'; },
      error: err => this.error = err.error?.message || 'Unable to save note'
    });
  }

  act(action: 'checkIn' | 'complete' | 'noShow' | 'cancel'): void {
    if (!this.reservation) return;
    const calls = {
      checkIn: this.api.checkIn(this.reservation.id),
      complete: this.api.complete(this.reservation.id),
      noShow: this.api.noShow(this.reservation.id),
      cancel: this.api.managerCancel(this.reservation.id)
    };
    calls[action].subscribe({
      next: data => { this.reservation = data; this.message = 'Updated'; },
      error: err => this.error = err.error?.message || 'Unable to update status'
    });
  }
}
