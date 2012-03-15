package org.nju.artemis.aejb.evolution;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.jboss.logging.Logger;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;
import org.nju.artemis.aejb.component.AEjbUtilities;
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
//                    	manageAEjbSwitch(switchMap);
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
		getAEjbUtilities().setAEjbStatus(aejbStatus);
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
