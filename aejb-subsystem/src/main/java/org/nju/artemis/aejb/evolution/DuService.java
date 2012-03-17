package org.nju.artemis.aejb.evolution;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicLong;

import org.jboss.logging.Logger;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;
import org.nju.artemis.aejb.component.AEjbUtilities;
import org.nju.artemis.aejb.evolution.behaviors.ComponentSwitcher;
import org.nju.artemis.aejb.management.client.AEjbClient;
import org.nju.artemis.aejb.management.client.AEjbClientImpl.AEjbStatus;

/**
 * @author <a href="wangjue1199@gmail.com">Jason</a>
 */
public class DuService implements Service<DuService> {
	Logger log = Logger.getLogger(DuService.class);
	public static final ServiceName SERVICE_NAME = ServiceName.JBOSS.append("aejb", "duservice");
	private final InjectedValue<AEjbClient> aejbClientValue = new InjectedValue<AEjbClient>();
	private final InjectedValue<AEjbUtilities> aejbUtilitiesValue = new InjectedValue<AEjbUtilities>();
	private final AtomicLong tick = new AtomicLong(2000);
	
	@Override
	public DuService getValue() throws IllegalStateException, IllegalArgumentException {
		return this;
	}
	
	AEjbClient getAEjbClient() {
		return aejbClientValue.getValue();
	}
	
	AEjbUtilities getAEjbUtilities() {
		return aejbUtilitiesValue.getValue();
	}

	private Thread AEJBCLIENT = new Thread() {
        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(tick.get());
                    Map<String, AEjbStatus> aejbStatus = getAEjbClient().getAEjbStatus();
                    if(!aejbStatus.isEmpty()) {
                    	manageAEjbState(aejbStatus);
                    	getAEjbClient().clearAEjbStatus();
                    }
                    Map<String, String> switchMap = getAEjbClient().getSwitchMap();
                    if(!switchMap.isEmpty()) {
                    	manageAEjbSwitch(switchMap);
                    	getAEjbClient().clearSwitchMap();
                    }
                } catch (InterruptedException e) {
                    interrupted();
                    break;
				}
            }
        }
    };
    
	@Override
	public void start(StartContext context) throws StartException {
		AEJBCLIENT.start();
	}

	private void manageAEjbState(Map<String, AEjbStatus> aejbStatus) {
		// simple
		getAEjbUtilities().setAEjbStatus(aejbStatus);
	}

	private void manageAEjbSwitch(Map<String, String> switchMap) {
		Iterator<Entry<String, String>> iterator = switchMap.entrySet().iterator();
		while(iterator.hasNext()) {
			Entry<String, String> entry = iterator.next();
			String fromName = entry.getKey();
			String toName = entry.getValue();
			OperationContext context = new OperationContext(null, fromName);
			context.getContextData().put(AEjbUtilities.class, getAEjbUtilities());
			try {
				new ComponentSwitcher(fromName, toName, getAEjbClient().getProtocol(fromName)).execute(context);
			} catch (OperationFailedException e) {
				log.warn(e.getMessage());
				log.info("Evolution: " + fromName + " switch to " + toName + " failed.");
			}
		}
	}
	
	@Override
	public void stop(StopContext context) {
		AEJBCLIENT.interrupt();
	}

	public InjectedValue<AEjbClient> getAejbClientValue() {
		return aejbClientValue;
	}

	public InjectedValue<AEjbUtilities> getAejbUtilitiesValue() {
		return aejbUtilitiesValue;
	}

}
