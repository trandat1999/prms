import {CommonModule} from '@angular/common';
import {Component, DestroyRef, inject, OnInit} from '@angular/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {ActivatedRoute, Router} from '@angular/router';
import {TranslatePipe} from '@ngx-translate/core';
import {NzTabsModule} from 'ng-zorro-antd/tabs';
import {Project} from '../models/project.model';
import {ProjectMembers} from '../project-members/project-members';
import {ProjectTasks} from '../project-tasks/project-tasks';
import {ProjectService} from '../services/project.service';

@Component({
  selector: 'app-project-detail',
  imports: [
    CommonModule,
    NzTabsModule,
    ProjectTasks,
    ProjectMembers,
    TranslatePipe,
  ],
  templateUrl: './project-detail.html',
  styleUrl: './project-detail.scss',
})
export class ProjectDetail implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);

  projectId = '';
  project: Project | null = null;
  loading = false;
  selectedIndex = 0;

  constructor(private projectService: ProjectService) {}

  ngOnInit(): void {
    this.route.queryParamMap.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((params) => {
      this.selectedIndex = params.get('tab') === 'members' ? 1 : 0;
    });

    this.route.paramMap.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((pm) => {
      const id = pm.get('projectId') ?? '';
      if (!id || id === this.projectId) {
        return;
      }
      this.projectId = id;
      const state =
        typeof history !== 'undefined' ? (history.state as { projectName?: string } | null) : null;
      const name = state?.projectName?.trim();
      this.project = name ? { id, name } : null;
      this.loadProject(id);
    });
  }

  get projectDisplayName(): string | null {
    return this.project?.name?.trim() || this.project?.code?.trim() || null;
  }

  onTabChange(index: number): void {
    this.selectedIndex = index;
    void this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { tab: index === 1 ? 'members' : null },
      queryParamsHandling: 'merge',
      replaceUrl: true,
    });
  }

  backToList(): void {
    void this.router.navigate(['/project']);
  }

  private loadProject(id: string): void {
    this.loading = true;
    this.projectService.getById(id).subscribe({
      next: ({ raw, project }) => {
        this.loading = false;
        if (raw?.code === 200 && project) {
          this.project = project;
        }
      },
      error: () => (this.loading = false),
    });
  }
}
