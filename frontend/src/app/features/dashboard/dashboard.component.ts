import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal, computed } from '@angular/core';

import { RequestService } from '../../core/services/request.service';
import { RequestFormComponent } from '../request-form/request-form.component';
import {
  ALLOWED_TRANSITIONS,
  CATEGORY_LABELS,
  PRIORITY_LABELS,
  Request,
  RequestStatus,
  STATUS_LABELS,
} from '../../models/request.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [DatePipe, RequestFormComponent],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css',
})
export class DashboardComponent implements OnInit {
  private readonly service = inject(RequestService);

  readonly requests = this.service.requests;
  readonly loading = this.service.loading;
  readonly error = this.service.error;

  // KPIs
  readonly totalRequests = computed(() => this.requests().length);
  readonly analysisRequests = computed(() => this.requests().filter(r => r.status === 'ANALYSIS').length);
  readonly approvedRequests = computed(() => this.requests().filter(r => r.status === 'APPROVAL').length);
  readonly doneRequests = computed(() => this.requests().filter(r => r.status === 'COMPLETED').length);

  readonly statusLabels = STATUS_LABELS;
  readonly categoryLabels = CATEGORY_LABELS;
  readonly priorityLabels = PRIORITY_LABELS;

  readonly showForm = signal(false);
  readonly busyId = signal<string | null>(null);

  ngOnInit(): void {
    this.service.load();
  }

  openForm(): void {
    this.showForm.set(true);
  }

  closeForm(): void {
    this.showForm.set(false);
  }

  onCreated(): void {
    this.showForm.set(false);
  }

  allowedTransitions(status: RequestStatus): RequestStatus[] {
    return ALLOWED_TRANSITIONS[status] ?? [];
  }

  changeStatus(request: Request, targetStatus: RequestStatus): void {
    this.busyId.set(request.id);
    this.service.changeStatus(request.id, targetStatus).subscribe({
      next: () => this.busyId.set(null),
      error: () => this.busyId.set(null),
    });
  }

  dismissError(): void {
    this.service.clearError();
  }
}
