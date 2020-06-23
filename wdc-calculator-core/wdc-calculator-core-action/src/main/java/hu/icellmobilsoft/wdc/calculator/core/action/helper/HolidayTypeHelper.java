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
package hu.icellmobilsoft.wdc.calculator.core.action.helper;

import hu.icellmobilsoft.wdc.core.dto.datafile.datafile.HolidayType;

public class HolidayTypeHelper {

    public static HolidayType toDto(hu.icellmobilsoft.wdc.calculator.core.action.type.HolidayType holidayType) {
        if (holidayType == null){
            return null;
        }
        switch (holidayType) {
            case WEEKEND:
                return HolidayType.WEEKEND;
            case REST_DAY:
                return HolidayType.RESTDAY;
            case FEAST_DAY:
                return HolidayType.FEASTDAY;
            case NATIONAL_DAY:
                return HolidayType.NATIONALDAY;
            case PLUS_WORKING_DAY:
                return HolidayType.PLUSWORKINGDAY;
            default:
                return null;
        }
    }

    public static hu.icellmobilsoft.wdc.calculator.core.action.type.HolidayType toCacheData(HolidayType holidayType) {
        if (holidayType == null) {
            return null;
        }
        switch (holidayType) {
            case WEEKEND:
                return hu.icellmobilsoft.wdc.calculator.core.action.type.HolidayType.WEEKEND;
            case RESTDAY:
                return hu.icellmobilsoft.wdc.calculator.core.action.type.HolidayType.REST_DAY;
            case FEASTDAY:
                return hu.icellmobilsoft.wdc.calculator.core.action.type.HolidayType.FEAST_DAY;
            case NATIONALDAY:
                return hu.icellmobilsoft.wdc.calculator.core.action.type.HolidayType.NATIONAL_DAY;
            case PLUSWORKINGDAY:
                return hu.icellmobilsoft.wdc.calculator.core.action.type.HolidayType.PLUS_WORKING_DAY;
            default:
                return null;
        }
    }
}
