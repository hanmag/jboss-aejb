package org.nju.artemis.aejb.evolution.behaviors;

import java.util.ArrayList;
import java.util.List;

import org.nju.artemis.aejb.evolution.OperationContext;
import org.nju.artemis.aejb.evolution.OperationFailedException;
import org.nju.artemis.aejb.evolution.handlers.OperationStepHandler;

/**
 * @author <a href="wangjue1199@gmail.com">Jason</a>
 */
public abstract class EvolutionBehavior {

	List<OperationStepHandler> handlers = new ArrayList<OperationStepHandler>();
	
	abstract protected void initializeStepHandlers();
	
	protected void perform() throws OperationFailedException {
		OperationContext context = generateOperationContext();
		initializeStepHandlers();
		int size = handlers.size();
		for(int index=0;index<size;index++) {
			handlers.get(index).execute(context);
		}
	}
	
	abstract protected OperationContext generateOperationContext();
}
