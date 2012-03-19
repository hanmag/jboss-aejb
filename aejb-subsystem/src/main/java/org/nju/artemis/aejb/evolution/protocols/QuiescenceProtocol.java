package org.nju.artemis.aejb.evolution.protocols;

import org.jboss.logging.Logger;
import org.nju.artemis.aejb.deployment.processors.TransactionManager;

/**
 * @author <a href="mailto:wangjue1199@gmail.com">Jason</a>
 */
public class QuiescenceProtocol implements Protocol {
	Logger log = Logger.getLogger(QuiescenceProtocol.class);
	
	@Override
	public boolean checkTransactionSecurity(String targetName,	String objectId, TransactionManager transactionManager) {
		log.debug("Using \"" + getName() + "\" protocol to check transaction security.");
		String[] ports = transactionManager.getInvolvedPorts();
		if(ports == null)
			return true;
		return transactionManager.isActive(objectId) == false;
	}

	@Override
	public String getName() {
		return "QUIESCENCE";
	}

}
