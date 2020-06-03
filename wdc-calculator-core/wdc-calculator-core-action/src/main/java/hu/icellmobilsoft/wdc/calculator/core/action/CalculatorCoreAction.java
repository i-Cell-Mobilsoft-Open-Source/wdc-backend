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

    @Inject
    private WorkdayCache workdayCache;

    /**
     * Calculate workday (with startDate and numberOfWorkDays)
     *
     * @param startDate
     *            The from date.\n Valid dates in: current year, last year, next year
     * @param numberOfWorkDays
     *            The number of workdays.\n Can't be 0
     * @return The calculated workday
     * @throws BusinessException
     *             Throws on invalid input parameters
     */
    public LocalDate calculateWorkday(LocalDate startDate, int numberOfWorkDays) throws BusinessException {
        validateCalculateWorkdayParams(startDate, numberOfWorkDays);

        NavigableMap<LocalDate, Boolean> years = calculateYears(startDate, numberOfWorkDays);

        Predicate<Map.Entry<LocalDate, Boolean>> isWorkDay = Map.Entry::getValue;
        Predicate<Map.Entry<LocalDate, Boolean>> isInDateRange = x -> numberOfWorkDays > 0 ? x.getKey().isAfter(startDate)
                : x.getKey().isBefore(startDate);

        List<Map.Entry<LocalDate, Boolean>> workdaysRelatedToStartDate = years.entrySet().stream().filter(isWorkDay.and(isInDateRange))
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

        NavigableMap<LocalDate, Boolean> years = calculateYears(startDate, 1);

        Predicate<Map.Entry<LocalDate, Boolean>> isWorkDay = Map.Entry::getValue;
        Predicate<Map.Entry<LocalDate, Boolean>> isInDateRange = x -> x.getKey().isAfter(startDate.minusDays(1))
                && x.getKey().isBefore(endDate.plusDays(1));

        return years.entrySet().stream().filter(isWorkDay.and(isInDateRange)).map(Map.Entry::getKey).collect(Collectors.toList());
    }

    private NavigableMap<LocalDate, Boolean> calculateYears(LocalDate startDate, int numberOfWorkDays) {
        NavigableMap<LocalDate, Boolean> result;
        if (numberOfWorkDays > 0) {
            result = filterYears(entry -> entry.getKey().getValue() >= startDate.getYear());
        } else {
            result = filterYears(entry -> entry.getKey().getValue() <= startDate.getYear()).descendingMap();
        }
        return result;
    }

    private NavigableMap<LocalDate, Boolean> filterYears(Predicate<Map.Entry<Year, TreeMap<LocalDate, Boolean>>> isInDateRange) {
        NavigableMap<LocalDate, Boolean> years = new TreeMap<>();
        workdayCache.getCache().entrySet().stream()
                .filter(isInDateRange)
                .forEach(entry -> years.putAll(entry.getValue()));
        return years;
    }

    private LocalDate createWorkDayResult(int numberOfWorkDays, List<Map.Entry<LocalDate, Boolean>> workdaysRelatedToStartDate) throws BusinessException {
        if (workdaysRelatedToStartDate.size() < numberOfWorkDays - 1) {
            throw new BusinessException(ReasonCode.INVALID_INPUT, "The number of working days is out of date range!");
        }

        return workdaysRelatedToStartDate.get(Math.abs(numberOfWorkDays) - 1).getKey();
    }

    private void validateCalculateWorkdayParams(LocalDate startDate, int numberOfWorkDays) throws BusinessException {
        if (numberOfWorkDays == 0) {
            throw new BusinessException(ReasonCode.INVALID_INPUT, "Number Of Workdays can't be 0!");
        }
        if (!workdayCache.getCache().containsKey(Year.of(startDate.getYear()))) {
            throw new BusinessException(ReasonCode.INVALID_INPUT, "Year not found: " + startDate.getYear());
        }
    }

    private void validateCalculateWorkdayListParams(LocalDate startDate, LocalDate endDate) throws BusinessException {
        if (startDate == null || endDate == null) {
            throw new BusinessException(ReasonCode.INVALID_INPUT, "StartDate and/or endDate is missing!");
        }
        if (startDate.isAfter(endDate)) {
            throw new BusinessException(ReasonCode.INVALID_INPUT, "EndDate can't be earlier then startDate!");
        }
        List<Year> yearsBetween = Stream.iterate(Year.of(startDate.getYear()), y -> y.plusYears(1)).limit(endDate.getYear() - startDate.getYear() + 1L)
                .collect(Collectors.toList());
        if (!workdayCache.getCache().keySet().containsAll(yearsBetween)) {
            throw new BusinessException(ReasonCode.INVALID_INPUT, "Data not found for one or more years between startDate and endDate!");

        }
    }
}
