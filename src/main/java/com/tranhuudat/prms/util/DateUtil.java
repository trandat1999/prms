package com.tranhuudat.prms.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;

public class DateUtil {
    private static final ZoneId ZONE_ID = ZoneId.systemDefault();
    public static Integer getYear(Date date) {
        if (date == null) {
            return null;
        }
        date = new Date(date.getTime());
        return date.toInstant()
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate()
                .getYear();
    }

    public static Integer getMonth(Date date) {
        if (date == null) {
            return null;
        }
        date = new Date(date.getTime());
        return date.toInstant()
                .atZone(java.time.ZoneId.systemDefault())
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
}
