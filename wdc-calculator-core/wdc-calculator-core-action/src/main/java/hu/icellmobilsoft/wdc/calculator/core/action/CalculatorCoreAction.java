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
     *            The from date.\n Valid dates in: current year, last year, next year
     * @param numberOfWorkDays
     *            The number of workdays.\n Can't be 0
     * @return The calculated workday
     * @throws BusinessException
     *             Throws on invalid input parameters
     */
    public LocalDate calculateWorkday(LocalDate startDate, int numberOfWorkdays) throws BusinessException {
        validateCalculateWorkdayParams(startDate, numberOfWorkdays);

        NavigableMap<LocalDate, WorkdayCacheData> years = calculateYears(startDate, numberOfWorkDays);

        Predicate<Map.Entry<LocalDate, WorkdayCacheData>> isWorkDay = x -> x.getValue().isWorkday();
        Predicate<Map.Entry<LocalDate, WorkdayCacheData>> isInDateRange = x -> numberOfWorkDays > 0 ? x.getKey().isAfter(startDate)
                : x.getKey().isBefore(startDate);

        List<Map.Entry<LocalDate, WorkdayCacheData>> workdaysRelatedToStartDate = years.entrySet().stream().filter(isWorkDay.and(isInDateRange))
                .collect(Collectors.toList());

        return createWorkDayResult(numberOfWorkDays, workdaysRelatedToStartDate);
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
