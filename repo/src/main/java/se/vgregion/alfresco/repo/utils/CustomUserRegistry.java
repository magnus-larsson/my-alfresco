package se.vgregion.alfresco.repo.utils;

import java.lang.reflect.Field;
import java.util.Map;

import org.alfresco.repo.security.sync.UserRegistry;
import org.alfresco.repo.security.sync.ldap.LDAPUserRegistry;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.ReflectionUtils;

public class CustomUserRegistry implements InitializingBean {

  UserRegistry _userRegistry;
  private Map<String, String> _personAttributeMapping;

  public void setUserRegistry(final UserRegistry userRegistry) {
    _userRegistry = userRegistry;
  }

  public void setPersonAttributeMapping(final Map<String, String> personAttributeMapping) {
    _personAttributeMapping = personAttributeMapping;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    if (!(_userRegistry instanceof LDAPUserRegistry)) {
      return;
    }

    final LDAPUserRegistry userRegistry = (LDAPUserRegistry) _userRegistry;

    final Field field = ReflectionUtils.findField(LDAPUserRegistry.class, "personAttributeMapping");

    ReflectionUtils.makeAccessible(field);

    @SuppressWarnings("unchecked")
    final Map<String, String> personAttributeMapping = (Map<String, String>) field.get(_userRegistry);

    personAttributeMapping.putAll(_personAttributeMapping);

    field.set(_userRegistry, personAttributeMapping);

    userRegistry.afterPropertiesSet();
  }

}
