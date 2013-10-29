/*
 * $Header: /cvs/cvsroot/vgr-soa-client/src/main/java/se/vgregion/client/objects/Function.java,v 1.1 2010/01/21 08:57:46 heka Exp $
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
public class Function extends VGRObject {
	private final se.vgregion.ws.objects.Function function;
	/**
	 * @param function
	 */
	public Function(se.vgregion.ws.objects.Function function) {
		super(function.getAttributes().getValue());
		this.function = function;
	}

	/**
	 * @return
	 * @see se.vgregion.ws.objects.Function#getDn()
	 */
	public String getDn() {
		return function.getDn().getValue();
	}
	
	

}
