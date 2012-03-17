package org.nju.artemis.aejb.evolution.behaviors;

import org.nju.artemis.aejb.component.AEjbUtilities;
import org.nju.artemis.aejb.evolution.OperationContext;
import org.nju.artemis.aejb.evolution.OperationFailedException;
import org.nju.artemis.aejb.evolution.handlers.DependencyHandler;
import org.nju.artemis.aejb.evolution.handlers.StatusShiftHandler;
import org.nju.artemis.aejb.management.client.AEjbClientImpl.AEjbStatus;

/**
 * @author <a href="wangjue1199@gmail.com">Jason</a>
 */
public class ComponentLocker extends EvolutionBehavior{
	private static final String HANDLER_NAME = "ComponentLocker";
	static final String LOCK = "lock";
	static final String UNLOCK = "unlock";
	private String aejbName;
	private AEjbUtilities utilities;
	private AEjbStatus status;
	
	private void lock(String aejbName) throws OperationFailedException {
		this.aejbName = aejbName;
		this.status = AEjbStatus.BLOCKING;
		
		perform();
	}
	
	private void unlock(String aejbName) throws OperationFailedException {
		this.aejbName = aejbName;
		this.status = AEjbStatus.RESUMING;
		
		perform();
	}

	@Override
	protected OperationContext generateOperationContext() {
		OperationContext context = new OperationContext(null, aejbName);
		context.getContextData().put(AEjbUtilities.class, utilities);
		context.getContextData().put(AEjbStatus.class, status);
		return context;
	}
	
	@Override
	protected void initializeStepHandlers() {
		handlers.add(new DependencyHandler());
		handlers.add(new StatusShiftHandler());
	}

	@Override
	public OperationResult execute(OperationContext context) throws OperationFailedException {
		String operationName = context.getOperationName();
		utilities = (AEjbUtilities) context.getContextData().get(AEjbUtilities.class);
		
		if(LOCK.equals(operationName)) {
			lock(context.getTargetName());
		} else if(UNLOCK.equals(operationName)) {
			unlock(context.getTargetName());
		} else
			throw new OperationFailedException("operation name: " + operationName + " has not been defined.");
		
		return OperationResult.Expected;
	}

	@Override
	public String getHandlerName() {
		return HANDLER_NAME;
	}
}
