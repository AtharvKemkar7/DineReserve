import { Component, OnInit, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { NgFor, NgIf } from '@angular/common';
import { ApiService } from '../../services/api.service';
import { DiningTable } from '../../models';

@Component({
  selector: 'app-admin-tables',
  standalone: true,
  imports: [NgIf, NgFor, ReactiveFormsModule],
  templateUrl: './admin-tables.component.html',
  styleUrl: './admin-tables.component.css'
})
export class AdminTablesComponent implements OnInit {
  private api = inject(ApiService);
  private fb = inject(FormBuilder);
  tables: DiningTable[] = [];
  error = '';
  form = this.fb.group({
    tableNumber: ['', Validators.required],
    name: [''],
    capacity: [2, Validators.required],
    section: ['Main'],
    active: [true]
  });
  ngOnInit(): void { this.load(); }
  load(): void { this.api.adminTables().subscribe(data => this.tables = data); }
  create(): void {
    this.api.createTable(this.form.getRawValue()).subscribe({
      next: () => this.load(),
      error: err => this.error = err.error?.message || 'Unable to create table'
    });
  }
  toggle(table: DiningTable): void {
    this.api.updateTable(table.id, {
      tableNumber: table.tableNumber,
      name: table.name,
      capacity: table.capacity,
      section: table.section,
      active: !table.active
    }).subscribe(() => this.load());
  }
}
