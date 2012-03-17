package org.nju.artemis.aejb.deployment.processors;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.aejb.Adaptive;

import org.jboss.as.ee.component.ComponentDescription;
import org.jboss.as.ee.component.EEModuleDescription;
import org.jboss.as.ejb3.component.session.SessionBeanComponentDescription;
import org.jboss.as.ejb3.component.session.SessionBeanComponentDescription.SessionBeanType;
import org.jboss.as.ejb3.deployment.EjbDeploymentMarker;
import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.as.server.deployment.Phase;
import org.jboss.as.server.deployment.annotation.CompositeIndex;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.logging.Logger;
import org.nju.artemis.aejb.component.AEjbUtilities;
import org.nju.artemis.aejb.component.AcContainer;

/**
 * Processes @Adaptive annotation first
 * 
 * @author <a href="wangjue1199@gmail.com">Jason</a>
 */
public class AnnotatedAEJBComponentDescriptionProcessor implements DeploymentUnitProcessor {

	Logger log = Logger.getLogger(AnnotatedAEJBComponentDescriptionProcessor.class);

	private static final DotName ADAPTIVE_ANNOTATION = DotName.createSimple(Adaptive.class.getName());
	
	public static final Phase PHASE = Phase.PARSE;
	// AEJBs must created after described EJBs
	public static final int PRIORITY = 0x1210;
	
	public String unitName;

	@Override
	public void deploy(DeploymentPhaseContext phaseContext)	throws DeploymentUnitProcessingException {
		// get hold of the deployment unit.
        final DeploymentUnit deploymentUnit = phaseContext.getDeploymentUnit();
        
        // First, contains EJBs
		if(!EjbDeploymentMarker.isEjbDeployment(deploymentUnit))
			return;
		// Second, contains annotations
		final CompositeIndex compositeIndex = deploymentUnit.getAttachment(Attachments.COMPOSITE_ANNOTATION_INDEX);
		if (compositeIndex == null)
			return;
		// Third, contains @Adaptive annotations
		final List<AnnotationInstance> adpAnnotations = compositeIndex.getAnnotations(ADAPTIVE_ANNOTATION);
		if(adpAnnotations.isEmpty())
			return;

		final Set<String> aejbClassInfo = getAEJBClassInfo(adpAnnotations);
		Map<String,AcContainer> aejbInfo = new HashMap<String,AcContainer>();
		this.unitName = deploymentUnit.getName();
		final EEModuleDescription moduleDescription = deploymentUnit.getAttachment(org.jboss.as.ee.component.Attachments.EE_MODULE_DESCRIPTION);
		for(ComponentDescription beanDescription:moduleDescription.getComponentDescriptions()){
			// Forth, it is a session bean
			if(!(beanDescription instanceof SessionBeanComponentDescription))
				continue;
			String beanClass = beanDescription.getComponentName();
			if(!aejbClassInfo.contains(beanClass))
				continue;
			
			SessionBeanComponentDescription sessionBeanDescription = (SessionBeanComponentDescription) beanDescription;
			String appName = sessionBeanDescription.getApplicationName();
			String moduleName = sessionBeanDescription.getModuleName();
			String beanName = sessionBeanDescription.getEJBName();
			String distinctName = "";
			SessionBeanType beanType = sessionBeanDescription.getSessionBeanType();
			
			AcContainer accon = new AcContainer(appName, moduleName, beanName, distinctName, beanType);
			AEjbUtilities.registerAEJB(unitName, accon);
			aejbInfo.put(beanClass, accon);
		}
		
		deploymentUnit.putAttachment(org.nju.artemis.aejb.deployment.Attachments.AEJB_INFO, aejbInfo);
	}

	@Override
	public void undeploy(DeploymentUnit deploymentUnit) {
		AEjbUtilities.removeDeploymentUnit(deploymentUnit.getName());
	}

	private Set<String> getAEJBClassInfo(List<AnnotationInstance> adpAnnotations){
		Set<String> aejbClassInfo = new HashSet<String>();
		for (final AnnotationInstance adpAnnotation : adpAnnotations) {
            final AnnotationTarget target = adpAnnotation.target();
            if (!(target instanceof ClassInfo)) {
                // Let's just WARN and move on. No need to throw an error
                log.warn(adpAnnotation.name() + " annotation is expected to be applied on class level. " + target + " is not a class");
                continue;
            }
            aejbClassInfo.add(((ClassInfo) target).name().local());
		}
		return aejbClassInfo;
	}
}
