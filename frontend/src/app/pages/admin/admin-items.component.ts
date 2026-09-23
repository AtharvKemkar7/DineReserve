import { Component, OnInit, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { CurrencyPipe, NgFor, NgIf } from '@angular/common';
import { ApiService } from '../../services/api.service';
import { MenuCategory, MenuItem } from '../../models';

@Component({
  selector: 'app-admin-items',
  standalone: true,
  imports: [NgIf, NgFor, ReactiveFormsModule, CurrencyPipe],
  templateUrl: './admin-items.component.html',
  styleUrl: './admin-items.component.css'
})
export class AdminItemsComponent implements OnInit {
  private api = inject(ApiService);
  private fb = inject(FormBuilder);
  items: MenuItem[] = [];
  categories: MenuCategory[] = [];
  form = this.fb.group({
    categoryId: ['', Validators.required],
    name: ['', Validators.required],
    description: [''],
    price: [10, Validators.required],
    available: [true]
  });
  ngOnInit(): void {
    this.api.adminCategories().subscribe(data => this.categories = data);
    this.load();
  }
  load(): void { this.api.adminItems().subscribe(data => this.items = data); }
  create(): void {
    const value = this.form.getRawValue();
    this.api.createItem({ ...value, categoryId: Number(value.categoryId) }).subscribe(() => this.load());
  }
  remove(id: number): void {
    if (!confirm('Delete this item?')) return;
    this.api.deleteItem(id).subscribe(() => this.load());
  }
}
