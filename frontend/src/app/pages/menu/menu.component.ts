import { Component, OnInit } from '@angular/core';
import { CurrencyPipe, NgFor, NgIf } from '@angular/common';
import { ApiService } from '../../services/api.service';
import { MenuCategory } from '../../models';

@Component({
  selector: 'app-menu',
  standalone: true,
  imports: [NgFor, NgIf, CurrencyPipe],
  templateUrl: './menu.component.html',
  styleUrl: './menu.component.css'
})
export class MenuComponent implements OnInit {
  categories: MenuCategory[] = [];
  loading = true;
  error = '';

  constructor(private api: ApiService) {}

  ngOnInit(): void {
    this.api.getMenu().subscribe({
      next: data => {
        this.categories = data.categories;
        this.loading = false;
      },
      error: () => {
        this.error = 'Unable to load menu';
        this.loading = false;
      }
    });
  }
}
