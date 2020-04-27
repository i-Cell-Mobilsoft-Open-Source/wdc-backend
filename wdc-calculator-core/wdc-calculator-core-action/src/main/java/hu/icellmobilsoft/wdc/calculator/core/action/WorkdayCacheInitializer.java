package hu.icellmobilsoft.wdc.calculator.core.action;

import java.time.Year;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import hu.icellmobilsoft.wdc.calculator.core.action.exception.BusinessException;

/**
 * Workday Calendar initializer class
 *
 * @author janos.hamrak
 */
@ApplicationScoped
public class WorkdayCacheInitializer {

    @Inject
    private WorkdayDataReader workdayDataReader;

    @Inject
    private WorkdayCache workdayCache;

    /**
     * Observer method that starts initializing the cache as soon as the application is up:
     * <p>
     * 1) Initialize cache with last, actual and next years' data
     * <p>
     * 2) Read and process workday data from config files
     *
     */
    public void init(@Observes @Initialized(ApplicationScoped.class) Object init) {
        this.initWorkdayCache();
        try {
            workdayDataReader.readAll();
        } catch (BusinessException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private void initWorkdayCache() {
        workdayCache.initYear(Year.now().minusYears(1));
        workdayCache.initYear(Year.now());
        workdayCache.initYear(Year.now().plusYears(1));
    }
}
