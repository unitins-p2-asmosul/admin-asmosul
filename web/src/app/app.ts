import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import {FooterComponent} from '@core/layout/footer.component';
import {HeaderComponent} from '@core/layout/header.component';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, FooterComponent, HeaderComponent],
  templateUrl: './app.html',
})
export class App {
  protected readonly title = signal('web');
}
