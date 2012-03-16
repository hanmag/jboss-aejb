/**
 * 
 */
package org.nju.artemis.aejb.deployment.processors;

import java.util.List;

import javax.aejb.TransactionTrigger;

/**
 * 
 * @author <a href="mailto:xiachen1987@gmail.com">Xia chen</a>
 * @author <a href="mailto:wangjue1199@gmail.com">Jason</a>
 */
public interface TransactionManager extends TransactionTrigger{

	String toString();

	/**
	 * Returns the number of times this transaction has executed so far.
	 * 
	 * @return
	 */
	int getRunNumber();

	String getName();

	/**
	 * Returns whether this transaction is active or not
	 * 
	 * @return
	 */
	boolean isActive();

	/**
	 * Get the name of all ports that have been used in this transaction and
	 * might still be required in the future for this transaction.
	 * 
	 * @return Get the name of all ports that have been used in this transaction
	 *         and might still be required in the future for this transaction.
	 */
	List<String> getAffectedPorts();
	
	/**
	 * Get the name of all ports that have been used in this transaction and
	 * might still be required in the future for this transaction.
	 * 
	 * @param The transaction id
	 * 
	 * @return Get the name of all ports that have been used in this transaction
	 *         and might still be required in the future for this transaction.
	 */
	List<String> getAffectedPorts(String objectId);
	
	/**
	 * Returns the name of all ports that are used at some point throughout this
	 * transaction
	 * 
	 * @return All ports that are used in this transaction (in general, not
	 *         specifically in an active instance)
	 */
	String[] getInvolvedPorts();

	String getTransactionmethodName();

	void createTransaction(String objectId);

	void destroyTransaction(String objectId);
	
	/**
	 * Get the name of all ports that have been used in this transaction.
	 * 
	 * @param The transaction id
	 * 
	 * @return Get the name of all ports that have been used in this transaction.
	 */
	List<String> getPassedPorts(String objectId);
	
	/**
	 * Get the name of all ports that might be required in the future for this transaction.
	 * 
	 * @param The transaction id
	 * 
	 * @return Get the name of all ports that might be required in the future for this transaction.
	 */
	List<String> getFuturePorts(String objectId);
}