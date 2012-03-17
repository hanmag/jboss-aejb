package org.nju.artemis.aejb.evolution.handlers;

import org.nju.artemis.aejb.evolution.OperationContext;
import org.nju.artemis.aejb.evolution.OperationFailedException;

/**
 * @author <a href="mailto:wangjue1199@gmail.com">Jason</a>
 */
public class InterceptorSwitchHandler implements OperationStepHandler {
	private final String HANDLER_NAME = "InterceptorSwitchHandler";
	
	@Override
	public OperationResult execute(OperationContext context) throws OperationFailedException {
		// TODO Auto-generated method stub
		return OperationResult.Expected;
	}

	@Override
	public String getHandlerName() {
		return HANDLER_NAME;
	}

}
