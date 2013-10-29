/*
 * $Header: /cvs/cvsroot/vgr-soa-client/src/main/java/se/vgregion/client/objects/Resource.java,v 1.1 2010/01/21 08:57:46 heka Exp $
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
public class Resource extends VGRObject {
	private final se.vgregion.ws.objects.Resource resource;
	
	public Resource(se.vgregion.ws.objects.Resource resource) {
		super(resource.getAttributes().getValue());
		this.resource = resource;
	}

	/**
	 * @return
	 * @see se.vgregion.ws.objects.Resource#getDn()
	 */
	public String getDn() {
		return resource.getDn().getValue();
	}
}
