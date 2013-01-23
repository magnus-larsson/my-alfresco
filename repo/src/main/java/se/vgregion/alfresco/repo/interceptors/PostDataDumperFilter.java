package se.vgregion.alfresco.repo.interceptors;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.commons.io.IOUtils;

public class PostDataDumperFilter implements Filter {

  private FilterConfig _filterConfig = null;

  @Override
  public void destroy() {
    _filterConfig = null;
  }

  @Override
  public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
  throws IOException, ServletException {
    if (_filterConfig == null) {
      return;
    }

    final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    final InputStream inputStream = request.getInputStream();

    try {
      IOUtils.copy(inputStream, outputStream);

      request.setAttribute("postdata", new String(outputStream.toByteArray()));
    } finally {
      IOUtils.closeQuietly(outputStream);
    }

    chain.doFilter(request, response);
  }

  @Override
  public void init(final FilterConfig filterConfig) throws ServletException {
    _filterConfig = filterConfig;
  }

}
