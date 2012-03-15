package org.nju.artemis.aejb.subsystem;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.DESCRIBE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import org.jboss.as.controller.Extension;
import org.jboss.as.controller.ExtensionContext;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.OperationStepHandler;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.SubsystemRegistration;
import org.jboss.as.controller.descriptions.DescriptionProvider;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.as.controller.descriptions.common.CommonDescriptions;
import org.jboss.as.controller.parsing.ExtensionParsingContext;
import org.jboss.as.controller.parsing.ParseUtils;
import org.jboss.as.controller.persistence.SubsystemMarshallingContext;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.as.controller.registry.OperationEntry;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.Property;
import org.jboss.logging.Logger;
import org.jboss.staxmapper.XMLElementReader;
import org.jboss.staxmapper.XMLElementWriter;
import org.jboss.staxmapper.XMLExtendedStreamReader;
import org.jboss.staxmapper.XMLExtendedStreamWriter;

/**
 * Extension that provides the AEJB subsystem.
 * 
 * @author <a href="wangjue1199@gmail.com">Jason</a>
 */
public class AEJBSubsystemExtension implements Extension {
	Logger log = Logger.getLogger(AEJBSubsystemExtension.class);
	/** The name space used for the {@code subsystem} element */
    public static final String NAMESPACE = "urn:org.nju.artemis.aejb:1.0";

    /** The name of our subsystem within the model. */
    public static final String SUBSYSTEM_NAME = "aejb";

    /** The parser used for parsing our subsystem */
    private final AEJBSubsystemParser parser = new AEJBSubsystemParser();
    
	public void initialize(ExtensionContext context) {
		final SubsystemRegistration subsystem = context.registerSubsystem(SUBSYSTEM_NAME);
        final ManagementResourceRegistration registration = subsystem.registerSubsystemModel(AEJBSubsystemProviders.SUBSYSTEM);
        //We always need to add an 'add' operation
        registration.registerOperationHandler(ADD, AEJBSubsystemAdd.INSTANCE, AEJBSubsystemProviders.SUBSYSTEM_ADD, false);
        //We always need to add a 'describe' operation
        registration.registerOperationHandler(DESCRIBE, AEJBSubsystemDescribeHandler.INSTANCE, AEJBSubsystemDescribeHandler.INSTANCE, false, OperationEntry.EntryType.PRIVATE);
        //Add the type child
        ManagementResourceRegistration jndiChild = registration.registerSubModel(PathElement.pathElement("jndi-name"), AEJBSubsystemProviders.JNDI_CHILD);
        jndiChild.registerOperationHandler(ModelDescriptionConstants.ADD, ClientJndiBindingHandler.INSTANCE, ClientJndiBindingHandler.INSTANCE);
        
        subsystem.registerXMLElementWriter(parser);
	}

	@SuppressWarnings("deprecation")
	public void initializeParsers(ExtensionParsingContext context) {
		context.setSubsystemXmlMapping(NAMESPACE, parser);
	}

	private static ModelNode createAEJBSubsystemOperation() {
        final ModelNode subsystem = new ModelNode();
        subsystem.get(OP).set(ADD);
        subsystem.get(OP_ADDR).add(SUBSYSTEM, SUBSYSTEM_NAME);
        return subsystem;
    }
	
	/**
     * The subsystem parser, which uses stax to read and write to and from xml
     */
    private static class AEJBSubsystemParser implements XMLStreamConstants, XMLElementReader<List<ModelNode>>, XMLElementWriter<SubsystemMarshallingContext> {
        /** {@inheritDoc} */
        @Override
        public void readElement(XMLExtendedStreamReader reader, List<ModelNode> list) throws XMLStreamException {
            // Require no attributes
            ParseUtils.requireNoAttributes(reader);
            //Add the main subsystem 'add' operation
            list.add(createAEJBSubsystemOperation());

            //Read the children
            while (reader.hasNext() && reader.nextTag() != END_ELEMENT) {
                if (!reader.getLocalName().equals("client-service")) {
                    throw ParseUtils.unexpectedElement(reader);
                }
                
				if (reader.getLocalName().equals("client-service"))
					readClientService(reader, list);
                
                while (reader.hasNext() && reader.nextTag() != END_ELEMENT) {
//                    if (reader.isStartElement()/* && reader.getLocalName().equals("client-service")*/) {
//                    	readClientService(reader, list);
//                    }
                }
            }
        }

