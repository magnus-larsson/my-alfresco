package se.vgregion.alfresco.migration.vgrdok;

import java.io.File;
import java.util.Date;

import org.apache.commons.lang.builder.ToStringBuilder;

public class VgrDokDocument {

  public String title; // mapped
  public String documentId; // mapped
  public String uploadedBy; // mapped
  public String createdBy; // not mapped - no relevance
  public Date createdDate; // mapped
  public String filename; // mapped
  public String filepath; // mapped
  public String version; // mapped
  public String documentType; // mapped
  public String documentStatus; // not mapped
  public String publishedBy; // mapped
  public Date publishedDate; // mapped
  public String description; // mapped
  public String publishType; // not mapped
  public String businessAreaMain; // not mapped
  public String businessAreaSub; // not mapped
  public String responsibleProject; // mapped
  public String responsiblePerson; // mapped
  public String documentSeries; // mapped
  public String externalReference; // mapped
  public Date availableDateFrom; // not mapped
  public Date availableDateTo; // not mapped
  public String accessZone; // mapped
  public String universalID; // mapped
  public File file; // mapped
  public String author; // mapped
  public String mimetype; // mapped
  public long size; // mapped
  public String name;
  public String checksum;
  public boolean duplicate = false;

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

  @Override
  public boolean equals(final Object object) {
    if (this == object) {
      return true;
    }

    if (!(object instanceof VgrDokDocument)) {
      return false;
    }

    final VgrDokDocument that = (VgrDokDocument) object;

    return this.name.equalsIgnoreCase(that.name);
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }

}
