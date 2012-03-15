package org.nju.artemis.aejb.subsystem;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.DESCRIPTION;

import java.util.List;
import java.util.Locale;

import org.jboss.as.controller.AbstractAddStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.ServiceVerificationHandler;
import org.jboss.as.controller.descriptions.DescriptionProvider;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.as.naming.ManagedReference;
import org.jboss.as.naming.ManagedReferenceFactory;
import org.jboss.as.naming.ServiceBasedNamingStore;
import org.jboss.as.naming.ValueManagedReference;
import org.jboss.as.naming.deployment.ContextNames;
import org.jboss.as.naming.service.BinderService;
import org.jboss.dmr.ModelNode;
import org.jboss.logging.Logger;
import org.jboss.msc.service.AbstractServiceListener;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceTarget;
import org.jboss.msc.value.ImmediateValue;
import org.nju.artemis.aejb.AEjbLogger;
import org.nju.artemis.aejb.management.client.AEjbClientService;

/**
 *
 * @author <a href="wangjue1199@gmail.com">Jason</a>
 */
class ClientJndiBindingHandler extends AbstractAddStepHandler implements DescriptionProvider {
	Logger log = Logger.getLogger(ClientJndiBindingHandler.class);
	public static final ServiceName SERVICE_NAME_BASE = ServiceName.JBOSS.append("aejb-client");
    public static final ClientJndiBindingHandler INSTANCE = new ClientJndiBindingHandler();

    private ClientJndiBindingHandler() {
    }

    @Override
    public ModelNode getModelDescription(Locale locale) {
        ModelNode node = new ModelNode();
        node.get(DESCRIPTION).set("Define the aejb client jndi name");
        return node;
    }

    @Override
    protected void populateModel(ModelNode operation, ModelNode model) throws OperationFailedException {
    }

    @Override
    protected void performRuntime(OperationContext context, ModelNode operation, ModelNode model,
            ServiceVerificationHandler verificationHandler, List<ServiceController<?>> newControllers)
            throws OperationFailedException {
    	final ServiceTarget serviceTarget = context.getServiceTarget();
        final String jndiName = PathAddress.pathAddress(operation.get(ModelDescriptionConstants.ADDRESS)).getLastElement().getValue();

        final AEjbClientService service = new AEjbClientService();
        final ServiceName serviceName = SERVICE_NAME_BASE.append(jndiName);
        final ServiceBuilder<?> aejbClientBuilder = serviceTarget.addService(serviceName, service);
        final ManagedReferenceFactory valueManagedReferenceFactory = new ManagedReferenceFactory() {

            @Override
            public ManagedReference getReference() {
                return new ValueManagedReference(new ImmediateValue<Object>(service.getValue()));
            }
        };
        final ContextNames.BindInfo bindInfo = ContextNames.bindInfoFor(jndiName);
        final BinderService binderService = new BinderService(bindInfo.getBindName());
        final ServiceBuilder<?> binderBuilder = serviceTarget
                .addService(bindInfo.getBinderServiceName(), binderService)
                .addInjection(binderService.getManagedObjectInjector(), valueManagedReferenceFactory)
                .addDependency(bindInfo.getParentContextServiceName(), ServiceBasedNamingStore.class, binderService.getNamingStoreInjector()).addListener(new AbstractServiceListener<Object>() {
                    public void transition(final ServiceController<? extends Object> controller, final ServiceController.Transition transition) {
                        switch (transition) {
                            case STARTING_to_UP: {
                                AEjbLogger.ROOT_LOGGER.boundClientService(jndiName);
                                break;
                            }
                            case START_REQUESTED_to_DOWN: {
                            	AEjbLogger.ROOT_LOGGER.unboundClientService(jndiName);
                                break;
                            }
                            case REMOVING_to_REMOVED: {
                            	AEjbLogger.ROOT_LOGGER.removeClientService(jndiName);
                                break;
                            }
                        }
                    }
                });
        
        aejbClientBuilder.setInitialMode(ServiceController.Mode.ACTIVE).addListener(verificationHandler);
		binderBuilder.setInitialMode(ServiceController.Mode.ACTIVE).addListener(verificationHandler);
		newControllers.add(aejbClientBuilder.install());
		newControllers.add(binderBuilder.install());
    }
}
