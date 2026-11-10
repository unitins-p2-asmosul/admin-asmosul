
import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { FooterComponent } from '@core/layout/footer.component';
import { HeaderComponent } from '@core/layout/header.component';
import { SidebarComponent } from '@core/layout/sidebar.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    RouterOutlet,
    FooterComponent,
    HeaderComponent,
    SidebarComponent,
  ],
  templateUrl: './app.html',
})
export class App {
  protected readonly menuMobileAberto = signal(false);

  protected alternarMenuMobile(): void {
    this.menuMobileAberto.update(aberto => !aberto);
  }

  protected fecharMenuMobile(): void {
    this.menuMobileAberto.set(false);
  }
}
