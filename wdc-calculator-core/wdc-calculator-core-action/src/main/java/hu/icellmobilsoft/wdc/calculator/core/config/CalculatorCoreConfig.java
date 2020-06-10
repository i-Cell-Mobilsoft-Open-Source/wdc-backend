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
package hu.icellmobilsoft.wdc.calculator.core.config;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * Config class for calculator core
 *
 * @author zsolt.vasi
 *
 */
@Dependent
public class CalculatorCoreConfig {

    @Inject
    @ConfigProperty(name = CalculatorCoreCatalog.WDC_CONFIG_INPUT_DATAFOLDER)
    private String dataFileFolder;

    @Inject
    @ConfigProperty(name = CalculatorCoreCatalog.WDC_CONFIG_INPUT_INCLUDE, defaultValue = "")
    private String includeDays;

    @Inject
    @ConfigProperty(name = CalculatorCoreCatalog.WDC_CONFIG_INPUT_EXCLUDE, defaultValue = "")
    private String excludeDays;

    public String getDataFileFolder() {
        return dataFileFolder;
    }

    public String getIncludeDays() {
        return includeDays;
    }

    public String getExcludeDays() {
        return excludeDays;
    }

}
