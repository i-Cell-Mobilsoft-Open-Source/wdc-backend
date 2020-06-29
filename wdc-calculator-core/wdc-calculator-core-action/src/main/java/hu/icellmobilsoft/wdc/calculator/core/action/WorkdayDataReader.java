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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import hu.icellmobilsoft.wdc.calculator.core.action.exception.BusinessException;
import hu.icellmobilsoft.wdc.calculator.core.action.exception.ReasonCode;
import hu.icellmobilsoft.wdc.calculator.core.action.helper.HolidayTypeHelper;
import hu.icellmobilsoft.wdc.calculator.core.config.CalculatorCoreConfig;
import hu.icellmobilsoft.wdc.core.dto.datafile.datafile.HolidayType;
import hu.icellmobilsoft.wdc.core.dto.datafile.datafile.WorkdayData;
import hu.icellmobilsoft.wdc.dto.xsd.tools.MarshallingUtil;

/**
 * Workday file and config data reader
 *
 * @author janos.hamrak
 */
@Dependent
public class WorkdayDataReader {

    private static final String DATE_DELIMITER = ";";

    public static final String FIELD_DELIMITER = ",";

    @Inject
    private CalculatorCoreConfig calculatorCoreConfig;

    @Inject
    private WorkdayCache workdayCache;

    /**
     * Reads and processes workday data from config files in the given order:
     * <p>
     * 1) from files under "datafolder" config param
     * <p>
     * 2) from "include" config param
     * <p>
     * 3) from "exclude" config param
     * <p>
     *
     */
    public void readAll() throws BusinessException {
        this.readFileData();
        this.readIncludeData();
        this.readExcludeData();
    }

    private void readFileData() throws BusinessException {
        Path folderPath = Paths.get(calculatorCoreConfig.getDataFileFolder()).toAbsolutePath();
        try (Stream<Path> filePaths = Files.walk(folderPath)) {
            for (Path path : filePaths.filter(p -> !p.toFile().isDirectory()).collect(Collectors.toList())) {
                WorkdayData workdayData = getWorkdayDataFromFile(path.toFile());
                workdayData.getWorkdayData().forEach(d -> workdayCache.put(d.getDate(), d.isWorkday(), HolidayTypeHelper.toCacheData(d.getHolidayType()), d.getSubstitutedDay(), d.getDescription()));
            }
        } catch (IOException e) {
            throw new BusinessException(ReasonCode.INVALID_INPUT, "Error accessing data folder at: " + folderPath.toString(), e);
        }
    }

    private WorkdayData getWorkdayDataFromFile(File file) throws BusinessException {
        try {
            WorkdayData workdayData = MarshallingUtil.unmarshalXml(WorkdayData.class, new FileInputStream(file),
                    "xsd/hu/icellmobilsoft/wdc/core/dto/datafile/workdayData.xsd");
            if (workdayData == null) {
                throw new BusinessException(ReasonCode.INVALID_INPUT, "Could not process file: " + file.getAbsolutePath());
            }
            return workdayData;
        } catch (IOException e) {
            throw new BusinessException(ReasonCode.INVALID_INPUT, "Could not process file: " + file.getAbsolutePath(), e);
        }
    }

    private void readIncludeData() throws BusinessException {
        try {
            readData(calculatorCoreConfig.getIncludeDays(), true);
        } catch (DateTimeParseException | IllegalArgumentException | BusinessException e) {
            throw new BusinessException(ReasonCode.INVALID_INPUT, "Include data could not be read. " + e.getMessage(), e);
        }
    }

    private void readExcludeData() throws BusinessException {
        try {
            readData(calculatorCoreConfig.getExcludeDays(),false);
        } catch (DateTimeParseException | IllegalArgumentException | BusinessException e) {
            throw new BusinessException(ReasonCode.INVALID_INPUT, "Exclude data could not be read. " + e.getMessage(), e);
        }
    }

    private void readData(String data, boolean isWorkingDay) throws BusinessException {
        for (String entry : data.split(DATE_DELIMITER)) {
            if (!entry.trim().isEmpty()) {
                String[] fields = entry.split(FIELD_DELIMITER);
                if (fields.length < 1 || fields.length > 4) {
                    throw new BusinessException(ReasonCode.INVALID_INPUT,
                            "Invalid format. Each entry should have between 1 and 4 fields. Entry {" + entry + "} is invalid.");
                }
                String[] fieldsFixLength = Arrays.copyOf(fields, 4);

                workdayCache.put(LocalDate.parse(fieldsFixLength[0]), isWorkingDay,
                        StringUtils.isBlank(fieldsFixLength[1]) ? null : HolidayTypeHelper.toCacheData(HolidayType.valueOf(fieldsFixLength[1])),
                        StringUtils.isBlank(fieldsFixLength[3]) ? null : LocalDate.parse(fieldsFixLength[3]), fieldsFixLength[2]);
            }
        }
    }
}
