package se.vgregion.alfresco.repo.admin.patch.impl;

import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.extensions.surf.util.I18NUtil;

import se.vgregion.alfresco.repo.model.VgrModel;
import se.vgregion.alfresco.repo.utils.impl.ServiceUtilsImpl;

/**
 * Switches a=true -> a=false on all vgr:dc.identifier and
 * vgr:dc.identifier.native links.
 *
 * @author Niklas Ekman (niklas.ekman@redpill-linpro.com)
 */
public class SwitchIdentifierAttachmentInlinePatch extends AbstractPatch {

  private static final String MSG_SUCCESS = "";

  private static final Logger LOG = Logger.getLogger(SwitchIdentifierAttachmentInlinePatch.class);

  private BehaviourFilter _behaviourFilter;

  public void setBehaviourFilter(final BehaviourFilter behaviourFilter) {
    _behaviourFilter = behaviourFilter;
  }

  @Override
  protected String applyInternal() throws Exception {
    _behaviourFilter.disableBehaviour();

    executeIdentifierChange(VgrModel.PROP_IDENTIFIER);

    executeIdentifierChange(VgrModel.PROP_IDENTIFIER_NATIVE);

    return I18NUtil.getMessage(MSG_SUCCESS);
  }

  private void executeIdentifierChange(final QName property) {
    final ResultSet documents = executeQuery(property);

    LOG.info("Documents to patch " + property.toString() + " for: " + documents.length());

    int count = 0;

    try {
      for (final ResultSetRow document : documents) {
        changeIdentifier(document, property);

        count++;
      }
    } finally {
      ServiceUtilsImpl.closeQuietly(documents);
    }

    LOG.info(property.toString() + " set for " + count + " nodes.");
  }

  private ResultSet executeQuery(final QName property) {
    final String query = "TYPE:\"vgr:document\" AND ISNOTNULL:\"" + property.toString() + "\"";

    final SearchParameters searchParameters = new SearchParameters();

    searchParameters.setLanguage(SearchService.LANGUAGE_LUCENE);
    searchParameters.setQuery(query);
    searchParameters.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
    searchParameters.setMaxItems(-1);

    return searchService.query(searchParameters);
  }

  private void changeIdentifier(final ResultSetRow document, final QName property) {
    final String storedIdentifier = (String) document.getValue(property);

    if (StringUtils.isBlank(storedIdentifier)) {
      return;
    }

    final String newIdentifier = StringUtils.replace(storedIdentifier, "a=true", "a=false");

    if (storedIdentifier.equals(newIdentifier)) {
      return;
    }

    nodeService.setProperty(document.getNodeRef(), property, newIdentifier);
  }

}
