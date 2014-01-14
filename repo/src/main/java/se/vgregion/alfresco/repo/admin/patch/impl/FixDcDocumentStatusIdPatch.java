package se.vgregion.alfresco.repo.admin.patch.impl;

import java.io.Serializable;
import java.util.List;

import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.util.Assert;

import se.vgregion.alfresco.repo.constraints.ApelonService;
import se.vgregion.alfresco.repo.model.VgrModel;
import se.vgregion.alfresco.repo.utils.ServiceUtils;

public class FixDcDocumentStatusIdPatch extends AbstractPatch implements InitializingBean {

  private static final Logger LOG = Logger.getLogger(FixDcDocumentStatusIdPatch.class);

  private static final String MSG_SUCCESS = "vgr.patch.fixDcDocumentStatusIdPatch.result";

  private BehaviourFilter _behaviourFilter;

  private ApelonService _apelonService;

  public void setBehaviourFilter(final BehaviourFilter behaviourFilter) {
    _behaviourFilter = behaviourFilter;
  }

  public void setApelonService(final ApelonService apelonService) {
    _apelonService = apelonService;
  }

  @Override
  protected String applyInternal() throws Exception {
    return AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<String>() {

      @Override
      public String doWork() throws Exception {
        return doApply();
      }

    }, AuthenticationUtil.getSystemUserName());
  }

  private String doApply() {
    _behaviourFilter.disableBehaviour();

    final String query = "TYPE:\"vgr:document\" AND (ISUNSET:\"vgr:vgr.status.document.id\" OR ISNULL:\"vgr:vgr.status.document.id\")";

    final SearchParameters searchParameters = new SearchParameters();

    searchParameters.setLanguage(SearchService.LANGUAGE_LUCENE);
    searchParameters.setQuery(query);
    searchParameters.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
    searchParameters.setMaxItems(-1);

    final ResultSet documents = searchService.query(searchParameters);

    try {
      for (final ResultSetRow document : documents) {
        if (!nodeService.exists(document.getNodeRef())) {
          continue;
        }

        // get the stored status value
        final Serializable storedStatus = document.getValue(VgrModel.PROP_STATUS_DOCUMENT);

        final String status = (String) (storedStatus != null ? storedStatus : null);

        if (StringUtils.isBlank(status)) {
          continue;
        }

        // if we're here, get the id for a status
        final String statusId = getStatusId(status);

        // if the statusId is blank here also, skip to the next
        if (StringUtils.isBlank(statusId)) {
          LOG.warn("The status id should not be blank for " + status);

          continue;
        }

        // set the vgr:vgr.status.document.id
        nodeService.setProperty(document.getNodeRef(), VgrModel.PROP_STATUS_DOCUMENT_ID, statusId);

        LOG.debug("Patched vgr:vgr.status.document.id for " + document.getNodeRef());
      }
    } finally {
      ServiceUtils.closeQuietly(documents);
    }

    return I18NUtil.getMessage(MSG_SUCCESS);
  }

  private String getStatusId(final String status) {
    final List<NodeRef> statuses = _apelonService.getDocumentStatusList();

    for (final NodeRef statusNode : statuses) {
      final String name = (String) nodeService.getProperty(statusNode, VgrModel.PROP_APELON_NAME);

      if (!name.equalsIgnoreCase(status)) {
        continue;
      }

      return (String) nodeService.getProperty(statusNode, VgrModel.PROP_APELON_INTERNALID);
    }

    return null;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    Assert.notNull(_apelonService);
    Assert.notNull(_behaviourFilter);
  }

}
