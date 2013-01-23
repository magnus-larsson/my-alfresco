package se.vgregion.alfresco.repo.admin.patch.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
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

import se.vgregion.alfresco.repo.model.VgrModel;
import se.vgregion.alfresco.repo.utils.ServiceUtils;

public class FixDcCreatorRecordsCreatorIdPatch extends AbstractPatch implements InitializingBean {

  private static final Logger LOG = Logger.getLogger(FixDcCreatorRecordsCreatorIdPatch.class);

  private static final String MSG_SUCCESS = "vgr.patch.fixDcCreatorRecordsCreatorIdPatch.result";

  private BehaviourFilter _behaviourFilter;

  public void setBehaviourFilter(final BehaviourFilter behaviourFilter) {
    _behaviourFilter = behaviourFilter;
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
    _behaviourFilter.disableAllBehaviours();

    final ResultSet documents = queryDocuments();

    try {
      for (final ResultSetRow document : documents) {
        if (!nodeService.exists(document.getNodeRef())) {
          continue;
        }

        // get the stored recordscreator value
        final Serializable storedRecordsCreators = document.getValue(VgrModel.PROP_CREATOR_RECORDSCREATOR);

        if (storedRecordsCreators == null) {
          continue;
        }

        // first parse the list of recordscreators
        final List<RecordsCreator> parsedRecordsCreators = parseRecordsCreators(storedRecordsCreators);

        // then get a list of all the recordscreators and recordscreators ids
        final List<String> recordsCreatorIds = new ArrayList<String>();

        final List<String> recordsCreators = new ArrayList<String>();

        for (final RecordsCreator recordsCreator : parsedRecordsCreators) {
          recordsCreatorIds.add(recordsCreator.id);
          recordsCreators.add(recordsCreator.value);
        }

        // if the recordsCreatorIds is empty here also, skip to the next
        if (recordsCreators.size() == 0 || recordsCreators.size() == 0) {
          LOG.warn("The dc.creator.recordscreator should not be blank for " + document.getNodeRef());

          continue;
        }

        // set the dc.creator.recordscreator
        nodeService.setProperty(document.getNodeRef(), VgrModel.PROP_CREATOR_RECORDSCREATOR, (Serializable) recordsCreators);

        // set the dc.creator.recordscreator.id
        nodeService.setProperty(document.getNodeRef(), VgrModel.PROP_CREATOR_RECORDSCREATOR_ID, (Serializable) recordsCreatorIds);

        LOG.debug("Patched vgr:dc.creator.recordscreator.id for " + document.getNodeRef());
      }
    } finally {
      ServiceUtils.closeQuietly(documents);
    }

    return I18NUtil.getMessage(MSG_SUCCESS);
  }

  private ResultSet queryDocuments() {
    final String query = "TYPE:\"vgr:document\" AND (ISUNSET:\"vgr:dc.creator.recordscreator.id\" OR ISNULL:\"vgr:dc.creator.recordscreator.id\")";

    final SearchParameters searchParameters = new SearchParameters();

    searchParameters.setLanguage(SearchService.LANGUAGE_LUCENE);
    searchParameters.setQuery(query);
    searchParameters.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
    searchParameters.setMaxItems(-1);

    return searchService.query(searchParameters);
  }

  private List<RecordsCreator> parseRecordsCreators(final Serializable storedRecordsCreators) {
    @SuppressWarnings("unchecked")
    final List<String> unparsedRecordsCreators = (List<String>) storedRecordsCreators;

    final List<RecordsCreator> recordsCreators = new ArrayList<RecordsCreator>();

    for (final String unparsedRecordsCreator : unparsedRecordsCreators) {
      final RecordsCreator recordsCreator = parseRecordsCreator(unparsedRecordsCreator);

      if (recordsCreator == null) {
        continue;
      }

      recordsCreators.add(recordsCreator);
    }

    return recordsCreators;
  }

  private RecordsCreator parseRecordsCreator(final String unparsedRecordsCreator) {
    final String[] parts = StringUtils.split(unparsedRecordsCreator, "|");

    if (parts.length != 2) {
      return null;
    }

    return new RecordsCreator(parts[0], parts[1]);
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    Assert.notNull(_behaviourFilter);
  }

  private class RecordsCreator {

    RecordsCreator(final String id, final String value) {
      this.id = id;
      this.value = value;
    }

    String id;

    String value;

  }

}
