package org.nju.artemis.aejb.evolution.handlers;

import org.nju.artemis.aejb.evolution.OperationContext;
import org.nju.artemis.aejb.evolution.OperationFailedException;

/**
 * This handler can alter OperationContext's operation name.
 * 
 * @author <a href="mailto:wangjue1199@gmail.com">Jason</a>
 */
public class OperationAlterHandler implements OperationStepHandler {
	private static final String HANDLER_NAME = "OperationAlterHandler";
	private final String operationName;
	
	public OperationAlterHandler(String operationName) {
		this.operationName = operationName;
	}
	
	@Override
	public OperationResult execute(OperationContext context) throws OperationFailedException {
		context.setOperationName(operationName);
		return OperationResult.Expected;
	}

	@Override
	public String getHandlerName() {
		return HANDLER_NAME;
	}

}
