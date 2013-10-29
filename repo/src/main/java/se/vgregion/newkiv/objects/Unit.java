/*
 * $Header: /cvs/cvsroot/vgr-soa-client/src/main/java/se/vgregion/client/objects/Unit.java,v 1.1 2010/01/21 08:57:46 heka Exp $
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
public class Unit extends VGRObject {
	private final se.vgregion.ws.objects.Unit unit;
	
	public Unit(se.vgregion.ws.objects.Unit unit) {
		super(unit.getAttributes().getValue());
		this.unit = unit;
	}

	/**
	 * @return
	 * @see se.vgregion.ws.objects.Unit#getDn()
	 */
	public String getDn() {
		return unit.getDn().getValue();
	}
}
