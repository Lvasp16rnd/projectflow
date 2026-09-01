import { Injectable, signal } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class AuthService {
  readonly isLoggedIn = signal<boolean>(false);

  login(username: string, pass: string): boolean {
    if (username === 'admin' && pass === 'admin') {
      this.isLoggedIn.set(true);
      return true;
    }
    return false;
  }

  logout(): void {
    this.isLoggedIn.set(false);
  }
}
