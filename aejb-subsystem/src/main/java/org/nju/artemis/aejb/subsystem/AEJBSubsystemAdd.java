package org.nju.artemis.aejb.subsystem;

import java.util.List;

import org.jboss.as.controller.AbstractBoottimeAddStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.ServiceVerificationHandler;
import org.jboss.as.server.AbstractDeploymentChainStep;
import org.jboss.as.server.DeploymentProcessorTarget;
import org.jboss.dmr.ModelNode;
import org.jboss.logging.Logger;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceController.Mode;
import org.jboss.msc.service.ServiceName;
import org.nju.artemis.aejb.AEjbLogger;
import org.nju.artemis.aejb.component.AEjbUtilities;
import org.nju.artemis.aejb.deployment.processors.AEjbInjectionAnnotationProcessor;
import org.nju.artemis.aejb.deployment.processors.AnnotatedAEJBComponentDescriptionProcessor;
import org.nju.artemis.aejb.deployment.processors.SubsystemDeploymentProcessor;
import org.nju.artemis.aejb.deployment.processors.TransactionAnnotationProcessor;
import org.nju.artemis.aejb.evolution.DuService;
import org.nju.artemis.aejb.management.client.AEjbClientImpl;

/**
 * Handler responsible for adding the subsystem resource to the model
 * 
 * @author <a href="wangjue1199@gmail.com">Jason</a>
 */
public class AEJBSubsystemAdd extends AbstractBoottimeAddStepHandler {
	Logger log = Logger.getLogger(AEJBSubsystemAdd.class);
	
	static final AEJBSubsystemAdd INSTANCE = new AEJBSubsystemAdd();

    static final String JAR_FILE_EXTENSION = "jar";
    
    private AEJBSubsystemAdd() {
    }
    
	@Override
	protected void performBoottime(OperationContext context, ModelNode operation, ModelNode model,
			ServiceVerificationHandler verificationHandler, List<ServiceController<?>> newControllers)
			throws OperationFailedException {
		AEjbLogger.ROOT_LOGGER.activatingAEjbSubsystem();
		//Add deployment processors here
        //Remove this if you don't need to hook into the deployers, or you can add as many as you like
        //see SubDeploymentProcessor for explanation of the phases
        context.addStep(new AbstractDeploymentChainStep() {
            public void execute(DeploymentProcessorTarget processorTarget) {
                processorTarget.addDeploymentProcessor(SubsystemDeploymentProcessor.PHASE, SubsystemDeploymentProcessor.PRIORITY, new SubsystemDeploymentProcessor());
                processorTarget.addDeploymentProcessor(AnnotatedAEJBComponentDescriptionProcessor.PHASE, AnnotatedAEJBComponentDescriptionProcessor.PRIORITY, new AnnotatedAEJBComponentDescriptionProcessor());
                processorTarget.addDeploymentProcessor(AEjbInjectionAnnotationProcessor.PHASE, AEjbInjectionAnnotationProcessor.PRIORITY, new AEjbInjectionAnnotationProcessor());
                processorTarget.addDeploymentProcessor(TransactionAnnotationProcessor.PHASE, TransactionAnnotationProcessor.PRIORITY, new TransactionAnnotationProcessor());
            }
        }, OperationContext.Stage.RUNTIME);
        
        TrackerService service = new TrackerService(JAR_FILE_EXTENSION, 60000/*model.get("tick").asLong()*/);
        ServiceName name = TrackerService.createServiceName(JAR_FILE_EXTENSION);
        ServiceController<TrackerService> controller = context.getServiceTarget()
                .addService(name, service).addListener(verificationHandler)
                .setInitialMode(Mode.ACTIVE).install();
        newControllers.add(controller);
        
        //Register AEjbUtilities
        final AEjbUtilities aejbUtilities = new AEjbUtilities();
        newControllers.add(context.getServiceTarget().addService(AEjbUtilities.SERVICE_NAME, aejbUtilities)
        		.setInitialMode(ServiceController.Mode.ACTIVE)
                .addListener(verificationHandler)
                .install());
        
        //Create DuService
        final DuService duService = new DuService();
        newControllers.add(context.getServiceTarget().addService(DuService.SERVICE_NAME, duService)
                .addInjection(duService.getAejbClientValue(), AEjbClientImpl.INSTANCE)
                .addDependency(AEjbUtilities.SERVICE_NAME, AEjbUtilities.class, duService.getAejbUtilitiesValue())
        		.setInitialMode(ServiceController.Mode.PASSIVE)
                .addListener(verificationHandler)
                .install());
	}

	@Override
	protected void populateModel(ModelNode operation, ModelNode model) throws OperationFailedException {
		// Initialize the 'type' child node
		model.get("jndi-name").setEmptyObject();
	}
}
