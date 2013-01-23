package se.vgregion.alfresco.repo.preference;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.preference.PreferenceServiceImpl;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.util.ReflectionUtils;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class CustomPreferenceServiceImpl extends PreferenceServiceImpl {

  @Override
  public Map<String, Serializable> getPreferences(String userName, String preferenceFilter) {
    Map<String, Serializable> preferences = new HashMap<String, Serializable>(20);

    // Get the user node reference
    NodeRef personNodeRef = getPersonService().getPerson(userName);
    if (personNodeRef == null) {
      throw new AlfrescoRuntimeException("Can not get preferences for " + userName + " because he/she does not exist.");
    }

    try {
      // Check for preferences aspect
      if (getNodeService().hasAspect(personNodeRef, ContentModel.ASPECT_PREFERENCES)) {
        // Get the preferences for this user
        JSONObject jsonPreferences = new JSONObject();
        ContentReader reader = getContentService().getReader(personNodeRef, ContentModel.PROP_PREFERENCE_VALUES);
        if (reader != null) {
          jsonPreferences = new JSONObject(reader.getContentString());
        }

        // Build hash from preferences stored in the repository
        @SuppressWarnings("unchecked")
        Iterator<String> keys = jsonPreferences.keys();

        while (keys.hasNext()) {
          String key = keys.next();

          if (preferenceFilter == null || preferenceFilter.length() == 0 || matchPreferenceNames(key, preferenceFilter)) {
            preferences.put(key, (Serializable) jsonPreferences.get(key));
          }
        }
      }
    } catch (JSONException exception) {
      throw new AlfrescoRuntimeException("Can not get preferences for " + userName + " because there was an error passing the JSON data.", exception);
    }

    return preferences;
  }

  /**
   * Matches the preference name to the partial preference name provided
   * NOTE: This method is overridden here cause it's broken in the original code.
   *
   * @param name    preference name
   * @param matchTo match to the partial preference name provided
   * @return boolean  true if matches, false otherwise
   */
  private boolean matchPreferenceNames(String name, String matchTo) {
    boolean result = true;

    // Split strings
    String[] nameArr = name.split("\\.");
    String[] matchToArr = matchTo.split("\\.");

    int index = 0;

    for (String matchToElement : matchToArr) {
      if (!matchToElement.equals(nameArr[index])) {
        result = false;
        break;
      }

      index++;
    }

    return result;
  }

  private ContentService getContentService() {
    Field field = ReflectionUtils.findField(super.getClass(), "contentService");

    ReflectionUtils.makeAccessible(field);

    try {
      return (ContentService) field.get(this);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  private NodeService getNodeService() {
    Field field = ReflectionUtils.findField(super.getClass(), "nodeService");

    ReflectionUtils.makeAccessible(field);

    try {
      return (NodeService) field.get(this);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  public PersonService getPersonService() {
    Field field = ReflectionUtils.findField(super.getClass(), "personService");

    ReflectionUtils.makeAccessible(field);

    try {
      return (PersonService) field.get(this);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }
}
