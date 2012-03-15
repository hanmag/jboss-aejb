package org.nju.artemis.aejb;

import org.jboss.logging.BasicLogger;
import org.jboss.logging.LogMessage;
import org.jboss.logging.Logger;
import org.jboss.logging.Logger.Level;
import org.jboss.logging.Message;
import org.jboss.logging.MessageLogger;

/**
 * This module is using message IDs in the range 19999- . See http://community.jboss.org/docs/DOC-16810 for the full list of currently reserved
 * JBAS message id blocks.
 * 
 * @author <a href="wangjue1199@gmail.com">Jason</a>
 */
@MessageLogger(projectCode = "JBAS")
public interface AEjbLogger extends BasicLogger {

	/**
     * Default root level logger with the package name for he category.
     */
    AEjbLogger ROOT_LOGGER = Logger.getMessageLogger(AEjbLogger.class, AEjbLogger.class.getPackage().getName());
    
    /**
     * Logs a message indicating that the aejb subsystem is being activated
     */
    @LogMessage(level = Level.INFO)
    @Message(id = 20000, value = "Activating AEJB Subsystem")
    void activatingAEjbSubsystem();
    
    /**
     * Logs a message indicating that the aejb client service is bound
     */
    @LogMessage(level = Level.INFO)
    @Message(id = 20001, value = "Bound AEJB Client Service [%s]")
    void boundClientService(String jndi);
    
    /**
     * Logs a message indicating that the aejb client service is unbound
     */
    @LogMessage(level = Level.INFO)
    @Message(id = 20002, value = "Unbound AEJB Client Service [%s]")
    void unboundClientService(String jndi);
    
    /**
     * Logs a message indicating that the aejb client service is removed
     */
    @LogMessage(level = Level.INFO)
    @Message(id = 20003, value = "Remove AEJB Client Service [%s]")
    void removeClientService(String jndi);
}
