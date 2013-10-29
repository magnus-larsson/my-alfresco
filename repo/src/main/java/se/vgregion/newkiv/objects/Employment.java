/*
 * $Header: /cvs/cvsroot/vgr-soa-client/src/main/java/se/vgregion/client/objects/Employment.java,v 1.1 2010/01/21 08:57:46 heka Exp $
 * $Revision: 1.1 $
 * $Date: 2010/01/21 08:57:46 $
 *
 * ====================================================================
 */
package se.vgregion.newkiv.objects;


/**
 * <Description goes here...>
 *
 * @author Henrik Karlsson
 * @version $Revision: 1.1 $, $Date: 2010/01/21 08:57:46 $
 */
public class Employment extends VGRObject {
	private final se.vgregion.ws.objects.Employment employment;
	
	public Employment(se.vgregion.ws.objects.Employment employment) {
		super(employment.getAttributes().getValue());
		this.employment = employment;
	}

	/**
	 * @return
	 * @see se.vgregion.ws.objects.Employment#getDn()
	 */
	public String getDn() {
		return employment.getDn().getValue();
	}
}
