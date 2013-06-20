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

  protected static String DELIMITER = "#alf#";

  @Override
  protected void processPropertyPersist(NodeRef nodeRef, Map<QName, PropertyDefinition> propDefs, FieldData fieldData, Map<QName, Serializable> propsToPersist, FormData data) {
    if (getLogger().isDebugEnabled()) {
      getLogger().debug("Processing field " + fieldData + " for property persistence");
    }

    Matcher m = this.propertyNamePattern.matcher(fieldData.getName().replaceAll(DOT_CHARACTER_REPLACEMENT, DOT_CHARACTER));

    if (m.matches()) {
      String qNamePrefix = m.group(1);

      String localName = m.group(2);

      QName fullQName = QName.createQName(qNamePrefix, localName, namespaceService);

      PropertyDefinition propDef = propDefs.get(fullQName);

      if (propDef == null) {
        propDef = this.dictionaryService.getProperty(fullQName);
      }

      if (propDef != null) {
        if (fullQName.equals(ContentModel.PROP_NAME)) {
          processNamePropertyPersist(nodeRef, fieldData, propsToPersist);
        } else if (propDef.getDataType().getName().equals(DataTypeDefinition.CONTENT)) {
          processContentPropertyPersist(nodeRef, fieldData, propsToPersist, data);
        } else {
          Object value = fieldData.getValue();

          if (propDef.isMultiValued()) {
            if (value instanceof String) {
              if (((String) value).length() == 0) {
                value = null;
              } else {
                String[] parts = StringUtils.splitByWholeSeparator((String) value, DELIMITER);

                List<String> list = new ArrayList<String>(8);

                for (String part : parts) {
                  list.add(part);
                }

                value = list;
              }
            } else if (value instanceof JSONArray) {
              JSONArray jsonArr = (JSONArray) value;

              int arrLength = jsonArr.length();

              List<Object> list = new ArrayList<Object>(arrLength);

              try {
                for (int x = 0; x < arrLength; x++) {
                  list.add(jsonArr.get(x));
                }
              } catch (JSONException je) {
                throw new FormException("Failed to convert JSONArray to List", je);
              }

              value = list;
            }
          } else if (propDef.getDataType().getName().equals(DataTypeDefinition.BOOLEAN)) {
            if (value instanceof String && ON.equals(value)) {
              value = Boolean.TRUE;
            }
          } else if (propDef.getDataType().getName().equals(DataTypeDefinition.LOCALE)) {
            value = I18NUtil.parseLocale((String) value);
          } else if (value instanceof String && ((String) value).length() == 0) {
            if (!propDef.getDataType().getName().equals(DataTypeDefinition.TEXT) && !propDef.getDataType().getName().equals(DataTypeDefinition.MLTEXT)) {
              value = null;
            } else {
              List<ConstraintDefinition> constraints = propDef.getConstraints();

              if (constraints != null && constraints.size() > 0) {
                for (ConstraintDefinition constraintDef : constraints) {
                  if ("REGEX".equals(constraintDef.getConstraint().getType())) {
                    value = null;
                    break;
                  }
                }
              }
            }
          }

          propsToPersist.put(fullQName, (Serializable) value);
        }
      } else if (getLogger().isWarnEnabled()) {
        getLogger().warn("Ignoring field '" + fieldData.getName() + "' as a property definition can not be found");
      }
    } else {
      Matcher tppm = this.transientPropertyPattern.matcher(fieldData.getName());

      if (tppm.matches()) {
        String fieldName = tppm.group(1);

        if (fieldName.equals(MimetypeFieldProcessor.KEY)) {
          processMimetypePropertyPersist(nodeRef, fieldData, propsToPersist);
        } else if (fieldName.equals(EncodingFieldProcessor.KEY)) {
          processEncodingPropertyPersist(nodeRef, fieldData, propsToPersist);
        } else if (fieldName.equals(SizeFieldProcessor.KEY)) {
        } else if (getLogger().isWarnEnabled()) {
          getLogger().warn("Ignoring unrecognised field '" + fieldData.getName() + "'");
        }
      } else if (getLogger().isWarnEnabled()) {
        getLogger().warn("Ignoring unrecognised field '" + fieldData.getName() + "'");
      }
    }
  }

}
