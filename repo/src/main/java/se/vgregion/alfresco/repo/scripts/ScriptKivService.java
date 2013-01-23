package se.vgregion.alfresco.repo.scripts;

import java.util.List;

import org.alfresco.repo.processor.BaseProcessorExtension;

import se.vgregion.alfresco.repo.constraints.KivService;
import se.vgregion.alfresco.repo.constraints.sync.KivUnitSynchronisation;
import se.vgregion.alfresco.repo.model.KivUnit;

public class ScriptKivService extends BaseProcessorExtension {

  private KivService _kivService;

  private KivUnitSynchronisation _kivUnitSynchronisation;

  public void setKivService(final KivService kivService) {
    _kivService = kivService;
  }

  public void setKivUnitSynchronisation(final KivUnitSynchronisation kivUnitSynchronisation) {
    _kivUnitSynchronisation = kivUnitSynchronisation;
  }

  public List<KivUnit> findOrganisationalUnits() {
    return _kivService.findOrganisationalUnits();
  }

  public List<KivUnit> findOrganisationalUnits(final String searchBase) {
    return _kivService.findOrganisationalUnits();
  }

  public List<KivUnit> findRecordsCreators() {
    return _kivService.findRecordsCreators();
  }

  public void synchronise() {
    _kivUnitSynchronisation.synchronise();
  }

}
