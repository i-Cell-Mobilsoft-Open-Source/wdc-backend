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
package hu.icellmobilsoft.wdc.dto.adapter;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * {@code XmlAdapter} mapping JSR-310 {@code LocalDate} to ISO-8601 string or epoch milli
 * <p>
 * String format details: {@link DateTimeFormatter#ISO_DATE}
 *
 * @author mark.petrenyi
 * @see XmlAdapter
 * @see LocalDate
 */
public class LocalDateXmlAdapter extends XmlAdapter<String, LocalDate> {

    /**
     * Unmarshalling object to {@link LocalDate}
     *
     * @param v Object to marshall as LocalDate. Valid values are UTC time in millis represented by {@link Number} or {@link String}; and ISO_DATE
     *          ('2011-12-03' or '2011-12-03+01:00') representation of date as {@link String}
     * @return parsed LocalDate
     * @throws Exception if input parameter is not {@link Number} or {@link String}
     */
    @Override
    public LocalDate unmarshal(String v) throws Exception {
        if (v == null) {
            return null;
        }
        try {
            Long timeInMillis = Long.parseLong(v);
            Instant instant = Instant.ofEpochMilli(timeInMillis);
            return LocalDateTime.ofInstant(instant, ZoneOffset.UTC).toLocalDate();
        } catch (NumberFormatException e) {
            return LocalDate.parse(v, DateTimeFormatter.ISO_DATE);
        }
    }

    /**
     * Marshalling LocalDate to String. Output format is '2011-12-03'.
     *
     * @param localDate
     * @return formatted LocalDate
     * @throws Exception
     * @see DateTimeFormatter#ISO_DATE
     */
    @Override
    public String marshal(LocalDate localDate) throws Exception {
        if (localDate == null) {
            return null;
        }
        return DateTimeFormatter.ISO_DATE.format(localDate);
    }
}
