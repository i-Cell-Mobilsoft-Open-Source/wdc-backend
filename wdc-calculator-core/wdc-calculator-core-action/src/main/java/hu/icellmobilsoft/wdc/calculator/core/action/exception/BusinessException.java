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
