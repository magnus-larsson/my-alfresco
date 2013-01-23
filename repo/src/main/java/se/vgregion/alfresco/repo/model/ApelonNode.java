package se.vgregion.alfresco.repo.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;

public class ApelonNode implements Serializable {

  private static final long serialVersionUID = -2291861004052185448L;

  private String _name;
  private String _internalId;
  private String _namespaceId;
  private String _sourceId;
  private boolean _hasChildren;
  private final Map<String, List<String>> _properties = new HashMap<String, List<String>>();
  private ApelonNode _parent;

  public String getName() {
    return _name;
  }

  public void setName(final String name) {
    _name = name;
  }

  public String getInternalId() {
    return _internalId;
  }

  public void setInternalId(final String internalId) {
    _internalId = internalId;
  }

  public String getNamespaceId() {
    return _namespaceId;
  }

  public void setNamespaceId(final String namespaceId) {
    _namespaceId = namespaceId;
  }

  public String getSourceId() {
    return _sourceId;
  }

  public void setSourceId(final String sourceId) {
    _sourceId = sourceId;
  }

  public boolean isHasChildren() {
    return _hasChildren;
  }

  public void setHasChildren(boolean hasChildren) {
    _hasChildren = hasChildren;
  }

  public Map<String, List<String>> getProperties() {
    return _properties;
  }

  public void addProperty(final String key, final String value) {
    List<String> values;

    if (_properties.containsKey(key)) {
      values = _properties.get(key);
    } else {
      values = new ArrayList<String>();
    }

    values.add(value);

    _properties.put(key, values);
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

  public void setParent(ApelonNode node) {
    _parent = node;
  }

  public String getPath() {
    return _parent != null ? _parent.getPath() + "/" + _name : _name;
  }
}
