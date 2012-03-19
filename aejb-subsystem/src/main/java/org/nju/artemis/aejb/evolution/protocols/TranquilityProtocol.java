package org.nju.artemis.aejb.evolution.protocols;

import java.util.List;

import org.jboss.logging.Logger;
import org.nju.artemis.aejb.deployment.processors.TransactionManager;

/**
 * @author <a href="mailto:wangjue1199@gmail.com">Jason</a>
 */
public class TranquilityProtocol implements Protocol {
	Logger log = Logger.getLogger(TranquilityProtocol.class);
	
	@Override
	public boolean checkTransactionSecurity(String targetName,	String objectId, TransactionManager transactionManager) {
		log.debug("Using \"" + getName() + "\" protocol to check transaction security.");
		List<String> ports = transactionManager.getAffectedPorts(objectId);
		if(ports != null && ports.contains(targetName))
			return false;
		return true;
	}

	@Override
	public String getName() {
		return "QUIESCENCE";
	}

}
