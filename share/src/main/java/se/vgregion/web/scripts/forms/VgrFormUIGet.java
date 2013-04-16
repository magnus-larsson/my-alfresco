package se.vgregion.web.scripts.forms;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.web.config.forms.FormField;
import org.alfresco.web.scripts.forms.FormUIGet;
import org.json.JSONArray;
import org.json.JSONException;
import org.springframework.util.StringUtils;

public class VgrFormUIGet extends FormUIGet {

  @Override
  protected void processFieldConstraintControl(final ModelContext context, final Field field,
      final FormField fieldConfig, final Constraint constraint) throws JSONException {
    if ("APELON_NODE_TYPE".equals(constraint.getId())) {
      if (fieldConfig == null || fieldConfig.getControl() == null || fieldConfig.getControl().getTemplate() == null) {
        if (field.isRepeating()) {
          field.getControl().setTemplate(CONTROL_SELECT_MANY);
        } else {
          field.getControl().setTemplate(CONTROL_SELECT_ONE);
        }
      }

      if (field.getControl().getParams().containsKey(CONTROL_PARAM_OPTIONS) == false) {
        final JSONArray options = constraint.getJSONParams().getJSONArray("allowedValues");

        final List<String> optionsList = new ArrayList<String>(options.length());

        for (int x = 0; x < options.length(); x++) {
          optionsList.add(options.getString(x));
        }

        // ALF-7961: don't use a comma as the list separator
        field.getControl().getParams().put(CONTROL_PARAM_OPTIONS, StringUtils.collectionToDelimitedString(optionsList, DELIMITER));
        field.getControl().getParams().put(CONTROL_PARAM_OPTION_SEPARATOR, DELIMITER);
      }
    } else {
      super.processFieldConstraintControl(context, field, fieldConfig, constraint);
    }
  }

}
