import { Component, OnInit, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { NgFor, NgIf } from '@angular/common';
import { ApiService } from '../../services/api.service';
import { User } from '../../models';

@Component({
  selector: 'app-admin-manager',
  standalone: true,
  imports: [NgIf, NgFor, ReactiveFormsModule],
  templateUrl: './admin-manager.component.html',
  styleUrl: './admin-manager.component.css'
})
export class AdminManagerComponent implements OnInit {
  private api = inject(ApiService);
  private fb = inject(FormBuilder);
  managers: User[] = [];
  message = '';
  error = '';
  form = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['manager123', Validators.required],
    fullName: ['', Validators.required],
    phone: ['']
  });
  ngOnInit(): void { this.load(); }
  load(): void { this.api.managers().subscribe(data => this.managers = data); }
  save(): void {
    this.api.upsertManager(this.form.getRawValue()).subscribe({
      next: () => { this.message = 'Manager account saved'; this.load(); },
      error: err => this.error = err.error?.message || 'Unable to save manager'
    });
  }
}
