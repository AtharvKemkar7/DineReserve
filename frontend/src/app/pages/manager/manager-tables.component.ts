import { Component, OnInit } from '@angular/core';
import { NgFor, NgIf } from '@angular/common';
import { ApiService } from '../../services/api.service';
import { DiningTable } from '../../models';

@Component({
  selector: 'app-manager-tables',
  standalone: true,
  imports: [NgIf, NgFor],
  templateUrl: './manager-tables.component.html',
  styleUrl: './manager-tables.component.css'
})
export class ManagerTablesComponent implements OnInit {
  tables: DiningTable[] = [];
  error = '';
  constructor(private api: ApiService) {}
  ngOnInit(): void { this.load(); }
  load(): void { this.api.managerTables().subscribe(data => this.tables = data); }
  setStatus(table: DiningTable, status: string): void {
    this.api.updateTableStatus(table.id, status).subscribe({
      next: () => this.load(),
      error: err => this.error = err.error?.message || 'Unable to update table'
    });
  }
}
