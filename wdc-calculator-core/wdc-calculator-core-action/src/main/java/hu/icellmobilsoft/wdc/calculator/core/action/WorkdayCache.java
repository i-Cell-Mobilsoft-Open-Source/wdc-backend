package hu.icellmobilsoft.wdc.calculator.core.action;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Year;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;

/**
 * Workday Calendar caching class
 *
 * @author janos.hamrak
 */
@ApplicationScoped
public class WorkdayCache {

    private Map<Year, TreeMap<LocalDate, Boolean>> cache = new HashMap<>();

    /**
     * Returns the cache of workdays
     *
     * @return Map<Year, TreeMap<LocalDate, Boolean>> Map with initialized years as keys and TreeMaps as values. Every TreeMap has LocalDates as keys
     *         (every date in the given year) and a boolean value referring to whether the given date is workday or not.
     */
    public Map<Year, TreeMap<LocalDate, Boolean>> getCache() {
        return cache;
    }

    /**
     * Puts the given date and workday boolean in the cache. If the given date is not present in the cache, first initializes its year.
     *
     * @param date
     *            LocalDate to put
     * @param isWorkday
     *            boolean value, referring to whether the given date is a workday
     */
    public void put(LocalDate date, boolean isWorkday) {
        if (date == null) {
            return;
        }
        Year year = Year.of(date.getYear());
        if (!this.getCache().containsKey(year)) {
            this.initYear(year);
        }
        this.getCache().get(year).put(date, isWorkday);
    }

    /**
     * Initializing cache of given year with weekdays and weekends.
     * 
     * @param year
     *            year to init
     */
    public void initYear(Year year) {
        if (year == null)
            return;
        LocalDate yearWithNowDate = LocalDate.now().withYear(year.getValue());
        final LocalDate start = yearWithNowDate.with(TemporalAdjusters.firstDayOfYear());
        final LocalDate end = yearWithNowDate.with(TemporalAdjusters.lastDayOfYear());
        final long days = start.until(end, ChronoUnit.DAYS);
        cache.put(year, Stream.iterate(start, d -> d.plusDays(1)).limit(days + 1)
                .collect(Collectors.toMap(d -> d, WorkdayCache::isWeekday, (o1, o2) -> o2, TreeMap::new)));
    }

    private static boolean isWeekday(LocalDate localDate) {
        if (localDate == null)
            return false;
        DayOfWeek dayOfWeek = localDate.getDayOfWeek();
        return !(dayOfWeek.equals(DayOfWeek.SATURDAY) || dayOfWeek.equals(DayOfWeek.SUNDAY));
    }
}
