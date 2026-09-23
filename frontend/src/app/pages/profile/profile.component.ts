import { Component, OnInit, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { NgIf } from '@angular/common';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [ReactiveFormsModule, NgIf],
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.css'
})
export class ProfileComponent implements OnInit {
  private fb = inject(FormBuilder);
  private auth = inject(AuthService);
  message = '';
  error = '';
  form = this.fb.group({
    fullName: ['', Validators.required],
    phone: ['']
  });

  ngOnInit(): void {
    this.auth.getProfile().subscribe(user => this.form.patchValue(user));
  }

  save(): void {
    const value = this.form.getRawValue();
    this.auth.updateProfile(value.fullName!, value.phone || '').subscribe({
      next: () => this.message = 'Profile updated',
      error: err => this.error = err.error?.message || 'Unable to update profile'
    });
  }
}
