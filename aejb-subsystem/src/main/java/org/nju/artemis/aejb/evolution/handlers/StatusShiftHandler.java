package org.nju.artemis.aejb.evolution.handlers;

import java.util.List;

import org.nju.artemis.aejb.component.AEjbUtilities;
import org.nju.artemis.aejb.component.AcContainer;
import org.nju.artemis.aejb.evolution.OperationContext;
import org.nju.artemis.aejb.evolution.OperationFailedException;
import org.nju.artemis.aejb.management.client.AEjbClientImpl.AEjbStatus;

/**
 * @author <a href="wangjue1199@gmail.com">Jason</a>
 */
public class StatusShiftHandler implements OperationStepHandler {

	@Override
	public void execute(OperationContext context) throws OperationFailedException {
		AEjbUtilities utilities = (AEjbUtilities) context.getContextData().get(AEjbUtilities.class);
		List<String> neighbors = (List<String>) context.getContextData().get("neighbors");
		String targetName = (String) context.getContextData().get("targetName");
		AEjbStatus status = (AEjbStatus) context.getContextData().get(AEjbStatus.class);
		if(neighbors == null)
			throw new OperationFailedException("Neighbors is null, failed to shift status.");
		for(String aejbName:neighbors) {
			AcContainer con = utilities.getContainer(aejbName);
			if(con == null)
				throw new OperationFailedException("Can not find neighbor's container.");
			con.setAEjbStatus(targetName, status);
		}
	}
}
