package org.nju.artemis.aejb.deployment.transaction;

import java.util.List;


/**
 * This class implements a statemachine for a transaction.
 * 
 * @author <a href="mailto:xiachen1987@gmail.com">Xia chen</a>
 * @author <a href="mailto:wangjue1199@gmail.com">Jason</a>
 */

public class Transaction {
	private State $currentState;

	/*
	 * (non-Javadoc)
	 * 
	 * @see artemis.transaction.Tran#init()
	 */
	public void init(State initialState) {
		$currentState = initialState;
	}
	
	public State getCurrentState() {
		return $currentState;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see artemis.transaction.Tran#trigger(java.lang.String)
	 */
	public void trigger(String eventName) {
		if ($currentState != null) {
			State next = $currentState.getStateAfterEvent(eventName);
			if (next != null) {
				$currentState = next;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see artemis.transaction.Tran#getAffectedPorts()
	 */
	public List<String> getAffectedPorts() {
		return $currentState.getAffectedPorts();
	}
	
	public List<String> getPassedPorts() {
		return $currentState.getPassedPorts();
	}

	public List<String> getFuturePorts() {
		return $currentState.getFuturePorts();
	}
}
