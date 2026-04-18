import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CdkDragDrop, DragDropModule, moveItemInArray, transferArrayItem } from '@angular/cdk/drag-drop';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { NzButtonComponent } from 'ng-zorro-antd/button';
import { NzIconDirective } from 'ng-zorro-antd/icon';
import { NzNotificationService } from 'ng-zorro-antd/notification';
import { NzSpinComponent } from 'ng-zorro-antd/spin';
import { NzTagComponent } from 'ng-zorro-antd/tag';
import { InputCommon } from '../../../shared/input/input';
import { TaskKanbanBoardUpdatePayload, TaskKanbanColumn } from '../../management/tasks/models/task.model';
import { TaskService } from '../../management/tasks/services/task.service';
import { TaskStatus } from '../../management/tasks/models/task.types';

@Component({
  selector: 'app-task-kanban',
  imports: [
    CommonModule,
    FormsModule,
    DragDropModule,
    NzButtonComponent,
    NzIconDirective,
    NzSpinComponent,
    NzTagComponent,
    TranslatePipe,
    InputCommon,
  ],
  templateUrl: './task-kanban.html',
  styleUrls: ['./task-kanban.scss'],
})
export class TaskKanban {
  readonly projectAutocompleteUrl = '/api/v1/autocomplete/projects';

  projectId: string | null = null;
  loading = false;
  saving = false;

  columns: TaskKanbanColumn[] = this.emptyColumns();

  private saveTimer: any = null;
  hoverStatus: TaskStatus | null = null;

  listId(status: TaskStatus): string {
    return `kanban-list-${status}`;
  }

  connectedListIds(): string[] {
    return (this.columns ?? []).map((c) => this.listId(c.status));
  }

  constructor(
    private taskService: TaskService,
    private notification: NzNotificationService,
    private translate: TranslateService
  ) {}

  onLoad(): void {
    if (!this.projectId) {
      this.notification.warning(this.translate.instant('common.error'), this.translate.instant('kanban.messages.projectRequired'));
      return;
    }
    this.loading = true;
    this.taskService.getKanbanBoard(this.projectId).subscribe({
      next: ({ raw, board }) => {
        this.loading = false;
        if (raw?.code === 200 && board?.columns?.length) {
          // đảm bảo đủ 5 cột
          const base = this.emptyColumns();
          for (const c of base) {
            const found = board.columns.find((x) => x.status === c.status);
            c.tasks = found?.tasks ?? [];
            c.name = found?.name ?? c.name;
          }
          this.columns = base;
          return;
        }
        this.columns = this.emptyColumns();
      },
      error: () => {
        this.loading = false;
        this.columns = this.emptyColumns();
      },
    });
  }

  drop(evt: CdkDragDrop<any[]>, toStatus: TaskStatus): void {
    if (!this.projectId) return;
    if (evt.previousContainer === evt.container) {
      moveItemInArray(evt.container.data, evt.previousIndex, evt.currentIndex);
    } else {
      transferArrayItem(evt.previousContainer.data, evt.container.data, evt.previousIndex, evt.currentIndex);
    }

    // sync status theo cột đích (để UI phản ánh ngay)
    const arr = evt.container.data as any[];
    const moved = arr?.[evt.currentIndex];
    if (moved) {
      moved.status = toStatus;
    }

    this.hoverStatus = null;
    this.scheduleSave();
  }

  onListEntered(status: TaskStatus): void {
    this.hoverStatus = status;
  }

  onListExited(status: TaskStatus): void {
    if (this.hoverStatus === status) {
      this.hoverStatus = null;
    }
  }

  /** Gán ngay cột nguồn khi bắt đầu kéo (trước khi có entered sang list khác). */
  onDragStarted(status: TaskStatus): void {
    this.hoverStatus = status;
  }

  onDragEnded(): void {
    this.hoverStatus = null;
  }

  priorityColor(p?: string | null): string {
    switch (p) {
      case 'URGENT':
        return 'red';
      case 'HIGH':
        return 'orange';
      case 'MEDIUM':
        return 'blue';
      case 'LOW':
        return 'default';
      default:
        return 'default';
    }
  }

  initials(display?: string | null): string {
    if (!display) return '';
    const parts = display.split('-').map((x) => x.trim()).filter(Boolean);
    const name = (parts[1] ?? parts[0] ?? '').trim();
    if (!name) return '';
    const tokens = name.split(/\s+/).slice(0, 2);
    return tokens.map((x) => x[0]?.toUpperCase()).join('');
  }

  private scheduleSave(): void {
    if (this.saveTimer) clearTimeout(this.saveTimer);
    this.saveTimer = setTimeout(() => this.save(), 250);
  }

  private save(): void {
    if (!this.projectId) return;
    const payload: TaskKanbanBoardUpdatePayload = {
      projectId: this.projectId,
      columns: this.columns.map((c) => ({
        status: c.status,
        taskIds: (c.tasks ?? []).map((t) => t.id).filter(Boolean),
      })),
    };
    this.saving = true;
    this.taskService.updateKanbanBoard(payload).subscribe({
      next: ({ raw }) => {
        this.saving = false;
        if (raw?.code !== 200) return;
      },
      error: () => (this.saving = false),
    });
  }

  private emptyColumns(): TaskKanbanColumn[] {
    return [
      { status: 'TODO', name: 'Todo', tasks: [] },
      { status: 'IN_PROGRESS', name: 'In Progress', tasks: [] },
      { status: 'REVIEW', name: 'Review', tasks: [] },
      { status: 'TESTING', name: 'Testing', tasks: [] },
      { status: 'DONE', name: 'Done', tasks: [] },
    ];
  }
}

