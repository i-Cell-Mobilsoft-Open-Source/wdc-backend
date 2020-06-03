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
