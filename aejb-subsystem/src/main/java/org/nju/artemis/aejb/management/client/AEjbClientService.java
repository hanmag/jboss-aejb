package org.nju.artemis.aejb.management.client;

import org.jboss.logging.Logger;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;

/**
 * @author <a href="wangjue1199@gmail.com">Jason</a>
 */
public class AEjbClientService implements Service<AEjbClient> {

	private static final Logger log = Logger.getLogger(AEjbClientService.class);
	
	public AEjbClientService() {
	}
	
	@Override
	public AEjbClient getValue() throws IllegalStateException, IllegalArgumentException {
		AEjbClientImpl.INSTANCE.refreshAEjbsInfo();
		return AEjbClientImpl.INSTANCE;
	}

	@Override
	public void start(StartContext context) throws StartException {
		log.info("start aejb client service...");
	}

	@Override
	public void stop(StopContext context) {
		log.info("stop aejb client service...");
	}
}
