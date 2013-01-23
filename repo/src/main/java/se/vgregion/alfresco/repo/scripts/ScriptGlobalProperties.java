package se.vgregion.alfresco.repo.scripts;

import java.util.Properties;
import org.alfresco.repo.jscript.BaseScopableProcessorExtension;

/**
 * Expose global properties to webscripts
 *
 */
public class ScriptGlobalProperties extends BaseScopableProcessorExtension {

    private Properties _properties;
    
    public void setProperties(final Properties globalProperties) {
        _properties = globalProperties;
    }

    public Properties getProperties() {
        return _properties;
    }

    public String get(String key) {
        return _properties.getProperty(key);
    }
    
    public String get(String key,String otherwise) {
        return _properties.getProperty(key,otherwise);
    }
}
