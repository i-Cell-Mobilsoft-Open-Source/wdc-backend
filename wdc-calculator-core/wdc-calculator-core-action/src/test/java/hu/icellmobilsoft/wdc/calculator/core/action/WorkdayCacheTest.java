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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.Year;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Testing WorkdayCalculator class
 *
 * @author janos.hamrak
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Testing WorkdayCalculator")
class WorkdayCacheTest {

    private WorkdayCache underTest;

    @BeforeEach
    void init() {
        underTest = new WorkdayCache();
    }

    @Test
    @DisplayName("Testing initYear's year parameter.")
    void testInitYearContainsGivenYear() {
        // Given
        Year year = Year.of(2019);
        // When
        underTest.initYear(year);
        // Then
        assertTrue(underTest.getCache().containsKey(Year.of(2019)));
    }

    @Test
    @DisplayName("Testing initYear's year parameter with different values.")
    void testInitYearDifferentYears() {
        // Given
        Year year_2018 = Year.of(2018);
        Year year_2019 = Year.of(2019);
        // When
        underTest.initYear(year_2018);
        underTest.initYear(year_2019);
        // Then
        assertEquals(2, underTest.getCache().size());
    }

    @Test
    @DisplayName("Testing initYear's year parameter with the same value twice.")
    void testInitYearSameYear() {
        // Given
        Year year = Year.of(2019);
        // When
        underTest.initYear(year);
        underTest.initYear(year);
        // Then
        assertEquals(1, underTest.getCache().size());
    }

    @Test
    @DisplayName("Testing initYear's year parameter with empty value.")
    void testInitYearNull() {
        // Given
        // When
        underTest.initYear(null);
        // Then
        assertEquals(0, underTest.getCache().size());
    }

    @Test
    @DisplayName("Testing initYear when input year is not leap year.")
    void testInitYearSizeWhenNotLeapYear() {
        // Given
        Year notLeapYear = Year.of(2019);
        // When
        underTest.initYear(notLeapYear);
        // Then
        assertEquals(365, underTest.getCache().get(notLeapYear).size());
    }

    @Test
    @DisplayName("Testing initYear when input year is leap year.")
    void testInitYearSizeWhenLeapYear() {
        // Given
        Year leapYear = Year.of(2020);
        // When
        underTest.initYear(leapYear);
        // Then
        assertEquals(366, underTest.getCache().get(leapYear).size());
    }

    @Test
    @DisplayName("Testing initYear for a given workday.")
    void testInitYearContainsGivenWorkday() {
        // Given
        Year year = Year.of(2019);
        LocalDate workday = LocalDate.of(2019, 12, 17);
        // When
        underTest.initYear(year);
        // Then
        assertTrue(underTest.getCache().get(year).get(workday));
    }

    @Test
    @DisplayName("Testing initYear for a given weekend day.")
    void testInitYearContainsGivenWeekendDay() {
        // Given
        Year year = Year.of(2019);
        LocalDate weekendDay = LocalDate.of(2019, 12, 21);
        // When
        underTest.initYear(year);
        // Then
        assertFalse(underTest.getCache().get(year).get(weekendDay));
    }

    @Test
    @DisplayName("Testing put() already initialized date.")
    void testPutDateOverwrite() {
        // Given
        Year year = Year.of(2019);
        LocalDate weekendDay = LocalDate.of(2019, 12, 21);
        underTest.initYear(year);
        // When
        underTest.put(weekendDay, true);
        // Then
        assertTrue(underTest.getCache().get(year).get(weekendDay));
    }

    @Test
    @DisplayName("Testing put() date is not in cache.")
    void testPutDateInsert() {
        // Given
        Year year = Year.of(2019);
        LocalDate weekendDay = LocalDate.of(2019, 12, 21);
        // When
        underTest.put(weekendDay, true);
        // Then
        assertTrue(underTest.getCache().get(year).get(weekendDay));
        assertEquals(365, underTest.getCache().get(year).size());
    }

    @Test
    @DisplayName("Testing put() null date")
    void testPutDateIsNull() {
        // Given
        // When
        underTest.put(null, false);
        // Then
        assertTrue(underTest.getCache().isEmpty());
    }
}
