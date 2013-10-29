/*
 * $Header: /cvs/cvsroot/vgr-soa-client/src/main/java/se/vgregion/client/VGRegionWebServiceClient.java,v 1.10 2010/10/04 11:00:19 heka Exp $
 * $Revision: 1.10 $
 * $Date: 2010/10/04 11:00:19 $
 *
 * ====================================================================
 */
package se.vgregion.client;

import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.cxf.configuration.security.AuthorizationPolicy;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.transport.http.HTTPConduit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.vgregion.ws.constants.VGRegionDirectory;
import se.vgregion.ws.objects.ArrayOfDeletedObject;
import se.vgregion.ws.objects.ArrayOfFunction;
import se.vgregion.ws.objects.ArrayOfPerson;
import se.vgregion.ws.objects.ArrayOfResource;
import se.vgregion.ws.objects.ArrayOfServer;
import se.vgregion.ws.objects.ArrayOfTransaction;
import se.vgregion.ws.objects.ArrayOfUnit;
import se.vgregion.ws.objects.ArrayOfUnsurePerson;
import se.vgregion.client.objects.DeletedObject;
import se.vgregion.client.objects.Function;
import se.vgregion.client.objects.Person;
import se.vgregion.client.objects.Resource;
import se.vgregion.client.objects.Server;
import se.vgregion.client.objects.Transaction;
import se.vgregion.client.objects.Unit;
import se.vgregion.client.objects.UnsurePerson;
import se.vgregion.ws.services.ArrayOfString;
import se.vgregion.ws.services.String2StringMap;
import se.vgregion.ws.services.VGRException;
import se.vgregion.ws.services.VGRegionWebServiceImpl;
import se.vgregion.ws.services.VGRegionWebServiceImplPortType;


/**
 * <Description goes here...>
 *
 * @author Henrik Karlsson
 * @version $Revision: 1.10 $, $Date: 2010/10/04 11:00:19 $
 */
public class VGRegionWebServiceClient {
	private static final Logger LOGGER = LoggerFactory.getLogger(VGRegionWebServiceClient.class); 
	
	private final VGRegionWebServiceImplPortType port;
		
	/**
	 * @param username
	 * @param password
	 */
	public VGRegionWebServiceClient(String username, String password) {
		this(username, password, false);
	}

	/**
	 * @param username
	 * @param password
	 * @param test true for test environment, false for production environment
	 */
	public VGRegionWebServiceClient(String username, String password, boolean test) {
		this(username, password, ClassLoader.getSystemResource(test ? "META-INF/wsdl/kivutvws.wsdl" : "META-INF/wsdl/kivws.wsdl"));
	}

	/**
	 * @param username
	 * @param password
	 * @param wsdl 
	 */
	public VGRegionWebServiceClient(String username, String password, URL wsdl) {
		super();
		
        VGRegionWebServiceImpl ss = new VGRegionWebServiceImpl(wsdl);
        this.port = ss.getVGRegionWebServiceImplPort();
        
        Client client = ClientProxy.getClient(this.port);
        HTTPConduit http = (HTTPConduit)client.getConduit();
        AuthorizationPolicy ap = new AuthorizationPolicy();
        ap.setUserName(username);
        ap.setPassword(password);
        http.setAuthorization(ap);
	}

	/**
	 * @param attribute
	 * @return
	 * @throws VGRException
	 */
	public Map<String,String> getAttributeCodesAndCleartexts(String attribute) throws VGRException {
		return transformString2StringMap(port.getAttributeCodesAndCleartexts(attribute));
	}
	
	/**
	 * @param timestamp
	 * @return
	 * @throws VGRException
	 */
	public List<DeletedObject> getDeletedObjects(String timestamp) throws VGRException {		
		return transformArrayOfDeletedObject(port.getDeletedUnits(timestamp));
	}

	/**
	 * @param hsaIdentity
	 * @param timestamp
	 * @return
	 * @throws VGRException
	 * @see se.vgregion.ws.services.VGRegionWebService#getFunctionAtSpecificTime(java.lang.String, java.lang.String)
	 */
	public Function getFunctionAtSpecificTime(String hsaIdentity, String timestamp)
			throws VGRException {
		return new Function(port.getFunctionAtSpecificTime(hsaIdentity, timestamp));
	}

