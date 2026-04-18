package com.tranhuudat.prms.service.impl;

import com.tranhuudat.prms.dto.BaseResponse;
import com.tranhuudat.prms.dto.report.ReportAllocationStackedDto;
import com.tranhuudat.prms.dto.report.ReportCostRevenueRowDto;
import com.tranhuudat.prms.dto.report.ReportDataDto;
import com.tranhuudat.prms.dto.report.ReportFilterRequest;
import com.tranhuudat.prms.dto.report.ReportPersonnelPerformanceDto;
import com.tranhuudat.prms.dto.report.ReportProjectPerformanceDto;
import com.tranhuudat.prms.dto.report.ReportStackSeriesDto;
import com.tranhuudat.prms.dto.report.ReportTaskWeekTrendDto;
import com.tranhuudat.prms.dto.report.ReportUserOtMonthDto;
import com.tranhuudat.prms.enums.ReportPeriodTypeEnum;
import com.tranhuudat.prms.repository.AppParamRepository;
import com.tranhuudat.prms.service.BaseService;
import com.tranhuudat.prms.service.ReportService;
import com.tranhuudat.prms.util.DateUtil;
import com.tranhuudat.prms.util.SystemMessage;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.hibernate.query.NativeQuery;
import org.hibernate.type.StandardBasicTypes;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl extends BaseService implements ReportService {

    public static final String REPORT_PARAM_GROUP = "REPORT";
    /**
     * Hệ số MM cho 1 FTE tháng đầy đủ (nhân với tổng hệ số (ngày làm thực/tổng ngày T2–T6 × %/100)).
     */
    public static final String PARAM_LABOR_MM_PER_FTE_MONTH = "REPORT_LABOR_MM_PER_FULL_FTE_MONTH";

    public static final BigDecimal DEFAULT_LABOR_MM_PRICING = BigDecimal.ONE;

    private static final DateTimeFormatter YM_FMT = DateTimeFormatter.ofPattern("yyyy-MM");

    private final AppParamRepository appParamRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @SuppressWarnings("unchecked")
    private <T> NativeQuery<T> nativeQuery(String sql) {
        return (NativeQuery<T>) entityManager.createNativeQuery(sql).unwrap(NativeQuery.class);
    }

    private record AllocationRow(
            UUID userId,
            String userLabel,
            String ym,
            Date startDate,
            Date endDate,
            BigDecimal allocationPercent
    ) {
    }

    private record UserMonthOtRow(String userLabel, String month, BigDecimal otHours) {
    }

    private record ProjectMetaRow(UUID id, String code, String name) {
    }

    private record WeekDoneRow(Timestamp weekStart, long count) {
    }

    private record UuidLongRow(UUID id, long value) {
    }

    private record UuidDecimalRow(UUID id, BigDecimal value) {
    }

    private record UuidStringRow(UUID id, String value) {
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponse loadReport(ReportFilterRequest request) {
        var errors = validation(request);
        if (!CollectionUtils.isEmpty(errors)) {
            return getResponse400(getMessage(SystemMessage.BAD_REQUEST), errors);
        }
        int year = request.getYear();
        BigDecimal laborPricing = resolveLaborMmPricing();
        List<AllocationRow> allocationRows = loadAllocationRowsForYear(year);
        LocalDateTime yearStart = LocalDate.of(year, 1, 1).atStartOfDay();
        LocalDateTime yearEndExclusive = LocalDate.of(year, 12, 31).plusDays(1).atStartOfDay();

        ReportDataDto data = ReportDataDto.builder()
                .periodType(request.getPeriodType())
                .year(year)
                .valueUnit("MM")
                .laborMmPerFullFteMonth(laborPricing)
                .costVsRevenue(buildCostVsRevenue(request.getPeriodType(), year, laborPricing, allocationRows))
                .allocationStacked(buildAllocationStacked(year, laborPricing, allocationRows))
                .personnelPerformance(buildPersonnelPerformance(yearStart, yearEndExclusive, laborPricing, allocationRows))
                .otByUserMonth(buildOtByUserMonth(yearStart, yearEndExclusive))
                .projectPerformance(buildProjectPerformance(yearStart, yearEndExclusive))
                .taskCompletionTrend(buildTaskWeekTrend(year))
                .build();
        return getResponse200(data, getMessage(SystemMessage.SUCCESS));
    }

    private BigDecimal resolveLaborMmPricing() {
        Optional<String> raw = appParamRepository.findParamValueByGroupAndName(
                REPORT_PARAM_GROUP, PARAM_LABOR_MM_PER_FTE_MONTH);
        if (raw.isEmpty() || raw.get().isBlank()) {
            return DEFAULT_LABOR_MM_PRICING;
        }
        try {
            return new BigDecimal(raw.get().trim());
        } catch (NumberFormatException ex) {
            return DEFAULT_LABOR_MM_PRICING;
        }
    }

    /**
     * Mỗi dòng: 0 user_id, 1 ulabel, 2 ym (yyyy-MM), 3 start_date, 4 end_date, 5 allocation_percent
     */
    private List<AllocationRow> loadAllocationRowsForYear(int year) {
        String sql =
                "select ra.user_id, "
                        + "coalesce(nullif(trim(u.full_name),''), u.username) as ulabel, "
                        + "to_char(date_trunc('month', ra.month), 'YYYY-MM') as ym, "
                        + "ra.start_date, ra.end_date, ra.allocation_percent "
                        + "from resource_allocation ra "
                        + "join tbl_user u on u.id = ra.user_id "
                        + "where (ra.voided is null or ra.voided = false) "
                        + "and (u.voided is null or u.voided = false) "
                        + "and ra.month is not null "
                        + "and extract(year from ra.month) = :y";
        return nativeQuery(sql)
                .addScalar("user_id", StandardBasicTypes.UUID_CHAR)
                .addScalar("ulabel", StandardBasicTypes.STRING)
                .addScalar("ym", StandardBasicTypes.STRING)
                .addScalar("start_date", StandardBasicTypes.DATE)
                .addScalar("end_date", StandardBasicTypes.DATE)
                .addScalar("allocation_percent", StandardBasicTypes.BIG_DECIMAL)
                .setParameter("y", year)
                .setTupleTransformer((tuple, aliases) -> new AllocationRow(
                        tuple[0] != null ? UUID.fromString(tuple[0].toString()) : null,
                        Objects.toString(tuple[1], ""),
                        Objects.toString(tuple[2], ""),
                        toUtilDate(tuple[3]),
                        toUtilDate(tuple[4]),
                        toBigDecimal(tuple[5])
                ))
                .getResultList();
    }

    private static Date toUtilDate(Object o) {
        if (o == null) {
            return null;
        }
        if (o instanceof Date d) {
            return d;
        }
        return null;
    }

    /**
     * Hệ số (chưa nhân giá FTE): (ngày làm T2–T6 trong giao hiệu / tổng ngày T2–T6 của tháng) × (% phân bổ / 100).
     */
    private static BigDecimal laborRawFactorForMonth(
            LocalDate monthStart, Date rowStart, Date rowEnd, BigDecimal allocationPercent) {
        LocalDate monthEnd = monthStart.with(TemporalAdjusters.lastDayOfMonth());
        int totalBiz = DateUtil.countWeekdaysMonFriInclusive(monthStart, monthEnd);
        if (totalBiz <= 0) {
            return BigDecimal.ZERO;
        }
        LocalDate s = rowStart != null ? DateUtil.toLocalDate(rowStart) : monthStart;
        LocalDate e = rowEnd != null ? DateUtil.toLocalDate(rowEnd) : monthEnd;
        LocalDate effStart = monthStart.isAfter(s) ? monthStart : s;
        LocalDate effEnd = monthEnd.isBefore(e) ? monthEnd : e;
        int workDays = DateUtil.countWeekdaysMonFriInclusive(effStart, effEnd);
        if (workDays <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal ratio = BigDecimal.valueOf(workDays)
                .divide(BigDecimal.valueOf(totalBiz), 12, RoundingMode.HALF_UP);
        BigDecimal pct = allocationPercent != null ? allocationPercent : BigDecimal.ZERO;
        return ratio.multiply(pct).divide(BigDecimal.valueOf(100), 12, RoundingMode.HALF_UP);
    }

    private static BigDecimal sumLaborRawForYm(List<AllocationRow> rows, String ym) {
        YearMonth ymParsed = YearMonth.parse(ym, YM_FMT);
        LocalDate monthStart = ymParsed.atDay(1);
        BigDecimal sum = BigDecimal.ZERO;
        for (AllocationRow r : rows) {
            if (!ym.equals(Objects.toString(r.ym(), ""))) {
                continue;
            }
            sum = sum.add(laborRawFactorForMonth(monthStart, r.startDate(), r.endDate(), r.allocationPercent()));
        }
        return sum;
    }

    private List<ReportCostRevenueRowDto> buildCostVsRevenue(
            ReportPeriodTypeEnum type, int year, BigDecimal laborPricing, List<AllocationRow> allocationRows) {
        return switch (type) {
            case MONTH -> buildCostVsRevenueMonths(year, laborPricing, allocationRows);
            case QUARTER -> buildCostVsRevenueQuarters(year, laborPricing, allocationRows);
            case YEAR -> List.of(buildCostVsRevenueYearRow(year, laborPricing, allocationRows));
        };
    }

    private List<ReportCostRevenueRowDto> buildCostVsRevenueMonths(
            int year, BigDecimal laborPricing, List<AllocationRow> allocationRows) {
        List<ReportCostRevenueRowDto> rows = new ArrayList<>();
        for (int m = 1; m <= 12; m++) {
            LocalDate monthStart = LocalDate.of(year, m, 1);
            Timestamp ts = Timestamp.valueOf(monthStart.atStartOfDay());
            String ym = monthStart.format(YM_FMT);
            BigDecimal revenue = sumNative(
                    "select coalesce(sum(p.project_value),0) from tbl_project p "
                            + "where (p.voided is null or p.voided = false) and p.project_value is not null "
                            + "and p.created_date is not null "
                            + "and date_trunc('month', p.created_date) = date_trunc('month', cast(:t as timestamp))",
                    ts);
            BigDecimal laborRaw = sumLaborRawForYm(allocationRows, ym);
            BigDecimal cost = scale2(laborRaw.multiply(laborPricing));
            rows.add(ReportCostRevenueRowDto.builder()
                    .periodLabel(ym)
                    .pycRevenue(scale2(revenue))
                    .resourceCost(cost)
                    .profit(scale2(revenue.subtract(cost)))
                    .build());
        }
        return rows;
    }

    private List<ReportCostRevenueRowDto> buildCostVsRevenueQuarters(
            int year, BigDecimal laborPricing, List<AllocationRow> allocationRows) {
        List<ReportCostRevenueRowDto> rows = new ArrayList<>();
        for (int q = 1; q <= 4; q++) {
            int m0 = (q - 1) * 3 + 1;
            BigDecimal revenue = BigDecimal.ZERO;
            BigDecimal laborRaw = BigDecimal.ZERO;
            for (int k = 0; k < 3; k++) {
                LocalDate monthStart = LocalDate.of(year, m0 + k, 1);
                Timestamp ts = Timestamp.valueOf(monthStart.atStartOfDay());
                String ym = monthStart.format(YM_FMT);
                revenue = revenue.add(sumNative(
                        "select coalesce(sum(p.project_value),0) from tbl_project p "
                                + "where (p.voided is null or p.voided = false) and p.project_value is not null "
                                + "and p.created_date is not null "
                                + "and date_trunc('month', p.created_date) = date_trunc('month', cast(:t as timestamp))",
                        ts));
                laborRaw = laborRaw.add(sumLaborRawForYm(allocationRows, ym));
            }
            BigDecimal cost = scale2(laborRaw.multiply(laborPricing));
            rows.add(ReportCostRevenueRowDto.builder()
                    .periodLabel(year + "-Q" + q)
                    .pycRevenue(scale2(revenue))
                    .resourceCost(cost)
                    .profit(scale2(revenue.subtract(cost)))
                    .build());
        }
        return rows;
    }

    private ReportCostRevenueRowDto buildCostVsRevenueYearRow(
            int year, BigDecimal laborPricing, List<AllocationRow> allocationRows) {
        Timestamp start = Timestamp.valueOf(LocalDate.of(year, 1, 1).atStartOfDay());
        Timestamp end = Timestamp.valueOf(LocalDate.of(year, 12, 31).plusDays(1).atStartOfDay());
        BigDecimal revenue = sumNativeRange(
                "select coalesce(sum(p.project_value),0) from tbl_project p "
                        + "where (p.voided is null or p.voided = false) and p.project_value is not null "
                        + "and p.created_date is not null and p.created_date >= :s and p.created_date < :e",
                start, end);
        BigDecimal laborRaw = BigDecimal.ZERO;
        for (int m = 1; m <= 12; m++) {
            laborRaw = laborRaw.add(sumLaborRawForYm(allocationRows, LocalDate.of(year, m, 1).format(YM_FMT)));
        }
        BigDecimal cost = scale2(laborRaw.multiply(laborPricing));
        return ReportCostRevenueRowDto.builder()
                .periodLabel(String.valueOf(year))
                .pycRevenue(scale2(revenue))
                .resourceCost(cost)
                .profit(scale2(revenue.subtract(cost)))
                .build();
    }

    private ReportAllocationStackedDto buildAllocationStacked(
            int year, BigDecimal laborPricing, List<AllocationRow> allocationRows) {
        Set<String> monthOrder = new LinkedHashSet<>();
        Map<String, BigDecimal> totalsByUser = new HashMap<>();
        Map<String, Map<String, BigDecimal>> userMonthMm = new LinkedHashMap<>();
        for (AllocationRow r : allocationRows) {
            String user = Objects.toString(r.userLabel(), "");
            String ym = Objects.toString(r.ym(), "");
            if (ym.isBlank()) {
                continue;
            }
            monthOrder.add(ym);
            LocalDate monthStart = YearMonth.parse(ym, YM_FMT).atDay(1);
            BigDecimal cellRaw = laborRawFactorForMonth(monthStart, r.startDate(), r.endDate(), r.allocationPercent());
            BigDecimal cellMm = cellRaw.multiply(laborPricing);
            userMonthMm.computeIfAbsent(user, k -> new LinkedHashMap<>()).merge(ym, cellMm, BigDecimal::add);
            totalsByUser.merge(user, cellMm, BigDecimal::add);
        }
        List<String> months = new ArrayList<>(monthOrder.stream().sorted().toList());
        if (months.isEmpty()) {
            for (int mm = 1; mm <= 12; mm++) {
                months.add(LocalDate.of(year, mm, 1).format(YM_FMT));
            }
        }
        List<String> topUsers = totalsByUser.entrySet().stream()
                .sorted(Comparator.comparing((Map.Entry<String, BigDecimal> e) -> e.getValue()).reversed())
                .map(Map.Entry::getKey)
                .limit(15)
                .toList();
        List<ReportStackSeriesDto> series = new ArrayList<>();
        for (String u : topUsers) {
            Map<String, BigDecimal> byM = userMonthMm.getOrDefault(u, Map.of());
            List<BigDecimal> vals = new ArrayList<>();
            for (String ym : months) {
                vals.add(scale2(byM.getOrDefault(ym, BigDecimal.ZERO)));
            }
            series.add(ReportStackSeriesDto.builder().userLabel(u).values(vals).build());
        }
        return ReportAllocationStackedDto.builder()
                .periodLabels(new ArrayList<>(months))
                .series(series)
                .build();
    }

    private List<ReportPersonnelPerformanceDto> buildPersonnelPerformance(
            LocalDateTime start, LocalDateTime end, BigDecimal laborPricing, List<AllocationRow> allocationRows) {
        Timestamp s = Timestamp.valueOf(start);
        Timestamp e = Timestamp.valueOf(end);
        Map<UUID, String> userLabels = loadUserLabels();
        Map<UUID, Long> done = countMap(
                "select t.assigned_id as id, count(*)::bigint as val from tbl_task t "
                        + "where (t.voided is null or t.voided = false) and t.status = 'DONE' "
                        + "and t.assigned_id is not null "
                        + "and t.last_modified_date >= :s and t.last_modified_date < :e "
                        + "group by t.assigned_id",
                s, e);
        Map<UUID, BigDecimal> ot = sumMapUser(
                "select e.user_id as id, coalesce(sum(e.ot_hours),0) as val from employee_ot e "
                        + "where (e.voided is null or e.voided = false) and e.status = 'APPROVED' "
                        + "and e.ot_date >= :s and e.ot_date < :e group by e.user_id",
                s, e);
        Map<UUID, BigDecimal> laborMmByUser = new HashMap<>();
        for (AllocationRow r : allocationRows) {
            UUID uid = r.userId();
            String ym = Objects.toString(r.ym(), "");
            LocalDate monthStart = YearMonth.parse(ym, YM_FMT).atDay(1);
            if (monthStart.isBefore(start.toLocalDate()) || !monthStart.isBefore(end.toLocalDate())) {
                continue;
            }
            BigDecimal raw = laborRawFactorForMonth(monthStart, r.startDate(), r.endDate(), r.allocationPercent());
            laborMmByUser.merge(uid, raw.multiply(laborPricing), BigDecimal::add);
        }
        Set<UUID> ids = new LinkedHashSet<>();
        ids.addAll(done.keySet());
        ids.addAll(ot.keySet());
        ids.addAll(laborMmByUser.keySet());
        List<ReportPersonnelPerformanceDto> list = new ArrayList<>();
        for (UUID id : ids) {
            list.add(ReportPersonnelPerformanceDto.builder()
                    .userLabel(userLabels.getOrDefault(id, id.toString()))
                    .tasksDone(done.getOrDefault(id, 0L))
                    .otHours(scale2(ot.getOrDefault(id, BigDecimal.ZERO)))
                    .laborMm(scale2(laborMmByUser.getOrDefault(id, BigDecimal.ZERO)))
                    .build());
        }
        list.sort(Comparator.comparingLong(ReportPersonnelPerformanceDto::getTasksDone).reversed());
        return list;
    }

    private List<ReportUserOtMonthDto> buildOtByUserMonth(LocalDateTime start, LocalDateTime end) {
        Timestamp s = Timestamp.valueOf(start);
        Timestamp e = Timestamp.valueOf(end);
        String sql =
                "select coalesce(nullif(trim(u.full_name),''), u.username) as ulabel, "
                        + "to_char(date_trunc('month', e.ot_date), 'YYYY-MM') as ym, "
                        + "coalesce(sum(e.ot_hours),0) as hrs "
                        + "from employee_ot e "
                        + "join tbl_user u on u.id = e.user_id "
                        + "where (e.voided is null or e.voided = false) "
                        + "and (u.voided is null or u.voided = false) "
                        + "and e.status = 'APPROVED' "
                        + "and e.ot_date >= :s and e.ot_date < :e "
                        + "group by u.id, ulabel, date_trunc('month', e.ot_date) "
                        + "order by ym, ulabel";
        List<UserMonthOtRow> rows = nativeQuery(sql)
                .addScalar("ulabel", StandardBasicTypes.STRING)
                .addScalar("ym", StandardBasicTypes.STRING)
                .addScalar("hrs", StandardBasicTypes.BIG_DECIMAL)
                .setParameter("s", s)
                .setParameter("e", e)
                .setTupleTransformer((tuple, aliases) -> new UserMonthOtRow(
                        Objects.toString(tuple[0], ""),
                        Objects.toString(tuple[1], ""),
                        toBigDecimal(tuple[2])
                ))
                .getResultList();
        List<ReportUserOtMonthDto> list = new ArrayList<>();
        for (UserMonthOtRow row : rows) {
            list.add(ReportUserOtMonthDto.builder()
                    .userLabel(row.userLabel())
                    .month(row.month())
                    .otHours(scale2(toBigDecimal(row.otHours())))
                    .build());
        }
        return list;
    }

    private List<ReportProjectPerformanceDto> buildProjectPerformance(LocalDateTime start, LocalDateTime end) {
        Timestamp s = Timestamp.valueOf(start);
        Timestamp e = Timestamp.valueOf(end);
        Map<UUID, String[]> projMeta = new HashMap<>();
        List<ProjectMetaRow> metaRows = nativeQuery(
                        "select p.id as id, p.code as code, p.name as name from tbl_project p "
                                + "where p.voided is null or p.voided = false")
                .addScalar("id", StandardBasicTypes.UUID_CHAR)
                .addScalar("code", StandardBasicTypes.STRING)
                .addScalar("name", StandardBasicTypes.STRING)
                .setTupleTransformer((tuple, aliases) -> new ProjectMetaRow(
                        tuple[0] != null ? UUID.fromString(tuple[0].toString()) : null,
                        Objects.toString(tuple[1], ""),
                        Objects.toString(tuple[2], "")
                ))
                .getResultList();
        for (ProjectMetaRow r : metaRows) {
            if (r.id() == null) {
                continue;
            }
            projMeta.put(r.id(), new String[]{r.code(), r.name()});
        }
        Map<UUID, Long> doneBy = countMap(
                "select t.project_id as id, count(*)::bigint as val from tbl_task t "
                        + "where (t.voided is null or t.voided = false) and t.status = 'DONE' "
                        + "and t.project_id is not null "
                        + "and t.last_modified_date >= :s and t.last_modified_date < :e "
                        + "group by t.project_id",
                s, e);
        Map<UUID, BigDecimal> otBy = sumMapUser(
                "select e.project_id as id, coalesce(sum(e.ot_hours),0) as val from employee_ot e "
                        + "where (e.voided is null or e.voided = false) and e.project_id is not null "
                        + "and e.status = 'APPROVED' "
                        + "and e.ot_date >= :s and e.ot_date < :e group by e.project_id",
                s, e);
        Map<UUID, BigDecimal> hoursBy = sumMapUuidBigDecimal(
                "select t.project_id as id, coalesce(sum(t.estimated_hours),0) as val from tbl_task t "
                        + "where (t.voided is null or t.voided = false) and t.project_id is not null "
                        + "group by t.project_id");
        BigDecimal totalHours = hoursBy.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        Set<UUID> pids = new LinkedHashSet<>();
        pids.addAll(doneBy.keySet());
        pids.addAll(otBy.keySet());
        pids.addAll(hoursBy.keySet());
        List<ReportProjectPerformanceDto> list = new ArrayList<>();
        for (UUID pid : pids) {
            String[] meta = projMeta.get(pid);
            if (meta == null) {
                continue;
            }
            BigDecimal h = hoursBy.getOrDefault(pid, BigDecimal.ZERO);
            BigDecimal share = BigDecimal.ZERO;
            if (totalHours.compareTo(BigDecimal.ZERO) > 0) {
                share = h.multiply(BigDecimal.valueOf(100)).divide(totalHours, 1, RoundingMode.HALF_UP);
            }
            list.add(ReportProjectPerformanceDto.builder()
                    .projectCode(meta[0])
                    .projectName(meta[1])
                    .tasksDone(doneBy.getOrDefault(pid, 0L))
                    .resourceSharePercent(scale2(share))
                    .otHours(scale2(otBy.getOrDefault(pid, BigDecimal.ZERO)))
                    .build());
        }
        list.sort(Comparator.comparingLong(ReportProjectPerformanceDto::getTasksDone).reversed());
        return list.stream().limit(30).collect(Collectors.toList());
    }

    private List<ReportTaskWeekTrendDto> buildTaskWeekTrend(int year) {
        LocalDate end = LocalDate.of(year, 12, 31);
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        if (year == today.getYear()) {
            end = today;
        }
        LocalDate start = end.minusWeeks(11).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        Timestamp s = Timestamp.valueOf(start.atStartOfDay());
        Timestamp e = Timestamp.valueOf(end.plusDays(1).atStartOfDay());
        String sql =
                "select date_trunc('week', tl.created_date) as wk, count(distinct tl.task_id)::bigint as cnt "
                        + "from tbl_task_log tl "
                        + "where (tl.voided is null or tl.voided = false) "
                        + "and tl.action = 'STATUS_CHANGE' and tl.new_value = 'DONE' "
                        + "and tl.created_date >= :s and tl.created_date < :e "
                        + "group by date_trunc('week', tl.created_date) "
                        + "order by date_trunc('week', tl.created_date)";
        List<WeekDoneRow> rows = nativeQuery(sql)
                .addScalar("wk", StandardBasicTypes.TIMESTAMP)
                .addScalar("cnt", StandardBasicTypes.LONG)
                .setParameter("s", s)
                .setParameter("e", e)
                .setTupleTransformer((tuple, aliases) -> new WeekDoneRow(
                        (Timestamp) tuple[0],
                        toLong(tuple[1])
                ))
                .getResultList();
        Map<String, Long> byWeek = new LinkedHashMap<>();
        for (WeekDoneRow row : rows) {
            if (row.weekStart() == null) {
                continue;
            }
            LocalDate wk = row.weekStart().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            int wy = wk.get(WeekFields.ISO.weekBasedYear());
            int ww = wk.get(WeekFields.ISO.weekOfWeekBasedYear());
            String label = wy + "-W" + String.format("%02d", ww);
            byWeek.put(label, row.count());
        }
        List<ReportTaskWeekTrendDto> out = new ArrayList<>();
        for (Map.Entry<String, Long> en : byWeek.entrySet()) {
            out.add(ReportTaskWeekTrendDto.builder()
                    .weekLabel(en.getKey())
                    .tasksDone(en.getValue())
                    .build());
        }
        return out;
    }

    private Map<UUID, String> loadUserLabels() {
        List<UuidStringRow> rows = nativeQuery(
                        "select u.id as id, coalesce(nullif(trim(u.full_name),''), u.username) as lbl "
                                + "from tbl_user u where u.voided is null or u.voided = false")
                .addScalar("id", StandardBasicTypes.UUID_CHAR)
                .addScalar("lbl", StandardBasicTypes.STRING)
                .setTupleTransformer((tuple, aliases) -> new UuidStringRow(
                        tuple[0] != null ? UUID.fromString(tuple[0].toString()) : null,
                        Objects.toString(tuple[1], "")
                ))
                .getResultList();
        Map<UUID, String> map = new HashMap<>();
        for (UuidStringRow r : rows) {
            if (r.id() == null) {
                continue;
            }
            map.put(r.id(), Objects.toString(r.value(), ""));
        }
        return map;
    }

    private Map<UUID, Long> countMap(String sql, Timestamp s, Timestamp e) {
        NativeQuery<UuidLongRow> q = nativeQuery(sql)
                .addScalar("id", StandardBasicTypes.UUID_CHAR)
                .addScalar("val", StandardBasicTypes.LONG)
                .setTupleTransformer((tuple, aliases) -> new UuidLongRow(
                        tuple[0] != null ? UUID.fromString(tuple[0].toString()) : null,
                        toLong(tuple[1])
                ));
        if (s != null) {
            q.setParameter("s", s);
        }
        if (e != null) {
            q.setParameter("e", e);
        }
        List<UuidLongRow> rows = q.getResultList();
        Map<UUID, Long> map = new HashMap<>();
        for (UuidLongRow row : rows) {
            if (row.id() == null) {
                continue;
            }
            map.put(row.id(), row.value());
        }
        return map;
    }

    private Map<UUID, BigDecimal> sumMapUser(String sql, Timestamp s, Timestamp e) {
        List<UuidDecimalRow> rows = nativeQuery(sql)
                .addScalar("id", StandardBasicTypes.UUID_CHAR)
                .addScalar("val", StandardBasicTypes.BIG_DECIMAL)
                .setParameter("s", s)
                .setParameter("e", e)
                .setTupleTransformer((tuple, aliases) -> new UuidDecimalRow(
                        tuple[0] != null ? UUID.fromString(tuple[0].toString()) : null,
                        toBigDecimal(tuple[1])
                ))
                .getResultList();
        Map<UUID, BigDecimal> map = new HashMap<>();
        for (UuidDecimalRow row : rows) {
            if (row.id() == null) {
                continue;
            }
            map.put(row.id(), toBigDecimal(row.value()));
        }
        return map;
    }

    private Map<UUID, BigDecimal> sumMapUuidBigDecimal(String sql) {
        List<UuidDecimalRow> rows = nativeQuery(sql)
                .addScalar("id", StandardBasicTypes.UUID_CHAR)
                .addScalar("val", StandardBasicTypes.BIG_DECIMAL)
                .setTupleTransformer((tuple, aliases) -> new UuidDecimalRow(
                        tuple[0] != null ? UUID.fromString(tuple[0].toString()) : null,
                        toBigDecimal(tuple[1])
                ))
                .getResultList();
        Map<UUID, BigDecimal> map = new HashMap<>();
        for (UuidDecimalRow row : rows) {
            if (row.id() == null) {
                continue;
            }
            map.put(row.id(), toBigDecimal(row.value()));
        }
        return map;
    }

    private BigDecimal sumNative(String sql, Timestamp t) {
        Query q = entityManager.createNativeQuery(sql);
        q.setParameter("t", t);
        return toBigDecimal(q.getSingleResult());
    }

    private BigDecimal sumNativeRange(String sql, Timestamp s, Timestamp e) {
        Query q = entityManager.createNativeQuery(sql);
        q.setParameter("s", s);
        q.setParameter("e", e);
        return toBigDecimal(q.getSingleResult());
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
