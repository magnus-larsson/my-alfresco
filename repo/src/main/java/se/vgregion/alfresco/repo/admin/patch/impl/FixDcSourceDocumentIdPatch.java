package se.vgregion.alfresco.repo.admin.patch.impl;

import java.io.Serializable;
import java.util.Collection;

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

public class FixDcSourceDocumentIdPatch extends AbstractPatch {

  private static final Logger LOG = Logger.getLogger(FixDcSourceDocumentIdPatch.class);

  private static final String MSG_SUCCESS = "vgr.patch.fixDcSourceDocumentIdPatch.result";

  private BehaviourFilter _behaviourFilter;

  public void setBehaviourFilter(final BehaviourFilter behaviourFilter) {
    _behaviourFilter = behaviourFilter;
  }

  @Override
  protected String applyInternal() throws Exception {
    _behaviourFilter.disableAllBehaviours();

    final String query = "TYPE:\"vgr:document\" AND ASPECT:\"vgr:published\"";

    final SearchParameters searchParameters = new SearchParameters();

    searchParameters.setLanguage(SearchService.LANGUAGE_LUCENE);
    searchParameters.setQuery(query);
    searchParameters.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
    searchParameters.setMaxItems(-1);

    final ResultSet documents = searchService.query(searchParameters);

    try {
      for (final ResultSetRow document : documents) {
        final Serializable source = document.getValue(ContentModel.PROP_COPY_REFERENCE);

        Serializable sourceDocumentId;

        if (source == null || StringUtils.isBlank(source.toString())) {
          @SuppressWarnings("unchecked")
          final Collection<String> identifiers = (Collection<String>) document.getValue(VgrModel.PROP_IDENTIFIER);

          if (identifiers.isEmpty()) {
            LOG.warn("Could not find either cm:source or vgr:dc.identifier on the node " + document.getNodeRef());

            continue;
          }

          String identifier = identifiers.iterator().next();

          if (StringUtils.contains(identifier, "workspace/SpacesStore")) {
            final int start = identifier.indexOf("workspace/SpacesStore/");

            int end = identifier.indexOf("?a=true");

            if (end == -1) {
              end = identifier.indexOf("?a=false");
            }

            identifier = "workspace://SpacesStore/" + identifier.substring(start + 22, end);
          } else if (StringUtils.contains(identifier, "link.asp?content=")) {
            final int start = identifier.indexOf("link.asp?content=");

            identifier = identifier.substring(start + 17);
          } else {
            LOG.error("Wrong identifier '" + identifier + "' on node " + document.getNodeRef());
          }

          sourceDocumentId = identifier;
        } else {
          sourceDocumentId = source;
        }

        nodeService.setProperty(document.getNodeRef(), VgrModel.PROP_SOURCE_DOCUMENTID, sourceDocumentId);
      }
    } finally {
      ServiceUtils.closeQuietly(documents);
    }

    return I18NUtil.getMessage(MSG_SUCCESS);
  }

}
