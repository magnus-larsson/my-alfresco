package se.vgregion.alfresco.repo.scripts;

import org.alfresco.repo.jscript.BaseScopableProcessorExtension;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.LimitBy;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import se.vgregion.alfresco.repo.storage.StorageService;
import se.vgregion.alfresco.repo.utils.impl.ServiceUtilsImpl;

public class ScriptVgrDok extends BaseScopableProcessorExtension implements InitializingBean {

  private static final Logger LOG = Logger.getLogger(ScriptVgrDok.class);

  protected ServiceUtilsImpl _serviceUtils;

  protected SearchService _searchService;

  protected StorageService _storageService;

  protected NodeService _nodeService;

  public void setServiceUtils(final ServiceUtilsImpl serviceUtils) {
    _serviceUtils = serviceUtils;
  }

  public void setSearchService(final SearchService searchService) {
    _searchService = searchService;
  }

  public void setStorageService(final StorageService storageService) {
    _storageService = storageService;
  }

  public void setNodeService(final NodeService nodeService) {
    _nodeService = nodeService;
  }

  public int publish() {
    final String query = "ASPECT:\"vgr:vgrdok\" AND ISNOTNULL:\"vgr:dc.publisher\" AND ISNOTNULL:\"vgr:dc.type.document\" AND ISNOTNULL:\"vgr:dc.type.record\" AND ISNOTNULL:\"vgr:dc.publisher.project-assignment\" AND ISNOTNULL:\"vgr:hc.status.document\" AND -ASPECT:\"vgr:published\"";

    final SearchParameters searchParameters = new SearchParameters();

    searchParameters.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
    searchParameters.setLanguage(SearchService.LANGUAGE_LUCENE);
    searchParameters.setQuery(query);
    searchParameters.setLimitBy(LimitBy.UNLIMITED);
    searchParameters.setMaxItems(-1);

    final ResultSet result = _searchService.query(searchParameters);

    int published = 0;

    try {
      for (final ResultSetRow row : result) {
        final NodeRef nodeRef = row.getNodeRef();

        if (publishNode(nodeRef)) {
          published++;
        }
      }
    } finally {
      ServiceUtilsImpl.closeQuietly(result);
    }

    return published;
  }

  private boolean publishNode(final NodeRef nodeRef) {
    final NodeRef publishedNodeRef = _storageService.getPublishedNodeRef(nodeRef);

    if (publishedNodeRef != null) {
      return false;
    }

    _storageService.publishToStorage(nodeRef);

    LOG.info("Published: " + nodeRef);
    LOG.info("");

    return true;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    Assert.notNull(_serviceUtils);
    Assert.notNull(_searchService);
    Assert.notNull(_storageService);
    Assert.notNull(_nodeService);
  }

}