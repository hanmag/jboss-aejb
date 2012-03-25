package org.nju.artemis.aejb.evolution.handlers;

import org.jboss.logging.Logger;
import org.nju.artemis.aejb.component.AEjbUtilities;
import org.nju.artemis.aejb.evolution.DuService;
import org.nju.artemis.aejb.evolution.OperationContext;
import org.nju.artemis.aejb.evolution.OperationFailedException;
import org.nju.artemis.aejb.evolution.protocols.Protocol;

/**
 * This is a continuous handler,it will wait for the safe point or timeout.
 * 
 * @author <a href="mailto:wangjue1199@gmail.com">Jason</a>
 */
public class ComponentSecurityHandler implements OperationStepHandler {
	Logger log = Logger.getLogger(ComponentSecurityHandler.class);
	
	private final String HANDLER_NAME = "ComponentSecurityHandler";
	
	@Override
	public OperationResult execute(OperationContext context) throws OperationFailedException {
		final String protocolName = (String) context.getContextData().get("protocol");
		final String aejbName = context.getTargetName();
		Protocol protocol = DuService.getProtocol(protocolName);
		if(protocol != null) {
			boolean res = protocol.setToSafePoint(AEjbUtilities.getContainer(aejbName));
			if(res)
				return OperationResult.Expected;
		}
		return OperationResult.UnExpected;
	}

	@Override
	public String getHandlerName() {
		return HANDLER_NAME;
	}

}
