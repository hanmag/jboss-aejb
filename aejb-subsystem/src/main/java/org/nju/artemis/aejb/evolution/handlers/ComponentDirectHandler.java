package org.nju.artemis.aejb.evolution.handlers;

import java.util.List;

import org.jboss.logging.Logger;
import org.nju.artemis.aejb.component.AEjbUtilities;
import org.nju.artemis.aejb.component.AcContainer;
import org.nju.artemis.aejb.evolution.OperationContext;
import org.nju.artemis.aejb.evolution.OperationFailedException;

/**
 * This handler used to modify {@link AcContainer} evolution statistic directional map and protocols. 
 * 
 * @author <a href="mailto:wangjue1199@gmail.com">Jason</a>
 */
public class ComponentDirectHandler implements OperationStepHandler {
	Logger log = Logger.getLogger(ComponentDirectHandler.class);
	private final String HANDLER_NAME = "ComponentDirectHandler";
	
	@SuppressWarnings("unchecked")
	@Override
	public OperationResult execute(OperationContext context) throws OperationFailedException {
		log.info("------- ComponentDirectHandler Start -------");
		final AEjbUtilities utilities = (AEjbUtilities) context.getContextData().get(AEjbUtilities.class);
		final List<String> neighbors = (List<String>) context.getContextData().get("neighbors");
		final String targetName = context.getTargetName();
		final String toName = (String) context.getContextData().get("toName");
		final String protocol = (String) context.getContextData().get("protocol");
		final AcContainer toCon = utilities.getContainer(toName);
		if(neighbors == null)
			throw new OperationFailedException("Neighbors is null, failed to switch interceptors.");
		if(toCon == null)
			throw new OperationFailedException("Can not find Container: " + toName + ".");
		for(String aejbName:neighbors) {
			AcContainer con = utilities.getContainer(aejbName);
			if(con == null)
				throw new OperationFailedException("Can not find neighbor's container: " + aejbName + ".");
			con.getEvolutionStatistics().addToDirectionalMap(targetName, toCon);
			con.getEvolutionStatistics().addProtocol(targetName, protocol);
		}
		log.info("------- ComponentDirectHandler Stop -------");
		return OperationResult.Expected;
	}

	@Override
	public String getHandlerName() {
		return HANDLER_NAME;
	}

}
