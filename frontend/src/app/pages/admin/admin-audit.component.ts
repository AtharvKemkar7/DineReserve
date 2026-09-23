import { Component, OnInit } from '@angular/core';
import { DatePipe, NgFor, NgIf } from '@angular/common';
import { ApiService } from '../../services/api.service';
import { AuditLog } from '../../models';

@Component({
  selector: 'app-admin-audit',
  standalone: true,
  imports: [NgIf, NgFor, DatePipe],
  templateUrl: './admin-audit.component.html',
  styleUrl: './admin-audit.component.css'
})
export class AdminAuditComponent implements OnInit {
  logs: AuditLog[] = [];
  constructor(private api: ApiService) {}
  ngOnInit(): void { this.api.auditLogs().subscribe(data => this.logs = data); }
}
