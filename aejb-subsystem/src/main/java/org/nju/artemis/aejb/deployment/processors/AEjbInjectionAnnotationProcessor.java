package org.nju.artemis.aejb.deployment.processors;

import java.util.List;
import java.util.Map;

import javax.aejb.AEjb;

import org.jboss.as.ee.component.Attachments;
import org.jboss.as.ee.component.BindingConfiguration;
import org.jboss.as.ee.component.EEModuleClassDescription;
import org.jboss.as.ee.component.EEModuleDescription;
import org.jboss.as.ee.component.FieldInjectionTarget;
import org.jboss.as.ee.component.InjectionTarget;
import org.jboss.as.ee.component.ResourceInjectionConfiguration;
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
import org.jboss.jandex.FieldInfo;
import org.jboss.logging.Logger;
import org.nju.artemis.aejb.component.AcContainer;

/**
 * @author <a href="wangjue1199@gmail.com">Jason</a>
 */
public class AEjbInjectionAnnotationProcessor implements DeploymentUnitProcessor {
	Logger log = Logger.getLogger(AEjbInjectionAnnotationProcessor.class);
	private static final DotName AEJB_ANNOTATION = DotName.createSimple(AEjb.class.getName());
	
	public static final Phase PHASE = Phase.PARSE;
	// should be after all components are known
	public static final int PRIORITY = 0x3800;
	private AcContainer container;

	@Override
	public void deploy(DeploymentPhaseContext phaseContext)	throws DeploymentUnitProcessingException {
		log.debug("AEjbInjectionAnnotationProcessor ===> deploy phaseContext.");
		// get hold of the deployment unit.
        final DeploymentUnit deploymentUnit = phaseContext.getDeploymentUnit();
        final Map<String,AcContainer> aejbInfo = deploymentUnit.getAttachment(org.nju.artemis.aejb.deployment.Attachments.AEJB_INFO);
        final CompositeIndex index = deploymentUnit.getAttachment(org.jboss.as.server.deployment.Attachments.COMPOSITE_ANNOTATION_INDEX);
        final EEModuleDescription moduleDescription = deploymentUnit.getAttachment(Attachments.EE_MODULE_DESCRIPTION);
        final List<AnnotationInstance> aejbAnnotations = index.getAnnotations(AEJB_ANNOTATION);
        for (final AnnotationInstance aejbAnnotation : aejbAnnotations) {
            final AnnotationTarget target = aejbAnnotation.target();
            if (!(target instanceof FieldInfo)) {
                log.warn(aejbAnnotation.name() + " annotation is expected to be applied on field level. " + target + " is not a field");
                continue;
            }
            
            String beanName = ((FieldInfo) target).declaringClass().name().local();
            if(!aejbInfo.containsKey(beanName)) {
            	log.warn(aejbAnnotation.name() + " annotation is expected to be applied with @Adaptive. " + beanName + " is not an AEJB");
            	continue;
            }

            container = aejbInfo.get(beanName);
            final AEJBResourceWrapper annotationWrapper = new AEJBResourceWrapper(aejbAnnotation);
            processField(deploymentUnit, annotationWrapper, (FieldInfo) target, moduleDescription);
		}
	}
	
	private void processField(final DeploymentUnit deploymentUnit, final AEJBResourceWrapper annotation, final FieldInfo fieldInfo, final EEModuleDescription eeModuleDescription) {
        final String fieldName = fieldInfo.name();
        final String fieldType = fieldInfo.type().name().toString();
        final InjectionTarget targetDescription = new FieldInjectionTarget(fieldInfo.declaringClass().name().toString(), fieldName, fieldType);
        final String localContextName = isEmpty(annotation.name()) ? fieldInfo.declaringClass().name().toString() + "/" + fieldInfo.name() : annotation.name();
        final String beanInterfaceType = isEmpty(annotation.beanInterface()) || annotation.beanInterface().equals(Object.class.getName()) ? fieldType : annotation.beanInterface();
        process(deploymentUnit, beanInterfaceType, annotation.beanName(), annotation.lookup(), fieldInfo.declaringClass(), targetDescription, localContextName, eeModuleDescription);
    }
	
	private void process(final DeploymentUnit deploymentUnit, final String beanInterface, final String beanName, final String lookup, final ClassInfo classInfo, final InjectionTarget targetDescription, final String localContextName, final EEModuleDescription eeModuleDescription) {

        if (!isEmpty(lookup) && !isEmpty(beanName)) {
            log.debug("Both beanName = " + beanName + " and lookup = " + lookup + " have been specified in @AEJB annotation." +
                    " lookup will be given preference. Class: " + classInfo.name());
        }

        final EEModuleClassDescription classDescription = eeModuleDescription.addOrGetLocalClassDescription(classInfo.name().toString());

        final AejbInjectionSource aejbInjectionSource;
        //give preference to lookup
		if (!isEmpty(lookup)) {
			if (!isEmpty(beanName)) {
				aejbInjectionSource = new AejbInjectionSource(lookup, beanName, beanInterface, localContextName, deploymentUnit, container);
			} else {
				aejbInjectionSource = new AejbInjectionSource(lookup, null, beanInterface, localContextName, deploymentUnit, container);
			}
		} else {
			if (!isEmpty(beanName)) {
				aejbInjectionSource = new AejbInjectionSource(beanName, beanInterface, localContextName, deploymentUnit, container);
			} else {
				aejbInjectionSource = new AejbInjectionSource(null, beanInterface, localContextName, deploymentUnit, container);
			}
		}
        
        final ResourceInjectionConfiguration injectionConfiguration = targetDescription != null ?
                new ResourceInjectionConfiguration(targetDescription, aejbInjectionSource) : null;

        // Create the binding from whence our injection comes.
        final BindingConfiguration bindingConfiguration = new BindingConfiguration(localContextName, aejbInjectionSource);

        classDescription.getBindingConfigurations().add(bindingConfiguration);
        if (injectionConfiguration != null) {
            classDescription.addResourceInjection(injectionConfiguration);
        }
    }

	@Override
	public void undeploy(DeploymentUnit context) {
		// TODO Auto-generated method stub
	}
	
	private boolean isEmpty(final String string) {
        return string == null || string.isEmpty();
    }
	
	private class AEJBResourceWrapper {
        private final String name;
        private final String beanInterface;
        private final String beanName;
        private final String lookup;
        private final String description;

        private AEJBResourceWrapper(final AnnotationInstance annotation) {
            name = stringValueOrNull(annotation, "name");
            beanInterface = classValueOrNull(annotation, "beanInterface");
            beanName = stringValueOrNull(annotation, "beanName");
            String lookupValue = stringValueOrNull(annotation, "lookup");
            // if "lookup" isn't specified, then fallback on "mappedName". We treat "mappedName" the same as "lookup"
            if (isEmpty(lookupValue)) {
                lookupValue = stringValueOrNull(annotation, "mappedName");
            }
            this.lookup = lookupValue;
            description = stringValueOrNull(annotation, "description");
        }

        private String name() {
            return name;
        }

        private String beanInterface() {
            return beanInterface;
        }

        private String beanName() {
            return beanName;
        }

        private String lookup() {
            return lookup;
        }

        private String description() {
            return description;
        }

        private String stringValueOrNull(final AnnotationInstance annotation, final String attribute) {
            final AnnotationValue value = annotation.value(attribute);
            return value != null ? value.asString() : null;
        }

        private String classValueOrNull(final AnnotationInstance annotation, final String attribute) {
            final AnnotationValue value = annotation.value(attribute);
            return value != null ? value.asClass().name().toString() : null;
        }
    }

}
