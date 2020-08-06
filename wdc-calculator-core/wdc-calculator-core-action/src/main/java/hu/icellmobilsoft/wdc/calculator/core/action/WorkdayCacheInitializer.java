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

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.time.Year;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import hu.icellmobilsoft.wdc.calculator.core.action.exception.BusinessException;
import hu.icellmobilsoft.wdc.calculator.core.config.CalculatorCoreConfig;

/**
 * Workday Calendar initializer class
 *
 * @author janos.hamrak
 */
@ApplicationScoped
public class WorkdayCacheInitializer {

    private WatchService watchService;

    @Inject
    private CalculatorCoreConfig calculatorCoreConfig;

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
            initDirWatch();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        initScheduler();
    }

    private void initWorkdayCache() {
        synchronized (workdayCache.getLockObject()) {
            workdayCache.clearCache();

            workdayCache.initYear(Year.now().minusYears(1));
            workdayCache.initYear(Year.now());
            workdayCache.initYear(Year.now().plusYears(1));

            try {
                workdayDataReader.readAll();
            } catch (BusinessException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
    }

    private void initScheduler() {
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                checkChanges();
            }
        };
        timer.scheduleAtFixedRate(task, 0, 3600000);
    }

    private void checkChanges() {
        boolean reInit = false;
        WatchKey watchKey;
        while ((watchKey=watchService.poll()) != null) {
            List<WatchEvent<?>> watchEvents = watchKey.pollEvents();
            if (!watchEvents.isEmpty()) {
                reInit = true;
            }
            watchKey.reset();
        }

        if (reInit) {
           initWorkdayCache();
        }
    }

    private void initDirWatch() throws IOException {
        watchService = FileSystems.getDefault().newWatchService();
        Paths.get(calculatorCoreConfig.getDataFileFolder())
                .register(watchService,
                        StandardWatchEventKinds.ENTRY_CREATE,
                        StandardWatchEventKinds.ENTRY_MODIFY,
                        StandardWatchEventKinds.ENTRY_DELETE);
    }
}
