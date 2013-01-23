package se.vgregion.alfresco.repo.preference.script;

import org.alfresco.repo.preference.script.ScriptPreferenceService;
import org.alfresco.service.cmr.preference.PreferenceService;
import org.mozilla.javascript.NativeObject;
import org.springframework.util.ReflectionUtils;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Map;

public class CustomScriptPreferenceService extends ScriptPreferenceService {

  @Override
  public NativeObject getPreferences(String userName, String preferenceFilter) {
    Map<String, Serializable> prefs = getPreferenceService().getPreferences(userName, preferenceFilter);

    NativeObject result = new NativeObject();

    for (Map.Entry<String, Serializable> entry : prefs.entrySet()) {
      String[] keys = entry.getKey().split("\\.");

      setPrefValue(keys, entry.getValue(), result);
    }

    return result;
  }

  protected PreferenceService getPreferenceService() {
    Field field = ReflectionUtils.findField(super.getClass(), "preferenceService");

    ReflectionUtils.makeAccessible(field);

    try {
      return (PreferenceService) field.get(this);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  protected void setPrefValue(String[] keys, Serializable value, NativeObject object) {
    NativeObject currentObject = object;

    int index = 0;

    for (String key : keys) {
      if (index == keys.length - 1) {
        currentObject.put(key, currentObject, value);
      } else {
        NativeObject newObject = null;

        Object temp = currentObject.get(key, currentObject);

        if (temp == null || !(temp instanceof NativeObject)) {
          newObject = new NativeObject();

          currentObject.put(key, currentObject, newObject);
        } else {
          newObject = (NativeObject) temp;
        }

        currentObject = newObject;
      }

      index++;
    }
  }

}