	/**
	 * @param hsaIdentity
	 * @return
	 * @throws VGRException
	 * @see se.vgregion.ws.services.VGRegionWebService#getFunctionTransactions(java.lang.String)
	 */
	public List<Transaction> getFunctionTransactions(String hsaIdentity)
			throws VGRException {
		return transformArrayOfTransaction(port.getFunctionTransactions(hsaIdentity));
	}

	/**
	 * @param hsaIdentity
	 * @param timestamp
	 * @return
	 * @throws VGRException
	 * @see se.vgregion.ws.services.VGRegionWebService#getPersonAtSpecificTime(java.lang.String, java.lang.String)
	 */
	public Person getPersonAtSpecificTime(String hsaIdentity, String timestamp)
			throws VGRException {
		return new Person(port.getPersonAtSpecificTime(hsaIdentity, timestamp));
	}

	/**
	 * @param hsaIdentity
	 * @param timestamp
	 * @return
	 * @throws VGRException
	 * @see se.vgregion.ws.services.VGRegionWebService#getPersonEmploymentAtSpecificTime(java.lang.String, java.lang.String)
	 */
	public Person getPersonEmploymentAtSpecificTime(String hsaIdentity, String timestamp)
			throws VGRException {
		return new Person(port.getPersonEmploymentAtSpecificTime(hsaIdentity, timestamp));
	}

	/**
	 * @param hsaIdentity
	 * @return
	 * @throws VGRException
	 * @see se.vgregion.ws.services.VGRegionWebService#getPersonTransactions(java.lang.String)
	 */
	public List<Transaction> getPersonTransactions(String hsaIdentity)
			throws VGRException {
		ArrayOfTransaction array = port.getPersonTransactions(hsaIdentity);
		if (array != null) {
			return transformArrayOfTransaction(array);
		}
		else {
			return null;
		}
	}

	/**
	 * @param directory
	 * @return
	 * @throws VGRException
	 * @see se.vgregion.ws.services.VGRegionWebService#getReturnAttributesForEmployment(se.vgregion.ws.constants.VGRegionDirectory)
	 */
	public List<String> getReturnAttributesForEmployment(VGRegionDirectory directory)
			throws VGRException {
		return transformArrayOfString(port.getReturnAttributesForEmployment(directory));
	}

	/**
	 * @param directory
	 * @return
	 * @throws VGRException
	 * @see se.vgregion.ws.services.VGRegionWebService#getReturnAttributesForFunction(se.vgregion.ws.constants.VGRegionDirectory)
	 */
	public List<String> getReturnAttributesForFunction(VGRegionDirectory directory)
			throws VGRException {
		return transformArrayOfString(port.getReturnAttributesForFunction(directory));
	}

	/**
	 * @param directory
	 * @return
	 * @throws VGRException
	 * @see se.vgregion.ws.services.VGRegionWebService#getReturnAttributesForPerson(se.vgregion.ws.constants.VGRegionDirectory)
	 */
	public List<String> getReturnAttributesForPerson(VGRegionDirectory directory)
			throws VGRException {
		return transformArrayOfString(port.getReturnAttributesForPerson(directory));
	}

	/**
	 * @return
	 * @throws VGRException
	 * @see se.vgregion.ws.services.VGRegionWebService#getReturnAttributesForResource()
	 */
	public List<String> getReturnAttributesForResource() throws VGRException {
		return transformArrayOfString(port.getReturnAttributesForResource());
	}

	/**
	 * @return
	 * @throws VGRException
	 * @see se.vgregion.ws.services.VGRegionWebService#getReturnAttributesForServer(se.vgregion.ws.constants.VGRegionDirectory)
	 */
	public List<String> getReturnAttributesForServer()
			throws VGRException {
		return transformArrayOfString(port.getReturnAttributesForServer());
	}

	/**
	 * @param directory
	 * @return
	 * @throws VGRException
	 * @see se.vgregion.ws.services.VGRegionWebService#getReturnAttributesForUnit(se.vgregion.ws.constants.VGRegionDirectory)
	 */
	public List<String> getReturnAttributesForUnit(VGRegionDirectory directory)
			throws VGRException {
		return transformArrayOfString(port.getReturnAttributesForUnit(directory));
	}

	/**
	 * @return
	 * @throws VGRException
	 * @see se.vgregion.ws.services.VGRegionWebService#getReturnAttributesForUnsurePerson()
	 */
	public List<String> getReturnAttributesForUnsurePerson()
			throws VGRException {
		return transformArrayOfString(port.getReturnAttributesForUnsurePerson());
	}

