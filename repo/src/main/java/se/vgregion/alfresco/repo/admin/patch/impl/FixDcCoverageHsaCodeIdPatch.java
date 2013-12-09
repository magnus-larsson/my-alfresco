package se.vgregion.alfresco.repo.admin.patch.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
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
import se.vgregion.alfresco.repo.utils.impl.ServiceUtilsImpl;

public class FixDcCoverageHsaCodeIdPatch extends AbstractPatch implements InitializingBean {

  private static final Logger LOG = Logger.getLogger(FixDcCoverageHsaCodeIdPatch.class);

  private static final String MSG_SUCCESS = "vgr.patch.fixDcCoverageHsaCodeIdPatch.result";

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

    final String query = "TYPE:\"vgr:document\" AND (ISUNSET:\"vgr:dc.coverage.hsacode.id\" OR ISNULL:\"vgr:dc.coverage.hsacode.id\")";

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

        // get the stored record value
        final Serializable storedHsaCodes = document.getValue(VgrModel.PROP_COVERAGE_HSACODE);

        @SuppressWarnings("unchecked")
        final List<String> hsaCodes = (List<String>) (storedHsaCodes != null ? storedHsaCodes : null);

        // if the hsaCodes is empty, exit
        if (hsaCodes == null || hsaCodes.size() == 0) {
          continue;
        }

        // if we're here, get the id for a record
        final List<String> hsaCodeIds = getHsaCodeIds(hsaCodes);

        // if the recordTypeId is blank here also, skip to the next
        if (hsaCodeIds.size() == 0) {
          LOG.warn("The hsa code ids should not be blank for " + document.getNodeRef());

          continue;
        }

        // set the recordTypeId
        nodeService.setProperty(document.getNodeRef(), VgrModel.PROP_COVERAGE_HSACODE_ID, (Serializable) hsaCodeIds);

        LOG.debug("Patched vgr:dc.coverage.hsacode.id for " + document.getNodeRef());
      }
    } finally {
      ServiceUtilsImpl.closeQuietly(documents);
    }

    return I18NUtil.getMessage(MSG_SUCCESS);
  }

  private List<String> getHsaCodeIds(final List<String> hsaCodes) {
    final List<String> result = new ArrayList<String>();

    for (String hsaCode : hsaCodes) {
      hsaCode = hsaCode.replace("&#44;", ",");

      final String hsaCodeId = getHsaCodeId(hsaCode);

      if (StringUtils.isBlank(hsaCodeId)) {
        continue;
      }

      result.add(hsaCodeId);
    }

    return result;
  }

  private String getHsaCodeId(final String hsaCode) {
    final List<NodeRef> hsaCodes = _apelonService.getHsacodeList();

    for (final NodeRef hsaCodeNodeRef : hsaCodes) {
      final String code = (String) nodeService.getProperty(hsaCodeNodeRef, VgrModel.PROP_APELON_NAME);

      if (!code.equalsIgnoreCase(hsaCode)) {
        continue;
      }

      final List<ChildAssociationRef> properties = nodeService.getChildAssocs(hsaCodeNodeRef);

      for (final ChildAssociationRef property : properties) {
        final String propertyName = (String) nodeService.getProperty(property.getChildRef(), VgrModel.PROP_APELON_KEY);

        if (!propertyName.equalsIgnoreCase("Verksamhetskod")) {
          continue;
        }

        @SuppressWarnings("unchecked")
        final List<String> value = (List<String>) nodeService.getProperty(property.getChildRef(), VgrModel.PROP_APELON_VALUE);

        return value != null && value.size() > 0 ? value.get(0) : null;
      }
    }

    return null;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    Assert.notNull(_apelonService);
    Assert.notNull(_behaviourFilter);
  }

}
