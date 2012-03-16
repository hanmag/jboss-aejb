package org.nju.artemis.aejb.deployment.transaction;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * This class implements a state in a statemachine.
 *
 * @author <a href="mailto:xiachen1987@gmail.com">Xia chen</a>
 * @author <a href="mailto:wangjue1199@gmail.com">Jason</a>
 */

public class TransactionState {

   private Map<String, TransactionState> $nextStates = new Hashtable<String, TransactionState>();   // maps: key->State

   /**
    * Passed ports
    */
   private List<String> $passedPorts;
   
   /**
    * Future ports
    */
   private List<String> $futurePorts;
   
   /**
    * Create a state for a state machine
    * @param passedPorts
    *    A list containing the names of ports that:
    *        (a) Have been used in the transaction so far
    * @param futurePorts
    *    A list containing the names of ports that:
    *        (b) Might be used to complete the transaction
    */
   public TransactionState(String[] passedPorts, String[] futurePorts) {
	   if(passedPorts != null) {
		   $passedPorts = new ArrayList<String>();
		   for(String passed:passedPorts)
			   $passedPorts.add(passed);
	   }
	   if(futurePorts != null) {
		   $futurePorts = new ArrayList<String>();
		   for(String future:futurePorts)
			   $futurePorts.add(future);
	   }
   }

   /**
    * Sets the next state our statemachine evolves to after an event with a given name takes place.
    * @param eventName
    *    The unique name describing the event
    * @param next
    *    The destination-tate we go to after the event took place.
    */
   public void setNextState(String eventName, TransactionState next) {
      $nextStates.put(eventName, next);
   }

   /**
    * Gets the state of our statemachine after a given event takes place
    * @param eventName
    *    The unique name describing the event
    * @return
    *    The new state of our statemachine
    */
   public TransactionState getStateAfterEvent(String eventName) {
      TransactionState s = (TransactionState) $nextStates.get(eventName);
      return s;
   }

   /**
    * Get all ports used in the past which may still be required to complete the transaction.
    * @return
    *    The ports which have been used and might be used in the future.
    */
	public List<String> getAffectedPorts() {
		List<String> affectedPorts = null;
		if ($passedPorts != null && $futurePorts != null) {
			affectedPorts = new ArrayList<String>($passedPorts);
			affectedPorts.retainAll($futurePorts);
		}
		return affectedPorts;
	}

	public List<String> getPassedPorts() {
		return $passedPorts;
	}

	public List<String> getFuturePorts() {
		return $futurePorts;
	}
}
