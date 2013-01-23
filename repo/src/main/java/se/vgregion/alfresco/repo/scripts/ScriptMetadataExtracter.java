package se.vgregion.alfresco.repo.scripts;

import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.nio.channels.ReadableByteChannel;
import java.util.Map;

import org.alfresco.repo.content.AbstractContentReader;
import org.alfresco.repo.content.metadata.MetadataExtracter;
import org.alfresco.repo.content.metadata.MetadataExtracterRegistry;
import org.alfresco.repo.content.metadata.TikaPoweredMetadataExtracter;
import org.alfresco.repo.processor.BaseProcessorExtension;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.springframework.extensions.surf.util.Content;
import org.springframework.util.ReflectionUtils;

public class ScriptMetadataExtracter extends BaseProcessorExtension {

  private MetadataExtracterRegistry _metadataExtracterRegistry;

  private MimetypeService _mimetypeService;

  public void setMetadataExtracterRegistry(final MetadataExtracterRegistry metadataExtracterRegistry) {
    _metadataExtracterRegistry = metadataExtracterRegistry;
  }

  public void setMimetypeService(final MimetypeService mimetypeService) {
    _mimetypeService = mimetypeService;
  }

  public Map<String, Serializable> extractMetadataProperties(final Content content, final String filename) {
    final String mimetype = _mimetypeService.guessMimetype(filename);

    final MetadataExtracter extracter = _metadataExtracterRegistry.getExtracter(mimetype);

    final ContentReader reader = new FakeContentReader("foo", content.getInputStream(), mimetype);

    return extractMetadata(extracter, reader);
  }

  public Serializable extractMetadataProperty(final String property, final Content content, final String filename) {
    final Map<String, Serializable> result = extractMetadataProperties(content, filename);

    return result.containsKey(property) ? result.get(property) : null;
  }

  @SuppressWarnings("unchecked")
  private Map<String, Serializable> extractMetadata(final MetadataExtracter extracter, final ContentReader reader) {
    Method method;

    try {
      method = TikaPoweredMetadataExtracter.class.getDeclaredMethod("extractRaw", ContentReader.class);
    } catch (final Exception ex) {
      throw new RuntimeException(ex);
    }

    ReflectionUtils.makeAccessible(method);

    return (Map<String, Serializable>) ReflectionUtils.invokeMethod(method, extracter, reader);
  }

  private class FakeContentReader extends AbstractContentReader {

    private InputStream _inputStream;

    private String _mimetype;

    protected FakeContentReader(final String contentUrl) {
      super(contentUrl);
    }

    public FakeContentReader(final String contentUrl, final InputStream inputStream, final String mimetype) {
      this(contentUrl);
      _inputStream = inputStream;
      _mimetype = mimetype;
    }

    @Override
    public boolean exists() {
      return true;
    }

    @Override
    public long getLastModified() {
      return 0;
    }

    @Override
    public long getSize() {
      return 1;
    }

    @Override
    public InputStream getContentInputStream() throws ContentIOException {
      return _inputStream;
    }

    @Override
    public String getMimetype() {
      return _mimetype;
    }

    @Override
    protected ContentReader createReader() throws ContentIOException {
      final FakeContentReader reader = new FakeContentReader(getContentUrl(), _inputStream, _mimetype);

      return reader;
    }

    @Override
    protected ReadableByteChannel getDirectReadableChannel() throws ContentIOException {
      return null;
    }

  }

}
