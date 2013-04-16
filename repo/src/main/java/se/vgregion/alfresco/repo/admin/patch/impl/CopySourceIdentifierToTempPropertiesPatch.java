package se.vgregion.alfresco.repo.admin.patch.impl;

import java.util.Collection;

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

public class CopySourceIdentifierToTempPropertiesPatch extends AbstractPatch {

  private static final String MSG_SUCCESS = "vgr.patch.copySourceIdentifierToTempPropertiesPatch.result";

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

        final String identifier = getIdentifier(document);

        final String source = getSource(document);

        // the stored identifier is going into the source_temp
        if (StringUtils.isNotBlank(identifier)) {
          nodeService.setProperty(document.getNodeRef(), VgrModel.PROP_SOURCE_TEMP, identifier);
        }

        // the stored source is going into the identifier_temp
        if (StringUtils.isNotBlank(source)) {
          nodeService.setProperty(document.getNodeRef(), VgrModel.PROP_IDENTIFIER_TEMP, source);
        }

        // now, the original fields has to be nulled, in order to be able to
        // remove the properties from the model file at a later stage
        if (document.getValue(VgrModel.PROP_SOURCE) != null) {
          nodeService.setProperty(document.getNodeRef(), VgrModel.PROP_SOURCE, null);
        }

        if (document.getValue(VgrModel.PROP_IDENTIFIER) != null) {
          nodeService.setProperty(document.getNodeRef(), VgrModel.PROP_IDENTIFIER, null);
        }
      }
    } finally {
      ServiceUtils.closeQuietly(documents);
    }


    return I18NUtil.getMessage(MSG_SUCCESS);
  }

  private String getIdentifier(final ResultSetRow document) {
    @SuppressWarnings("unchecked")
    final Collection<String> identifiers = (Collection<String>) document.getValue(VgrModel.PROP_IDENTIFIER);

    if (identifiers == null || identifiers.isEmpty()) {
      return null;
    }

    return identifiers.iterator().next();
  }

  private String getSource(final ResultSetRow document) {
    @SuppressWarnings("unchecked")
    final Collection<String> sources = (Collection<String>) document.getValue(VgrModel.PROP_SOURCE);

    if (sources == null || sources.isEmpty()) {
      return null;
    }

    return sources.iterator().next();
  }

}
