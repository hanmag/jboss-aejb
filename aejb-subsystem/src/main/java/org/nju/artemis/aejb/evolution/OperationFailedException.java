package org.nju.artemis.aejb.evolution;

import org.jboss.dmr.ModelNode;

/**
 * @author <a href="wangjue1199@gmail.com">Jason</a>
 */
public class OperationFailedException extends Exception {

	private final ModelNode failureDescription;
	private static final long serialVersionUID = -7739982937774202930L;
	
	/**
     * Constructs a {@code OperationFailedException} with the specified detail message. The cause is not initialized,
     * and may subsequently be initialized by a call to {@link #initCause(Throwable) initCause}.
     *
     * @param msg the detail message
     * @param description the description of the failure
     */
    public OperationFailedException(final String msg, final ModelNode description) {
        super(msg);
        failureDescription = description;
    }
    
	/**
     * Constructs a {@code OperationFailedException} with the given message.
     * The message is also used as the {@link #getFailureDescription() failure description}.
     * The cause is not initialized, and may
     * subsequently be initialized by a call to {@link #initCause(Throwable) initCause}.
     *
     * @param message the description of the failure
     */
    public OperationFailedException(final String message) {
        this(message, new ModelNode(message));
    }
    
	@Override
    public String toString() {
        return super.toString() + " [ " + failureDescription + " ]";
    }
}
