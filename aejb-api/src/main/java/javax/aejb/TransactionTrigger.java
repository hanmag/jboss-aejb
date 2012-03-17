package javax.aejb;

/**
 * @author <a href="mailto:wangjue1199@gmail.com">Jason</a>
 */
public interface TransactionTrigger {

	/**
	 * Trigger a given event. If the event corresponds with one of the
	 * exit-events of the state the current state is updated. Otherwise, nothing
	 * happens.
	 * 
	 * @param eventName
	 *            The name of the event being triggered
	 */
	void trigger(String objectId, String eventName);
	
	void trigger(String objectId);
}
