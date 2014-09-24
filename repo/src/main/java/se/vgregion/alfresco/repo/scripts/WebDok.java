package se.vgregion.alfresco.repo.scripts;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.web.scripts.content.ContentGet;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import se.vgregion.alfresco.repo.model.VgrModel;
import se.vgregion.alfresco.repo.storage.StorageService;
import se.vgregion.alfresco.repo.utils.impl.ServiceUtilsImpl;

public class WebDok extends ContentGet {

  private SearchService _searchService;

  private ServiceUtilsImpl _serviceUtils;

  private StorageService _storageService;

  public void setSearchService(final SearchService searchService) {
    _searchService = searchService;
  }

  public void setServiceUtils(final ServiceUtilsImpl serviceUtils) {
    _serviceUtils = serviceUtils;
  }

  public void setStorageService(StorageService storageService) {
    _storageService = storageService;
  }

  @Override
  public void execute(final WebScriptRequest req, final WebScriptResponse res) throws IOException {
    // extract the id from the argument list, this is a required argument
    final String id = req.getParameter("id");

    // extract the version, this is an optional argument
    final String version = req.getParameter("version");

    final NodeRef document = findDocument(req, id, version);

    if (document == null) {
      sendNotFoundStatus(req, res);
    } else {
      streamDocument(req, res, document);
    }
  }

  /**
   * Finds a particular document and optionally a version. Returns null if no
   * document found.
   *
   * @param req
   * @param id
   *          The vgr:vgr_dok_document_id to find, required.
   * @param version
   *          The version to find, optional.
   * @return The nodeRef or null if no document found.
   */
  private NodeRef findDocument(final WebScriptRequest req, final String id, final String version) {
    final String query = "ASPECT:\"vgr:vgrdok\" AND ASPECT:\"vgr:published\" AND @vgr:vgr_dok_document_id:" + id;

    final ResultSet documents = _searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, SearchService.LANGUAGE_FTS_ALFRESCO, query);

    try {
      if (documents.length() == 0) {
        return null;
      }

      NodeRef document;

      if (StringUtils.isNotBlank(version)) {
        document = extractVersion(documents, version);
      } else {
        document = extractHighestVersion(documents);
      }

      return document;
    } finally {
      ServiceUtilsImpl.closeQuietly(documents);
    }
  }

  /**
   * Streams the document to the browser as an attachment.
   *
   * @param req
   * @param res
   * @param document
   * @throws IOException
   */
  private void streamDocument(final WebScriptRequest req, final WebScriptResponse res, final NodeRef document) throws IOException {
    final QName propertyQName = ContentModel.PROP_CONTENT;

    // get the PDF/A rendition if it exists
    NodeRef nodeRef = _storageService.getOrCreatePdfaRendition(document);

    final boolean attach = Boolean.valueOf(req.getParameter("a"));

    final String attachFilename = getFilename(nodeRef);

    streamContent(req, res, nodeRef, propertyQName, attach, attachFilename);
  }

  /**
   * Sends a 404 error to the browser.
   *
   * @param req
   * @param res
   * @throws IOException
   */
  private void sendNotFoundStatus(final WebScriptRequest req, final WebScriptResponse res) throws IOException {
    final Status status = new Status();

    status.setCode(404);
    status.setMessage("Inget dokument med det id:t kunde hittas.");
    status.setRedirect(true);

    final Cache cache = new Cache(getDescription().getRequiredCache());

    final Map<String, Object> model = new HashMap<String, Object>();

    final String format = req.getFormat();

    model.put("status", status);
    model.put("cache", cache);

    final Map<String, Object> templateModel = createTemplateParameters(req, res, model);

    sendStatus(req, res, status, cache, format, templateModel);
  }

  /**
   * Extracts the filename for a particular nodeRef.
   *
   * @param nodeRef
   * @return the filename
   */
  private String getFilename(final NodeRef nodeRef) {
    final String extension = _serviceUtils.getFileExtension(nodeRef);

    String filename = (String) nodeService.getProperty(nodeRef, VgrModel.PROP_TITLE_FILENAME);

    filename = FilenameUtils.getBaseName(filename);

    return "\"" + filename + extension + "\"";
  }

  /**
   * Extracts a particular version from a list of documents. Returns null of the
   * required version is not found.
   *
   * @param documents
   *          The list to extract the version from.
   * @param version
   *          The version to extract.
   * @return The particular version or null if the required version was not
   *         found.
   */
  private NodeRef extractVersion(final ResultSet documents, final String version) {
    for (final ResultSetRow document : documents) {
      final String storedVersion = (String) document.getValue(VgrModel.PROP_VGR_DOK_VERSION);

      if (version.equalsIgnoreCase(storedVersion)) {
        // only return the document if it's published
        return _serviceUtils.isPublished(document.getNodeRef()) ? document.getNodeRef() : null;
      }
    }

    return null;
  }

  /**
   * Extracts the highest version from a list of documents.
   *
   * @param documents
   * @return the nodeRef representing the highest version.
   */
  private NodeRef extractHighestVersion(final ResultSet documents) {
    final List<ResultSetRow> rows = new ArrayList<ResultSetRow>();

    for (final ResultSetRow row : documents) {
      // only add published versions to this list...
      if (!_serviceUtils.isPublished(row.getNodeRef())) {
        continue;
      }

      rows.add(row);
    }

    if (rows.size() == 0) {
      return null;
    }

    Collections.sort(rows, new Comparator<ResultSetRow>() {

      @Override
      public int compare(final ResultSetRow row1, final ResultSetRow row2) {
        final Float version11 = Float.parseFloat((String) row1.getValue(VgrModel.PROP_VGR_DOK_VERSION));
        final Float version12 = Float.parseFloat((String) row1.getValue(VgrModel.PROP_IDENTIFIER_VERSION));

        final float[] array1 = { version11, version12 };

        final Float version1 = NumberUtils.max(array1);

        final Float version21 = Float.parseFloat((String) row2.getValue(VgrModel.PROP_VGR_DOK_VERSION));
        final Float version22 = Float.parseFloat((String) row2.getValue(VgrModel.PROP_IDENTIFIER_VERSION));

        final float[] array2 = { version21, version22 };

        final Float version2 = NumberUtils.max(array2);

        return version1.compareTo(version2);
      }

    });

    return rows.get(rows.size() - 1).getNodeRef();
  }

  @Override
  protected void setAttachment(final WebScriptRequest req, final WebScriptResponse res, final boolean attach, final String attachFileName) {
    String headerValue = attach ? "attachment" : "inline";

    if (StringUtils.isNotBlank(attachFileName)) {
      headerValue += "; filename=" + attachFileName;
    }

    res.setHeader("Content-Disposition", headerValue);
  }

}
