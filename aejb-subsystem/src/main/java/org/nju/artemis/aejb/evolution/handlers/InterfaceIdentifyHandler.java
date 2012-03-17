package org.nju.artemis.aejb.evolution.handlers;

import java.lang.reflect.Method;

import org.nju.artemis.aejb.component.AEjbUtilities;
import org.nju.artemis.aejb.component.AcContainer;
import org.nju.artemis.aejb.evolution.OperationContext;
import org.nju.artemis.aejb.evolution.OperationFailedException;

/**
 * @author <a href="mailto:wangjue1199@gmail.com">Jason</a>
 */
public class InterfaceIdentifyHandler implements OperationStepHandler {
	private static final String HANDLER_NAME = "InterfaceIdentifyHandler";
	
	@Override
	public OperationResult execute(OperationContext context) throws OperationFailedException {
		String fromName = context.getTargetName();
		String toName = (String) context.getContextData().get("toName");
		AEjbUtilities utilities = (AEjbUtilities) context.getContextData().get(AEjbUtilities.class);
		if(utilities == null || fromName == null)
			throw new OperationFailedException("Don't have enough resources to indetify interfaces.");
		if(toName == null)
			throw new OperationFailedException(fromName + "'s toName is null");
		
		AcContainer fromContainer = utilities.getContainer(fromName);
		AcContainer toContainer = utilities.getContainer(toName);
		if(fromContainer == null || toContainer == null)
			throw new OperationFailedException(fromName + " or " + toName + " is not an AEJB.");
		
		boolean locale = equals(fromContainer.getLocalView(), toContainer.getLocalView());
		boolean remote = equals(fromContainer.getRemoteView(), toContainer.getRemoteView());
		
		return (locale && remote) ? OperationResult.Expected : OperationResult.UnExpected;
	}

	private boolean equals(Class<?> c1, Class<?> c2) {
		if(c1 == null && c2 == null)
			return true;
		if (c1 != null && c2 != null) {
			Method[] methods1 = c1.getMethods();
			Method[] methods2 = c2.getMethods();

			if (methods1 == null || methods2 == null || methods1.length != methods2.length)
				return false;
			int methodsSum = methods1.length;
			int methodsNum = 0;

			for (Method method1 : methods1) {
				for (Method method2 : methods2) {
					if (method1.getName() == method2.getName()) {
						if (!method1.getReturnType().equals(method2.getReturnType()))
							continue;
						/* Avoid unnecessary cloning */
						Class[] params1 = method1.getParameterTypes();
						Class[] params2 = method2.getParameterTypes();
						if (params1.length == params2.length) {
							int i;
							for (i = 0; i < params1.length; i++) {
								if (params1[i] != params2[i])
									break;
							}
							if (i == params1.length)
								methodsNum++;
						}
					}
				}
			}

			if (methodsNum == methodsSum)
				return true;
		}
		return false;
	}

	@Override
	public String getHandlerName() {
		return HANDLER_NAME;
	}
}
