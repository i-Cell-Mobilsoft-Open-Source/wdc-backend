package hu.icellmobilsoft.wdc.dto.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Testing LocalDateXmlAdapter class
 *
 * @author janos.hamrak
 */
@DisplayName("Testing LocalDateXmlAdapter class")
class LocalDateXmlAdapterTest {

    private LocalDateXmlAdapter underTest;

    @BeforeEach
    void init() {
        underTest = new LocalDateXmlAdapter();
    }

    @Test
    @DisplayName("Testing unmarshal() with ISO representation")
    void testUnmarshalISOString() throws Exception {
        // Given
        String string = "2011-12-03";
        LocalDate expected = LocalDate.of(2011, 12, 3);
        // When
        LocalDate actual = underTest.unmarshal(string);
        // Then
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Testing unmarshal() with zoned ISO representation")
    void testUnmarshalTimeZoneString() throws Exception {
        // Given
        String string = "2011-12-03+01:00";
        LocalDate expected = LocalDate.of(2011, 12, 3);
        // When
        LocalDate actual = underTest.unmarshal(string);
        // Then
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Testing unmarshal() with Unix timestamp representation")
    void testUnmarshalUnixTimestampString() throws Exception {
        // Given
        String string = "1322874000000";
        LocalDate expected = LocalDate.of(2011, 12, 3);
        // When
        LocalDate actual = underTest.unmarshal(string);
        // Then
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Testing unmarshal() with null value")
    void testUnmarshalNull() throws Exception {
        // Given
        String string = null;
        // When
        // Then
        assertNull(underTest.unmarshal(string));
    }

    @Test
    @DisplayName("Testing unmarshal() with invalid input format")
    void testUnmarshalInvalidString() {
        // Given
        String string = "2011:12:03";
        // When
        // Then
        assertThrows(Exception.class, () -> underTest.unmarshal(string));
    }

    @Test
    @DisplayName("Testing marshal() with valid LocalDate")
    void testMarshal() throws Exception {
        // Given
        LocalDate localDate = LocalDate.of(2011, 12, 3);
        String expected = "2011-12-03";
        // When
        String actual = underTest.marshal(localDate);
        // Then
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Testing marshal() with null value")
    void testMarshalNull() throws Exception {
        // Given
        LocalDate localDate = null;
        // When
        // Then
        assertNull(underTest.marshal(localDate));

    }
}