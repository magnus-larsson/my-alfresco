/*
 * $Header: /cvs/cvsroot/vgr-soa-client/src/main/java/se/vgregion/client/objects/Server.java,v 1.1 2010/01/21 08:57:46 heka Exp $
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
public class Server extends VGRObject {
	private final se.vgregion.ws.objects.Server	server;
	
	public Server(se.vgregion.ws.objects.Server server) {
		super(server.getAttributes().getValue());
		this.server = server;
	}

	/**
	 * @return
	 * @see se.vgregion.ws.objects.Server#getDn()
	 */
	public String getDn() {
		return server.getDn().getValue();
	}
}
