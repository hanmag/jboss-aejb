package org.nju.artemis.aejb.evolution.handlers;

import java.util.List;

import org.nju.artemis.aejb.component.AEjbUtilities;
import org.nju.artemis.aejb.component.AcContainer;
import org.nju.artemis.aejb.evolution.OperationContext;
import org.nju.artemis.aejb.evolution.OperationFailedException;

/**
 * @author <a href="mailto:wangjue1199@gmail.com">Jason</a>
 */
public class DependencyChangeHandler implements OperationStepHandler {
	private static final String HANDLER_NAME = "DependencyChangeHandler";
	
	@Override
	public OperationResult execute(OperationContext context) throws OperationFailedException {
		final AEjbUtilities utilities = (AEjbUtilities) context.getContextData().get(AEjbUtilities.class);
		final List<String> neighbors = (List<String>) context.getContextData().get("neighbors");
		final String targetName = context.getTargetName();
		final String toName = (String) context.getContextData().get("toName");
		if(neighbors == null)
			throw new OperationFailedException("Neighbors is null, failed to change dependency.");
		for(String aejbName:neighbors) {
			AcContainer con = utilities.getContainer(aejbName);
			if(con == null)
				throw new OperationFailedException("Can not find neighbor's container: " + aejbName + ".");
			con.changeDependencies(targetName, toName);
		}
		return OperationResult.Expected;
	}

	@Override
	public String getHandlerName() {
		return HANDLER_NAME;
	}

}
