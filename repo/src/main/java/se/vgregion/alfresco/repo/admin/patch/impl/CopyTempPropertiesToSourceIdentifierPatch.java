package se.vgregion.alfresco.repo.admin.patch.impl;

import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.apache.commons.lang.StringUtils;
import org.springframework.extensions.surf.util.I18NUtil;

import se.vgregion.alfresco.repo.model.VgrModel;
import se.vgregion.alfresco.repo.utils.ServiceUtils;

public class CopyTempPropertiesToSourceIdentifierPatch extends AbstractPatch {

  private static final String MSG_SUCCESS = "vgr.patch.copyTempPropertiesToSourceIdentifierPatch.result";

  private BehaviourFilter _behaviourFilter;

  public void setBehaviourFilter(final BehaviourFilter behaviourFilter) {
    _behaviourFilter = behaviourFilter;
  }

  @Override
  protected String applyInternal() throws Exception {
    _behaviourFilter.disableBehaviour();

    final String query = "TYPE:\"vgr:document\"";

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

        final String tempIdentifier = getTempIdentifier(document);

        final String tempSource = getTempSource(document);

        // the temp identifier is going into the identifier
        if (StringUtils.isNotBlank(tempIdentifier)) {
          nodeService.setProperty(document.getNodeRef(), VgrModel.PROP_IDENTIFIER, tempIdentifier);
        }

        // the temp source is going into the source
        if (StringUtils.isNotBlank(tempSource)) {
          nodeService.setProperty(document.getNodeRef(), VgrModel.PROP_SOURCE, tempSource);
        }
      }
    } finally {
      ServiceUtils.closeQuietly(documents);
    }

    return I18NUtil.getMessage(MSG_SUCCESS);
  }

  private String getTempIdentifier(final ResultSetRow document) {
    return (String) document.getValue(VgrModel.PROP_IDENTIFIER_TEMP);
  }

  private String getTempSource(final ResultSetRow document) {
    return (String) document.getValue(VgrModel.PROP_SOURCE_TEMP);
  }

}
