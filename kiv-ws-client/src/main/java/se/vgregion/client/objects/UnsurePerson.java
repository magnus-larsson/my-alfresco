/*
 * $Header: /cvs/cvsroot/vgr-soa-client/src/main/java/se/vgregion/client/objects/UnsurePerson.java,v 1.1 2010/01/21 08:57:46 heka Exp $
 * $Revision: 1.1 $
 * $Date: 2010/01/21 08:57:46 $
 *
 * ====================================================================
 */
package se.vgregion.client.objects;


/**
 * <Description goes here...>
 *
 * @author Henrik Karlsson
 * @version $Revision: 1.1 $, $Date: 2010/01/21 08:57:46 $
 */
public class UnsurePerson extends VGRObject {
	private final se.vgregion.ws.objects.UnsurePerson unsurePerson;
	
	public UnsurePerson(se.vgregion.ws.objects.UnsurePerson unsurePerson) {
		super(unsurePerson.getAttributes().getValue());
		this.unsurePerson = unsurePerson;
	}

	/**
	 * @return
	 * @see se.vgregion.ws.objects.UnsurePerson#getDn()
	 */
	public String getDn() {
		return unsurePerson.getDn().getValue();
	}
}
