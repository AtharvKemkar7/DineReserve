import { Component, OnInit, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { NgFor, NgIf } from '@angular/common';
import { ApiService } from '../../services/api.service';
import { MenuCategory } from '../../models';

@Component({
  selector: 'app-admin-categories',
  standalone: true,
  imports: [NgIf, NgFor, ReactiveFormsModule],
  templateUrl: './admin-categories.component.html',
  styleUrl: './admin-categories.component.css'
})
export class AdminCategoriesComponent implements OnInit {
  private api = inject(ApiService);
  private fb = inject(FormBuilder);
  categories: MenuCategory[] = [];
  form = this.fb.group({
    name: ['', Validators.required],
    description: [''],
    displayOrder: [0],
    active: [true]
  });
  ngOnInit(): void { this.load(); }
  load(): void { this.api.adminCategories().subscribe(data => this.categories = data); }
  create(): void {
    this.api.createCategory(this.form.getRawValue()).subscribe(() => { this.form.reset({ active: true, displayOrder: 0 }); this.load(); });
  }
  remove(id: number): void {
    if (!confirm('Delete this category?')) return;
    this.api.deleteCategory(id).subscribe(() => this.load());
  }
}
