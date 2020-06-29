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

import hu.icellmobilsoft.wdc.calculator.core.action.type.HolidayType;

/**
 * Stores a date and descriptor information associated with it.
 *
 * @author adam.magyari
 */
public class WorkdayCacheData {

    private LocalDate date;

    private boolean isWorkday;

    private HolidayType holidayType;

    private LocalDate substitutedDay;

    private String description;

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public boolean isWorkday() {
        return isWorkday;
    }

    public void setWorkday(boolean workday) {
        isWorkday = workday;
    }

    public HolidayType getHolidayType() {
        return holidayType;
    }

    public void setHolidayType(HolidayType holidayType) {
        this.holidayType = holidayType;
    }

    public LocalDate getSubstitutedDay() {
        return substitutedDay;
    }

    public void setSubstitutedDay(LocalDate substitutedDay) {
        this.substitutedDay = substitutedDay;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
