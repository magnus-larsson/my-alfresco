package se.vgregion.alfresco.repo.forms.processor.node;

import static org.alfresco.repo.forms.processor.node.FormFieldConstants.DOT_CHARACTER;
import static org.alfresco.repo.forms.processor.node.FormFieldConstants.DOT_CHARACTER_REPLACEMENT;
import static org.alfresco.repo.forms.processor.node.FormFieldConstants.ON;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.forms.FormData;
import org.alfresco.repo.forms.FormData.FieldData;
import org.alfresco.repo.forms.FormException;
import org.alfresco.repo.forms.processor.node.EncodingFieldProcessor;
import org.alfresco.repo.forms.processor.node.MimetypeFieldProcessor;
import org.alfresco.repo.forms.processor.node.NodeFormProcessor;
import org.alfresco.repo.forms.processor.node.SizeFieldProcessor;
import org.alfresco.service.cmr.dictionary.ConstraintDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.springframework.extensions.surf.util.I18NUtil;

public class VgrNodeFormProcessor extends NodeFormProcessor {

  protected static final String DELIMITER = "#alf#";

  @Override
  protected void processPropertyPersist(final NodeRef nodeRef, final Map<QName, PropertyDefinition> propDefs,
      final FieldData fieldData, final Map<QName, Serializable> propsToPersist, final FormData data) {
    if (getLogger().isDebugEnabled()) {
      getLogger().debug("Processing field " + fieldData + " for property persistence");
    }

    // match and extract the prefix and name parts
    final Matcher m = this.propertyNamePattern.matcher(fieldData.getName().replaceAll(DOT_CHARACTER_REPLACEMENT,
        DOT_CHARACTER));

    if (m.matches()) {
      final String qNamePrefix = m.group(1);

      final String localName = m.group(2);

      final QName fullQName = QName.createQName(qNamePrefix, localName, namespaceService);

      // ensure that the property being persisted is defined in the model
      PropertyDefinition propDef = propDefs.get(fullQName);

      // if the property is not defined on the node, check for the
      // property in all models
      if (propDef == null) {
        propDef = this.dictionaryService.getProperty(fullQName);
      }

      // if we have a property definition attempt the persist
      if (propDef != null) {
        // look for properties that have well known handling
        // requirements
        if (fullQName.equals(ContentModel.PROP_NAME)) {
          processNamePropertyPersist(nodeRef, fieldData, propsToPersist);
        } else if (propDef.getDataType().getName().equals(DataTypeDefinition.CONTENT)) {
          processContentPropertyPersist(nodeRef, fieldData, propsToPersist, data);
        } else {
          Object value = fieldData.getValue();

          // before persisting check data type of property
          if (propDef.isMultiValued()) {
            // depending on client the value could be a comma separated string,
            // a List object or a JSONArray object
            if (value instanceof String) {
              if (((String) value).length() == 0) {
                // empty string for multi-valued properties should be stored as
                // null
                value = null;
              } else {
                // if value is a String convert to List of String
                final String[] parts = StringUtils.splitByWholeSeparator((String) value, DELIMITER);

                final List<String> list = new ArrayList<String>(8);

                for(final String part : parts) {
                  list.add(part);
                }

                // persist the List
                value = list;
              }
            } else if (value instanceof JSONArray) {
              // if value is a JSONArray convert to List of Object
              final JSONArray jsonArr = (JSONArray) value;

              final int arrLength = jsonArr.length();

              final List<Object> list = new ArrayList<Object>(arrLength);

              try {
                for (int x = 0; x < arrLength; x++) {
                  list.add(jsonArr.get(x));
                }
              } catch (final JSONException je) {
                throw new FormException("Failed to convert JSONArray to List", je);
              }

              // persist the list
              value = list;
            }
          } else if (propDef.getDataType().getName().equals(DataTypeDefinition.BOOLEAN)) {
            // check for browser representation of true, that being "on"
            if (value instanceof String && ON.equals(value)) {
              value = Boolean.TRUE;
            }
          } else if (propDef.getDataType().getName().equals(DataTypeDefinition.LOCALE)) {
            value = I18NUtil.parseLocale((String) value);
          } else if (value instanceof String && ((String) value).length() == 0) {
            // make sure empty strings stay as empty strings,
            // everything else should be represented as null
            if (!propDef.getDataType().getName().equals(DataTypeDefinition.TEXT)
                && !propDef.getDataType().getName().equals(DataTypeDefinition.MLTEXT)) {
              value = null;
            } else {
              // if the text property has a regex constraint set the empty
              // string to null otherwise the integrity checker will reject it
              final List<ConstraintDefinition> constraints = propDef.getConstraints();

              if (constraints != null && constraints.size() > 0) {
                for (final ConstraintDefinition constraintDef : constraints) {
                  if ("REGEX".equals(constraintDef.getConstraint().getType())) {
                    value = null;
                    break;
                  }
                }
              }
            }
          }

          // add the property to the map
          propsToPersist.put(fullQName, (Serializable) value);
        }
      } else if (getLogger().isWarnEnabled()) {
        getLogger().warn("Ignoring field '" + fieldData.getName() + "' as a property definition can not be found");
      }
    } else {
      // the field is potentially a well know transient property
      // check for the ones we know about, anything else is ignored
      final Matcher tppm = this.transientPropertyPattern.matcher(fieldData.getName());

      if (tppm.matches()) {
        final String fieldName = tppm.group(1);

        if (fieldName.equals(MimetypeFieldProcessor.KEY)) {
          processMimetypePropertyPersist(nodeRef, fieldData, propsToPersist);
        } else if (fieldName.equals(EncodingFieldProcessor.KEY)) {
          processEncodingPropertyPersist(nodeRef, fieldData, propsToPersist);
        } else if (fieldName.equals(SizeFieldProcessor.KEY)) {
          // the size property is well known but should never be persisted
          // as it is calculated so this is intentionally ignored
        } else if (getLogger().isWarnEnabled()) {
          getLogger().warn("Ignoring unrecognised field '" + fieldData.getName() + "'");
        }
      } else if (getLogger().isWarnEnabled()) {
        getLogger().warn("Ignoring unrecognised field '" + fieldData.getName() + "'");
      }
    }
  }

}
