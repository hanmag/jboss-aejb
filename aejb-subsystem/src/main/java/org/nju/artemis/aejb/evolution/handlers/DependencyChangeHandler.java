package org.nju.artemis.aejb.evolution.handlers;

import java.util.List;

import org.jboss.logging.Logger;
import org.nju.artemis.aejb.component.AEjbUtilities;
import org.nju.artemis.aejb.component.AcContainer;
import org.nju.artemis.aejb.evolution.OperationContext;
import org.nju.artemis.aejb.evolution.OperationFailedException;

/**
 * This handler used to change component runtime dependencies.
 * 
 * @author <a href="mailto:wangjue1199@gmail.com">Jason</a>
 */
public class DependencyChangeHandler implements OperationStepHandler {
	Logger log = Logger.getLogger(DependencyChangeHandler.class);
	private static final String HANDLER_NAME = "DependencyChangeHandler";
	
	@SuppressWarnings("unchecked")
	@Override
	public OperationResult execute(OperationContext context) throws OperationFailedException {
		log.info("------- DependencyChangeHandler Start -------");
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
			con.changeRuntimeDependencies(targetName, toName);
			log.info(aejbName + "'s dependency changed from " + targetName + " to " + toName);
		}
		log.info("------- DependencyChangeHandler Stop -------");
		return OperationResult.Expected;
	}

	@Override
	public String getHandlerName() {
		return HANDLER_NAME;
	}

}
