/*
 * $Header: /cvs/cvsroot/vgr-soa-client/src/main/java/se/vgregion/client/objects/Transaction.java,v 1.1 2010/01/21 08:57:46 heka Exp $
 * $Revision: 1.1 $
 * $Date: 2010/01/21 08:57:46 $
 *
 * ====================================================================
 */
package se.vgregion.newkiv.objects;

import se.vgregion.ws.constants.Event;

/**
 * <Description goes here...>
 *
 * @author Henrik Karlsson
 * @version $Revision: 1.1 $, $Date: 2010/01/21 08:57:46 $
 */
public class Transaction {
	private final se.vgregion.ws.objects.Transaction transaction;

	/**
	 * @param transaction
	 */
	public Transaction(se.vgregion.ws.objects.Transaction transaction) {
		super();
		this.transaction = transaction;
	}

	/**
	 * @return
	 * @see se.vgregion.ws.objects.Transaction#getAttrName()
	 */
	public String getAttrName() {
		return transaction.getAttrName().getValue();
	}

	/**
	 * @return
	 * @see se.vgregion.ws.objects.Transaction#getEvent()
	 */
	public Event getEvent() {
		return transaction.getEvent().getValue();
	}

	/**
	 * @return
	 * @see se.vgregion.ws.objects.Transaction#getObjectDN()
	 */
	public String getObjectDN() {
		return transaction.getObjectDN().getValue();
	}

	/**
	 * @return
	 * @see se.vgregion.ws.objects.Transaction#getTimestamp()
	 */
	public String getTimestamp() {
		return transaction.getTimestamp().getValue();
	}

	/**
	 * @return
	 * @see se.vgregion.ws.objects.Transaction#getValue()
	 */
	public String getValue() {
		return transaction.getValue().getValue();
	}

	/**
	 * @return
	 * @see se.vgregion.ws.objects.Transaction#getValueB()
	 */
	public String getValueB() {
		return transaction.getValueB().getValue();
	}

	/**
	 * @return
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return transaction.hashCode();
	}

}
