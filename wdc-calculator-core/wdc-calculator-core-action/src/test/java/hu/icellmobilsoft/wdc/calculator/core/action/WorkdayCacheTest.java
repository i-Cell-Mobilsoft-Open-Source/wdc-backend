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
    void initYearContainsGivenYear() {
        // Given
        Year year = Year.of(2019);
        // When
        underTest.initYear(year);
        // Then
        assertTrue(underTest.getCache().containsKey(Year.of(2019)));
    }

    @Test
    @DisplayName("Testing initYear's year parameter with different values.")
    void initYearDifferentYears() {
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
    void initYearSameYear() {
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
    void initYearNull() {
        // Given

        // When
        underTest.initYear(null);
        // Then
        assertEquals(0, underTest.getCache().size());
    }

    @Test
    @DisplayName("Testing initYear when input year is not leap year.")
    void initYearSizeWhenNotLeapYear() {
        // Given
        Year notLeapYear = Year.of(2019);
        // When
        underTest.initYear(notLeapYear);
        // Then
        assertEquals(365, underTest.getCache().get(notLeapYear).size());
    }

    @Test
    @DisplayName("Testing initYear when input year is leap year.")
    void initYearSizeWhenLeapYear() {
        // Given
        Year leapYear = Year.of(2020);
        // When
        underTest.initYear(leapYear);
        // Then
        assertEquals(366, underTest.getCache().get(leapYear).size());
    }

    @Test
    @DisplayName("Testing initYear for a given workday.")
    void initYearContainsGivenWorkday() {
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
    void initYearContainsGivenWeekendDay() {
        // Given
        Year year = Year.of(2019);
        LocalDate weekendDay = LocalDate.of(2019, 12, 21);
        // When
        underTest.initYear(year);
        // Then
        assertFalse(underTest.getCache().get(year).get(weekendDay));
    }
}