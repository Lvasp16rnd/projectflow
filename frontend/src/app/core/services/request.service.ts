import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable, signal } from '@angular/core';
import { Observable, catchError, tap, throwError } from 'rxjs';

import {
  ChangeStatusPayload,
  CreateRequestPayload,
  Request,
  RequestStatus,
} from '../../models/request.model';

interface FieldError {
  field: string;
  message: string;
}

interface ErrorBody {
  message?: string;
  errors?: FieldError[];
}

@Injectable({ providedIn: 'root' })
export class RequestService {
  private readonly baseUrl = '/api/requests';

  private readonly _requests = signal<Request[]>([]);
  private readonly _loading = signal(false);
  private readonly _error = signal<string | null>(null);

  readonly requests = this._requests.asReadonly();
  readonly loading = this._loading.asReadonly();
  readonly error = this._error.asReadonly();

  constructor(private readonly http: HttpClient) {}

  load(): void {
    this._loading.set(true);
    this._error.set(null);

    this.http.get<Request[]>(this.baseUrl).subscribe({
      next: (data) => {
        this._requests.set(data);
        this._loading.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this._error.set(this.describeError(err));
        this._loading.set(false);
      },
    });
  }

  create(payload: CreateRequestPayload): Observable<Request> {
    return this.http.post<Request>(this.baseUrl, payload).pipe(
      tap(() => this.load()),
      catchError((err: HttpErrorResponse) => this.captureError(err)),
    );
  }

  changeStatus(id: string, targetStatus: RequestStatus): Observable<Request> {
    const payload: ChangeStatusPayload = { targetStatus };
    return this.http.patch<Request>(`${this.baseUrl}/${id}/status`, payload).pipe(
      tap(() => this.load()),
      catchError((err: HttpErrorResponse) => this.captureError(err)),
    );
  }

  clearError(): void {
    this._error.set(null);
  }

  private captureError(err: HttpErrorResponse): Observable<never> {
    this._error.set(this.describeError(err));
    return throwError(() => err);
  }

  private describeError(error: HttpErrorResponse): string {
    switch (error.status) {
      case 400:
        return this.describeValidation(error) ?? 'Payload inválido. Verifique os campos informados.';
      case 404:
        return 'Solicitação não encontrada.';
      case 409:
        return 'Conflito de concorrência: a solicitação foi alterada por outro usuário. Recarregue e tente novamente.';
      case 422: {
        const message = (error.error as ErrorBody | undefined)?.message;
        return message ?? 'Operação inválida para o estado atual da solicitação.';
      }
      default:
        return 'Erro inesperado ao comunicar com o servidor.';
    }
  }

  private describeValidation(error: HttpErrorResponse): string | null {
    const errors = (error.error as ErrorBody | undefined)?.errors;
    if (errors && errors.length > 0) {
      return errors.map((e) => `${e.field}: ${e.message}`).join(' · ');
    }
    return null;
  }
}