        private void readClientService(XMLExtendedStreamReader reader, List<ModelNode> list) throws XMLStreamException {
        	String jndiName = null;
            for (int i = 0 ; i < reader.getAttributeCount() ; i++) {
                String attr = reader.getAttributeLocalName(i);
                if (attr.equals("jndi-name")){
                	jndiName = reader.getAttributeValue(i);
                } else {
                    throw ParseUtils.unexpectedAttribute(reader, i);
                }
            }
            ParseUtils.requireNoContent(reader);
            if (jndiName == null) {
                throw ParseUtils.missingRequiredElement(reader, Collections.singleton("jndi-name"));
            }

            //Add the 'add' operation for each 'type' child
            ModelNode addType = new ModelNode();
            addType.get(OP).set(ModelDescriptionConstants.ADD);
            PathAddress addr = PathAddress.pathAddress(PathElement.pathElement(SUBSYSTEM, SUBSYSTEM_NAME), PathElement.pathElement("jndi-name", jndiName));
            addType.get(OP_ADDR).set(addr.toModelNode());
            list.add(addType);
        }


        /** {@inheritDoc} */
        @Override
        public void writeContent(final XMLExtendedStreamWriter writer, final SubsystemMarshallingContext context) throws XMLStreamException {

            //Write out the main subsystem element
            context.startSubsystemElement(AEJBSubsystemExtension.NAMESPACE, false);

            writer.writeStartElement("client-service");

            ModelNode node = context.getModelNode();
            ModelNode jndiName = node.get("jndi-name");
            for (Property property : jndiName.asPropertyList()) {

                //write each child element to xml
                writer.writeAttribute("jndi-name", property.getName());
//                ModelNode entry = property.getValue();
//                if (entry.hasDefined("tick")) {
//                    writer.writeAttribute("tick", entry.get("tick").asString());
//                }
            }

            //End deployment-types
            writer.writeEndElement();
            //End subsystem
            writer.writeEndElement();
        }
    }
    
    /**
     * Recreate the steps to put the subsystem in the same state it was in.
     * This is used in domain mode to query the profile being used, in order to
     * get the steps needed to create the servers
     */
    private static class AEJBSubsystemDescribeHandler implements OperationStepHandler, DescriptionProvider {
        static final AEJBSubsystemDescribeHandler INSTANCE = new AEJBSubsystemDescribeHandler();

        public void execute(OperationContext context, ModelNode operation) throws OperationFailedException {
            //Add the main operation
            context.getResult().add(createAEJBSubsystemOperation());

            //Add the operations to create each child

            ModelNode node = context.readModel(PathAddress.EMPTY_ADDRESS);
            for (Property property : node.get("jndi-name").asPropertyList()) {

                ModelNode addType = new ModelNode();
                addType.get(OP).set(ModelDescriptionConstants.ADD);
                PathAddress addr = PathAddress.pathAddress(PathElement.pathElement(SUBSYSTEM, SUBSYSTEM_NAME), PathElement.pathElement("jndi-name", property.getName()));
                addType.get(OP_ADDR).set(addr.toModelNode());
//                if (property.getValue().hasDefined("tick")) {
//                    addType.get("tick").set(property.getValue().get("tick").asLong());
//                }
                context.getResult().add(addType);
            }
            context.completeStep();
        }

        @Override
        public ModelNode getModelDescription(Locale locale) {
            return CommonDescriptions.getSubsystemDescribeOperation(locale);
        }
    }
}
