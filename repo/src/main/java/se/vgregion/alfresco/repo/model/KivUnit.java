package se.vgregion.alfresco.repo.model;

import org.apache.commons.lang.builder.ToStringBuilder;

public class KivUnit {

  private String _organisationalUnit;

  private String _hsaIdentity;

  private String _distinguishedName;

  public String getOrganisationalUnit() {
    return _organisationalUnit;
  }

  public void setOrganisationalUnit(final String organisationalUnit) {
    _organisationalUnit = organisationalUnit;
  }

  public String getHsaIdentity() {
    return _hsaIdentity;
  }

  public void setHsaIdentity(final String hsaIdentity) {
    _hsaIdentity = hsaIdentity;
  }

  public String getDistinguishedName() {
    return _distinguishedName;
  }

  public void setDistinguishedName(final String distinguishedName) {
    _distinguishedName = distinguishedName;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

}
