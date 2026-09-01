export type Category = 'TI' | 'FINANCE' | 'HR' | 'FACILITIES';

export type Priority = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';

export type RequestStatus =
  | 'CREATED'
  | 'ANALYSIS'
  | 'APPROVAL'
  | 'PROCESSING'
  | 'COMPLETED'
  | 'REJECTED'
  | 'CANCELLED';

export const CATEGORIES: Category[] = ['TI', 'FINANCE', 'HR', 'FACILITIES'];

export const PRIORITIES: Priority[] = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'];

/**
 * Espelha a máquina de estados do backend (Request.ALLOWED_TRANSITIONS).
 */
export const ALLOWED_TRANSITIONS: Record<RequestStatus, RequestStatus[]> = {
  CREATED: ['ANALYSIS', 'CANCELLED'],
  ANALYSIS: ['APPROVAL', 'PROCESSING', 'CANCELLED'],
  APPROVAL: ['PROCESSING', 'REJECTED', 'CANCELLED'],
  PROCESSING: ['COMPLETED'],
  COMPLETED: [],
  REJECTED: [],
  CANCELLED: [],
};

export const CATEGORY_LABELS: Record<Category, string> = {
  TI: 'TI',
  FINANCE: 'Financeiro',
  HR: 'RH',
  FACILITIES: 'Facilities',
};

export const PRIORITY_LABELS: Record<Priority, string> = {
  LOW: 'Baixa',
  MEDIUM: 'Média',
  HIGH: 'Alta',
  CRITICAL: 'Crítica',
};

export const STATUS_LABELS: Record<RequestStatus, string> = {
  CREATED: 'Criado',
  ANALYSIS: 'Em análise',
  APPROVAL: 'Em aprovação',
  PROCESSING: 'Em processamento',
  COMPLETED: 'Concluído',
  REJECTED: 'Rejeitado',
  CANCELLED: 'Cancelado',
};

export interface Request {
  id: string;
  title: string;
  requesterId: string;
  category: Category;
  priority: Priority;
  status: RequestStatus;
  createdAt: string;
  updatedAt: string;
  version: number | null;
}

export interface CreateRequestPayload {
  title: string;
  requesterId: string;
  category: Category;
  priority: Priority;
}

export interface ChangeStatusPayload {
  targetStatus: RequestStatus;
}