	/**
	 * @param directory
	 * @return
	 * @throws VGRException
	 * @see se.vgregion.ws.services.VGRegionWebService#getSearchAttributesForEmployment(se.vgregion.ws.constants.VGRegionDirectory)
	 */
	public List<String> getSearchAttributesForEmployment(VGRegionDirectory directory)
			throws VGRException {
		return transformArrayOfString(port.getSearchAttributesForEmployment(directory));
	}

	/**
	 * @param directory
	 * @return
	 * @throws VGRException
	 * @see se.vgregion.ws.services.VGRegionWebService#getSearchAttributesForFunction(se.vgregion.ws.constants.VGRegionDirectory)
	 */
	public List<String> getSearchAttributesForFunction(VGRegionDirectory directory)
			throws VGRException {
		return transformArrayOfString(port.getSearchAttributesForFunction(directory));
	}

	/**
	 * @param directory
	 * @return
	 * @throws VGRException
	 * @see se.vgregion.ws.services.VGRegionWebService#getSearchAttributesForPerson(se.vgregion.ws.constants.VGRegionDirectory)
	 */
	public List<String> getSearchAttributesForPerson(VGRegionDirectory directory)
			throws VGRException {
		return transformArrayOfString(port.getSearchAttributesForPerson(directory));
	}

	/**
	 * @return
	 * @throws VGRException
	 * @see se.vgregion.ws.services.VGRegionWebService#getSearchAttributesForResource()
	 */
	public List<String> getSearchAttributesForResource() throws VGRException {
		return transformArrayOfString(port.getSearchAttributesForResource());
	}

	/**
	 * @return
	 * @throws VGRException
	 * @see se.vgregion.ws.services.VGRegionWebService#getSearchAttributesForServer(se.vgregion.ws.constants.VGRegionDirectory)
	 */
	public List<String> getSearchAttributesForServer()
			throws VGRException {
		return transformArrayOfString(port.getSearchAttributesForServer());
	}

	/**
	 * @param arg0
	 * @return
	 * @throws VGRException
	 * @see se.vgregion.ws.services.VGRegionWebService#getSearchAttributesForUnit(se.vgregion.ws.constants.VGRegionDirectory)
	 */
	public List<String> getSearchAttributesForUnit(VGRegionDirectory directory)
			throws VGRException {
		return transformArrayOfString(port.getSearchAttributesForUnit(directory));
	}

	/**
	 * @return
	 * @throws VGRException
	 * @see se.vgregion.ws.services.VGRegionWebService#getSearchAttributesForUnsurePerson()
	 */
	public List<String> getSearchAttributesForUnsurePerson()
			throws VGRException {
		return transformArrayOfString(port.getSearchAttributesForUnsurePerson());
	}

	/**
	 * @param hsaIdentity
	 * @param timestamp
	 * @return
	 * @throws VGRException
	 * @see se.vgregion.ws.services.VGRegionWebService#getUnitAtSpecificTime(java.lang.String, java.lang.String)
	 */
	public Unit getUnitAtSpecificTime(String hsaIdentity, String timestamp)
			throws VGRException {
		return new Unit(port.getUnitAtSpecificTime(hsaIdentity, timestamp));
	}

	/**
	 * @param hsaIdentity
	 * @return
	 * @throws VGRException
	 * @see se.vgregion.ws.services.VGRegionWebService#getUnitTransactions(java.lang.String)
	 */
	public List<Transaction> getUnitTransactions(String hsaIdentity)
			throws VGRException {
		return transformArrayOfTransaction(port.getUnitTransactions(hsaIdentity));
	}

	/**
	 * @param filter
	 * @param attributes
	 * @param directory
	 * @return
	 * @throws VGRException
	 */
	public List<Function> searchFunction(String filter, List<String> attributes,
			VGRegionDirectory directory) throws VGRException {
		return searchFunction(filter, attributes, directory, null, null);
	}
	
	/**
	 * @param filter
	 * @param attributes
	 * @param directory
	 * @param searchBase
	 * @return
	 * @throws VGRException
	 */
	public List<Function> searchFunction(String filter, List<String> attributes,
			VGRegionDirectory directory, String searchBase, String searchScope) throws VGRException {
		ArrayOfString array = new ArrayOfString();
		array.getString().addAll(attributes);
		return transformArrayOfFunction(port.searchFunction(filter, array, directory, searchBase, searchScope));
	}

