import { Component, OnInit, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { NgFor, NgIf } from '@angular/common';
import { ApiService } from '../../services/api.service';
import { User } from '../../models';

@Component({
  selector: 'app-admin-users',
  standalone: true,
  imports: [NgIf, NgFor, ReactiveFormsModule],
  templateUrl: './admin-users.component.html',
  styleUrl: './admin-users.component.css'
})
export class AdminUsersComponent implements OnInit {
  private api = inject(ApiService);
  private fb = inject(FormBuilder);
  users: User[] = [];
  error = '';
  form = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]],
    fullName: ['', Validators.required],
    phone: [''],
    role: ['CUSTOMER', Validators.required]
  });
  ngOnInit(): void { this.load(); }
  load(): void { this.api.adminUsers().subscribe(data => this.users = data); }
  create(): void {
    this.api.createUser(this.form.getRawValue()).subscribe({
      next: () => { this.form.reset({ role: 'CUSTOMER' }); this.load(); },
      error: err => this.error = err.error?.message || 'Unable to create user'
    });
  }
  toggle(user: User): void {
    this.api.updateUser(user.id, { enabled: !user.enabled }).subscribe(() => this.load());
  }
}
