/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package se.vgregion.alfresco.repo.scripts;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;

import org.alfresco.cmis.CMISFilterNotValidException;
import org.alfresco.cmis.CMISRendition;
import org.alfresco.cmis.CMISRenditionService;
import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.web.scripts.FileTypeImageUtils;
import org.alfresco.repo.web.scripts.content.StreamContent;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.FileTypeImageSize;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.thumbnail.ThumbnailService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.context.support.ServletContextResource;

import se.vgregion.alfresco.repo.model.VgrModel;
import se.vgregion.alfresco.repo.storage.StorageService;
import se.vgregion.alfresco.repo.utils.ServiceUtils;

/**
 * Content Retrieval Service for the Storage
 * <p/>
 * Stream content from the Repository.
 * 
 * @author Niklas Ekman
 */
public class StorageContentGet extends StreamContent implements ServletContextAware {

  private ServletContext _servletContext;

  private DictionaryService _dictionaryService;

  private NamespaceService _namespaceService;

  private CMISRenditionService _cmisRenditionService;

  private ServiceUtils _serviceUtils;

  private ThumbnailService _thumbnailService;

  private StorageService _storageService;

  @Override
  public void setServletContext(final ServletContext servletContext) {
    _servletContext = servletContext;
  }

  public void setDictionaryService(final DictionaryService dictionaryService) {
    _dictionaryService = dictionaryService;
  }

  public void setNamespaceService(final NamespaceService namespaceService) {
    _namespaceService = namespaceService;
  }

  public void setCMISRenditionService(final CMISRenditionService cmisRenditionService) {
    _cmisRenditionService = cmisRenditionService;
  }

  public void setServiceUtils(final ServiceUtils serviceUtils) {
    _serviceUtils = serviceUtils;
  }

  public void setThumbnailService(ThumbnailService thumbnailService) {
    _thumbnailService = thumbnailService;
  }

  public void setStorageService(StorageService storageService) {
    _storageService = storageService;
  }

