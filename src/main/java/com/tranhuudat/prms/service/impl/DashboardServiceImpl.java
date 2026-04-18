package com.tranhuudat.prms.service.impl;

import com.tranhuudat.prms.dto.BaseResponse;
import com.tranhuudat.prms.dto.dashboard.DashboardLabeledHoursDto;
import com.tranhuudat.prms.dto.dashboard.DashboardLabeledPercentDto;
import com.tranhuudat.prms.dto.dashboard.DashboardMonthUtilizationDto;
import com.tranhuudat.prms.dto.dashboard.DashboardOverviewDto;
import com.tranhuudat.prms.dto.dashboard.DashboardPycProgressDto;
import com.tranhuudat.prms.dto.dashboard.DashboardSummaryDto;
import com.tranhuudat.prms.dto.dashboard.DashboardTaskStatusCountDto;
import com.tranhuudat.prms.service.BaseService;
import com.tranhuudat.prms.service.DashboardService;
import com.tranhuudat.prms.util.SystemMessage;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.query.NativeQuery;
import org.hibernate.type.StandardBasicTypes;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class DashboardServiceImpl extends BaseService implements DashboardService {

    @PersistenceContext
    private EntityManager entityManager;

    @SuppressWarnings("unchecked")
    private <T> NativeQuery<T> nativeQuery(String sql) {
        return (NativeQuery<T>) entityManager.createNativeQuery(sql).unwrap(NativeQuery.class);
    }

    private record MonthPctRow(String ym, BigDecimal pct) {
    }

    private record StatusCountRow(String status, long count) {
    }

    private record LabelLongRow(String label, long value) {
    }

    private record LabelDecimalRow(String label, BigDecimal value) {
    }

    private record ProjectProgressRow(String code, String name, BigDecimal progress) {
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponse getOverview() {
        DashboardOverviewDto overview = DashboardOverviewDto.builder()
                .summary(buildSummary())
                .resourceUtilizationByMonth(buildResourceUtilizationByMonth())
                .taskStatusDistribution(buildTaskStatusDistribution())
                .topPycByResource(buildTopPycByResource())
                .otHoursByProject(buildOtByProjectMonth())
                .employeeWorkload(buildEmployeeWorkload())
                .pycProgress(buildPycProgress())
                .build();
        return getResponse200(overview, getMessage(SystemMessage.SUCCESS));
    }

    private DashboardSummaryDto buildSummary() {
        long totalPyc = countSingle(
                "select count(*) from tbl_project p where p.voided is null or p.voided = false");
        long activePyc = countSingle(
                "select count(*) from tbl_project p where (p.voided is null or p.voided = false) "
                        + "and p.status = 'IN_PROGRESS'");
        long totalTask = countSingle(
                "select count(*) from tbl_task t where t.voided is null or t.voided = false");
        long taskDone = countSingle(
                "select count(*) from tbl_task t where (t.voided is null or t.voided = false) "
                        + "and t.status = 'DONE'");
        BigDecimal otHours = sumSingle(
                "select coalesce(sum(e.ot_hours), 0) from employee_ot e "
                        + "where (e.voided is null or e.voided = false) "
                        + "and e.status = 'APPROVED' "
                        + "and e.ot_date >= date_trunc('month', current_timestamp) "
                        + "and e.ot_date < date_trunc('month', current_timestamp) + interval '1 month'");
        BigDecimal resourcePct = sumSingle(
                "select coalesce(avg(ra.allocation_percent), 0) from resource_allocation ra "
                        + "where (ra.voided is null or ra.voided = false) "
                        + "and ra.month is not null "
                        + "and date_trunc('month', ra.month) = date_trunc('month', current_timestamp)");
        return DashboardSummaryDto.builder()
                .totalPyc(totalPyc)
                .activePyc(activePyc)
                .totalTask(totalTask)
                .taskDone(taskDone)
                .otHoursMonth(scale2(otHours))
                .resourceUsagePercent(scale2(resourcePct))
                .build();
    }

    private List<DashboardMonthUtilizationDto> buildResourceUtilizationByMonth() {
        String sql =
                "select to_char(date_trunc('month', ra.month), 'YYYY-MM') as ym, "
                        + "round(avg(ra.allocation_percent)::numeric, 1) as pct "
                        + "from resource_allocation ra "
                        + "where (ra.voided is null or ra.voided = false) and ra.month is not null "
                        + "and date_trunc('month', ra.month) >= date_trunc('month', current_timestamp) "
                        + "- interval '11 months' "
                        + "group by date_trunc('month', ra.month) "
                        + "order by date_trunc('month', ra.month)";
        List<MonthPctRow> rows = nativeQuery(sql)
                .addScalar("ym", StandardBasicTypes.STRING)
                .addScalar("pct", StandardBasicTypes.BIG_DECIMAL)
                .setTupleTransformer((tuple, aliases) -> new MonthPctRow(
                        Objects.toString(tuple[0], null),
                        toBigDecimal(tuple[1])
                ))
                .getResultList();
        Map<String, BigDecimal> byMonth = new HashMap<>();
        for (MonthPctRow row : rows) {
            if (row.ym() == null) {
                continue;
            }
            byMonth.put(row.ym(), toBigDecimal(row.pct()));
        }
        List<DashboardMonthUtilizationDto> list = new ArrayList<>();
        LocalDate start = LocalDate.now().withDayOfMonth(1).minusMonths(11);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM");
        for (int i = 0; i < 12; i++) {
            LocalDate d = start.plusMonths(i);
            String key = d.format(fmt);
            list.add(DashboardMonthUtilizationDto.builder()
                    .month(key)
                    .utilizationPercent(byMonth.getOrDefault(key, BigDecimal.ZERO))
                    .build());
        }
        return list;
    }

    private List<DashboardTaskStatusCountDto> buildTaskStatusDistribution() {
        String sql =
                "select case when t.status in ('REVIEW','TESTING') then 'REVIEW' else t.status end as st, "
                        + "count(*)::bigint as cnt "
                        + "from tbl_task t "
                        + "where t.voided is null or t.voided = false "
                        + "group by case when t.status in ('REVIEW','TESTING') then 'REVIEW' else t.status end";
        List<StatusCountRow> rows = nativeQuery(sql)
                .addScalar("st", StandardBasicTypes.STRING)
                .addScalar("cnt", StandardBasicTypes.LONG)
                .setTupleTransformer((tuple, aliases) -> new StatusCountRow(
                        Objects.toString(tuple[0], null),
                        toLong(tuple[1])
                ))
                .getResultList();
        Map<String, Long> map = new HashMap<>();
        for (StatusCountRow row : rows) {
            if (row.status() == null) {
                continue;
            }
            map.put(row.status(), row.count());
        }
        List<DashboardTaskStatusCountDto> out = new ArrayList<>();
        for (String st : List.of("TODO", "IN_PROGRESS", "REVIEW", "DONE")) {
            out.add(DashboardTaskStatusCountDto.builder()
                    .status(st)
                    .count(map.getOrDefault(st, 0L))
                    .build());
        }
        return out;
    }

    private List<DashboardLabeledPercentDto> buildTopPycByResource() {
        String sql =
                "select p.code as code, count(distinct t.assigned_id)::bigint as hc "
                        + "from tbl_task t "
                        + "join tbl_project p on p.id = t.project_id "
                        + "where (t.voided is null or t.voided = false) "
                        + "and (p.voided is null or p.voided = false) "
                        + "and t.assigned_id is not null "
                        + "and t.status <> 'DONE' "
                        + "group by p.id, p.code "
                        + "having count(distinct t.assigned_id) > 0";
        List<LabelLongRow> rows = nativeQuery(sql)
                .addScalar("code", StandardBasicTypes.STRING)
                .addScalar("hc", StandardBasicTypes.LONG)
                .setTupleTransformer((tuple, aliases) -> new LabelLongRow(
                        Objects.toString(tuple[0], ""),
                        toLong(tuple[1])
                ))
                .getResultList();
        if (rows.size() < 2) {
            return List.of();
        }
        long totalHc = rows.stream().mapToLong(LabelLongRow::value).sum();
        if (totalHc <= 0) {
            return List.of();
        }
        List<LabelLongRow> sorted = new ArrayList<>(rows);
        sorted.sort(Comparator.comparingLong(LabelLongRow::value).reversed());
        List<DashboardLabeledPercentDto> top = new ArrayList<>();
        int limit = Math.min(5, sorted.size());
        for (int i = 0; i < limit; i++) {
            LabelLongRow r = sorted.get(i);
            long hc = r.value();
            BigDecimal pct = BigDecimal.valueOf(hc)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(totalHc), 1, RoundingMode.HALF_UP);
            top.add(DashboardLabeledPercentDto.builder()
                    .label(r.label())
                    .percent(pct)
                    .build());
        }
        return top;
    }

    private List<DashboardLabeledHoursDto> buildOtByProjectMonth() {
        String sql =
                "select p.code as code, coalesce(sum(e.ot_hours), 0) as hrs "
                        + "from employee_ot e "
                        + "join tbl_project p on p.id = e.project_id "
                        + "where (e.voided is null or e.voided = false) "
                        + "and (p.voided is null or p.voided = false) "
                        + "and e.status = 'APPROVED' "
                        + "and e.ot_date >= date_trunc('month', current_timestamp) "
                        + "and e.ot_date < date_trunc('month', current_timestamp) + interval '1 month' "
                        + "group by p.id, p.code "
                        + "order by hrs desc nulls last "
                        + "limit 10";
        List<LabelDecimalRow> rows = nativeQuery(sql)
                .addScalar("code", StandardBasicTypes.STRING)
                .addScalar("hrs", StandardBasicTypes.BIG_DECIMAL)
                .setTupleTransformer((tuple, aliases) -> new LabelDecimalRow(
                        Objects.toString(tuple[0], ""),
                        toBigDecimal(tuple[1])
                ))
                .getResultList();
        List<DashboardLabeledHoursDto> list = new ArrayList<>();
        for (LabelDecimalRow row : rows) {
            list.add(DashboardLabeledHoursDto.builder()
                    .label(row.label())
                    .hours(scale2(toBigDecimal(row.value())))
                    .build());
        }
        return list;
    }

    private List<DashboardLabeledPercentDto> buildEmployeeWorkload() {
        String sql =
                "select coalesce(nullif(trim(u.full_name), ''), u.username) as lbl, "
                        + "sum(ra.allocation_percent) as pct "
                        + "from resource_allocation ra "
                        + "join tbl_user u on u.id = ra.user_id "
                        + "where (ra.voided is null or ra.voided = false) "
                        + "and (u.voided is null or u.voided = false) "
                        + "and ra.month is not null "
                        + "and date_trunc('month', ra.month) = date_trunc('month', current_timestamp) "
                        + "group by u.id, u.full_name, u.username "
                        + "having sum(ra.allocation_percent) > 0 "
                        + "order by pct desc nulls last "
                        + "limit 20";
        List<LabelDecimalRow> rows = nativeQuery(sql)
                .addScalar("lbl", StandardBasicTypes.STRING)
                .addScalar("pct", StandardBasicTypes.BIG_DECIMAL)
                .setTupleTransformer((tuple, aliases) -> new LabelDecimalRow(
                        Objects.toString(tuple[0], ""),
                        toBigDecimal(tuple[1])
                ))
                .getResultList();
        List<DashboardLabeledPercentDto> list = new ArrayList<>();
        for (LabelDecimalRow row : rows) {
            list.add(DashboardLabeledPercentDto.builder()
                    .label(row.label())
                    .percent(scale2(toBigDecimal(row.value())))
                    .build());
        }
        return list;
    }

    private List<DashboardPycProgressDto> buildPycProgress() {
        String sql =
                "select p.code as code, p.name as name, coalesce(p.progress_percentage, 0) as prog "
                        + "from tbl_project p "
                        + "where p.voided is null or p.voided = false "
                        + "order by p.code "
                        + "limit 12";
        List<ProjectProgressRow> rows = nativeQuery(sql)
                .addScalar("code", StandardBasicTypes.STRING)
                .addScalar("name", StandardBasicTypes.STRING)
                .addScalar("prog", StandardBasicTypes.BIG_DECIMAL)
                .setTupleTransformer((tuple, aliases) -> new ProjectProgressRow(
                        Objects.toString(tuple[0], ""),
                        Objects.toString(tuple[1], ""),
                        toBigDecimal(tuple[2])
                ))
                .getResultList();
        List<DashboardPycProgressDto> list = new ArrayList<>();
        for (ProjectProgressRow row : rows) {
            list.add(DashboardPycProgressDto.builder()
                    .code(row.code())
                    .name(row.name())
                    .progressPercent(scale2(toBigDecimal(row.progress())))
                    .build());
        }
        return list;
    }

    private long countSingle(String sql) {
        Object single = entityManager.createNativeQuery(sql).getSingleResult();
        return toLong(single);
    }

    private BigDecimal sumSingle(String sql) {
        Object single = entityManager.createNativeQuery(sql).getSingleResult();
        return toBigDecimal(single);
    }

    private static BigDecimal scale2(BigDecimal v) {
        return v.setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal toBigDecimal(Object o) {
        if (o == null) {
            return BigDecimal.ZERO;
        }
        if (o instanceof BigDecimal bd) {
            return bd;
        }
        if (o instanceof Number n) {
            return BigDecimal.valueOf(n.doubleValue());
        }
        return new BigDecimal(o.toString());
    }

    private static long toLong(Object o) {
        if (o == null) {
            return 0L;
        }
        if (o instanceof Number n) {
            return n.longValue();
        }
        return Long.parseLong(o.toString());
    }
}
