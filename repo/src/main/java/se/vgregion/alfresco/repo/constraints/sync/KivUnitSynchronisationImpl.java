package se.vgregion.alfresco.repo.constraints.sync;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import se.vgregion.alfresco.repo.constraints.KivService;
import se.vgregion.alfresco.repo.model.KivUnit;
import se.vgregion.alfresco.repo.model.VgrModel;
import se.vgregion.alfresco.repo.utils.ServiceUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KivUnitSynchronisationImpl implements InitializingBean,
		KivUnitSynchronisation {

	private static final Logger LOG = Logger
			.getLogger(KivUnitSynchronisationImpl.class);

	private KivService _kivService;

	private SearchService _searchService;

	private NodeService _nodeService;

	private FileFolderService _fileFolderService;

	protected RetryingTransactionHelper _retryingTransactionHelper;

	public void setKivService(final KivService kivService) {
		_kivService = kivService;
	}

	public void setSearchService(final SearchService searchService) {
		_searchService = searchService;
	}

	public void setNodeService(final NodeService nodeService) {
		_nodeService = nodeService;
	}

	public void setRetryingTransactionHelper(
			final RetryingTransactionHelper retryingTransactionHelper) {
		_retryingTransactionHelper = retryingTransactionHelper;
	}

	public void setFileFolderService(final FileFolderService fileFolderService) {
		_fileFolderService = fileFolderService;
	}

	@Override
	public void synchronise() {
		AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Void>() {

			@Override
			public Void doWork() throws Exception {
				_retryingTransactionHelper
						.doInTransaction(
								new RetryingTransactionHelper.RetryingTransactionCallback<Object>() {
									public Object execute() throws Throwable {
										doSynchronise();
										return null;
									}
								}, false, false);

				LOG.info("Finished synchronising KIV units.");
				return null;
			}

		}, AuthenticationUtil.getSystemUserName());
	}

	private void doSynchronise() {
		final Date start = new Date();

		final List<KivUnit> units = _kivService.findOrganisationalUnits();

		if (units.size() > 0) {
			synchroniseUnits(units);
		}

		deleteRemovedUnits(start);
	}

	private void deleteRemovedUnits(final Date start) {
		final Long startTime = start.getTime();

		final String query = "TYPE:\"kiv:unit\" AND  @kiv\\:accessed:[MIN TO "
				+ startTime + "]";

		final ResultSet nodeRefs = search(query);

		try {
			LOG.info("Query to find removed KIV units:");
			LOG.info(query);
			LOG.info("Number of deleted KIV units: " + nodeRefs.length());
		} finally {
			ServiceUtils.closeQuietly(nodeRefs);
		}
	}

	private void synchroniseUnits(final List<KivUnit> units) {
		synchroniseUnits(units, null);
	}

	private void synchroniseUnits(final List<KivUnit> units,
			final KivUnit parentUnit) {
		for (final KivUnit unit : units) {
			final NodeRef parentNodeRef = findNodeRef(parentUnit);

			synchroniseUnit(unit, parentNodeRef);

			final List<KivUnit> subunits = _kivService
					.findOrganisationalUnits(unit.getDistinguishedName());

			if (subunits.size() > 0) {
				synchroniseUnits(subunits, unit);
			}
		}
	}

	private void synchroniseUnit(final KivUnit unit, final NodeRef parentNodeRef) {
		final NodeRef nodeRef = findNodeRef(unit);

		if (nodeRef == null) {
			createNode(unit, parentNodeRef);

			LOG.info("Created: node '" + unit.getOrganisationalUnit()
					+ "' created");

			return;
		}

		final boolean updated = updateNode(unit, nodeRef);

		if (updated) {
			LOG.info("Updated: node '" + unit.getOrganisationalUnit() + "'.");

			return;
		}

		final boolean moved = moveNode(unit, nodeRef, parentNodeRef);

		if (moved) {
			LOG.info("Moved: node '" + unit.getOrganisationalUnit() + "'.");

			return;
		}

		// last but not the least, if neither created nor updated at least
		// update
		// the accessed date...
		markUnitAsAccessed(nodeRef);
	}

	private boolean moveNode(final KivUnit unit, final NodeRef nodeRef,
			final NodeRef parentNodeRef) {
		// if there is no parent node ref, then don't move it
		if (parentNodeRef == null) {
			return false;
		}

		// get a list of all parent associations
		final List<ChildAssociationRef> parentAssociations = _nodeService
				.getParentAssocs(nodeRef);

		// if there are not parent associations, don't move
		if (parentAssociations.size() == 0) {
			return false;
		}

		// get the stored parent node ref
		final NodeRef storedParentNodeRef = parentAssociations.get(0)
				.getParentRef();

		// if the parent node ref is same as stored parent node ref, don't move
		if (parentNodeRef.equals(storedParentNodeRef)) {
			return false;
		}

		try {
			return _fileFolderService.move(nodeRef, parentNodeRef, null) != null;
		} catch (final Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	private void markUnitAsAccessed(final NodeRef nodeRef) {
		final Map<QName, Serializable> properties = new HashMap<QName, Serializable>();

		properties.put(VgrModel.PROP_KIV_ACCESSED, new Date().getTime());

		_nodeService.addProperties(nodeRef, properties);
	}

	private boolean updateNode(final KivUnit unit, final NodeRef nodeRef) {
		boolean updated = false;

		final Map<QName, Serializable> updatedValues = new HashMap<QName, Serializable>();

		final String liveHsaIdentity = unit.getHsaIdentity();
		final Serializable savedHsaIdentity = _nodeService.getProperty(nodeRef,
				VgrModel.PROP_KIV_HSAIDENTITY);

		if (isChanged(liveHsaIdentity, savedHsaIdentity)) {
			updatedValues.put(VgrModel.PROP_KIV_HSAIDENTITY, liveHsaIdentity);
		}

		final String liveOu = unit.getOrganisationalUnit();
		final Serializable savedOu = _nodeService.getProperty(nodeRef,
				VgrModel.PROP_KIV_OU);

		if (isChanged(liveOu, savedOu)) {
			updatedValues.put(VgrModel.PROP_KIV_OU, liveOu);
		}

		if (updatedValues.size() > 0) {
			updatedValues.put(VgrModel.PROP_KIV_MODIFIED, new Date().getTime());
			updatedValues.put(VgrModel.PROP_KIV_ACCESSED, new Date().getTime());

			_nodeService.addProperties(nodeRef, updatedValues);

			updated = true;
		}

		return updated;
	}

	private void createNode(final KivUnit unit, final NodeRef parentUnitNodeRef) {
		final NodeRef parent = parentUnitNodeRef != null ? parentUnitNodeRef
				: getKivTypeNode();

		final String name = getValidName(unit.getOrganisationalUnit()) + " - "
				+ unit.getHsaIdentity();

		final Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
		properties.put(VgrModel.PROP_KIV_OU, unit.getOrganisationalUnit());
		properties.put(VgrModel.PROP_KIV_HSAIDENTITY, unit.getHsaIdentity());
		properties.put(VgrModel.PROP_KIV_DN, unit.getDistinguishedName());
		properties.put(ContentModel.PROP_NAME, name);
		properties.put(VgrModel.PROP_KIV_MODIFIED, new Date().getTime());
		properties.put(VgrModel.PROP_KIV_ACCESSED, new Date().getTime());

		final String uri = _nodeService.getPrimaryParent(parent).getQName()
				.getNamespaceURI();
		final String validLocalName = QName.createValidLocalName(unit
				.getOrganisationalUnit());

		final NodeRef unitNodeRef = _nodeService.createNode(parent,
				ContentModel.ASSOC_CONTAINS,
				QName.createQName(uri, validLocalName), VgrModel.TYPE_KIV_UNIT)
				.getChildRef();

		_nodeService.addProperties(unitNodeRef, properties);
	}

	private NodeRef getKivTypeNode() {
		final String query = "PATH:\"/app:company_home/app:dictionary/app:kiv/app:units\"";

		final ResultSet result = search(query);

		try {
			if (result.length() > 0) {
				return result.getNodeRef(0);
			}
		} finally {
			ServiceUtils.closeQuietly(result);
		}

		final NodeRef kivStorageNode = getKivStorageNode();

		final String uri = _nodeService.getPrimaryParent(kivStorageNode)
				.getQName().getNamespaceURI();
		final String validLocalName = QName.createValidLocalName("units");

		final NodeRef kivTypeNode = _nodeService.createNode(kivStorageNode,
				ContentModel.ASSOC_CONTAINS,
				QName.createQName(uri, validLocalName),
				ContentModel.TYPE_FOLDER).getChildRef();

		_nodeService.setProperty(kivTypeNode, ContentModel.PROP_NAME, "Units");

		return kivTypeNode;
	}

	/**
	 * Finds or creates the "Kiv" node under the Data Dictionary
	 * 
	 * @return
	 */
	private NodeRef getKivStorageNode() {
		final String query = "PATH:\"/app:company_home/app:dictionary/app:kiv\"";

		final ResultSet result = search(query);

		try {
			if (result.length() > 0) {
				return result.getNodeRef(0);
			}
		} finally {
			ServiceUtils.closeQuietly(result);
		}

		final NodeRef dataDictionaryNode = getDataDictionaryNode();

		final String uri = _nodeService.getPrimaryParent(dataDictionaryNode)
				.getQName().getNamespaceURI();
		final String validLocalName = QName.createValidLocalName("kiv");

		final NodeRef kivStorageNode = _nodeService.createNode(
				dataDictionaryNode, ContentModel.ASSOC_CONTAINS,
				QName.createQName(uri, validLocalName),
				ContentModel.TYPE_FOLDER).getChildRef();

		_nodeService.setProperty(kivStorageNode, ContentModel.PROP_NAME, "Kiv");

		return kivStorageNode;
	}

	private NodeRef getDataDictionaryNode() {
		final String query = "PATH:\"/app:company_home/app:dictionary\"";

		final ResultSet result = search(query);

		try {
			return result.getNodeRef(0);
		} finally {
			ServiceUtils.closeQuietly(result);
		}
	}

	private NodeRef findNodeRef(final KivUnit unit) {
		if (unit == null) {
			return null;
		}

		final String query = "TYPE:\"kiv:unit\" AND @kiv\\:hsaidentity:\""
				+ unit.getHsaIdentity() + "\"";

		final ResultSet nodeRefs = search(query);

		final NodeRef result;

		try {
			if (nodeRefs.length() == 0) {
				result = null;
			} else if (nodeRefs.length() > 1) {
				result = nodeRefs.getNodeRef(0);

				for (int x = 1; x < nodeRefs.length(); x++) {
					_nodeService.deleteNode(nodeRefs.getNodeRef(x));
				}
			} else {
				result = nodeRefs.getNodeRef(0);
			}
		} finally {
			ServiceUtils.closeQuietly(nodeRefs);
		}

		return result;
	}

	private ResultSet search(final String query) {
		final SearchParameters searchParameters = new SearchParameters();
		searchParameters.setLanguage(SearchService.LANGUAGE_LUCENE);
		searchParameters.setQuery(query);
		searchParameters.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);

		return _searchService.query(searchParameters);
	}

	private String getValidName(final String name) {
		String validName = StringUtils.replace(name, "/", "-");
		validName = StringUtils.replace(validName, "*", " ");
		validName = StringUtils.replace(validName, ":", " ");
		validName = StringUtils.replace(validName, "\\", " ");

		return validName;
	}

	private boolean isChanged(final String liveValue,
			final Serializable savedValue) {
		final String safeSavedValue = savedValue == null ? "" : savedValue
				.toString();

		return !StringUtils.equals(liveValue, safeSavedValue);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(_kivService);
		Assert.notNull(_searchService);
		Assert.notNull(_nodeService);
	}

}
