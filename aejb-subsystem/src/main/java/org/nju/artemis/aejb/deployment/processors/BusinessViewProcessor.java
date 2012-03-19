package org.nju.artemis.aejb.deployment.processors;

import static org.jboss.as.ee.component.Attachments.EE_MODULE_DESCRIPTION;

import java.util.Collection;
import java.util.Map;

import javax.ejb.Local;
import javax.ejb.Remote;

import org.jboss.as.ee.component.ComponentDescription;
import org.jboss.as.ee.component.EEModuleDescription;
import org.jboss.as.ee.component.ViewDescription;
import org.jboss.as.ejb3.component.EJBViewDescription;
import org.jboss.as.ejb3.component.MethodIntf;
import org.jboss.as.ejb3.component.session.SessionBeanComponentDescription;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.as.server.deployment.Phase;
import org.jboss.logging.Logger;
import org.jboss.modules.Module;
import org.nju.artemis.aejb.component.AcContainer;

/**
 * Processes {@link Local @Local} and {@link Remote @Remote} annotation of a session bean and sets up the {@link AcContainer}
 * out of it.
 * <p/>
 * 
 * @author <a href="mailto:wangjue1199@gmail.com">Jason</a>
 */
public class BusinessViewProcessor implements DeploymentUnitProcessor {
	private static final Logger logger = Logger.getLogger(BusinessViewProcessor.class);
	
	public static final Phase POST_MODULE = Phase.POST_MODULE;
	public static final int PRIORITY = 0x0440;
	
	@Override
	public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
		logger.info("BusinessViewProcessor ===> start deploy phaseContext.");
		final DeploymentUnit deploymentUnit = phaseContext.getDeploymentUnit();
		final EEModuleDescription eeModuleDescription = deploymentUnit.getAttachment(EE_MODULE_DESCRIPTION);
		final Map<String,AcContainer> aejbInfo = deploymentUnit.getAttachment(org.nju.artemis.aejb.deployment.Attachments.AEJB_INFO);
        final Collection<ComponentDescription> componentDescriptions = eeModuleDescription.getComponentDescriptions();
        final Module module = deploymentUnit.getAttachment(org.jboss.as.server.deployment.Attachments.MODULE);
        if(aejbInfo == null || module == null) {
            return;
        }
        final ClassLoader moduleClassLoader = module.getClassLoader();
        if (componentDescriptions != null) {
            for (ComponentDescription componentDescription : componentDescriptions) {
                if (componentDescription instanceof SessionBeanComponentDescription == false || aejbInfo.containsKey(componentDescription.getComponentName()) == false) {
                    continue;
                }
                for(ViewDescription view:componentDescription.getViews()) {
                	if(view instanceof EJBViewDescription == false)
                		continue;
                	final Class<?> viewClass;
                	try {
                		viewClass = moduleClassLoader.loadClass(view.getViewClassName());
					} catch (ClassNotFoundException e) {
						throw new DeploymentUnitProcessingException("View Class: " + view.getViewClassName() + " Not Found.", e);
					}
                	logger.info("view = " + view.getViewClassName());
                	final MethodIntf intf =  ((EJBViewDescription) view).getMethodIntf();
                	final AcContainer container = aejbInfo.get(componentDescription.getComponentName());
					if (intf == MethodIntf.LOCAL) {
						container.setLocalView(viewClass);
					} else if (intf == MethodIntf.REMOTE) {
						container.setRemoteView(viewClass);
					}
                }
            }
        }
        logger.info("BusinessViewProcessor ===> stop deploy phaseContext.");
	}

	@Override
	public void undeploy(DeploymentUnit context) {
	}

}
