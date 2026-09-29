import { Component, EventEmitter, Output, inject } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';

import { RequestService } from '../../core/services/request.service';
import {
  CATEGORIES,
  CreateRequestPayload,
  PRIORITIES,
} from '../../models/request.model';

@Component({
  selector: 'app-request-form',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './request-form.component.html',
  styleUrl: './request-form.component.css',
})
export class RequestFormComponent {
  @Output() created = new EventEmitter<void>();
  @Output() close = new EventEmitter<void>();

  private readonly fb = inject(FormBuilder);
  private readonly service = inject(RequestService);

  readonly categories = CATEGORIES;
  readonly priorities = PRIORITIES;

  submitting = false;
  errorMessage: string | null = null;

  form: FormGroup = this.fb.group({
    title: ['', Validators.required],
    requesterId: ['', Validators.required],
    category: ['TI', Validators.required],
    priority: ['MEDIUM', Validators.required],
  });

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.submitting = true;
    this.errorMessage = null;

    const payload: CreateRequestPayload = this.form.value;

    this.service.create(payload).subscribe({
      next: () => {
        this.submitting = false;
        this.form.reset({ category: 'TI', priority: 'MEDIUM' });
        this.created.emit();
      },
      error: () => {
        this.submitting = false;
        this.errorMessage = this.service.error() ?? 'Falha ao criar a solicitação.';
      },
    });
  }

  cancel(): void {
    this.close.emit();
  }

  isInvalid(field: string): boolean {
    const control = this.form.get(field);
    return !!control && control.invalid && control.touched;
  }
}
