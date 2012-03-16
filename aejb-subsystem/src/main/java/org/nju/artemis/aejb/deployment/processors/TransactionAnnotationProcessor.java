package org.nju.artemis.aejb.deployment.processors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.aejb.Transaction;

import org.jboss.as.ee.component.Attachments;
import org.jboss.as.ee.component.BindingConfiguration;
import org.jboss.as.ee.component.EEModuleClassDescription;
import org.jboss.as.ee.component.EEModuleDescription;
import org.jboss.as.ee.component.InjectionSource;
import org.jboss.as.naming.ManagedReference;
import org.jboss.as.naming.ManagedReferenceFactory;
import org.jboss.as.naming.ValueManagedReference;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.as.server.deployment.Phase;
import org.jboss.as.server.deployment.annotation.CompositeIndex;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.MethodInfo;
import org.jboss.logging.Logger;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.value.ImmediateValue;
import org.nju.artemis.aejb.AEjbLogger;
import org.nju.artemis.aejb.component.AcContainer;
import org.nju.artemis.aejb.deployment.transaction.TransactionState;
import org.nju.artemis.aejb.deployment.transaction.TransactionManagerImpl;

/**
 * @author <a href="wangjue1199@gmail.com">Jason</a>
 */
public class TransactionAnnotationProcessor implements DeploymentUnitProcessor {
	Logger log = Logger.getLogger(TransactionAnnotationProcessor.class);
	private static final DotName TRANSACTION_ANNOTATION = DotName.createSimple(Transaction.class.getName());
	
	public static final Phase PHASE = Phase.PARSE;
	// should be after all components are known
	public static final int PRIORITY = 0x3900;
	private AcContainer container;
	
	@Override
	public void deploy(DeploymentPhaseContext phaseContext)	throws DeploymentUnitProcessingException {
        final DeploymentUnit deploymentUnit = phaseContext.getDeploymentUnit();
        final Map<String,AcContainer> aejbInfo = deploymentUnit.getAttachment(org.nju.artemis.aejb.deployment.Attachments.AEJB_INFO);
        final CompositeIndex index = deploymentUnit.getAttachment(org.jboss.as.server.deployment.Attachments.COMPOSITE_ANNOTATION_INDEX);
        final EEModuleDescription moduleDescription = deploymentUnit.getAttachment(Attachments.EE_MODULE_DESCRIPTION);
        final List<AnnotationInstance> aejbAnnotations = index.getAnnotations(TRANSACTION_ANNOTATION);
        for (final AnnotationInstance aejbAnnotation : aejbAnnotations) {
            final AnnotationTarget target = aejbAnnotation.target();
            if (!(target instanceof MethodInfo)) {
                log.warn(aejbAnnotation.name() + " annotation is expected to be applied on method level. " + target + " is not a method");
                continue;
            }
            
            String beanName = ((MethodInfo) target).declaringClass().name().local();
            if(!aejbInfo.containsKey(beanName)) {
            	log.warn(aejbAnnotation.name() + " annotation is expected to be applied with @Adaptive. " + beanName + " is not an AEJB");
            	continue;
            }

            container = aejbInfo.get(beanName);
            final TransactionResourceWrapper annotationWrapper = new TransactionResourceWrapper(aejbAnnotation);
            if(annotationWrapper.states() == null) {
            	log.warn(aejbAnnotation.name() + " annotation states attribute is empty");
            	return ;
            }
            try {
				processTransaction(annotationWrapper, (MethodInfo) target, moduleDescription);
			} catch (TransactionProcessFailedException e) {
				e.printStackTrace();
				return ;
			}
		}
	}
	
