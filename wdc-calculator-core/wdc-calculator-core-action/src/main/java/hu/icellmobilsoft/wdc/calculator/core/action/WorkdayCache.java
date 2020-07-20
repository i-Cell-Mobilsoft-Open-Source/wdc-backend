/*-
 * #%L
 * WDC
 * %%
 * Copyright (C) 2020 i-Cell Mobilsoft Zrt.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package hu.icellmobilsoft.wdc.calculator.core.action;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Year;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;

import hu.icellmobilsoft.wdc.calculator.core.action.type.HolidayType;

/**
 * Workday Calendar caching class
 *
 * @author janos.hamrak
 */
@ApplicationScoped
public class WorkdayCache {

    private Map<Year, TreeMap<LocalDate, WorkdayCacheData>> cache = new HashMap<>();

    private Set<Year> guaranteedYears = new HashSet<>();

    private final Object lockObject = new Object();

    /**
     * Returns the cache of workdays
     *
     * @return Map<Year, TreeMap<LocalDate, WorkdayCacheData>> Map with initialized years as keys and TreeMaps as values. Every TreeMap has LocalDates as keys
     *         (every date in the given year) and a WorkdayCacheData as value.
     */
    public Map<Year, TreeMap<LocalDate, WorkdayCacheData>> getCache() {
        synchronized (lockObject) {
            return Collections.unmodifiableMap(cache);
        }
    }

    /**
     * Returns the lock object for concurrent modifications
     *
     * @return lockObject
     */
    public Object getLockObject() {
        return lockObject;
    }

    /**
     * Clears cache
     */
    public void clearCache() {
        this.cache.clear();
    }

    /**
     * Inserts given date, with given descriptor information into the cache.
     * If the given date is not present in the cache, first initializes its year.
     *
     * @param date
     *          date to insert
     * @param isWorkday
     *          boolean value, referring to whether the given date is a workday
     * @param holidayType
     *          type of non-workday date
     * @param substitutedDay
     *          workday, non-workday substitution descriptor
     * @param description
     *          description of date
     */
    public void put(LocalDate date, boolean isWorkday, HolidayType holidayType, LocalDate substitutedDay, String description) {
        if (date == null) {
            return;
        }
        Year year = Year.of(date.getYear());
        if (!this.getCache().containsKey(year)) {
            this.initYear(year);
        }

        WorkdayCacheData workdayCacheData = new WorkdayCacheData();
        workdayCacheData.setDate(date);
        workdayCacheData.setWorkday(isWorkday);
        workdayCacheData.setSubstitutedDay(substitutedDay);
        workdayCacheData.setDescription(description);
        workdayCacheData.setHolidayType(holidayType);

        this.getCache().get(year).put(date, workdayCacheData);
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
                .collect(Collectors.toMap(d -> d, WorkdayCache::initDay, (o1, o2) -> o2, TreeMap::new)));
    }

    /**
     * Returns whether the given year is guaranteed.
     *
     * @param year
     *            year whose presence in the guaranteed set is to be tested
     *
     * @return boolean
     */
    public boolean isGuaranteedYear(Year year) {
        return guaranteedYears.contains(year);
    }

    /**
     * Adds given year into the set of guaranteed years.
     *
     * @param year
     *            year to be added to the guaranteed set
     */
    public void addToGuaranteedYears(Year year) {
        if (year != null) {
            guaranteedYears.add(year);
        }
    }

    private static boolean isWeekday(LocalDate localDate) {
        if (localDate == null)
            return false;
        DayOfWeek dayOfWeek = localDate.getDayOfWeek();
        return !(dayOfWeek.equals(DayOfWeek.SATURDAY) || dayOfWeek.equals(DayOfWeek.SUNDAY));
    }

    private static WorkdayCacheData initDay(LocalDate date) {
        if (date == null) {
            return null;
        }

        WorkdayCacheData workdayCacheData = new WorkdayCacheData();
        workdayCacheData.setDate(date);
        workdayCacheData.setWorkday(isWeekday(date));

        return workdayCacheData;
    }
}
