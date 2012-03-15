package org.nju.artemis.aejb.evolution.handlers;

import java.util.ArrayList;
import java.util.List;

import org.nju.artemis.aejb.component.AEjbUtilities;
import org.nju.artemis.aejb.component.AcContainer;
import org.nju.artemis.aejb.evolution.OperationContext;
import org.nju.artemis.aejb.evolution.OperationFailedException;

/**
 * @author <a href="wangjue1199@gmail.com">Jason</a>
 */
public class DependencyHandler implements OperationStepHandler {

	@Override
	public void execute(OperationContext context) throws OperationFailedException {
		AEjbUtilities utilities = (AEjbUtilities) context.getContextData().get(AEjbUtilities.class);
		String targetName = (String) context.getContextData().get("targetName");
		if(utilities == null || targetName == null)
			throw new OperationFailedException("Don't have enough resources to compute neighbors.");
		List<String> neighbors = new ArrayList<String>();
		for(AcContainer container:utilities.getAllContainers()) {
			List<String> dependencies = container.getDependencies();
			if(dependencies != null && dependencies.contains(targetName)) {
				neighbors.add(container.getAEJBName());
			}
		}
		context.getContextData().put("neighbors", neighbors);
	}
}
