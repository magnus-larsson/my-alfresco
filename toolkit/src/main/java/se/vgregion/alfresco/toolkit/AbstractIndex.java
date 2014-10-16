package se.vgregion.alfresco.toolkit;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.annotation.Resource;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.extensions.webscripts.WebScriptResponse;

import com.github.dynamicextensionsalfresco.webscripts.resolutions.Resolution;

public abstract class AbstractIndex {

  @Resource(name = "global-properties")
  private Properties _globalProperties;

  private Document _document;

  public Resolution index(WebScriptResponse response) throws IOException {
    String html = getDocument().toString();

    return new HtmlResolution(html);
  }

  public Document getDocument() throws IOException {
    if (_document == null) {
      InputStream inputStream = this.getClass().getResourceAsStream(getIndexHtmlPath());

      final String html = IOUtils.toString(inputStream);

      _document = Jsoup.parse(html);

      Elements baseElements = _document.getElementsByTag("base");

      Element base = baseElements.isEmpty() ? null : baseElements.get(0);

      if (base == null) {
        Elements headElements = _document.getElementsByTag("head");

        if (!headElements.isEmpty()) {
          base = _document.createElement("base");

          headElements.add(0, base);
        }
      }

      if (base != null) {
        String context = _globalProperties.getProperty("alfresco.context");
        
        if (StringUtils.isEmpty(context)) {
          context = "alfresco";
        }

        base.attr("href", "/" + context + "/service" + getIndexAppPath());
      }
    }

    return _document;
  }

  public abstract String getIndexHtmlPath();

  public abstract String getIndexAppPath();

}
