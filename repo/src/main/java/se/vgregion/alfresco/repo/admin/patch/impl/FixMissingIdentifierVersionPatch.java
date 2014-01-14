package se.vgregion.alfresco.repo.admin.patch.impl;

import java.io.Serializable;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.extensions.surf.util.I18NUtil;

import se.vgregion.alfresco.repo.model.VgrModel;
import se.vgregion.alfresco.repo.utils.ServiceUtils;

public class FixMissingIdentifierVersionPatch extends AbstractPatch {

  private static final String MSG_SUCCESS = "vgr.patch.fixMissingIdentifierVersionPatch.result";

  private static final Logger LOG = Logger.getLogger(FixMissingIdentifierVersionPatch.class);

  private BehaviourFilter _behaviourFilter;

  public void setBehaviourFilter(final BehaviourFilter behaviourFilter) {
    _behaviourFilter = behaviourFilter;
  }

  @Override
  protected String applyInternal() throws Exception {
    _behaviourFilter.disableBehaviour();

    final String query = "TYPE:\"vgr:document\" AND ISUNSET:\"vgr:dc.identifier.version\"";
    final SearchParameters searchParameters = new SearchParameters();
    searchParameters.setLanguage(SearchService.LANGUAGE_LUCENE);
    searchParameters.setQuery(query);
    searchParameters.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
    searchParameters.setMaxItems(-1);

    final ResultSet documents = searchService.query(searchParameters);

    LOG.info("Documents to patch vgr:dc.identifier.version for: " + documents.length());

    int count = 0;

    try {
      for (final ResultSetRow document : documents) {
        final Serializable version = document.getValue(VgrModel.PROP_IDENTIFIER_VERSION);

        if (version != null && StringUtils.isNotBlank(version.toString())) {
          continue;
        }

        // if the node is not version, add the versionable aspect
        if (!nodeService.hasAspect(document.getNodeRef(), ContentModel.ASPECT_VERSIONABLE)) {
          nodeService.addAspect(document.getNodeRef(), ContentModel.ASPECT_VERSIONABLE, null);
        }

        // If the version is not set, set it to the same as cm:versionLabel
        Serializable versionLabel = document.getValue(ContentModel.PROP_VERSION_LABEL);

        // if no version label, set default version to 0.1
        if (versionLabel == null || StringUtils.isBlank(versionLabel.toString())) {
          versionLabel = "0.1";
        }

        nodeService.setProperty(document.getNodeRef(), VgrModel.PROP_IDENTIFIER_VERSION, versionLabel);

        count++;
      }
    } finally {
      ServiceUtils.closeQuietly(documents);
    }

    LOG.info("vgr:dc.identifier.version set for " + count + " nodes.");

    return I18NUtil.getMessage(MSG_SUCCESS);
  }

}
