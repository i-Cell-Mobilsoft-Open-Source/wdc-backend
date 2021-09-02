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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Year;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import hu.icellmobilsoft.coffee.dto.exception.BusinessException;

/**
 * Testing CalculatorCoreAction class
 *
 * @author janos.hamrak
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Testing CalculatorCoreAction class")
class CalculatorCoreActionTest {

    private final Map<Year, TreeMap<LocalDate, WorkdayCacheData>> cache = new HashMap<>();

    @Mock
    private WorkdayCache workdayCache;

    @InjectMocks
    private CalculatorCoreAction underTest;

    void init2020() {
        cache.put(Year.of(2020), Stream.iterate(LocalDate.of(2020, 1, 1), d -> d.plusDays(1)).limit(366).collect(Collectors.toMap(d -> d, d -> {
            WorkdayCacheData data = new WorkdayCacheData();
            data.setDate(d);
            data.setWorkday(!d.getDayOfWeek().equals(DayOfWeek.SATURDAY) && !d.getDayOfWeek().equals(DayOfWeek.SUNDAY));
            return data;
        }, (o1, o2) -> o2, TreeMap::new)));
        when(workdayCache.getCache()).thenReturn(cache);
    }

    void setIsGuaranteed(Year year, boolean guaranteed) {
        when(workdayCache.isGuaranteedYear(year)).thenReturn(guaranteed);
    }

    void enableInit(int year) {
        doAnswer(invocationOnMock -> {
            cache.put(Year.of(year), Stream.iterate(LocalDate.of(year, 1, 1), d -> d.plusDays(1)).limit(365).collect(Collectors.toMap(d -> d, d -> {
                WorkdayCacheData data = new WorkdayCacheData();
                data.setDate(d);
                data.setWorkday(!d.getDayOfWeek().equals(DayOfWeek.SATURDAY) && !d.getDayOfWeek().equals(DayOfWeek.SUNDAY));
                return data;
            }, (o1, o2) -> o2, TreeMap::new)));
            return null;
        }).when(workdayCache).initYear(Year.of(year));
    }

    @Test
    @DisplayName("Testing calculateWorkday() when positive numberOfWorkdays and no weekend in the interval")
    void testCalculateWorkdayWhenPositiveNOWDAndHasNoWeekend() throws BusinessException {
        // Given
        init2020();
        LocalDate startDate = LocalDate.of(2020, 5, 12);
        int numberOfWorkdays = 3;
        // When
        LocalDate actual = underTest.calculateWorkday(startDate, numberOfWorkdays);
        // Then
        assertEquals(LocalDate.of(2020, 5, 15), actual);
    }

    @Test
    @DisplayName("Testing calculateWorkday() when positive numberOfWorkdays and weekend in the interval")
    void testCalculateWorkdayWhenPositiveNOWDAndHasWeekend() throws BusinessException {
        // Given
        init2020();
        LocalDate startDate = LocalDate.of(2020, 5, 15);
        int numberOfWorkdays = 3;
        // When
        LocalDate actual = underTest.calculateWorkday(startDate, numberOfWorkdays);
        // Then
        assertEquals(LocalDate.of(2020, 5, 20), actual);
    }

    @Test
    @DisplayName("Testing calculateWorkday() when negative numberOfWorkdays and no weekend in the interval")
    void testCalculateWorkdayWhenNegativeNOWDAndHasNoWeekend() throws BusinessException {
        // Given
        init2020();
        LocalDate startDate = LocalDate.of(2020, 5, 15);
        int numberOfWorkdays = -3;
        // When
        LocalDate actual = underTest.calculateWorkday(startDate, numberOfWorkdays);
        // Then
        assertEquals(LocalDate.of(2020, 5, 12), actual);
    }

    @Test
    @DisplayName("Testing calculateWorkday() when negative numberOfWorkdays and weekend in the interval")
    void testCalculateWorkdayWhenNegativeNOWDAndHasWeekend() throws BusinessException {
        // Given
        init2020();
        LocalDate startDate = LocalDate.of(2020, 5, 13);
        int numberOfWorkdays = -3;
        // When
        LocalDate actual = underTest.calculateWorkday(startDate, numberOfWorkdays);
        // Then
        assertEquals(LocalDate.of(2020, 5, 8), actual);
    }

    @Test
    @DisplayName("Testing calculateWorkday() when startDate year is not in cache")
    void testCalculateWorkdayWhenStartDateNotInCache() throws BusinessException {
        // Given
        init2020();
        enableInit(2021);
        LocalDate startDate = LocalDate.of(2021, 5, 13); // thursday
        int numberOfWorkdays = 3;
        // When
        LocalDate actual = underTest.calculateWorkday(startDate, numberOfWorkdays);
        // Then
        assertEquals(LocalDate.of(2021, 5, 18), actual); // tuesday
    }

    @Test
    @DisplayName("Testing calculateWorkday() when numberOfWorkdays param is 0 (empty interval)")
    void testCalculateWorkdayWhenNOWDZero() {
        // Given
        LocalDate startDate = LocalDate.of(2020, 5, 15);
        int numberOfWorkdays = 0;
        // When
        // Then
        assertThrows(BusinessException.class, () -> underTest.calculateWorkday(startDate, numberOfWorkdays));
    }

    @Test
    @DisplayName("Testing calculateWorkday() when numberOfWorkdays param is more than cached workdays")
    void testCalculateWorkdayWhenNOWDOutOfCacheRange() throws BusinessException {
        // Given
        init2020();
        enableInit(2021);
        LocalDate startDate = LocalDate.of(2020, 12, 31); // thursday
        int numberOfWorkdays = 3;
        // When
        LocalDate actual = underTest.calculateWorkday(startDate, numberOfWorkdays);
        // Then
        assertEquals(LocalDate.of(2021, 1, 5), actual); // tuesday
    }

    @Test
    @DisplayName("Testing calculateWorkday() when numberOfWorkdays is negative and is higher than valid year range")
    void testCalculateWorkdayWhenPositiveNOWDOutOfInvalidYearRange() {
        // Given
        init2020();
        IntStream.rangeClosed(2021, 2100).forEach(this::enableInit);
        LocalDate startDate = LocalDate.of(2020, 12, 31);
        int numberOfWorkdays = 200000;
        // When
        // Then
        assertThrows(BusinessException.class, () -> underTest.calculateWorkday(startDate, numberOfWorkdays));
    }

    @Test
    @DisplayName("Testing calculateWorkday() when numberOfWorkdays is negative and is lower than valid year range")
    void testCalculateWorkdayWhenNegativeNOWDOutOfInvalidYearRange() {
        // Given
        init2020();
        IntStream.rangeClosed(1, 2019).forEach(this::enableInit);
        LocalDate startDate = LocalDate.of(2020, 12, 31);
        int numberOfWorkdays = -5000000;
        // When
        // Then
        assertThrows(BusinessException.class, () -> underTest.calculateWorkday(startDate, numberOfWorkdays));
    }

    @Test
    @DisplayName("Testing calculateWorkday() when startDate is lower than valid year range")
    void testCalculateWorkdayWhenStartDateOutOfInvalidYearRange() {
        // Given
        LocalDate startDate = LocalDate.of(-1, 12, 31); // thursday
        int numberOfWorkdays = 20;
        // When
        // Then
        assertThrows(BusinessException.class, () -> underTest.calculateWorkday(startDate, numberOfWorkdays));
    }

    @Test
    @DisplayName("Testing calculateWorkdayList() when no weekend in interval")
    void testCalculateWorkdayListWhenNoWeekend() throws BusinessException {
        // Given
        init2020();
        LocalDate startDate = LocalDate.of(2020, 5, 11);
        LocalDate endDate = LocalDate.of(2020, 5, 15);
        // When
        List<LocalDate> workdayList = underTest.calculateWorkdayList(startDate, endDate);
        // Then
        assertEquals(5, workdayList.size());
    }

    @Test
    @DisplayName("Testing calculateWorkdayList() when weekend startDate")
    void testCalculateWorkdayListWhenStartDateWeekend() throws BusinessException {
        // Given
        init2020();
        LocalDate startDate = LocalDate.of(2020, 5, 9);
        LocalDate endDate = LocalDate.of(2020, 5, 15);
        // When
        List<LocalDate> workdayList = underTest.calculateWorkdayList(startDate, endDate);
        // Then
        assertEquals(5, workdayList.size());
    }

    @Test
    @DisplayName("Testing calculateWorkdayList() when weekend endDate")
    void testCalculateWorkdayListWhenEndDateWeekend() throws BusinessException {
        // Given
        init2020();
        LocalDate startDate = LocalDate.of(2020, 5, 11);
        LocalDate endDate = LocalDate.of(2020, 5, 17);
        // When
        List<LocalDate> workdayList = underTest.calculateWorkdayList(startDate, endDate);
        // Then
        assertEquals(5, workdayList.size());
    }

    @Test
    @DisplayName("Testing calculateWorkdayList() when startDate is null")
    void testCalculateWorkdayListWhenStartDateIsNull() {
        // Given
        LocalDate startDate = null;
        LocalDate endDate = LocalDate.of(2020, 5, 14);
        // When
        // Then
        assertThrows(BusinessException.class, () -> underTest.calculateWorkdayList(startDate, endDate));
    }

    @Test
    @DisplayName("Testing calculateWorkdayList() when endDate is null")
    void testCalculateWorkdayListWhenEndDateIsNull() {
        // Given
        LocalDate startDate = LocalDate.of(2020, 5, 15);
        LocalDate endDate = null;
        // When
        // Then
        assertThrows(BusinessException.class, () -> underTest.calculateWorkdayList(startDate, endDate));
    }

    @Test
    @DisplayName("Testing calculateWorkdayList() when startDate param is not in cache")
    void testCalculateWorkdayListWhenStartDateNotInCache() throws BusinessException {
        // Given
        init2020();
        IntStream.rangeClosed(2018, 2019).forEach(this::enableInit);
        LocalDate startDate = LocalDate.of(2018, 5, 15);
        LocalDate endDate = LocalDate.of(2020, 5, 15);
        // When
        List<LocalDate> workdayList = underTest.calculateWorkdayList(startDate, endDate);
        // Then
        assertNotEquals(0, workdayList.size());
    }

    @Test
    @DisplayName("Testing calculateWorkdayList() when endDate param is not in cache")
    void testCalculateWorkdayListWhenEndDateNotInCache() throws BusinessException {
        // Given
        init2020();
        IntStream.rangeClosed(2021, 2025).forEach(this::enableInit);
        LocalDate startDate = LocalDate.of(2020, 5, 15);
        LocalDate endDate = LocalDate.of(2025, 5, 15);
        // When
        List<LocalDate> workdayList = underTest.calculateWorkdayList(startDate, endDate);
        // Then
        assertNotEquals(0, workdayList.size());
    }

    @Test
    @DisplayName("Testing calculateWorkdayList() when endDate is before startDate")
    void testCalculateWorkdayListWhenEndDateBeforeStartDate() {
        // Given
        LocalDate startDate = LocalDate.of(2020, 5, 15);
        LocalDate endDate = LocalDate.of(2020, 5, 14);
        // When
        // Then
        assertThrows(BusinessException.class, () -> underTest.calculateWorkdayList(startDate, endDate));
    }

    @Test
    @DisplayName("Testing calculateWorkdayList() when startDate is lower than valid year range")
    void testCalculateWorkdayListWhenStartDateOutOfInvalidYearRange() {
        // Given
        LocalDate startDate = LocalDate.of(-1, 5, 15);
        LocalDate endDate = LocalDate.of(2020, 5, 14);
        // When
        // Then
        assertThrows(BusinessException.class, () -> underTest.calculateWorkdayList(startDate, endDate));
    }

    @Test
    @DisplayName("Testing calculateWorkdayList() when endDate is higher than valid year range")
    void testCalculateWorkdayListWhenEndDateOutOfInvalidYearRange() {
        // Given
        LocalDate startDate = LocalDate.of(2020, 5, 15);
        LocalDate endDate = LocalDate.of(2101, 5, 14);
        // When
        // Then
        assertThrows(BusinessException.class, () -> underTest.calculateWorkdayList(startDate, endDate));
    }

    @Test
    @DisplayName("Testing isGuaranteedResultOfCalculateWorkday() when positive numberOfWorkdays")
    void testIsGuaranteedResultOfCalculateWorkdayWhenPositiveNOWD() throws BusinessException {
        // Given
        init2020();
        setIsGuaranteed(Year.of(2020), true);
        LocalDate startDate = LocalDate.of(2020, 5, 12);
        int numberOfWorkdays = 3;
        // When
        boolean actual = underTest.isGuaranteedResultOfCalculateWorkday(startDate, numberOfWorkdays);
        // Then
        assertTrue(actual);
    }

    @Test
    @DisplayName("Testing isGuaranteedResultOfCalculateWorkday() when negative numberOfWorkdays")
    void testIsGuaranteedResultOfCalculateWorkdayWhenNegativeNOWD() throws BusinessException {
        // Given
        init2020();
        setIsGuaranteed(Year.of(2020), true);
        LocalDate startDate = LocalDate.of(2020, 5, 15);
        int numberOfWorkdays = -3;
        // When
        boolean actual = underTest.isGuaranteedResultOfCalculateWorkday(startDate, numberOfWorkdays);
        // Then
        assertTrue(actual);
    }

    @Test
    @DisplayName("Testing isGuaranteedResultOfCalculateWorkday() when startDate year is not guaranteed")
    void testIsGuaranteedResultOfCalculateWorkdayWhenStartYearNotGuaranteed() throws BusinessException {
        // Given
        setIsGuaranteed(Year.of(2020), false);
        LocalDate startDate = LocalDate.of(2020, 5, 13);
        int numberOfWorkdays = 30;
        // When
        boolean actual = underTest.isGuaranteedResultOfCalculateWorkday(startDate, numberOfWorkdays);
        // Then
        assertFalse(actual);
    }

    @Test
    @DisplayName("Testing isGuaranteedResultOfCalculateWorkday() when numberOfWorkdays param is 0 (empty interval)")
    void testIsGuaranteedResultOfCalculateWorkdayWhenNOWDZero() {
        // Given
        LocalDate startDate = LocalDate.of(2020, 5, 15);
        int numberOfWorkdays = 0;
        // When
        // Then
        assertThrows(BusinessException.class, () -> underTest.isGuaranteedResultOfCalculateWorkday(startDate, numberOfWorkdays));
    }

    @Test
    @DisplayName("Testing isGuaranteedResultOfCalculateWorkday() when numberOfWorkdays param is more than cached workdays")
    void testIsGuaranteedResultOfCalculateWorkdayWhenNOWDOutOfCacheRange() throws BusinessException {
        // Given
        init2020();
        setIsGuaranteed(Year.of(2020), true);
        setIsGuaranteed(Year.of(2021), false);
        LocalDate startDate = LocalDate.of(2020, 5, 31);
        int numberOfWorkdays = 250;
        // When
        boolean actual = underTest.isGuaranteedResultOfCalculateWorkday(startDate, numberOfWorkdays);
        // Then
        assertFalse(actual);
    }

    @Test
    @DisplayName("Testing isGuaranteedResultOfCalculateWorkdayList() when year is guaranteed")
    void testIsGuaranteedResultOfCalculateWorkdayListWhenGuaranteed() throws BusinessException {
        // Given
        setIsGuaranteed(Year.of(2020), true);
        LocalDate startDate = LocalDate.of(2020, 5, 12);
        LocalDate endDate = LocalDate.of(2020, 5, 12);
        // When
        boolean actual = underTest.isGuaranteedResultOfCalculateWorkdayList(startDate, endDate);
        // Then
        assertTrue(actual);
    }

    @Test
    @DisplayName("Testing isGuaranteedResultOfCalculateWorkdayList() when two years are guaranteed")
    void testIsGuaranteedResultOfCalculateWorkdayListWhenGuaranteedTwoYears() throws BusinessException {
        // Given
        setIsGuaranteed(Year.of(2020), true);
        setIsGuaranteed(Year.of(2021), true);
        LocalDate startDate = LocalDate.of(2020, 5, 15);
        LocalDate endDate = LocalDate.of(2021, 5, 15);
        // When
        boolean actual = underTest.isGuaranteedResultOfCalculateWorkdayList(startDate, endDate);
        // Then
        assertTrue(actual);
    }

    @Test
    @DisplayName("Testing isGuaranteedResultOfCalculateWorkdayList() when not guaranteed")
    void testIsGuaranteedResultOfCalculateWorkdayListWhenNotGuaranteed() throws BusinessException {
        // Given
        setIsGuaranteed(Year.of(2020), false);
        LocalDate startDate = LocalDate.of(2020, 5, 13);
        LocalDate endDate = LocalDate.of(2020, 6, 13);
        // When
        boolean actual = underTest.isGuaranteedResultOfCalculateWorkdayList(startDate, endDate);
        // Then
        assertFalse(actual);
    }

    @Test
    @DisplayName("Testing isGuaranteedResultOfCalculateWorkdayList() when one year is not guaranteed out of two")
    void testIsGuaranteedResultOfCalculateWorkdayListWhenNotGuaranteedTwoYears() throws BusinessException {
        // Given
        setIsGuaranteed(Year.of(2020), true);
        setIsGuaranteed(Year.of(2021), false);
        LocalDate startDate = LocalDate.of(2020, 5, 13);
        LocalDate endDate = LocalDate.of(2021, 5, 13);
        // When
        boolean actual = underTest.isGuaranteedResultOfCalculateWorkdayList(startDate, endDate);
        // Then
        assertFalse(actual);
    }
}