	/**
	 * @param filter
	 * @param attributes
	 * @param directory
	 * @return
	 * @throws VGRException
	 * @see se.vgregion.ws.services.VGRegionWebService#searchPerson(java.lang.String, se.vgregion.ws.services.ArrayOfString, se.vgregion.ws.constants.VGRegionDirectory)
	 */
	public List<Person> searchPerson(String filter, List<String> attributes,
			VGRegionDirectory directory) throws VGRException {
		return searchPerson(filter, attributes, directory, null, null);
	}
	
	/**
	 * @param filter
	 * @param attributes
	 * @param directory
	 * @param searchBase
	 * @return
	 * @throws VGRException
	 */
	public List<Person> searchPerson(String filter, List<String> attributes,
			VGRegionDirectory directory, String searchBase, String searchScope) throws VGRException {
		ArrayOfString array = new ArrayOfString();
		array.getString().addAll(attributes);
		return transformArrayOfPerson(port.searchPerson(filter, array, directory, searchBase, searchScope));
	}

	/**
	 * @param filterPerson
	 * @param attributesPerson
	 * @param filterEmployment
	 * @param attributesEmployment
	 * @param directory
	 * @return
	 * @throws VGRException
	 * @see se.vgregion.ws.services.VGRegionWebService#searchPersonEmployment(java.lang.String, se.vgregion.ws.services.ArrayOfString, java.lang.String, se.vgregion.ws.services.ArrayOfString, se.vgregion.ws.constants.VGRegionDirectory)
	 */
	public List<Person> searchPersonEmployment(String filterPerson,
			List<String> attributesPerson, String filterEmployment,
			List<String> attributesEmployment, VGRegionDirectory directory) throws VGRException {
		return searchPersonEmployment(filterPerson, attributesPerson, filterEmployment, attributesEmployment, directory, null, null);
	}

	/**
	 * @param filterPerson
	 * @param attributesPerson
	 * @param filterEmployment
	 * @param attributesEmployment
	 * @param directory
	 * @param searchBase
	 * @return
	 * @throws VGRException
	 */
	public List<Person> searchPersonEmployment(String filterPerson,
			List<String> attributesPerson, String filterEmployment,
			List<String> attributesEmployment, VGRegionDirectory directory, 
			String searchBase, String searchScope) throws VGRException {
		ArrayOfString arrayPerson = new ArrayOfString();
		arrayPerson.getString().addAll(attributesPerson);
		ArrayOfString arrayEmployment = new ArrayOfString();
		arrayEmployment.getString().addAll(attributesEmployment);

		return transformArrayOfPerson(port.searchPersonEmployment(filterPerson, arrayPerson, filterEmployment, arrayEmployment, directory, searchBase, searchScope));
	}

	/**
	 * @param filter
	 * @param attributes
	 * @return
	 * @throws VGRException
	 * @see se.vgregion.ws.services.VGRegionWebService#searchResource(java.lang.String, se.vgregion.ws.services.ArrayOfString)
	 */
	public List<Resource> searchResource(String filter, List<String> attributes)
			throws VGRException {
		ArrayOfString array = new ArrayOfString();
		array.getString().addAll(attributes);

		return transformArrayOfResource(port.searchResource(filter, array));
	}

	/**
	 * @param filter
	 * @param attributes
	 * @return
	 * @throws VGRException
	 * @see se.vgregion.ws.services.VGRegionWebService#searchServer(java.lang.String, se.vgregion.ws.services.ArrayOfString, se.vgregion.ws.constants.VGRegionDirectory)
	 */
	public List<Server> searchServer(String filter, List<String> attributes) throws VGRException {
		ArrayOfString array = new ArrayOfString();
		array.getString().addAll(attributes);

		return transformArrayOfServer(port.searchServer(filter, array));
	}

	/**
	 * @param filter
	 * @param attributes
	 * @param directory
	 * @return
	 * @throws VGRException
	 * @see se.vgregion.ws.services.VGRegionWebService#searchUnit(java.lang.String, se.vgregion.ws.services.ArrayOfString, se.vgregion.ws.constants.VGRegionDirectory)
	 */
	public List<Unit> searchUnit(String filter, List<String> attributes,
			VGRegionDirectory directory) throws VGRException {
		return searchUnit(filter, attributes, directory, null, null);
	}
	
