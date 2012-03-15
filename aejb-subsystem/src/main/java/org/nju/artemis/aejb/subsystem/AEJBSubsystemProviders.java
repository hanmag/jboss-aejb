package org.nju.artemis.aejb.subsystem;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.CHILDREN;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.DESCRIPTION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.HEAD_COMMENT_ALLOWED;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.MODEL_DESCRIPTION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.NAMESPACE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.TAIL_COMMENT_ALLOWED;

import java.util.Locale;

import org.jboss.as.controller.descriptions.DescriptionProvider;
import org.jboss.dmr.ModelNode;

/**
 * Contains the description providers. The description providers are what print out the
 * information when you execute the {@code read-resource-description} operation.
 * 
 * @author <a href="wangjue1199@gmail.com">Jason</a>
 */
class AEJBSubsystemProviders {

	/**
     * Used to create the description of the subsystem
     */
    public static DescriptionProvider SUBSYSTEM = new DescriptionProvider() {
        public ModelNode getModelDescription(Locale locale) {
            //The locale is passed in so you can internationalize the strings used in the descriptions

            final ModelNode subsystem = new ModelNode();
            //Change the description
            subsystem.get(DESCRIPTION).set("This subsystem makes deployments adaptive in runtime");
            subsystem.get(HEAD_COMMENT_ALLOWED).set(true);
            subsystem.get(TAIL_COMMENT_ALLOWED).set(true);
            subsystem.get(NAMESPACE).set(AEJBSubsystemExtension.NAMESPACE);

            //Add information about the 'type' child
            subsystem.get(CHILDREN, "jndi-name", DESCRIPTION).set("Define client service");
            subsystem.get(CHILDREN, "jndi-name", MODEL_DESCRIPTION);

            return subsystem;
        }
    };
    
    /**
     * Used to create the description of the subsystem add method
     */
    public static DescriptionProvider SUBSYSTEM_ADD = new DescriptionProvider() {
        public ModelNode getModelDescription(Locale locale) {
            //The locale is passed in so you can internationalize the strings used in the descriptions

            final ModelNode subsystem = new ModelNode();
            subsystem.get(DESCRIPTION).set("Adds the aejb subsystem");

            return subsystem;
        }
    };
    
    /**
     * Used to create the description of the {@code type} child
     */
    public static DescriptionProvider JNDI_CHILD = new DescriptionProvider() {
        @Override
        public ModelNode getModelDescription(Locale locale) {
            ModelNode node = new ModelNode();
            node.get(DESCRIPTION).set("Contains information about aejb client jndi name");
            return node;
        }
    };
}
