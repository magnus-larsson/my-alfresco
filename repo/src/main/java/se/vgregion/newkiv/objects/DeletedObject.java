/*
 * $Header: /cvs/cvsroot/vgr-soa-client/src/main/java/se/vgregion/client/objects/DeletedObject.java,v 1.1 2010/06/04 08:46:40 heka Exp $
 * $Revision: 1.1 $
 * $Date: 2010/06/04 08:46:40 $
 *
 * ====================================================================
 */
package se.vgregion.newkiv.objects;

import javax.xml.bind.JAXBElement;

/**
 * <Description goes here...>
 *
 * @author Henrik Karlsson
 * @version $Revision: 1.1 $, $Date: 2010/06/04 08:46:40 $
 */
public class DeletedObject {
	private final se.vgregion.ws.objects.DeletedObject deletedObject;

	/**
	 * @param transaction
	 */
	public DeletedObject(se.vgregion.ws.objects.DeletedObject deletedObject) {
		super();
		this.deletedObject = deletedObject;
	}

	
	/**
	 * @return
	 * @see se.vgregion.ws.objects.DeletedObject#getHsaIdentity()
	 */
	public String getHsaIdentity() {
		return deletedObject.getHsaIdentity().getValue();
	}


	/**
	 * @return
	 * @see se.vgregion.ws.objects.DeletedObject#getObjectDN()
	 */
	public String getObjectDN() {
		return deletedObject.getObjectDN().getValue();
	}


	/**
	 * @return
	 * @see se.vgregion.ws.objects.DeletedObject#getTimestamp()
	 */
	public String getTimestamp() {
		return deletedObject.getTimestamp().getValue();
	}

	/**
	 * @return
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return deletedObject.hashCode();
	}

}
