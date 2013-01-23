package se.vgregion.alfresco.repo.admin.patch.impl;

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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class FixDcPublisherForUnitIdPatch extends AbstractPatch implements InitializingBean {

  private static final Logger LOG = Logger.getLogger(FixDcPublisherForUnitIdPatch.class);

  private static final String MSG_SUCCESS = "vgr.patch.fixDcPublisherForUnitIdPatch.result";

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

        // get the stored unit value
        final Serializable storedUnits = document.getValue(VgrModel.PROP_PUBLISHER_FORUNIT);

        if (storedUnits == null) {
          continue;
        }

        // first parse the list of units
        final List<Unit> parsedUnits = parseUnits(storedUnits);

        // then get a list of all the units and unit ids
        final List<String> unitIds = new ArrayList<String>();

        final List<String> units = new ArrayList<String>();

        for (final Unit unit : parsedUnits) {
          unitIds.add(unit.id);
          units.add(unit.value);
        }

        // if the unitIds is empty here also, skip to the next
        if (unitIds.size() == 0 || units.size() == 0) {
          LOG.warn("The dc.publisher.forunit should not be blank for " + document.getNodeRef());

          continue;
        }

        // set the dc.publisher.forunit
        nodeService.setProperty(document.getNodeRef(), VgrModel.PROP_PUBLISHER_FORUNIT, (Serializable) units);

        // set the dc.publisher.forunit.id
        nodeService.setProperty(document.getNodeRef(), VgrModel.PROP_PUBLISHER_FORUNIT_ID, (Serializable) unitIds);

        LOG.debug("Patched vgr:dc.publisher.forunit.id for " + document.getNodeRef());
      }
    } finally {
      ServiceUtils.closeQuietly(documents);
    }


    return I18NUtil.getMessage(MSG_SUCCESS);
  }

  private ResultSet queryDocuments() {
    final String query = "TYPE:\"vgr:document\" AND (ISUNSET:\"vgr:dc.publisher.forunit.id\" OR ISNULL:\"vgr:dc.publisher.forunit.id\")";

    final SearchParameters searchParameters = new SearchParameters();

    searchParameters.setLanguage(SearchService.LANGUAGE_LUCENE);
    searchParameters.setQuery(query);
    searchParameters.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
    searchParameters.setMaxItems(-1);

    return searchService.query(searchParameters);
  }

  private List<Unit> parseUnits(final Serializable storedUnits) {
    @SuppressWarnings("unchecked")
    final List<String> unparsedUnits = (List<String>) storedUnits;

    final List<Unit> units = new ArrayList<Unit>();

    for (final String unparsedUnit : unparsedUnits) {
      final Unit unit = parseUnit(unparsedUnit);

      if (unit == null) {
        continue;
      }

      units.add(unit);
    }

    return units;
  }

  private Unit parseUnit(final String unparsedUnit) {
    final String[] parts = StringUtils.split(unparsedUnit, "|");

    if (parts.length != 2) {
      return null;
    }

    final Unit unit = new Unit(parts[0], parts[1]);

    return unit;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    Assert.notNull(_behaviourFilter);
  }

  private class Unit {

    Unit(final String id, final String value) {
      this.id = id;
      this.value = value;
    }

    String id;

    String value;

  }

}
