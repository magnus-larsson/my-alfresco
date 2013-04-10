package se.vgregion.alfresco.repo.node;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies.OnUpdatePropertiesPolicy;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;

import se.vgregion.alfresco.repo.model.VgrModel;
import se.vgregion.alfresco.repo.utils.ServiceUtils;

public class PreventPublishedDuplicatesPolicy extends AbstractPolicy implements OnUpdatePropertiesPolicy {

  private SearchService _searchService;

  public void setSearchService(SearchService searchService) {
    _searchService = searchService;
  }

  @Override
  public void onUpdateProperties(final NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {
    runSafe(new DefaultRunSafe(nodeRef) {

      @Override
      public void execute() {
        doUpdateProperties(nodeRef);
      }

    });
  }

  private void doUpdateProperties(NodeRef nodeRef) {
    // if the node does not exist, exit
    if (!_nodeService.exists(nodeRef)) {
      return;
    }

    // don't do this for working copies
    if (_nodeService.hasAspect(nodeRef, ContentModel.ASPECT_WORKING_COPY)) {
      return;
    }

    // if it's not the spaces store, exit
    if (!StoreRef.STORE_REF_WORKSPACE_SPACESSTORE.equals(nodeRef.getStoreRef())) {
      return;
    }

    // if it's not a vgr:document, exit
    if (!_nodeService.getType(nodeRef).isMatch(VgrModel.TYPE_VGR_DOCUMENT)) {
      return;
    }

    // if it hasn't the published aspect, exit
    if (!_nodeService.hasAspect(nodeRef, VgrModel.ASPECT_PUBLISHED)) {
      return;
    }

    // if it's not the Lagret, exit
    if (!isStorage(nodeRef)) {
      return;
    }

    // ok so now it's time for the version check
    // uniquity is based on dc.source.documentid and dc.identifier.version
    // TODO add a new metadata property called dc.source.version?
    String id = (String) _nodeService.getProperty(nodeRef, VgrModel.PROP_SOURCE_DOCUMENTID);
    String version = (String) _nodeService.getProperty(nodeRef, VgrModel.PROP_IDENTIFIER_VERSION);

    // if no documents are found then all is safe and nice just exit
    if (!foundDocument(id, version)) {
      return;
    }

    // if some document was found it means that a version is to be saved that
    // has the same id and version as an existing one and that is not allowed
    throw new AlfrescoRuntimeException("Node with id '" + id + "' and version '" + version + "' already exists.");
  }

  private boolean foundDocument(String id, String version) {
    StringBuffer query = new StringBuffer();
    query.append("TYPE:\"vgr:document\" AND ASPECT:\"vgr:published\" AND ");
    query.append("vgr:dc\\.source\\.documentid:\"" + id + "\" AND ");
    query.append("vgr:dc\\.identifier\\.version:\"" + version + "\"");

    SearchParameters searchParameters = new SearchParameters();

    searchParameters.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
    searchParameters.setQuery(query.toString());
    searchParameters.setLanguage(SearchService.LANGUAGE_FTS_ALFRESCO);

    ResultSet result = _searchService.query(searchParameters);

    try {
      return result.getNodeRefs().size() > 1;
    } finally {
      ServiceUtils.closeQuietly(result);
    }
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    super.afterPropertiesSet();

    _policyComponent.bindClassBehaviour(OnUpdatePropertiesPolicy.QNAME, VgrModel.TYPE_VGR_DOCUMENT, new JavaBehaviour(this, "onUpdateProperties", NotificationFrequency.TRANSACTION_COMMIT));
  }

}
