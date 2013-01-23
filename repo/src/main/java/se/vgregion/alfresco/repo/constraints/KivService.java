package se.vgregion.alfresco.repo.constraints;

import java.util.Date;
import java.util.List;

import se.vgregion.alfresco.repo.model.KivUnit;

public interface KivService {

  List<KivUnit> findOrganisationalUnits();

  List<KivUnit> findOrganisationalUnits(final String searchBase);

  List<KivUnit> findOrganisationalUnits(Date modifyTimestamp);

  List<KivUnit> findOrganisationalUnits(final String searchBase, Date modifyTimestamp);

  List<KivUnit> findRecordsCreators();

}
