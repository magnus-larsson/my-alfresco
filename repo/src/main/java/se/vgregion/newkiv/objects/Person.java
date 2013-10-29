/*
 * $Header: /cvs/cvsroot/vgr-soa-client/src/main/java/se/vgregion/client/objects/Person.java,v 1.2 2010/03/25 13:02:18 tobl Exp $
 * $Revision: 1.2 $
 * $Date: 2010/03/25 13:02:18 $
 *
 * ====================================================================
 */
package se.vgregion.newkiv.objects;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * <Description goes here...>
 *
 * @author Henrik Karlsson
 * @version $Revision: 1.2 $, $Date: 2010/03/25 13:02:18 $
 */
public class Person extends VGRObject {
	private final se.vgregion.ws.objects.Person person;
	private List<Employment> employments;
	
	/**
	 * @param map
	 */
	public Person(se.vgregion.ws.objects.Person person) {
		super(person.getAttributes().getValue());
		this.person = person;
		
		employments = new ArrayList<Employment>();
		if (person.getEmployments().getValue() != null) {
			Iterator<se.vgregion.ws.objects.Employment> i = person.getEmployments().getValue().getEmployment().iterator();
			while (i.hasNext()) {
				employments.add(new Employment(i.next()));
			}
		}
	}
	
	/**
	 * @return
	 * @see se.vgregion.ws.objects.Person#getDn()
	 */
	public String getDn() {
		return person.getDn().getValue();
	}
	
	/**
	 * @return
	 * @see se.vgregion.ws.objects.Person#getEmployments()
	 */
	public List<Employment> getEmployments() {
		return employments;
	}
	

}
