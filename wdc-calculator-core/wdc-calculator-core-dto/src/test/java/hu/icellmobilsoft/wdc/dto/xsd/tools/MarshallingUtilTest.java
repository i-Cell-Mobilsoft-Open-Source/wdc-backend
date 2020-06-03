package hu.icellmobilsoft.wdc.dto.xsd.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import hu.icellmobilsoft.wdc.core.dto.datafile.datafile.WorkdayData;

class MarshallingUtilTest {
    private static final String VALID_XML = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
            "<WorkdayData xmlns=\"http://datafile.dto.core.wdc.icellmobilsoft.hu/datafile\">" +
            "  <workdayData>" +
            "    <date>1998-12-28</date>" +
            "    <workday>false</workday>" +
            "  </workdayData>" +
            "  <workdayData>" +
            "    <date>2014-10-25</date>" +
            "    <workday>1</workday>" +
            "  </workdayData>" +
            "</WorkdayData>";
    private static final String INVALID_XML = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
            "<WorkdayData xmlns=\"http://datafile.dto.core.wdc.icellmobilsoft.hu/datafile\">" +
            "  <workdayData>" +
            "    <date>1998-12-28</date>" +
            "    <workday>false</workday>" +
            "  </workdayData>" +
            "  <workdayData>" +
            "    <date>2014-100-25</date>" +
            "    <workday>1</workday>" +
            "  </workdayData>" +
            "</WorkdayData>";

    @Test
    void whenUnmarshalValidXmlThenReturnGoodObject() {
        // Given
        String input = VALID_XML;

        InputStream is = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        // When
        WorkdayData workdayData = MarshallingUtil.unmarshalXml(WorkdayData.class, is, "xsd/hu/icellmobilsoft/wdc/core/dto/datafile/workdayData.xsd");
        // Then
        assertEquals(2, workdayData.getWorkdayData().size());
        assertEquals(LocalDate.of(1998, 12, 28), workdayData.getWorkdayData().get(0).getDate());
        assertEquals(LocalDate.of(2014, 10, 25), workdayData.getWorkdayData().get(1).getDate());
        assertFalse(workdayData.getWorkdayData().get(0).isWorkday());
        assertTrue(workdayData.getWorkdayData().get(1).isWorkday());
    }

    @Test
    void whenUnmarshalInvalidXmlThenReturnNull() {
        // Given
        String input = INVALID_XML;

        InputStream is = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        // When
        WorkdayData workdayData = MarshallingUtil.unmarshalXml(WorkdayData.class, is, "xsd/hu/icellmobilsoft/wdc/core/dto/datafile/workdayData.xsd");
        // Then
        assertNull(workdayData);
    }

    @Test
    void whenUnmarshalValidXmlWithInvalidXsdThenReturnNull() {
        // Given
        String input = VALID_XML;

        InputStream is = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        // When
        WorkdayData workdayData = MarshallingUtil.unmarshalXml(WorkdayData.class, is, "xsd/hu/icellmobilsoft/wdc/core/dto/datafile/workday.xsd");
        // Then
        assertNull(workdayData);
    }
}