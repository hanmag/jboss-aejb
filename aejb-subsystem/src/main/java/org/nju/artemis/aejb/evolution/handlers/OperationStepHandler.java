package org.nju.artemis.aejb.evolution.handlers;

import org.nju.artemis.aejb.evolution.OperationContext;
import org.nju.artemis.aejb.evolution.OperationFailedException;

/**
 * @author <a href="wangjue1199@gmail.com">Jason</a>
 */
public interface OperationStepHandler {

	void execute(OperationContext context) throws OperationFailedException;
}
