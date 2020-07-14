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

import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.enterprise.inject.Model;
import javax.inject.Inject;

import hu.icellmobilsoft.wdc.calculator.core.action.exception.BusinessException;
import hu.icellmobilsoft.wdc.calculator.core.action.exception.ReasonCode;

/**
 * Calculator core action class
 *
 * @author zsolt.vasi
 */
@Model
public class CalculatorCoreAction {

    private static final int MIN_YEAR = 1;
    private static final int MAX_YEAR = 2100;

    private static final String NUMBER_OF_WORKING_DAYS_OUT_OF_RANGE = "The number of working days is out of date range!";

    private static final Predicate<Map.Entry<LocalDate, WorkdayCacheData>> isWorkday = x -> x.getValue().isWorkday();

    @Inject
    private WorkdayCache workdayCache;

    /**
     * Calculate workday (with startDate and numberOfWorkdays)
     *
     * @param startDate
     *            From date.\n Valid dates between years 1 and 2100.
     * @param numberOfWorkdays
     *            Number of workdays.\n Cannot be 0, may be negative.
     * @return The calculated workday
     * @throws BusinessException
     *             Throws on invalid input parameters
     */
    public LocalDate calculateWorkday(LocalDate startDate, int numberOfWorkdays) throws BusinessException {
        validateCalculateWorkdayParams(startDate, numberOfWorkdays);
        return calculateWorkdayRecursively(startDate, numberOfWorkdays);
    }

    /**
     * Returns whether the result of the given calculateWorkday method is guaranteed.
     *
     * @param startDate
     *            From date.\n Valid dates between years 1 and 2100.
     * @param numberOfWorkdays
     *            Number of workdays.\n Cannot be 0, may be negative.
     * @return boolean
     * @throws BusinessException
     *             Throws on invalid input parameters
     */
    public boolean isGuaranteedResultOfCalculateWorkday(LocalDate startDate, int numberOfWorkdays) throws BusinessException {
        validateCalculateWorkdayParams(startDate, numberOfWorkdays);
        return isGuaranteedResultOfCalculateWorkdayRecursive(startDate, numberOfWorkdays);
    }

    /**
     * Calculate workdayList
     *
     * @param startDate
     *            from date (inclusive)
     * @param endDate
     *            to date (inclusive)
     * @return workdays between startDate and endDate
     * @throws BusinessException
     *             Throws on invalid input parameters
     */
    public List<LocalDate> calculateWorkdayList(LocalDate startDate, LocalDate endDate) throws BusinessException {
        validateCalculateWorkdayListParams(startDate, endDate);

        NavigableMap<LocalDate, WorkdayCacheData> years = calculateYears(startDate, 1);

        Predicate<Map.Entry<LocalDate, WorkdayCacheData>> isInDateRange = x -> x.getKey().isAfter(startDate.minusDays(1))
                && x.getKey().isBefore(endDate.plusDays(1));

        return years.entrySet().stream().filter(isWorkday.and(isInDateRange)).map(Map.Entry::getKey).collect(Collectors.toList());
    }

    private LocalDate calculateWorkdayRecursively(LocalDate startDate, int numberOfWorkdays) throws BusinessException {
        LocalDate result;

        int year = startDate.getYear();
        if (year > MAX_YEAR || year < MIN_YEAR) {
            throw new BusinessException(ReasonCode.INVALID_INPUT, NUMBER_OF_WORKING_DAYS_OUT_OF_RANGE);
        }
        Predicate<Map.Entry<LocalDate, WorkdayCacheData>> isInDateRange = getDateRangePredicate(startDate, numberOfWorkdays);

        if (!workdayCache.getCache().containsKey(Year.of(year))) {
            workdayCache.initYear(Year.of(year));
        }

        List<LocalDate> workdaysInYear = getRelevantWorkdaysInYear(year, isInDateRange);
        int numberOfWorkdaysInYear = workdaysInYear.size();

        if (numberOfWorkdaysInYear >= Math.abs(numberOfWorkdays)) {
            result = getWorkdayResult(workdaysInYear, numberOfWorkdaysInYear, numberOfWorkdays);
        } else {
            LocalDate nextStartDate = numberOfWorkdays > 0 ? LocalDate.of(year + 1, 1, 1) : LocalDate.of(year - 1, 12, 31);
            int remainingNumberOfWorkdays = numberOfWorkdays > 0 ? numberOfWorkdays - numberOfWorkdaysInYear
                    : numberOfWorkdays + numberOfWorkdaysInYear;
            result = calculateWorkdayRecursively(nextStartDate, remainingNumberOfWorkdays);
        }

        return result;
    }

