import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CdkDragDrop, CdkDrag, CdkDropList, DragDropModule, moveItemInArray, transferArrayItem } from '@angular/cdk/drag-drop';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { ActivatedRoute } from '@angular/router';
import { NzIconDirective } from 'ng-zorro-antd/icon';
import { NzNotificationService } from 'ng-zorro-antd/notification';
import { NzSpinComponent } from 'ng-zorro-antd/spin';
import { NzTagComponent } from 'ng-zorro-antd/tag';
import { NzPopoverDirective } from 'ng-zorro-antd/popover';
import { InputCommon } from '../../../shared/input/input';
import { TaskChecklistItem, TaskKanbanBoardUpdatePayload, TaskKanbanColumn, Task } from '../../project/models/task.model';
import { TaskService } from '../../project/services/task.service';
import { TaskStatus } from '../../project/models/task.types';
import { StoreService } from '../../../core/services/store-service';
import { ProjectService } from '../../project/services/project.service';

@Component({
  selector: 'app-task-kanban',
  imports: [
    CommonModule,
    FormsModule,
    DragDropModule,
    NzIconDirective,
    NzSpinComponent,
    NzTagComponent,
    NzPopoverDirective,
    TranslatePipe,
    InputCommon,
  ],
  templateUrl: './task-kanban.html',
  styleUrls: ['./task-kanban.scss'],
})
export class TaskKanban {
  readonly projectAutocompleteUrl = '/api/v1/autocomplete/projects/kanban';

  projectId: string | null = null;
  loading = false;
  saving = false;
  private currentUserId: string | null = null;
  private projectManagerId: string | null = null;

  columns: TaskKanbanColumn[] = this.emptyColumns();

  private saveTimer: any = null;
  hoverStatus: TaskStatus | null = null;
  checklistByTaskId: Record<string, { loading: boolean; items: TaskChecklistItem[] }> = {};
  activeChecklistTask: Task | null = null;

  listId(status: TaskStatus): string {
    return `kanban-list-${status}`;
  }

  connectedListIds(): string[] {
    return (this.columns ?? []).map((c) => this.listId(c.status));
  }

  constructor(
    private taskService: TaskService,
    private notification: NzNotificationService,
    private translate: TranslateService,
    private store: StoreService,
    private projectService: ProjectService,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.route.queryParamMap.subscribe((params) => {
      const pid = params.get('projectId');
      // Ưu tiên projectId từ URL (vd: click từ notification)
      if (pid && pid !== this.projectId) {
        this.projectId = pid;
      }
      this.onLoad();
    });
    this.store.getCurrentUser().subscribe((u) => {
      this.currentUserId = u?.id ?? null;
      this.resolveProjectContext();
    });
  }

  checklistSummary(t: Task): { done: number; total: number } {
    const totalCount = t?.checklistTotalCount;
    const doneCount = t?.checklistDoneCount;
    if (typeof totalCount === 'number' && typeof doneCount === 'number') {
      return { done: doneCount, total: totalCount };
    }
    const items = (t as any)?.checklists as TaskChecklistItem[] | null | undefined;
    const list = Array.isArray(items) ? items : [];
    const total = list.length;
    const done = list.filter((x) => !!x?.checked).length;
    return { done, total };
  }

  ensureChecklistLoaded(t: Task): void {
    const id = t?.id;
    if (!id) return;
    this.activeChecklistTask = t;
    const state = this.checklistByTaskId[id];
    if (state && (state.loading || state.items.length)) {
      return;
    }
    this.checklistByTaskId[id] = { loading: true, items: [] };
    this.taskService.getChecklists(id).subscribe({
      next: ({ raw, items }) => {
        this.checklistByTaskId[id] = { loading: false, items: raw?.code === 200 ? items ?? [] : [] };
        // gắn vào task để hiện summary nếu muốn
        (t as any).checklists = this.checklistByTaskId[id].items;
        (t as any).checklistTotalCount = this.checklistByTaskId[id].items.length;
        (t as any).checklistDoneCount = this.checklistByTaskId[id].items.filter((x) => !!x.checked).length;
      },
      error: () => (this.checklistByTaskId[id] = { loading: false, items: [] }),
    });
  }