	private void processTransaction(TransactionResourceWrapper annotationWrapper, MethodInfo method, final EEModuleDescription eeModuleDescription) throws TransactionProcessFailedException{
		int states_count = annotationWrapper.states().length;
		if(states_count == 1) {
			log.warn(annotationWrapper.name() + " annotation states number can not equal 1");
			return ;
		}
		else if(states_count != annotationWrapper.next().length + 1)
			throw new TransactionProcessFailedException("[states] length must equal to [next] length + 1.");
		TransactionState[] states = new TransactionState[states_count];
		Map[] nextStates = new HashMap[states_count-1];
		for (int i = 0; i < states_count-1; i++)
			nextStates[i] = new HashMap<String, String>();
		List<String> portNames = new ArrayList<String>();

		for (int i = 0; i < states_count; i++) {
			String[] passedPorts = null, futurePorts = null;
			String[] portz = annotationWrapper.states()[i].split(";");
			int pLength = portz.length;
			if(pLength > 2) {
				throw new TransactionProcessFailedException("There are more than 1 ';' in a state.");
			}
			for(int index = 0; index < pLength; index++) {
				String[] ports = portz[index].split(",");
				if(index == 0)
					passedPorts = ports;
				else
					futurePorts = ports;
			}
			
			for (String port : passedPorts) {
				if (!portNames.contains(port))
					portNames.add(port);
			}
			TransactionState s = new TransactionState(passedPorts, futurePorts);
			states[i] = s;
			if(i == states_count-1)
				continue;
			String[] nexts = annotationWrapper.next()[i].split(",");
			for (String next : nexts) {
				if (next != null && !next.equals("")) {
					String[] n = next.split("-");
					if (n.length != 2)
						throw new TransactionProcessFailedException("There must be 1 '-' in a next state.");
//					log.info("i:" + i + "-next:" + n[1]);
					nextStates[i].put(n[0], n[1]);
//					log.info(nextStates[i]);
				}
			}
		}
		final int size = portNames.size();
		TransactionManager tm = new TransactionManagerImpl(annotationWrapper.name(), method.name(), portNames.toArray(new String[size]), states, nextStates);
		container.setTransactionManager(tm);
		bindTransactionManager(tm, method.declaringClass(), eeModuleDescription);
	}

	private void bindTransactionManager(final TransactionManager tm, final ClassInfo classInfo, final EEModuleDescription eeModuleDescription) {
		final EEModuleClassDescription classDescription = eeModuleDescription.addOrGetLocalClassDescription(classInfo.name().toString());
		final ManagedReferenceFactory valueManagedReferenceFactory = new ManagedReferenceFactory() {

            @Override
            public ManagedReference getReference() {
                return new ValueManagedReference(new ImmediateValue<Object>(tm));
            }
        };
        final String jndiName = "java:global/aejb/transactionmanager/" + tm.getName();
		// Create the binding.
        final BindingConfiguration bindingConfiguration = new BindingConfiguration(jndiName, new InjectionSource() {

			@Override
			public void getResourceValue(ResolutionContext arg0, ServiceBuilder<?> builder, DeploymentPhaseContext arg2,
					Injector<ManagedReferenceFactory> injector)	throws DeploymentUnitProcessingException {
				injector.inject(valueManagedReferenceFactory);
				AEjbLogger.ROOT_LOGGER.boundTransactionManager(jndiName);
			}
        	
        });
        
        classDescription.getBindingConfigurations().add(bindingConfiguration);
	}

	@Override
	public void undeploy(DeploymentUnit arg0) {
		// TODO Auto-generated method stub

	}

	private class TransactionResourceWrapper {
        private final String name;
        private final String[] states;
        private final String[] next;

        private TransactionResourceWrapper(final AnnotationInstance annotation) {
            name = stringValueOrNull(annotation, "name");
            states = arrayValueOrNull(annotation, "states");
            next = arrayValueOrNull(annotation, "next");
        }

        private String name() {
            return name;
        }

        private String[] states() {
            return states;
        }

        private String[] next() {
            return next;
        }

        private String stringValueOrNull(final AnnotationInstance annotation, final String attribute) {
            final AnnotationValue value = annotation.value(attribute);
            return value != null ? value.asString() : null;
        }
        
        private String[] arrayValueOrNull(final AnnotationInstance annotation, final String attribute) {
            final AnnotationValue value = annotation.value(attribute);
            return value != null ? value.asStringArray() : null;
        }
    }
}