	/**
	 * @param filter
	 * @param attributes
	 * @param directory
	 * @param searchBase
	 * @return
	 * @throws VGRException
	 */
	public List<Unit> searchUnit(String filter, List<String> attributes,
			VGRegionDirectory directory, String searchBase, String searchScope) throws VGRException {
		ArrayOfString array = new ArrayOfString();
		array.getString().addAll(attributes);

		return transformArrayOfUnit(port.searchUnit(filter, array, directory, searchBase, searchScope));
	}

	/**
	 * @param filter
	 * @param attributes
	 * @return
	 * @throws VGRException
	 * @see se.vgregion.ws.services.VGRegionWebService#searchUnsurePerson(java.lang.String, se.vgregion.ws.services.ArrayOfString)
	 */
	public List<UnsurePerson> searchUnsurePerson(String filter,
			List<String> attributes) throws VGRException {
		ArrayOfString array = new ArrayOfString();
		array.getString().addAll(attributes);

		return transformArrayOfUnsurePerson(port.searchUnsurePerson(filter, array));
	}
	
	private List<DeletedObject> transformArrayOfDeletedObject(ArrayOfDeletedObject array) {
		if (array != null) {
			List<DeletedObject> list = new ArrayList<DeletedObject>();
			for (se.vgregion.ws.objects.DeletedObject deletedObject : array.getDeletedObject()) {
				list.add(new DeletedObject(deletedObject));
			}
			return list;
		}
		else {
			return null;
		}
	}	
	
	private List<Function> transformArrayOfFunction(ArrayOfFunction array) {
		if (array != null) {
			List<Function> list = new ArrayList<Function>();
			for(se.vgregion.ws.objects.Function function : array.getFunction()) {
				list.add(new Function(function));
			}
			return list;
		}
		else {
			return null;
		}
	}

	private List<Person> transformArrayOfPerson(ArrayOfPerson array) {
		if (array != null) {
			List<Person> list = new ArrayList<Person>();
			for (se.vgregion.ws.objects.Person person : array.getPerson()) {
				list.add(new Person(person));
			}
			return list;
		}
		else {
			return null;
		}
	}

	private List<Resource> transformArrayOfResource(ArrayOfResource array) {
		if (array != null) {
			List<Resource> list = new ArrayList<Resource>();
			for (se.vgregion.ws.objects.Resource resource : array.getResource()) {
				list.add(new Resource(resource));
			}
			return list;
		}
		else {
			return null;
		}
	}
	
	private List<Server> transformArrayOfServer(ArrayOfServer array) {
		if (array != null) {
			List<Server> list = new ArrayList<Server>();
			for (se.vgregion.ws.objects.Server server : array.getServer()) {
				list.add(new Server(server));
			}
			return list;
		}
		else {
			return null;
		}
	}	
	
	private List<String> transformArrayOfString(ArrayOfString array) {
		if (array != null) {
			return array.getString();
		}
		else {
			return null;
		}		
	}
	
	private List<Transaction> transformArrayOfTransaction(ArrayOfTransaction array) {
		if (array != null) {
			List<Transaction> list = new ArrayList<Transaction>();
			for (se.vgregion.ws.objects.Transaction transaction : array.getTransaction()) {
				list.add(new Transaction(transaction));
			}
			return list;
		}
		else {
			return null;
		}
	}
	
	private List<Unit> transformArrayOfUnit(ArrayOfUnit array) {
		if (array != null) {
			List<Unit> list = new ArrayList<Unit>();
			for (se.vgregion.ws.objects.Unit unit : array.getUnit()) {
				list.add(new Unit(unit));
			}
			return list;
		}
		else {
			return null;
		}
	}	

	private List<UnsurePerson> transformArrayOfUnsurePerson(ArrayOfUnsurePerson array) {
		if (array != null) {
			List<UnsurePerson> list = new ArrayList<UnsurePerson>();
			for (se.vgregion.ws.objects.UnsurePerson unsurePerson : array.getUnsurePerson()) {
				list.add(new UnsurePerson(unsurePerson));
			}
			return list;
		}
		else {
			return null;
		}
	}
	
	private Map<String,String> transformString2StringMap(String2StringMap s2smap) {
		if (s2smap != null) {
			Map<String, String> map = new LinkedHashMap<String, String>();
			for (String2StringMap.Entry entry : s2smap.getEntry()) {
				map.put(entry.getKey(), entry.getValue());
			}
			return map;
		}
		else {
			return null;
		}
	}

}
