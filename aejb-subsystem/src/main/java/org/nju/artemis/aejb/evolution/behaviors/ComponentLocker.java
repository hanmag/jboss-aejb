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

	private String aejbName;
	private AEjbUtilities utilities;
	private AEjbStatus status;
	
	public ComponentLocker(AEjbUtilities utilities) {
		this.utilities = utilities;
	}
	
	public void lock(String aejbName) throws OperationFailedException {
		this.aejbName = aejbName;
		this.status = AEjbStatus.BLOCKING;
		perform();
	}
	
	public void unlock(String aejbName) throws OperationFailedException {
		this.aejbName = aejbName;
		this.status = AEjbStatus.RESUMING;
		perform();
	}

	@Override
	protected OperationContext generateOperationContext() {
		OperationContext context = new OperationContext();
		context.getContextData().put("targetName", aejbName);
		context.getContextData().put(AEjbUtilities.class, utilities);
		context.getContextData().put(AEjbStatus.class, status);
		return context;
	}
	
	@Override
	protected void initializeStepHandlers() {
		handlers.add(new DependencyHandler());
		handlers.add(new StatusShiftHandler());
	}
}
