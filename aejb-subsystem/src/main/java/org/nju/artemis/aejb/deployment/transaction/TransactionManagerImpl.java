package org.nju.artemis.aejb.deployment.transaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.nju.artemis.aejb.deployment.processors.TransactionManager;

/**
 * This class implements a statemachine for a transaction.
 * 
 * @author <a href="mailto:xiachen1987@gmail.com">Xia chen</a>
 * @author <a href="mailto:wangjue1199@gmail.com">Jason</a>
 */

public class TransactionManagerImpl implements TransactionManager {
	//All states
	private TransactionState[] $states;
	//Transaction name
	private String $name;
	// the number of transaction has executed
	private int $runNumber;

	private Map<String, Transaction> $transactions;
	private String[] $portNames;
	private String $transactionmethod;

	public String getTransactionmethodName() {
		return $transactionmethod;
	}

	public TransactionManagerImpl(String name, String method, String[] portNames, TransactionState[] states, Map<String, String>[] nextStates) {
		$name = name;
		$states = states;
		$transactionmethod = method;
		$transactions = new HashMap<String,Transaction>();
		$portNames = portNames;
		final int index = $states.length-1;
		for (int i = 0; i < index; i++) {
			Map<String, String> table = nextStates[i];
			Iterator<Entry<String, String>> itrator = table.entrySet().iterator();
			while(itrator.hasNext()) {
				Entry<String, String> entry = itrator.next();
				String eventName = entry.getKey();
				String next = entry.getValue();
				int n = Integer.parseInt(next.substring(1));
				$states[i].setNextState(eventName, $states[n]);
			}
		}
	}

	public String getName() {
		return $name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see artemis.transaction.Tran#toString()
	 */
	public String toString() {
		return $name + "_" + getRunNumber();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see artemis.transaction.Tran#getRunNumber()
	 */
	public int getRunNumber() {
		return $runNumber;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see artemis.transaction.Tran#isActive()
	 */
	public boolean isActive() {
		return getRunNumber() != 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see artemis.transaction.Tran#trigger(java.lang.String)
	 */
	public void trigger(String objectId, String eventName) {
		synchronized ($transactions) {
			Transaction tran = $transactions.get(objectId);
			if (tran != null) {
				tran.trigger(eventName);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see artemis.transaction.Tran#getTranquilityPortNames()
	 */
	public List<String> getAffectedPorts() {
		List<String> affectedports = new ArrayList<String>();
		for (Transaction tran : $transactions.values()) {
			affectedports.addAll(tran.getAffectedPorts());
		}
		return affectedports;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see artemis.transaction.Tran#getInvolvedPorts()
	 */
	public String[] getInvolvedPorts() {
		return $portNames;
	}
	
	public void createTransaction(String objectId){
		Transaction transaction = new Transaction();
		transaction.init($states[0]);
		$runNumber++;
		$transactions.put(objectId, transaction);
	}

	public void destroyTransaction(String objectId){
		$transactions.remove(objectId);
		$runNumber++;
	}

	/**
	 * May be null
	 */
	@Override
	public List<String> getAffectedPorts(String objectId) {
		if($transactions.containsKey(objectId)) {
			Transaction transaction = $transactions.get(objectId);
			return transaction.getAffectedPorts();
		}
		return null;
	}

	@Override
	public List<String> getPassedPorts(String objectId) {
		if($transactions.containsKey(objectId)) {
			Transaction transaction = $transactions.get(objectId);
			return transaction.getPassedPorts();
		}
		return null;
	}

	@Override
	public List<String> getFuturePorts(String objectId) {
		if($transactions.containsKey(objectId)) {
			Transaction transaction = $transactions.get(objectId);
			return transaction.getFuturePorts();
		}
		return null;
	}
}
