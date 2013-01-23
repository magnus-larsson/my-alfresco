package se.vgregion.alfresco.repo.forms.processor.node;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.alfresco.repo.forms.processor.node.ContentModelItemData;
import org.alfresco.repo.forms.processor.node.FormFieldConstants;
import org.alfresco.repo.forms.processor.node.PropertyFieldProcessor;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO8601DateFormat;
import org.springframework.util.StringUtils;

public class VgrPropertyFieldProcessor extends PropertyFieldProcessor {

  protected static final String DELIMITER = "#alf#";

  @Override
  public Object getValue(final QName name, final ContentModelItemData<?> data) {
    final Serializable value = data.getPropertyValue(name);

    if (value == null) {
      return getDefaultValue(name, data);
    }

    if (value instanceof Collection<?>) {
      // temporarily add repeating field data as a comma
      // separated list, this will be changed to using
      // a separate field for each value once we have full
      // UI support in place.
      final Collection<?> values = (Collection<?>) value;

      // if the non empty collection is a List of Date objects
      // we need to convert each date to a ISO8601 format
      if (value instanceof List<?> && !values.isEmpty()) {
        final List<?> list = (List<?>) values;

        if (list.get(0) instanceof Date) {
          final List<String> isoDates = new ArrayList<String>(list.size());

          for (final Object date : list) {
            isoDates.add(ISO8601DateFormat.format((Date) date));
          }

          // return the ISO formatted dates as a comma delimited string
          return StringUtils.collectionToDelimitedString(isoDates, DELIMITER);
        }
      }

      // return everything else using toString()
      return StringUtils.collectionToDelimitedString(values, DELIMITER);
    } else if (value instanceof ContentData) {
      // for content properties retrieve the info URL rather than the
      // the object value itself
      final ContentData contentData = (ContentData) value;

      return contentData.getInfoUrl();
    }

    return value;
  }

  private Object getDefaultValue(final QName name, final ContentModelItemData<?> data) {
    final PropertyDefinition propDef = data.getPropertyDefinition(name);

    if (propDef != null) {
      return propDef.getDefaultValue();
    }

    return null;
  }

  @Override
  protected String getRegistryKey() {
    return "vgr";
  }

  @Override
  protected QName getFullName(final String name) {
    final String[] parts = name.split(FormFieldConstants.FIELD_NAME_SEPARATOR);

    final String prefix = parts[0];

    final String localName = parts[1];

    return QName.createQName(prefix, localName, namespaceService);
  }

}
