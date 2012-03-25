package org.nju.artemis.aejb.evolution.protocols;

import org.jboss.logging.Logger;
import org.nju.artemis.aejb.component.AcContainer;
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
		if(ports != null && transactionManager.isActive(objectId)) {
			if(transactionManager.getPassedPorts(objectId).contains(targetName) || transactionManager.getFuturePorts(objectId).contains(targetName))
				return false;
		}
		return true;
	}

	@Override
	public String getName() {
		return "QUIESCENCE";
	}

	@Override
	public boolean setToSafePoint(AcContainer container) {
		// first 
		return false;
	}

}