  toggleChecklist(t: Task, item: TaskChecklistItem, checked: boolean): void {
    const taskId = t?.id;
    const checklistId = item?.id as string | undefined;
    if (!taskId || !checklistId) return;
    this.taskService.toggleChecklist(taskId, checklistId, checked).subscribe({
      next: ({ raw, items }) => {
        if (raw?.code === 200) {
          this.checklistByTaskId[taskId] = { loading: false, items: items ?? [] };
          (t as any).checklists = items ?? [];
          (t as any).checklistTotalCount = (items ?? []).length;
          (t as any).checklistDoneCount = (items ?? []).filter((x) => !!x.checked).length;
          this.maybeAutoAdvanceAfterChecklistDone(t);
          return;
        }
        this.notification.warning(this.translate.instant('common.error'), raw?.message ?? '');
      },
    });
  }

  onLoad(): void {
    this.resolveProjectContext();
    this.loading = true;
    this.taskService.getKanbanBoard(this.projectId ?? '').subscribe({
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

  /**
   * Predicate cho CDK: chặn kéo vào cột đích không hợp lệ ngay từ lúc hover.
   * (Giữ logic chính ở canMoveTask để drop() cũng check lại.)
   */
  enterPredicate(toStatus: TaskStatus) {
    return (drag: CdkDrag<Task>, _drop: CdkDropList<Task[]>) => {
      const t = (drag?.data ?? null) as Task | null;
      return this.canMoveTask(t, toStatus);
    };
  }

  canDragTask(t: Task): boolean {
    // Cho phép kéo nếu user có thể reorder trong cột hiện tại hoặc có thể kéo sang trạng thái kế tiếp
    const current = (t?.status ?? null) as TaskStatus | null;
    if (!current) return false;
    if (this.canReorderTask(t)) return true;
    const next = this.nextStatus(current);
    return !!next && this.canAdvanceTask(t, next);
  }

  drop(evt: CdkDragDrop<any[]>, toStatus: TaskStatus): void {
    const task = (evt?.item?.data ?? null) as Task | null;
    if (!this.canMoveTask(task, toStatus)) return;

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

    // Nếu đang lọc theo 1 project: lưu lại thứ tự kanban theo board
    if (this.projectId) {
      this.scheduleSave();
      return;
    }

    // Không lọc project: chỉ update status theo từng task (không thể update kanban board theo 1 project)
    if (moved?.id && evt.previousContainer !== evt.container) {
      this.saving = true;
      this.taskService.updateStatus(moved.id, { status: toStatus }).subscribe({
        next: ({ raw, task: updated }) => {
          this.saving = false;
          if (raw?.code === 200) {
            moved.status = (updated?.status ?? toStatus) as TaskStatus;
            return;
          }
          this.notification.warning(this.translate.instant('common.error'), raw?.message ?? '');
          this.onLoad();
        },
        error: () => {
          this.saving = false;
          this.onLoad();
        },
      });
    }
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
    if (!this.projectId) return;
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
        if (raw?.code === 200) {
          return;
        }
        this.notification.warning(
          this.translate.instant('common.error'),
          raw?.message ?? this.translate.instant('kanban.messages.savePrerequisite')
        );
        this.onLoad();
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

  private maybeAutoAdvanceAfterChecklistDone(t: Task): void {
    if (!this.canManageByAssigneeOrManager(t)) return;
    const status = (t.status ?? null) as TaskStatus | null;
    if (!status || status === 'DONE') return;
    const total = Number((t as any)?.checklistTotalCount ?? 0);
    const done = Number((t as any)?.checklistDoneCount ?? 0);
    if (!total || done !== total) return;

    const next = this.nextStatus(status);
    if (!next) return;

    this.taskService.updateStatus(t.id, { status: next }).subscribe({
      next: ({ raw, task }) => {
        if (raw?.code !== 200) {
          this.notification.warning(this.translate.instant('common.error'), raw?.message ?? '');
          return;
        }
        // sync local status + move card to next column
        t.status = (task?.status ?? next) as TaskStatus;
        this.moveTaskToStatus(t, t.status as TaskStatus);
        this.scheduleSave();
      },
    });
  }

  private nextStatus(s: TaskStatus): TaskStatus | null {
    switch (s) {
      case 'TODO':
        return 'IN_PROGRESS';
      case 'IN_PROGRESS':
        return 'REVIEW';
      case 'REVIEW':
        return 'TESTING';
      case 'TESTING':
        return 'DONE';
      default:
        return null;
    }
  }

  private canMoveTask(t: Task | null, toStatus: TaskStatus): boolean {
    if (!t || !this.currentUserId) return false;

    // validate task thuộc project đang chọn (tránh kéo nhầm data)
    if (this.projectId && t.projectId && String(t.projectId) !== String(this.projectId)) return false;

    const from = (t.status ?? null) as TaskStatus | null;
    if (!from) return false;

    // reorder trong cùng cột: cho phép nếu user là assignee hoặc manager project
    if (from === toStatus) {
      return this.canReorderTask(t);
    }

    // chỉ cho kéo sang trạng thái kế tiếp (không lùi, không nhảy)
    const next = this.nextStatus(from);
    if (!next || next !== toStatus) return false;

    // kiểm tra điều kiện theo từng status
    return this.canAdvanceTask(t, toStatus);
  }

  private canReorderTask(t: Task): boolean {
    return this.canManageByAssigneeOrManager(t);
  }

  private canAdvanceTask(t: Task, toStatus: TaskStatus): boolean {
    const from = (t.status ?? null) as TaskStatus | null;
    if (!from) return false;

    // TODO -> IN_PROGRESS: chỉ assignee được kéo
    if (from === 'TODO') {
      return this.isAssignee(t);
    }

    // Các trạng thái khác: bắt buộc hoàn thành checklist và user là assignee hoặc manager của project
    if (!this.isChecklistCompleted(t)) return false;
    return this.canManageByAssigneeOrManager(t);
  }

  private isChecklistCompleted(t: Task): boolean {
    const total = Number(t?.checklistTotalCount ?? 0);
    const done = Number(t?.checklistDoneCount ?? 0);
    // không có checklist => coi như đạt điều kiện
    if (!total) return true;
    return done === total;
  }

  private isAssignee(t: Task): boolean {
    return !!t?.assignedId && !!this.currentUserId && String(t.assignedId) === String(this.currentUserId);
  }

  private isProjectManager(): boolean {
    return !!this.projectManagerId && !!this.currentUserId && String(this.projectManagerId) === String(this.currentUserId);
  }

  private isProjectManagerOfTask(t: Task): boolean {
    if (!this.currentUserId) return false;
    const mid = (t?.projectManagerId ?? null) as string | null;
    if (!mid) return false;
    return String(mid) === String(this.currentUserId);
  }

  private canManageByAssigneeOrManager(t: Task): boolean {
    // Nếu đang lọc theo project: dùng managerId của project đang chọn
    if (this.projectId) {
      return this.isAssignee(t) || this.isProjectManager();
    }
    // Không lọc: check theo manager của đúng project của task
    return this.isAssignee(t) || this.isProjectManagerOfTask(t);
  }

  private moveTaskToStatus(t: Task, to: TaskStatus): void {
    const id = t?.id;
    if (!id) return;
    const cols = this.columns ?? [];
    let fromCol: TaskKanbanColumn | undefined;
    let fromIdx = -1;

    for (const c of cols) {
      const idx = (c.tasks ?? []).findIndex((x) => x?.id === id);
      if (idx >= 0) {
        fromCol = c;
        fromIdx = idx;
        break;
      }
    }
    const toCol = cols.find((c) => c.status === to);
    if (!toCol) return;

    // remove from current column (if found)
    if (fromCol && fromIdx >= 0) {
      const nextTasks = [...(fromCol.tasks ?? [])];
      nextTasks.splice(fromIdx, 1);
      fromCol.tasks = nextTasks;
    }

    // add to top of target column (avoid duplicates)
    const target = (toCol.tasks ?? []).filter((x) => x?.id !== id);
    toCol.tasks = [t, ...target];
  }

  private resolveProjectContext(): void {
    if (!this.projectId) {
      this.projectManagerId = null;
      return;
    }
    const pid = this.projectId;
    this.projectService.getById(pid).subscribe({
      next: ({ raw, project }) => {
        this.projectManagerId = raw?.code === 200 ? ((project?.managerId ?? null) as any) : null;
      },
      error: () => (this.projectManagerId = null),
    });

    // NOTE: Tạm thời chỉ dùng managerId để check "người quản lý project".
    // Nếu cần mở rộng theo role member (PROJECT_MANAGER/TEAM_LEAD) thì có thể OR thêm ở isProjectManager().
  }
}

