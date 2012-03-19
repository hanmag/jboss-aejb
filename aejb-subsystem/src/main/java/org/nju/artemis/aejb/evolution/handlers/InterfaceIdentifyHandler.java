package org.nju.artemis.aejb.evolution.handlers;

import java.lang.reflect.Method;
import java.util.Set;

import org.jboss.logging.Logger;
import org.nju.artemis.aejb.component.AEjbUtilities;
import org.nju.artemis.aejb.component.AcContainer;
import org.nju.artemis.aejb.evolution.OperationContext;
import org.nju.artemis.aejb.evolution.OperationFailedException;

/**
 * This handler used to identify whether two interface classes are equal.The class name can be different.<br>
 * Methods must be same.
 * 
 * @author <a href="mailto:wangjue1199@gmail.com">Jason</a>
 */
public class InterfaceIdentifyHandler implements OperationStepHandler {
	Logger log = Logger.getLogger(InterfaceIdentifyHandler.class);
	private static final String HANDLER_NAME = "InterfaceIdentifyHandler";
	
	@Override
	public OperationResult execute(OperationContext context) throws OperationFailedException {
		log.info("------- InterfaceIdentifyHandler Start -------");
		final String fromName = context.getTargetName();
		final String toName = (String) context.getContextData().get("toName");
		final AEjbUtilities utilities = (AEjbUtilities) context.getContextData().get(AEjbUtilities.class);
		if(utilities == null || fromName == null)
			throw new OperationFailedException("Don't have enough resources to indetify interfaces.");
		if(toName == null)
			throw new OperationFailedException(fromName + "'s toName is null");
		log.info("fromName = " + fromName + ";toName = " + toName);
		AcContainer fromContainer = utilities.getContainer(fromName);
		AcContainer toContainer = utilities.getContainer(toName);
		if(fromContainer == null || toContainer == null)
			throw new OperationFailedException(fromName + " or " + toName + " is not an AEJB.");
		if(fromContainer.getLocalView().size() != toContainer.getLocalView().size() || fromContainer.getRemoteView().size() != toContainer.getRemoteView().size())
			throw new OperationFailedException(fromName + " and " + toName + "'s view size are not equal.");
		boolean locale = equalSet(fromContainer.getLocalView(), toContainer.getLocalView());
		boolean remote = equalSet(fromContainer.getRemoteView(), toContainer.getRemoteView());
		log.info("------- InterfaceIdentifyHandler Stop -------");
		return (locale && remote) ? OperationResult.Expected : OperationResult.UnExpected;
	}

	private boolean equalSet(Set<Class<?>> sets1, Set<Class<?>> sets2) {
		int sum = sets1.size();
		int num = 0;
		for (Class<?> class1 : sets1) {
			for (Class<?> class2 : sets2) {
				if (equals(class1, class2)) {
					num++;
					break;
				}
			}
		}
		return sum == num;
	}
	
	public static boolean equals(Class<?> c1, Class<?> c2) {
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
						Class<?>[] params1 = method1.getParameterTypes();
						Class<?>[] params2 = method2.getParameterTypes();
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