    private boolean isGuaranteedResultOfCalculateWorkdayRecursive(LocalDate startDate, int numberOfWorkdays) throws BusinessException {
        boolean guaranteed;

        int year = startDate.getYear();
        if (year > MAX_YEAR || year < MIN_YEAR) {
            throw new BusinessException(ReasonCode.INVALID_INPUT, NUMBER_OF_WORKING_DAYS_OUT_OF_RANGE);
        }
        Predicate<Map.Entry<LocalDate, WorkdayCacheData>> isInDateRange = getDateRangePredicate(startDate, numberOfWorkdays);

        if (!workdayCache.isGuaranteedYear(Year.of(year))) {
            guaranteed = false;
        } else {
            List<LocalDate> workdaysInYear = getRelevantWorkdaysInYear(year, isInDateRange);
            int numberOfWorkdaysInYear = workdaysInYear.size();

            if (numberOfWorkdaysInYear >= Math.abs(numberOfWorkdays)) {
                guaranteed = true;
            } else {
                LocalDate nextStartDate = numberOfWorkdays > 0 ? LocalDate.of(year + 1, 1, 1) : LocalDate.of(year - 1, 12, 31);
                int remainingNumberOfWorkdays = numberOfWorkdays > 0 ? numberOfWorkdays - numberOfWorkdaysInYear
                        : numberOfWorkdays + numberOfWorkdaysInYear;
                guaranteed = isGuaranteedResultOfCalculateWorkdayRecursive(nextStartDate, remainingNumberOfWorkdays);
            }
        }

        return guaranteed;
    }

    private Predicate<Map.Entry<LocalDate, WorkdayCacheData>> getDateRangePredicate(LocalDate startDate, int numberOfWorkdays) {
        Predicate<Map.Entry<LocalDate, WorkdayCacheData>> predicate;
        if (numberOfWorkdays > 0) {
            predicate = x -> x.getKey().isAfter(startDate.minusDays(1));
        } else {
            predicate = x -> x.getKey().isBefore(startDate.plusDays(1));
        }
        return predicate;
    }

    private List<LocalDate> getRelevantWorkdaysInYear(int year, Predicate<Map.Entry<LocalDate, WorkdayCacheData>> isInDateRange) {
        return workdayCache.getCache().get(Year.of(year)).entrySet().stream().filter(isWorkday.and(isInDateRange)).map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private LocalDate getWorkdayResult(List<LocalDate> workdaysInYear, int workdayCountInYear, int numberOfWorkdays) {
        LocalDate result;
        if (numberOfWorkdays > 0) {
            result = workdaysInYear.get(numberOfWorkdays);
        } else {
            result = workdaysInYear.get((workdayCountInYear + numberOfWorkdays) - 1);
        }
        return result;
    }

    private NavigableMap<LocalDate, WorkdayCacheData> calculateYears(LocalDate startDate, int numberOfWorkDays) {
        NavigableMap<LocalDate, WorkdayCacheData> result;
        if (numberOfWorkDays > 0) {
            result = filterYears(entry -> entry.getKey().getValue() >= startDate.getYear());
        } else {
            result = filterYears(entry -> entry.getKey().getValue() <= startDate.getYear()).descendingMap();
        }
        return result;
    }

    private NavigableMap<LocalDate, WorkdayCacheData> filterYears(Predicate<Map.Entry<Year, TreeMap<LocalDate, WorkdayCacheData>>> isInDateRange) {
        NavigableMap<LocalDate, WorkdayCacheData> years = new TreeMap<>();
        workdayCache.getCache().entrySet().stream().filter(isInDateRange).forEach(entry -> years.putAll(entry.getValue()));
        return years;
    }

    private void validateCalculateWorkdayParams(LocalDate startDate, int numberOfWorkdays) throws BusinessException {
        if (numberOfWorkdays == 0) {
            throw new BusinessException(ReasonCode.INVALID_INPUT, "Number Of Workdays cannot be 0!");
        }
        if (startDate == null) {
            throw new BusinessException(ReasonCode.INVALID_INPUT, "StartDate is missing!");
        }
        if (startDate.getYear() < MIN_YEAR || startDate.getYear() > MAX_YEAR) {
            throw new BusinessException(ReasonCode.INVALID_INPUT, "Start year must be between " + MIN_YEAR + " and " + MAX_YEAR + "!");
        }
    }

    private void validateCalculateWorkdayListParams(LocalDate startDate, LocalDate endDate) throws BusinessException {
        if (startDate == null || endDate == null) {
            throw new BusinessException(ReasonCode.INVALID_INPUT, "StartDate and/or endDate is missing!");
        }
        if (startDate.isAfter(endDate)) {
            throw new BusinessException(ReasonCode.INVALID_INPUT, "EndDate can't be earlier then startDate!");
        }
        List<Year> yearsBetween = Stream.iterate(Year.of(startDate.getYear()), y -> y.plusYears(1))
                .limit(endDate.getYear() - startDate.getYear() + 1L).collect(Collectors.toList());
        if (!workdayCache.getCache().keySet().containsAll(yearsBetween)) {
            throw new BusinessException(ReasonCode.INVALID_INPUT, "Data not found for one or more years between startDate and endDate!");

        }
    }
}
