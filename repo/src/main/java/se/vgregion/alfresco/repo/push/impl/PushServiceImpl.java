package se.vgregion.alfresco.repo.push.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import se.vgregion.alfresco.repo.push.PushService;
import se.vgregion.alfresco.repo.utils.ServiceUtils;

public class PushServiceImpl implements PushService, InitializingBean {

  private static final Logger LOG = Logger.getLogger(PushServiceImpl.class);

  private boolean _dumpFeed;

  private String _pushServerUrl;

  private String _feedUrl;

  private ServiceUtils _serviceUtils;

  public void setDumpFeed(final boolean dumpFeed) {
    _dumpFeed = dumpFeed;
  }

  public void setPushServerUrl(final String pushServerUrl) {
    _pushServerUrl = pushServerUrl;
  }

  public void setFeedUrl(final String feedUrl) {
    _feedUrl = feedUrl;
  }

  public void setServiceUtils(final ServiceUtils serviceUtils) {
    _serviceUtils = serviceUtils;
  }

  @Override
  public boolean pushFile(final NodeRef nodeRef) {
    final List<NodeRef> nodeRefs = new ArrayList<NodeRef>();

    nodeRefs.add(nodeRef);

    return pushFiles(nodeRefs);
  }

  @Override
  public boolean pushFiles(final List<NodeRef> nodeRefs) {
    if (_dumpFeed) {
      dumpFeed();
    }

    if (!_serviceUtils.pingServer(_pushServerUrl)) {
      LOG.warn("Can't contact PuSH server for feed '" + _feedUrl + "', exiting...");

      return false;
    }

    final HttpClient client = new HttpClient();

    final PostMethod post = new UTF8PostMethod(_pushServerUrl);
    post.setRequestHeader("ContentType", "application/x-www-form-urlencoded;charset=UTF-8");

    post.addParameter("hub.mode", "publish");
    post.addParameter("hub.url", _feedUrl);

    try {
      final int response = client.executeMethod(post);

      final ByteArrayOutputStream baos = new ByteArrayOutputStream();

      post.getRequestEntity().writeRequest(baos);

      if (LOG.isDebugEnabled()) {
        LOG.debug(post.getResponseBodyAsString());

        for (final NodeRef nodeRef : nodeRefs) {
          LOG.debug("File '" + nodeRef + "' published to '" + _pushServerUrl + "' with feed URL '" + _feedUrl
              + "', response code " + response);
        }
      }

      return true;
    } catch (final Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  private void dumpFeed() {
    InputStream inputStream = null;
    OutputStream outputStream = null;

    try {
      final HttpClient client = new HttpClient();

      final GetMethod get = new GetMethod(_feedUrl);

      client.executeMethod(get);

      inputStream = get.getResponseBodyAsStream();

      final File file = File.createTempFile("alfresco_feed_", ".xml");

      outputStream = new FileOutputStream(file);

      IOUtils.copy(inputStream, outputStream);

      if (LOG.isDebugEnabled()) {
        LOG.debug("Dumped the feed '" + _feedUrl + "' to the file '" + file.getAbsolutePath() + "'.");
      }
    } catch (final Exception ex) {
      LOG.warn("Although dumpFeed is configured, the feed could not be dumped.", ex);
    } finally {
      IOUtils.closeQuietly(inputStream);
      IOUtils.closeQuietly(outputStream);
    }
  }

  private class UTF8PostMethod extends PostMethod {

    public UTF8PostMethod(final String url) {
      super(url);
    }

    @Override
    public String getRequestCharSet() {
      return "UTF-8";
    }
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    Assert.notNull(_serviceUtils);
    Assert.hasText(_feedUrl);
    Assert.hasText(_pushServerUrl);
  }

}
