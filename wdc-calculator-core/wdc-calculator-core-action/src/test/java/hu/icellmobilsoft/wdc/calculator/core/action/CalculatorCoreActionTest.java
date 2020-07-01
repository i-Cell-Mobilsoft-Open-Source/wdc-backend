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
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import hu.icellmobilsoft.wdc.calculator.core.action.exception.BusinessException;

/**
 * Testing CalculatorCoreAction class
 *
 * @author janos.hamrak
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Testing CalculatorCoreAction class")
class CalculatorCoreActionTest {

    private Map<Year, TreeMap<LocalDate, WorkdayCacheData>> cache = new HashMap<>();

    @Mock
    private WorkdayCache workdayCache;

    @InjectMocks
    private CalculatorCoreAction underTest;

    void initCache2020May() {
        cache.put(Year.of(2020), Stream.iterate(LocalDate.of(2020, 5, 1), d -> d.plusDays(1)).limit(31).collect(Collectors.toMap(d -> d, d -> {
            WorkdayCacheData data = new WorkdayCacheData();
            data.setDate(d);
            data.setWorkday(!d.getDayOfWeek().equals(DayOfWeek.SATURDAY) && !d.getDayOfWeek().equals(DayOfWeek.SUNDAY));
            return data;
        }, (o1, o2) -> o2, TreeMap::new)));
        when(workdayCache.getCache()).thenReturn(cache);
    }

    void enableInit2021() {
        doAnswer(invocationOnMock -> {
            cache.put(Year.of(2021), Stream.iterate(LocalDate.of(2021, 1, 1), d -> d.plusDays(1)).limit(365).collect(Collectors.toMap(d -> d, d -> {
                WorkdayCacheData data = new WorkdayCacheData();
                data.setDate(d);
                data.setWorkday(!d.getDayOfWeek().equals(DayOfWeek.SATURDAY) && !d.getDayOfWeek().equals(DayOfWeek.SUNDAY));
                return data;
            }, (o1, o2) -> o2, TreeMap::new)));
            return null;
        }).when(workdayCache).initYear(Year.of(2021));
    }

    @Test
    @DisplayName("Testing calculateWorkday() with positive numberOfWorkdays and no weekend in the interval")
    void testCalculateWorkdayPositiveNOWDAndHasNoWeekend() throws BusinessException {
        // Given
        initCache2020May();
        LocalDate startDate = LocalDate.of(2020, 5, 12);
        int numberOfWorkdays = 3;
        // When
        LocalDate actual = underTest.calculateWorkday(startDate, numberOfWorkdays);
        // Then
        assertEquals(LocalDate.of(2020, 5, 15), actual);
    }

    @Test
    @DisplayName("Testing calculateWorkday() with positive numberOfWorkdays and weekend in the interval")
    void testCalculateWorkdayPositiveNOWDAndHasWeekend() throws BusinessException {
        // Given
        initCache2020May();
        LocalDate startDate = LocalDate.of(2020, 5, 15);
        int numberOfWorkdays = 3;
        // When
        LocalDate actual = underTest.calculateWorkday(startDate, numberOfWorkdays);
        // Then
        assertEquals(LocalDate.of(2020, 5, 20), actual);
    }

    @Test
    @DisplayName("Testing calculateWorkday() with negative numberOfWorkdays and no weekend in the interval")
    void testCalculateWorkdayNegativeNOWDAndHasNoWeekend() throws BusinessException {
        // Given
        initCache2020May();
        LocalDate startDate = LocalDate.of(2020, 5, 15);
        int numberOfWorkdays = -3;
        // When
        LocalDate actual = underTest.calculateWorkday(startDate, numberOfWorkdays);
        // Then
        assertEquals(LocalDate.of(2020, 5, 12), actual);
    }

    @Test
    @DisplayName("Testing calculateWorkday() with negative numberOfWorkdays and weekend in the interval")
    void testCalculateWorkdayNegativeNOWDAndHasWeekend() throws BusinessException {
        // Given
        initCache2020May();
        LocalDate startDate = LocalDate.of(2020, 5, 13);
        int numberOfWorkdays = -3;
        // When
        LocalDate actual = underTest.calculateWorkday(startDate, numberOfWorkdays);
        // Then
        assertEquals(LocalDate.of(2020, 5, 8), actual);
    }

    @Test
    @DisplayName("Testing calculateWorkday() when startDate year is not in cache")
    void testCalculateWorkdayStartDateNotInCache() throws BusinessException {
        // Given
        initCache2020May();
        enableInit2021();
        LocalDate startDate = LocalDate.of(2021, 5, 13); // thursday
        int numberOfWorkdays = 3;
        // When
        LocalDate actual = underTest.calculateWorkday(startDate, numberOfWorkdays);
        // Then
        assertEquals(LocalDate.of(2021, 5, 18), actual); // tuesday
    }

    @Test
    @DisplayName("Testing calculateWorkday() when numberOfWorkdays param is 0 (empty interval)")
    void testCalculateWorkdayNOWDZero() {
        // Given
        LocalDate startDate = LocalDate.of(2020, 5, 15);
        int numberOfWorkdays = 0;
        // When
        // Then
        assertThrows(BusinessException.class, () -> underTest.calculateWorkday(startDate, numberOfWorkdays));
    }

    @Test
    @DisplayName("Testing calculateWorkday() when numberOfWorkdays param is more than cached workdays")
    void testCalculateWorkdayNOWDOutOfCacheRange() throws BusinessException {
        // Given
        initCache2020May();
        enableInit2021();
        LocalDate startDate = LocalDate.of(2020, 5, 31);
        int numberOfWorkdays = 250;
        // When
        LocalDate actual = underTest.calculateWorkday(startDate, numberOfWorkdays);
        // Then
        assertEquals(LocalDate.of(2021, 12, 17), actual); // tuesday
    }

    @Test
    @DisplayName("Testing calculateWorkdayList() with no weekend")
    void testCalculateWorkdayListNoWeekend() throws BusinessException {
        // Given
        initCache2020May();
        LocalDate startDate = LocalDate.of(2020, 5, 11);
        LocalDate endDate = LocalDate.of(2020, 5, 15);
        // When
        List<LocalDate> workdayList = underTest.calculateWorkdayList(startDate, endDate);
        // Then
        assertEquals(5, workdayList.size());
    }

    @Test
    @DisplayName("Testing calculateWorkdayList() with weekend startDate")
    void testCalculateWorkdayListStartDateWeekend() throws BusinessException {
        // Given
        initCache2020May();
        LocalDate startDate = LocalDate.of(2020, 5, 9);
        LocalDate endDate = LocalDate.of(2020, 5, 15);
        // When
        List<LocalDate> workdayList = underTest.calculateWorkdayList(startDate, endDate);
        // Then
        assertEquals(5, workdayList.size());
    }

    @Test
    @DisplayName("Testing calculateWorkdayList() with weekend endDate")
    void testCalculateWorkdayListEndDateWeekend() throws BusinessException {
        // Given
        initCache2020May();
        LocalDate startDate = LocalDate.of(2020, 5, 11);
        LocalDate endDate = LocalDate.of(2020, 5, 17);
        // When
        List<LocalDate> workdayList = underTest.calculateWorkdayList(startDate, endDate);
        // Then
        assertEquals(5, workdayList.size());
    }

    @Test
    @DisplayName("Testing calculateWorkdayList() when startDate is null")
    void testCalculateWorkdayListStartDateIsNull() {
        // Given
        LocalDate startDate = null;
        LocalDate endDate = LocalDate.of(2020, 5, 14);
        // When
        // Then
        assertThrows(BusinessException.class, () -> underTest.calculateWorkdayList(startDate, endDate));
    }

    @Test
    @DisplayName("Testing calculateWorkdayList() when endDate is null")
    void testCalculateWorkdayListEndDateIsNull() {
        // Given
        LocalDate startDate = LocalDate.of(2020, 5, 15);
        LocalDate endDate = null;
        // When
        // Then
        assertThrows(BusinessException.class, () -> underTest.calculateWorkdayList(startDate, endDate));
    }

    @Test
    @DisplayName("Testing calculateWorkday() when startDate param is not in cache")
    void testCalculateWorkdayListStartDateNotInCache() {
        // Given
        initCache2020May();
        LocalDate startDate = LocalDate.of(2010, 5, 15);
        LocalDate endDate = LocalDate.of(2020, 5, 15);
        // When
        // Then
        assertThrows(BusinessException.class, () -> underTest.calculateWorkdayList(startDate, endDate));
    }

    @Test
    @DisplayName("Testing calculateWorkday() when endDate param is not in cache")
    void testCalculateWorkdayListEndDateNotInCache() {
        // Given
        initCache2020May();
        LocalDate startDate = LocalDate.of(2020, 5, 15);
        LocalDate endDate = LocalDate.of(2025, 5, 15);
        // When
        // Then
        assertThrows(BusinessException.class, () -> underTest.calculateWorkdayList(startDate, endDate));
    }

    @Test
    @DisplayName("Testing calculateWorkdayList() when endDate is before startDate")
    void testCalculateWorkdayListEndDateBeforeStartDate() {
        // Given
        LocalDate startDate = LocalDate.of(2020, 5, 15);
        LocalDate endDate = LocalDate.of(2020, 5, 14);
        // When
        // Then
        assertThrows(BusinessException.class, () -> underTest.calculateWorkdayList(startDate, endDate));
    }

    @Test
    @DisplayName("Testing isGuaranteedResultOfCalculateWorkday() with positive numberOfWorkdays")
    void testIsGuaranteedResultOfCalculateWorkdayPositiveNOWD() {
        // Given
        initCache2020May();
        LocalDate startDate = LocalDate.of(2020, 5, 12);
        int numberOfWorkdays = 3;
        // When
        boolean actual = underTest.isGuaranteedResultOfCalculateWorkday(startDate, numberOfWorkdays);
        // Then
        assertTrue(actual);
    }

    @Test
    @DisplayName("Testing isGuaranteedResultOfCalculateWorkday() with negative numberOfWorkdays")
    void testIsGuaranteedResultOfCalculateWorkdayNegativeNOWD() {
        // Given
        initCache2020May();
        LocalDate startDate = LocalDate.of(2020, 5, 15);
        int numberOfWorkdays = -3;
        // When
        boolean actual = underTest.isGuaranteedResultOfCalculateWorkday(startDate, numberOfWorkdays);
        // Then
        assertTrue(actual);
    }

    @Test
    @DisplayName("Testing isGuaranteedResultOfCalculateWorkday() when startDate year is not in cache")
    void testIsGuaranteedResultOfCalculateWorkdayStartDateNotInCache() {
        // Given
        initCache2020May();
        LocalDate startDate = LocalDate.of(2020, 5, 13);
        int numberOfWorkdays = 30;
        // When
        boolean actual = underTest.isGuaranteedResultOfCalculateWorkday(startDate, numberOfWorkdays);
        // Then
        assertFalse(actual);
    }

    @Test
    @DisplayName("Testing isGuaranteedResultOfCalculateWorkday() when numberOfWorkdays param is 0 (empty interval)")
    void testIsGuaranteedResultOfCalculateWorkdayNOWDZero() {
        // Given
        LocalDate startDate = LocalDate.of(2020, 5, 15);
        int numberOfWorkdays = 0;
        // When
        boolean actual = underTest.isGuaranteedResultOfCalculateWorkday(startDate, numberOfWorkdays);
        // Then
        assertFalse(actual);
    }

    @Test
    @DisplayName("Testing isGuaranteedResultOfCalculateWorkday() when numberOfWorkdays param is more than cached workdays")
    void testIsGuaranteedResultOfCalculateWorkdayNOWDOutOfCacheRange() {
        // Given
        initCache2020May();
        LocalDate startDate = LocalDate.of(2020, 5, 31);
        int numberOfWorkdays = 250;
        // When
        boolean actual = underTest.isGuaranteedResultOfCalculateWorkday(startDate, numberOfWorkdays);
        // Then
        assertFalse(actual);
    }
}
