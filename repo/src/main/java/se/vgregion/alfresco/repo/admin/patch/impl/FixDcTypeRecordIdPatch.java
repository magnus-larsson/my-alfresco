package se.vgregion.alfresco.repo.admin.patch.impl;

import java.io.Serializable;

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
import se.vgregion.alfresco.repo.utils.impl.ServiceUtilsImpl;

public class FixDcTypeRecordIdPatch extends AbstractPatch implements InitializingBean {

  private static final Logger LOG = Logger.getLogger(FixDcTypeRecordIdPatch.class);

  private static final String MSG_SUCCESS = "vgr.patch.fixDcTypeRecordIdPatch.result";

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

    final String query = "TYPE:\"vgr:document\" AND (ISUNSET:\"vgr:dc.type.record.id\" OR ISNULL:\"vgr:dc.type.record.id\")";

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
        final Serializable storedrecordTypePath = document.getValue(VgrModel.PROP_TYPE_RECORD);
        final Serializable storedRecordTypeId = document.getValue(VgrModel.PROP_TYPE_RECORD_ID);

        String recordTypePath = storedrecordTypePath != null ? storedrecordTypePath.toString() : null;
        String recordTypeId = storedRecordTypeId != null ? storedRecordTypeId.toString() : null;

        // if the recordType is empty, just continue
        if (StringUtils.isBlank(recordTypePath)) {
          continue;
        }

        // if the record.id value is stored, just continue
        if (StringUtils.isNotBlank(recordTypeId)) {
          continue;
        }

        recordTypePath = fixOldValuesForRecordTypePath(recordTypePath);

        // if we're here, get the id for a record
        recordTypeId = getRecordId(recordTypePath);

        // if the recordTypeId is blank here also, skip to the next
        if (StringUtils.isBlank(recordTypeId)) {
          LOG.warn("The record type id should not be blank for the record type '" + recordTypePath + "'");

          continue;
        }

        // set the recordTypeId
        nodeService.setProperty(document.getNodeRef(), VgrModel.PROP_TYPE_RECORD_ID, recordTypeId);

        LOG.debug("Patched vgr:dc.type.record.id for " + document.getNodeRef());
      }
    } finally {
      ServiceUtilsImpl.closeQuietly(documents);
    }

    return I18NUtil.getMessage(MSG_SUCCESS);
  }

  private String fixOldValuesForRecordTypePath(final String recordTypePath) {
    String result = recordTypePath;

    if (result.equalsIgnoreCase("Hemsidor/Information/Lednings- och förvaltningsövergripande uppgifter")) {
      result = "Hemsidor [Information/Lednings- och förvaltningsövergripande uppgifter]";
    }

    if (result.equalsIgnoreCase("Supportdokumentation/IT-administration/Lednings- och förvaltningsövergripande uppgifter")) {
      result = "Supportdokumentation [IT-administration/Lednings- och förvaltningsövergripande uppgifter]";
    }

    if (result.equalsIgnoreCase("Anbudsförfrågan eller anbudsinbjudan eller anbudsinfordran*/Framtagning av förfrågningsunderlag/Upphandling")) {
      result = "Anbudsförfrågan eller anbudsinbjudan eller anbudsinfordran* [Framtagning av förfrågningsunderlag/Upphandling]";
    }

    if (result
        .equalsIgnoreCase("Undervisningsmaterial, OH bilder, material som harsammanställts i en rapport eller dylikt/Allmän administration/Lednings- och förvaltningsövergripande uppgifter")) {
      result = "Undervisningsmaterial, OH bilder, material som harsammanställts i en rapport eller dylikt [Allmän administration/Lednings- och förvaltningsövergripande uppgifter]";
    }

    return result;
  }

  private String getRecordId(final String recordTypePath) {
    final NodeRef recordType = _apelonService.getRecordTypeFromPath(recordTypePath);

    return recordType != null ? (String) nodeService.getProperty(recordType, VgrModel.PROP_APELON_INTERNALID) : null;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    Assert.notNull(_apelonService);
    Assert.notNull(_behaviourFilter);
  }

}
