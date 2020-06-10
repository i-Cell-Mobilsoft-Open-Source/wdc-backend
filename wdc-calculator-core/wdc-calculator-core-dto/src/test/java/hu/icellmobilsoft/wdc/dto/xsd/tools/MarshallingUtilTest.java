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
