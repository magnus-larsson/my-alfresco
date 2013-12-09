package se.vgregion.alfresco.repo.admin.patch.impl;

import java.io.Serializable;
import java.util.Collection;

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
import se.vgregion.alfresco.repo.utils.impl.ServiceUtilsImpl;

public class FixDcSourceOriginPatch extends AbstractPatch {

  private static final Logger LOG = Logger.getLogger(FixDcSourceOriginPatch.class);

  private static final String MSG_SUCCESS = "vgr.patch.fixDcSourceOriginPatch.result";

  private BehaviourFilter _behaviourFilter;

  public void setBehaviourFilter(final BehaviourFilter behaviourFilter) {
    _behaviourFilter = behaviourFilter;
  }

  @Override
  protected String applyInternal() throws Exception {
    _behaviourFilter.disableBehaviour();

    final String query = "TYPE:\"vgr:document\" AND ASPECT:\"vgr:published\" AND (ISUNSET:\"vgr:dc.source.origin\" OR ISNULL:\"vgr:dc.source.origin\")";

    final SearchParameters searchParameters = new SearchParameters();

    searchParameters.setLanguage(SearchService.LANGUAGE_LUCENE);
    searchParameters.setQuery(query);
    searchParameters.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
    searchParameters.setMaxItems(-1);

    final ResultSet documents = searchService.query(searchParameters);

    try {
      for (final ResultSetRow document : documents) {
        // get the stored origin
        final Serializable storedOrigin = document.getValue(VgrModel.PROP_SOURCE_ORIGIN);

        // if the origin is already stored, just continue
        if (storedOrigin != null && StringUtils.isNotBlank(storedOrigin.toString())) {
          continue;
        }

        final String origin = getOrigin(document);

        // if the origin is blank, skip to the next
        if (StringUtils.isBlank(origin)) {
          continue;
        }

        // set the origin
        nodeService.setProperty(document.getNodeRef(), VgrModel.PROP_SOURCE_ORIGIN, origin);
      }
    } finally {
      ServiceUtilsImpl.closeQuietly(documents);
    }

    return I18NUtil.getMessage(MSG_SUCCESS);
  }

  private String getIdentifier(final ResultSetRow document) {
    // get the identifier, this is a hint of the system origin
    @SuppressWarnings("unchecked")
    final Collection<String> identifiers = (Collection<String>) document.getValue(VgrModel.PROP_IDENTIFIER);

    // if the identifier is empty (should not be...) continue
    if (identifiers == null || identifiers.isEmpty()) {
      LOG.warn("Could not find vgr:dc.identifier on the node " + document.getNodeRef());

      return null;
    }

    // get the first best identifier
    return identifiers.iterator().next();
  }

  public String getOrigin(final ResultSetRow document) {
    final String identifier = getIdentifier(document);

    String source = "";

    if (StringUtils.isBlank(identifier)) {
      source = getSource(document);
    }

    String origin;

    // get the origin from the identifier, either Alfresco or Barium or null if
    // neither
    if (StringUtils.contains(identifier, "SpacesStore") || StringUtils.contains(source, "SpacesStore")) {
      origin = "Alfresco";
    } else if (StringUtils.contains(identifier, "link.asp?content=")) {
      origin = "Barium";
    } else {
      LOG.error("Couldn't find either source nor identifier on node " + document.getNodeRef());

      origin = null;
    }

    return origin;
  }

  private String getSource(final ResultSetRow document) {
    final Serializable source = document.getValue(VgrModel.PROP_SOURCE_DOCUMENTID);

    if (source == null || StringUtils.isBlank(source.toString())) {
      return null;
    }

    return source.toString();
  }

}