  @Override
  public void execute(final WebScriptRequest req, final WebScriptResponse res) throws IOException {
    // create map of args
    final String[] names = req.getParameterNames();

    final Map<String, String> args = new HashMap<String, String>(names.length, 1.0f);

    for (final String name : names) {
      args.put(name, req.getParameter(name));
    }

    // create map of template vars
    final Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();

    final String version = req.getParameter("version");

    final String id = parseId(templateVars);

    final boolean nativ = StringUtils.isNotBlank(req.getParameter("native")) ? req.getParameter("native").equalsIgnoreCase("true") : false;

    NodeRef nodeRef;

    if (StringUtils.isNotBlank(version)) {
      nodeRef = _storageService.getPublishedStorageVersion(id, version);
    } else {
      nodeRef = _storageService.getLatestPublishedStorageVersion(id);
    }

    if (nodeRef == null) {
      sendNotFoundStatus(req, res);
      return;
    }

    // must have a nodeRef for the filename later on, base it on the
    // original node, not an eventual PDF/A node
    NodeRef filenameNodeRef = nodeRef;

    // stream content on node, or rendition of node
    final String streamId = req.getParameter("streamId");

    if (!nativ) {
      // get the PDF/A rendition if it exists
      NodeRef pdfRendition = _thumbnailService.getThumbnailByName(nodeRef, ContentModel.PROP_CONTENT, "pdfa");

      if (pdfRendition == null) {
        _storageService.createPdfRendition(nodeRef, false);

        pdfRendition = _thumbnailService.getThumbnailByName(nodeRef, ContentModel.PROP_CONTENT, "pdfa");
      }

      // use the PDF/A rendition if it exists, otherwise use the nodeRef
      nodeRef = pdfRendition != null ? pdfRendition : nodeRef;
    }

    // determine attachment
    final boolean attach = Boolean.valueOf(req.getParameter("a"));

    if (streamId != null && streamId.length() > 0) {
      // render content rendition
      streamRendition(req, res, nodeRef, streamId, attach);
    } else {
      // render content
      QName propertyQName = ContentModel.PROP_CONTENT;

      final String contentPart = templateVars.get("property");

      if (contentPart.length() > 0 && contentPart.charAt(0) == ';') {
        if (contentPart.length() < 2) {
          throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, "Content property malformed");
        }

        final String propertyName = contentPart.substring(1);

        if (propertyName.length() > 0) {
          propertyQName = QName.createQName(propertyName, _namespaceService);
        }
      }

      final String filename = extractFilename(filenameNodeRef, nodeRef);

      // Stream the content
      streamContent(req, res, nodeRef, propertyQName, attach, filename);
    }
  }

  private String parseId(final Map<String, String> templateVars) {
    String id;

    if (templateVars.containsKey("store_type") && templateVars.containsKey("store_id")) {
      id = templateVars.get("store_type") + "://" + templateVars.get("store_id") + "/" + templateVars.get("id");
    } else {
      id = templateVars.get("id");
    }

    return id;
  }

  private String extractFilename(NodeRef filenameNodeRef, NodeRef fileExtensionNodeRef) {
    final String extension = _serviceUtils.getFileExtension(fileExtensionNodeRef);

    String filename = (String) nodeService.getProperty(filenameNodeRef, VgrModel.PROP_TITLE_FILENAME);

    filename = FilenameUtils.getBaseName(filename);

    return "\"" + filename + extension + "\"";
  }

  /**
   * Stream content rendition
   * 
   * @param req
   * @param res
   * @param nodeRef
   * @param streamId
   * @param attach
   * @throws IOException
   */
  private void streamRendition(final WebScriptRequest req, final WebScriptResponse res, final NodeRef nodeRef, final String streamId, final boolean attach) throws IOException {
    try {
      // find rendition
      CMISRendition rendition = null;

      final List<CMISRendition> renditions = _cmisRenditionService.getRenditions(nodeRef, "*");

      for (final CMISRendition candidateRendition : renditions) {
        if (candidateRendition.getStreamId().equals(streamId)) {
          rendition = candidateRendition;
          break;
        }
      }

      if (rendition == null) {
        throw new WebScriptException(HttpServletResponse.SC_NOT_FOUND, "Unable to find rendition " + streamId + " for " + nodeRef.toString());
      }

      // determine if special case for icons
      if (streamId.startsWith("alf:icon")) {
        streamIcon(res, nodeRef, streamId, attach);
      } else {
        streamContent(req, res, rendition.getNodeRef(), ContentModel.PROP_CONTENT, attach);
      }
    } catch (final CMISFilterNotValidException e) {
      throw new WebScriptException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Invalid Rendition Filter");
    }
  }

  /**
   * Stream Icon
   * 
   * @param res
   * @param nodeRef
   * @param streamId
   * @param attach
   * @throws IOException
   */
  private void streamIcon(final WebScriptResponse res, final NodeRef nodeRef, final String streamId, final boolean attach) throws IOException {
    // convert stream id to icon size
    final FileTypeImageSize imageSize = streamId.equals("alf:icon16") ? FileTypeImageSize.Small : FileTypeImageSize.Medium;

    final String iconSize = streamId.equals("alf:icon16") ? "-16" : "";

    // calculate icon file name and path
    String iconPath = null;

    if (_dictionaryService.isSubClass(nodeService.getType(nodeRef), ContentModel.TYPE_CONTENT)) {
      final String name = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);

      iconPath = FileTypeImageUtils.getFileTypeImage(_servletContext, name, imageSize);
    } else {
      final String icon = (String) nodeService.getProperty(nodeRef, ApplicationModel.PROP_ICON);

      if (icon != null) {
        iconPath = "/images/icons/" + icon + iconSize + ".gif";
      } else {
        iconPath = "/images/icons/space-icon-default" + iconSize + ".gif";
      }
    }

    // set mimetype
    String mimetype = MimetypeMap.MIMETYPE_BINARY;

    final int extIndex = iconPath.lastIndexOf('.');

    if (extIndex != -1) {
      final String ext = iconPath.substring(extIndex + 1);

      mimetype = mimetypeService.getMimetype(ext);
    }
    res.setContentType(mimetype);

    // stream icon
    final ServletContextResource resource = new ServletContextResource(_servletContext, iconPath);

    if (!resource.exists()) {
      throw new WebScriptException(HttpServletResponse.SC_NOT_FOUND, "Unable to find rendition " + streamId + " for " + nodeRef.toString());
    }

    FileCopyUtils.copy(resource.getInputStream(), res.getOutputStream());
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

  @Override
  protected void setAttachment(final WebScriptRequest req, final WebScriptResponse res, final boolean attach, final String attachFileName) {
    String headerValue = attach ? "attachment" : "inline";

    if (StringUtils.isNotBlank(attachFileName)) {
      headerValue += "; filename=" + attachFileName;
    }

    res.setHeader("Content-Disposition", headerValue);
  }

}