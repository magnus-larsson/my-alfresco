/*
 * $Header: /cvs/cvsroot/vgr-soa-client/src/main/java/se/vgregion/client/objects/VGRObject.java,v 1.2 2010/06/04 11:58:36 tobl Exp $
 * $Revision: 1.2 $
 * $Date: 2010/06/04 11:58:36 $
 *
 * ====================================================================
 */
package se.vgregion.client.objects;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import se.vgregion.ws.services.String2ArrayOfAnyTypeMap;
import se.vgregion.ws.services.String2ArrayOfAnyTypeMap.Entry;

/**
 * <Description goes here...>
 *
 * @author Henrik Karlsson
 * @version $Revision: 1.2 $, $Date: 2010/06/04 11:58:36 $
 */
public abstract class VGRObject {
	private final Map<String, List<Object>> attributes;

	/**
	 * 
	 */
	public VGRObject(String2ArrayOfAnyTypeMap map) {
		super();
		attributes = new LinkedHashMap<String, List<Object>>(); 
		Iterator<Entry> i = map.getEntry().iterator();
		while (i.hasNext()) {
			Entry entry = i.next();
			attributes.put(entry.getKey(), entry.getValue().getAnyType());
		}
	}
	
	/**
	 * @return
	 */
	public Map<String, List<Object>> getAttributes() {
		return attributes;
	}
	
	/**
	 * @param name
	 * @return
	 */
	public Object getAttributeValue(String name) {
		if (getAttributes() != null) {
			List<Object> list = getAttributes().get(name.toLowerCase());
			return list.get(0);
		}
		else {
			return null;
		}
	}
	
	/**
	 * @param name
	 * @return
	 */
	public List<Object> getAttributeValues(String name) {
		if (getAttributes() != null) {
			return getAttributes().get(name.toLowerCase());
		}
		else {
			return null;
		}		
	}
	
}
