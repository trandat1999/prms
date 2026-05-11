package com.tranhuudat.prms.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;
import java.util.Date;

public class DateUtil {
    private static final ZoneId ZONE_ID = ZoneId.systemDefault();
    public static Integer getYear(Date date) {
        if (date == null) {
            return null;
        }
        date = new Date(date.getTime());
        return date.toInstant()
                .atZone(ZONE_ID)
                .toLocalDate()
                .getYear();
    }

    public static Integer getMonth(Date date) {
        if (date == null) {
            return null;
        }
        date = new Date(date.getTime());
        return date.toInstant()
                .atZone(ZONE_ID)
                .toLocalDate()
                .getMonthValue();
    }

    public static LocalDate toLocalDate(Date date) {
        if (date == null) {
            return null;
        }
        date = new Date(date.getTime());
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    /**
     * Đếm ngày làm việc từ thứ Hai đến thứ Sáu (cả hai đầu mút), bỏ thứ 7 và Chủ nhật.
     */
    public static int countWeekdaysMonFriInclusive(LocalDate start, LocalDate end) {
        if (start == null || end == null || end.isBefore(start)) {
            return 0;
        }
        int n = 0;
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            DayOfWeek w = d.getDayOfWeek();
            if (w != DayOfWeek.SATURDAY && w != DayOfWeek.SUNDAY) {
                n++;
            }
        }
        return n;
    }

    public static Date getStartOfDay(Date date) {
        if (date == null) {
            return null;
        }
        date = new Date(date.getTime());
        LocalDateTime localDateTime = date.toInstant()
                .atZone(ZONE_ID)
                .toLocalDate()
                .atStartOfDay();

        return Date.from(localDateTime.atZone(ZONE_ID).toInstant());
    }

    public static Date getEndOfDay(Date date) {
        if (date == null) {
            return null;
        }
        date = new Date(date.getTime());
        LocalDateTime localDateTime = date.toInstant()
                .atZone(ZONE_ID)
                .toLocalDate()
                .atTime(23, 59, 59);

        return Date.from(localDateTime.atZone(ZONE_ID).toInstant());
    }

    public static Integer getQuarter(Date date) {
        if (date == null) {
            return null;
        }
        date = new Date(date.getTime());
        int month = date.toInstant()
                .atZone(ZONE_ID)
                .toLocalDate()
                .getMonthValue();
        return (month - 1) / 3 + 1;
    }

    /**
     * So sánh hai {@link Date} theo phần ngày lịch ({@link LocalDate}) tại {@link #ZONE_ID}.
     *
     * @return số âm nếu {@code d1} trước {@code d2}, 0 nếu cùng ngày, số dương nếu sau; {@code null} nếu một trong hai tham số {@code null}
     */
    public static Integer compareDates(Date d1, Date d2) {
        if (d1 == null || d2 == null) {
            return null;
        }
        return toLocalDate(d1).compareTo(toLocalDate(d2));
    }

    /**
     * Kiểm tra {@code date} (theo ngày lịch tại {@link #ZONE_ID}) có nằm trong đoạn {@code [rangeStart, rangeEnd]} (cả hai đầu mút) hay không.
     *
     * @return {@code false} nếu tham số {@code null} hoặc {@code rangeStart} sau {@code rangeEnd}
     */
    public static boolean isDateInRange(Date date, Date rangeStart, Date rangeEnd) {
        if (date == null || rangeStart == null || rangeEnd == null) {
            return false;
        }
        LocalDate d = toLocalDate(date);
        LocalDate start = toLocalDate(rangeStart);
        LocalDate end = toLocalDate(rangeEnd);
        if (start.isAfter(end)) {
            return false;
        }
        return !d.isBefore(start) && !d.isAfter(end);
    }

    /**
     * Cộng/trừ số ngày vào {@code date}, giữ nguyên giờ-phút-giây theo {@link #ZONE_ID}.
     */
    public static Date addDays(Date date, long days) {
        if (date == null) {
            return null;
        }
        date = new Date(date.getTime());
        ZonedDateTime zdt = date.toInstant().atZone(ZONE_ID);
        return Date.from(zdt.plusDays(days).toInstant());
    }

    /**
     * Số ngày lịch chính xác giữa hai mốc: lấy {@link LocalDate} tại {@link #ZONE_ID} rồi dùng {@link ChronoUnit#DAYS}.
     * Ví dụ cùng ngày lịch thì {@code 0}; từ 01/01 23:00 đến 02/01 01:00 vẫn là {@code 1} ngày lịch.
     *
     * @return {@code null} nếu một trong hai tham số {@code null}
     */
    public static Long daysBetween(Date start, Date end) {
        if (start == null || end == null) {
            return null;
        }
        return ChronoUnit.DAYS.between(toLocalDate(start), toLocalDate(end));
    }

    /**
     * Format {@code date} theo pattern {@link DateTimeFormatter#ofPattern(String)} tại {@link #ZONE_ID}.
     *
     * @return {@code null} nếu {@code date} hoặc {@code pattern} {@code null}
     */
    public static String format(Date date, String pattern) {
        if (date == null || pattern == null) {
            return null;
        }
        date = new Date(date.getTime());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return date.toInstant().atZone(ZONE_ID).format(formatter);
    }

    /**
     * Parse chuỗi theo {@code pattern}: nếu pattern chỉ có phần ngày thì trả {@link Date} tại đầu ngày theo {@link #ZONE_ID};
     * nếu có cả giờ thì map đúng instant tương ứng.
     *
     * @return {@code null} nếu {@code text} rỗng/chỉ khoảng trắng hoặc {@code pattern} {@code null}
     */
    public static Date parse(String text, String pattern) {
        if (pattern == null || text == null || text.isBlank()) {
            return null;
        }
        String trimmed = text.trim();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        TemporalAccessor parsed = formatter.parse(trimmed);
        if (parsed.isSupported(ChronoField.HOUR_OF_DAY)) {
            LocalDateTime ldt = LocalDateTime.from(parsed);
            return Date.from(ldt.atZone(ZONE_ID).toInstant());
        }
        LocalDate ld = LocalDate.from(parsed);
        return Date.from(ld.atStartOfDay(ZONE_ID).toInstant());
    }
}
