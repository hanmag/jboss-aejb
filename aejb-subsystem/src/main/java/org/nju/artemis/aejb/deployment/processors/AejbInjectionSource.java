package org.nju.artemis.aejb.deployment.processors;

import static org.jboss.as.ee.component.Attachments.EE_APPLICATION_DESCRIPTION;

import java.util.HashSet;
import java.util.Set;

import org.jboss.as.ee.component.EEApplicationDescription;
import org.jboss.as.ee.component.EEModuleDescription;
import org.jboss.as.ee.component.InjectionSource;
import org.jboss.as.ee.component.ViewDescription;
import org.jboss.as.ejb3.EjbMessages;
import org.jboss.as.ejb3.component.EJBComponentDescription;
import org.jboss.as.ejb3.component.EJBViewDescription;
import org.jboss.as.naming.ManagedReferenceFactory;
import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.module.ResourceRoot;
import org.jboss.logging.Logger;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceName;
import org.nju.artemis.aejb.component.AcContainer;
import org.nju.artemis.aejb.component.ContainerManagedReferenceFactory;

/**
 * @author <a href="wangjue1199@gmail.com">Jason</a>
 */
public class AejbInjectionSource extends InjectionSource {
	Logger log = Logger.getLogger(AejbInjectionSource.class);

	private final String lookup;
	private final String beanName;
    private final String typeName;
    private final String bindingName;
    private final DeploymentUnit deploymentUnit;
    private final AcContainer container;
    private volatile String error = null;
    private volatile ServiceName resolvedViewName;
    ContainerManagedReferenceFactory containerFactory;
    private volatile boolean resolved = false;

    public AejbInjectionSource(final String lookup, final String beanName, final String typeName, final String bindingName, final DeploymentUnit deploymentUnit, AcContainer container) {
    	this.beanName = beanName;
        this.typeName = typeName;
        this.bindingName = bindingName;
        this.deploymentUnit = deploymentUnit;
        this.lookup = lookup;
        this.container = container;
    }

    public AejbInjectionSource(final String beanName, final String typeName, final String bindingName, final DeploymentUnit deploymentUnit, AcContainer container) {
        this.bindingName = bindingName;
        this.deploymentUnit = deploymentUnit;
        this.beanName = beanName;
        this.typeName = typeName;
        this.lookup = null;
        this.container = container;
    }
    
	@Override
	public void getResourceValue(ResolutionContext resolutionContext, ServiceBuilder<?> serviceBuilder,	DeploymentPhaseContext phaseContext, Injector<ManagedReferenceFactory> injector) throws DeploymentUnitProcessingException {
		resolve();
		if (error != null) {
            throw new DeploymentUnitProcessingException(error);
        }
		if (containerFactory != null) {
            injector.inject(containerFactory);
        }
	}

	/**
     * Checks if this aejb injection has been resolved yet, and if not resolves it.
     */
    private void resolve() {
        if (!resolved) {
            synchronized (this) {
                if (!resolved) {
                	// Gets injected ejb views
                    final Set<ViewDescription> views = getViews();
                    final Set<EJBViewDescription> ejbsForViewName = new HashSet<EJBViewDescription>();
                    
                    for (final ViewDescription view : views) {
                        if (view instanceof EJBViewDescription) {
                            ejbsForViewName.add((EJBViewDescription) view);
                        }
                    }
                    
                    if (ejbsForViewName.isEmpty()) {
                        if (beanName == null) {
                            error = EjbMessages.MESSAGES.ejbNotFound(typeName, bindingName);
                        } else {
                            error = EjbMessages.MESSAGES.ejbNotFound(typeName, beanName, bindingName);
                        }
                    } else if (ejbsForViewName.size() > 1) {
                        if (beanName == null) {
                            error = EjbMessages.MESSAGES.moreThanOneEjbFound(typeName, bindingName, ejbsForViewName);
                        } else {
                            error = EjbMessages.MESSAGES.moreThanOneEjbFound(typeName, beanName, bindingName, ejbsForViewName);
                        }
                        error = "More than 1 component found for type '" + typeName + "' and bean name " + beanName + " for binding " + bindingName;
                    } else {
                        final EJBViewDescription description = ejbsForViewName.iterator().next();
                        log.debug("Checks if this aejb injection has been resolved yet, and if not resolves it.");
                        final EJBComponentDescription componentDescription = (EJBComponentDescription) description.getComponentDescription();
                        final EEModuleDescription moduleDescription = componentDescription.getModuleDescription();
                        final String earApplicationName = moduleDescription.getEarApplicationName();
                        containerFactory = new ContainerManagedReferenceFactory(earApplicationName, moduleDescription.getModuleName(), moduleDescription.getDistinctName(), componentDescription.getComponentName(), description.getViewClassName(), componentDescription.isStateful(), container);
                        final ServiceName serviceName = description.getServiceName();
                        resolvedViewName = serviceName;
                    }
                    resolved = true;
                }
            }
        }
    }
    
    private Set<ViewDescription> getViews() {
        final EEApplicationDescription applicationDescription = deploymentUnit.getAttachment(EE_APPLICATION_DESCRIPTION);
        final ResourceRoot deploymentRoot = deploymentUnit.getAttachment(Attachments.DEPLOYMENT_ROOT);
        final Set<ViewDescription> componentsForViewName;
        if (beanName != null) {
            componentsForViewName = applicationDescription.getComponents(beanName, typeName, deploymentRoot.getRoot());
        } else {
            componentsForViewName = applicationDescription.getComponentsForViewName(typeName, deploymentRoot.getRoot());
        }
        return componentsForViewName;
    }

    public boolean equals(Object o) {
        if (this == o)
            return true;

        if (!(o instanceof AejbInjectionSource))
            return false;

        resolve();

        if (error != null) {
            //we can't do a real equals comparison in this case, so just return false
            return false;
        }

        final AejbInjectionSource other = (AejbInjectionSource) o;
        return eq(typeName, other.typeName) && eq(resolvedViewName, other.resolvedViewName);
    }

    public int hashCode() {
        return typeName.hashCode();
    }

    private static boolean eq(Object a, Object b) {
        return a == b || (a != null && a.equals(b));
    }
}
