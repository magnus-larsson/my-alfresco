package se.vgregion.alfresco.repo.report;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.audit.AuditQueryParameters;
import org.alfresco.service.cmr.audit.AuditService;
import org.alfresco.service.cmr.audit.AuditService.AuditQueryCallback;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.apache.log4j.Logger;

public class ActiveUsers {
	private static ServiceRegistry serviceRegistry;
	private final static Logger LOG = Logger.getLogger(ActiveUsers.class);
	private static final String FULL_NAME_PATH = "/vgr-authentication/login/no-error/fullName";
	private static final String INTERNAL_ZONE_NAME = "AUTH.EXT.ldapPersonal";

	public static final String INTERNAL_USERS = "internal";
	public static final String EXTERNAL_USERS = "external";

	private static final int NUM_LOGINS_LIMIT = 5;
	private static final int ACTIVE_DAYS_BACK = 30;

	public Map<String, List<UserLoginDetails>> getActiveUsersByZone() {
		Map<String, List<UserLoginDetails>> activeUsersByZone = new HashMap<String, List<UserLoginDetails>>();
		List<UserLoginDetails> internalUsers = new ArrayList<UserLoginDetails>();
		List<UserLoginDetails> externalUsers = new ArrayList<UserLoginDetails>();
		PersonService personService = serviceRegistry.getPersonService();
		NodeService nodeService = serviceRegistry.getNodeService();
		List<UserLoginDetails> activeUsers = getActiveUsers();
		for (UserLoginDetails user : activeUsers) {
			NodeRef person = personService.getPerson(user.getUserName(), false);

			List<ChildAssociationRef> parentAssocs = nodeService
					.getParentAssocs(person);
			boolean isInternalUser = false;
			for (ChildAssociationRef parentAssoc : parentAssocs) {
				if (ContentModel.ASSOC_IN_ZONE.equals(parentAssoc
						.getTypeQName())) {
					NodeRef parentRef = parentAssoc.getParentRef();
					String property = (String) nodeService.getProperty(
							parentRef, ContentModel.PROP_NAME);
					if (LOG.isDebugEnabled()) {
						LOG.debug("User: " + user.getUserName()
								+ " belongs to zone: " + property);
					}

					if (INTERNAL_ZONE_NAME.equalsIgnoreCase(property)) {
						// Internal user (ldap user)
						isInternalUser = true;
					}
				}
			}

			if (isInternalUser) {
				// Internal user (ldap user)
				internalUsers.add(user);
			} else {
				// External user (alfresco internal user)
				externalUsers.add(user);
			}
		}
		activeUsersByZone.put(INTERNAL_USERS, internalUsers);
		activeUsersByZone.put(EXTERNAL_USERS, externalUsers);
		return activeUsersByZone;
	}

	/**
	 * Querys the audit service to get last login for users
	 * 
	 * @return
	 */
	public List<UserLoginDetails> getActiveUsers() {
		List<UserLoginDetails> resultList = new ArrayList<UserLoginDetails>();
		final Map<String, UserLoginDetails> resultMap = new HashMap<String, UserLoginDetails>();

		AuditService auditService = serviceRegistry.getAuditService();

		AuditQueryCallback callback = new AuditQueryCallback() {
			@Override
			public boolean valuesRequired() {
				return true;
			}

			@Override
			public boolean handleAuditEntry(Long entryId,
					String applicationName, String user, long time,
					Map<String, Serializable> values) {
				UserLoginDetails userLoginDetails;
				// If the user has already been found, just increment number of
				// logins and track last activity
				if (resultMap.containsKey(user)) {
					userLoginDetails = resultMap.get(user);
					userLoginDetails
							.setLogins(userLoginDetails.getLogins() + 1);
					Date date = new Date(time);
					if (date.after(userLoginDetails.getLastActivity())) {
						userLoginDetails.setLastActivity(date);
					}
				} else if (user != null) {
					userLoginDetails = new UserLoginDetails();
					userLoginDetails.setLogins(1);
					Date date = new Date(time);
					userLoginDetails.setLastActivity(date);
					userLoginDetails.setUserName(user);
					userLoginDetails.setFullName((String) values
							.get(FULL_NAME_PATH));
					resultMap.put(user, userLoginDetails);
				}
				if (LOG.isDebugEnabled()) {
					LOG.debug("Application name: " + applicationName);
					LOG.debug("Values: " + values.toString());
					LOG.debug("Username: " + user);
					LOG.debug("Time: " + time);
					LOG.debug("FullName: "
							+ (String) values.get(FULL_NAME_PATH));
				}

				return true;
			}

			@Override
			public boolean handleAuditEntryError(Long entryId, String errorMsg,
					Throwable error) {
				throw new AlfrescoRuntimeException(errorMsg, error);
			}

		};

		AuditQueryParameters parameters = new AuditQueryParameters();
		parameters.setApplicationName("vgr-authentication");
		Date now = new Date();
		long startTime = now.getTime() - ACTIVE_DAYS_BACK * 24 * 3600
				* 1000;
		parameters.setFromTime(startTime);
		parameters.setToTime(now.getTime());
		auditService.auditQuery(callback, parameters, 0);
		if (resultMap.size() == 0) {
			// If starttime is before logging started the above query will
			// return 0 results for some reason, so if 0 results are returned,
			// then search without a limit
			parameters.setFromTime(null);
			auditService.auditQuery(callback, parameters, 0);
		}
		// Filter out users who are not active
		for (UserLoginDetails user : resultMap.values()) {
			if (user.getLogins() >= NUM_LOGINS_LIMIT) {
				resultList.add(user);
			}
		}
		return resultList;
	}

	public ServiceRegistry getServiceRegistry() {
		return ActiveUsers.serviceRegistry;
	}

	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		ActiveUsers.serviceRegistry = serviceRegistry;
	}
}
