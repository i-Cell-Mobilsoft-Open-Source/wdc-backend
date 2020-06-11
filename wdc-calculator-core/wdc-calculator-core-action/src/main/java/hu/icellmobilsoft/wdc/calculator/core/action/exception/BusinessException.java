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
package hu.icellmobilsoft.wdc.calculator.core.action.exception;

/**
 * BusinessException: provide calculator-core specific Exception
 *
 * @author ferenc.lutischan
 */
public class BusinessException extends Exception {

    private static final long serialVersionUID = 1L;

    private Enum<?> reasonCode;

    /**
     * Creates new BusinessException instance
     *
     * @param reasonCode
     *            The reason code enum
     * @param msg
     *            The message
     */
    public BusinessException(Enum<?> reasonCode, String msg) {
        super(msg);
        this.reasonCode = reasonCode;
    }

    /**
     * Creates new BusinessException instance
     *
     * @param reasonCode
     *            The reason code enum
     * @param msg
     *            The message
     * @param cause
     *            The throwable to wrap
     */
    public BusinessException(Enum<?> reasonCode, String msg, Throwable cause) {
        super(msg, cause);
        this.reasonCode = reasonCode;
    }

    public Enum<?> getReasonCode() {
        return reasonCode;
    }
}
