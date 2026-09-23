import { Component, OnInit } from '@angular/core';
import { DatePipe, NgFor, NgIf } from '@angular/common';
import { ApiService } from '../../services/api.service';
import { NotificationItem } from '../../models';

@Component({
  selector: 'app-notifications',
  standalone: true,
  imports: [NgIf, NgFor, DatePipe],
  templateUrl: './notifications.component.html',
  styleUrl: './notifications.component.css'
})
export class NotificationsComponent implements OnInit {
  items: NotificationItem[] = [];
  constructor(private api: ApiService) {}
  ngOnInit(): void {
    this.api.notifications().subscribe(data => this.items = data);
  }
  read(item: NotificationItem): void {
    this.api.markRead(item.id).subscribe(updated => item.read = updated.read);
  }
}
