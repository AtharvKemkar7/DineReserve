import { Component, OnInit } from '@angular/core';
import { NgIf } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { DashboardStats } from '../../models';

@Component({
  selector: 'app-manager-dashboard',
  standalone: true,
  imports: [NgIf, RouterLink],
  templateUrl: './manager-dashboard.component.html',
  styleUrl: './manager-dashboard.component.css'
})
export class ManagerDashboardComponent implements OnInit {
  stats?: DashboardStats;
  constructor(private api: ApiService) {}
  ngOnInit(): void {
    this.api.managerDashboard().subscribe(data => this.stats = data);
  }
}
