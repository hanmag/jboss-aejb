package org.nju.artemis.aejb.evolution.handlers;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.jboss.logging.Logger;
import javax.aejb.AEjb;
import org.nju.artemis.aejb.component.AEjbUtilities;
import org.nju.artemis.aejb.component.AcContainer;
import org.nju.artemis.aejb.evolution.OperationContext;
import org.nju.artemis.aejb.evolution.OperationFailedException;

/**
 * This handler used to compute component's dependencies declared by {@link AEjb @AEjb}.
 * 
 * @author <a href="wangjue1199@gmail.com">Jason</a>
 */
public class DependencyComputeHandler implements OperationStepHandler {
	Logger log = Logger.getLogger(DependencyComputeHandler.class);
	private static final String HANDLER_NAME = "DependencyComputeHandler";
	
	@Override
	public OperationResult execute(OperationContext context) throws OperationFailedException {
		log.info("------- DependencyComputeHandler Start -------");
		final AEjbUtilities utilities = (AEjbUtilities) context.getContextData().get(AEjbUtilities.class);
		final String targetName = context.getTargetName();
		if(utilities == null || targetName == null)
			throw new OperationFailedException("Don't have enough resources to compute neighbors.");
		List<String> neighbors = new ArrayList<String>();
		for(AcContainer container:utilities.getAllContainers()) {
			Set<String> dependencies = container.getRuntimeDependencies();
			log.info("container = " + container.getAEJBName() + ", dependencies = " + dependencies);
			if(dependencies != null && dependencies.contains(targetName)) {
				neighbors.add(container.getAEJBName());
			}
		}
		log.info(targetName + "'s neighbors = " + neighbors);
		context.getContextData().put("neighbors", neighbors);
		log.info("------- DependencyComputeHandler Stop -------");
		return OperationResult.Expected;
	}

	@Override
	public String getHandlerName() {
		return HANDLER_NAME;
	}
}
