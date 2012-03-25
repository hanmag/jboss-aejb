package org.nju.artemis.aejb.evolution.behaviors;

import org.jboss.logging.Logger;
import org.nju.artemis.aejb.component.AEjbUtilities;
import org.nju.artemis.aejb.evolution.OperationContext;
import org.nju.artemis.aejb.evolution.OperationFailedException;
import org.nju.artemis.aejb.evolution.handlers.ComponentInstanceDirectHandler;
import org.nju.artemis.aejb.evolution.handlers.ComponentSecurityHandler;
import org.nju.artemis.aejb.evolution.handlers.DependencyChangeHandler;
import org.nju.artemis.aejb.evolution.handlers.InterfaceCompareHandler;
import org.nju.artemis.aejb.evolution.handlers.OperationAlterHandler;
import org.nju.artemis.aejb.evolution.handlers.OperationStepHandler;

/**
 * This behavior used to replace component with another one.<br>
 * It composed by six handlers:
 * 
 * {@link InterfaceIdentifyHandler};
 * {@link OperationAlterHandler};
 * {@link ComponentLocker};
 * {@link ComponentSecurityHandler};
 * {@link ComponentInstanceDirectHandler};
 * {@link DependencyChangeHandler}.
 * 
 * @author <a href="mailto:wangjue1199@gmail.com">Jason</a>
 */
public class ComponentReplacer extends EvolutionBehavior {
	Logger log = Logger.getLogger(ComponentReplacer.class);
	
	private static final String HANDLER_NAME = "ComponentReplacer";
	private final String fromName;
	private final String toName;
	private final String protocol;
	private AEjbUtilities utilities;
	
	public ComponentReplacer(String fromName, String toName, String protocol) {
		this.fromName = fromName;
		this.toName = toName;
		this.protocol = protocol;
	}

	@Override
	public OperationResult execute(OperationContext context) throws OperationFailedException {
		utilities = (AEjbUtilities) context.getContextData().get(AEjbUtilities.class);
		perform();
		return OperationResult.Expected;
	}

	@Override
	public String getHandlerName() {
		return HANDLER_NAME;
	}

	@Override
	protected void initializeStepHandlers() {
		handlers.add(new InterfaceCompareHandler());
		handlers.add(new OperationAlterHandler(ComponentLocker.LOCK));
		handlers.add(new ComponentLocker());
		handlers.add(new ComponentSecurityHandler());
		handlers.add(new ComponentInstanceDirectHandler());
		handlers.add(new OperationAlterHandler(ComponentLocker.UNLOCK));
		handlers.add(new ComponentLocker());
		handlers.add(new DependencyChangeHandler());
	}

	@Override
	protected OperationContext generateOperationContext() {
		OperationContext context = new OperationContext(null, fromName);
		context.getContextData().put(AEjbUtilities.class, utilities);
		context.getContextData().put("toName", toName);
		context.getContextData().put("protocol", protocol);
		return context;
	}

	@Override
	void rollBackWhenUnExpectedResult(OperationStepHandler handler) throws OperationFailedException {
		
	}

}
