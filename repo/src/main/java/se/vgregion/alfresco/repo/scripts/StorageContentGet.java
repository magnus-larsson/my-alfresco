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
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.web.scripts.content.ContentGet;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.extensions.surf.util.URLEncoder;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.util.ReflectionUtils;

import se.vgregion.alfresco.repo.model.VgrModel;
import se.vgregion.alfresco.repo.storage.StorageService;
import se.vgregion.alfresco.repo.utils.impl.ServiceUtilsImpl;

/**
 * Content Retrieval Service for the Storage
 * <p/>
 * Stream content from the Repository.
 * 
 * @author Niklas Ekman
 */
public class StorageContentGet extends ContentGet {

  private NamespaceService _namespaceService;

  private ServiceUtilsImpl _serviceUtils;

  private StorageService _storageService;

  public void setNamespaceService(final NamespaceService namespaceService) {
    _namespaceService = namespaceService;
  }

  public void setServiceUtils(final ServiceUtilsImpl serviceUtils) {
    _serviceUtils = serviceUtils;
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

    String targetFilename = templateVars.get("filename");

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
      // use the PDF/A rendition if it exists, otherwise use the nodeRef
      nodeRef = _storageService.getOrCreatePdfaRendition(nodeRef);
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

      final String filename = extractFilename(filenameNodeRef, nodeRef, true);

      // if the targetFilename is blank and we're not going to attach the file,
      // then we redirect to the same URL but with the filename as parameter
      if (StringUtils.isBlank(targetFilename) && !attach) {
        String serverPath = req.getServerPath();
        String servicePath = req.getServicePath();
        String queryString = req.getQueryString();

        String url = serverPath + servicePath + "/" + URLEncoder.encode(extractFilename(filenameNodeRef, nodeRef, false)) + "?" + queryString;

        res.setHeader(WebScriptResponse.HEADER_LOCATION, url);
        res.setStatus(Status.STATUS_MOVED_TEMPORARILY);

        return;
      }

      // Stream the content
      streamContentLocal(req, res, nodeRef, attach, propertyQName, filename);
    }
  }

  private void streamContentLocal(WebScriptRequest req, WebScriptResponse res, NodeRef nodeRef, boolean attach, QName propertyQName, String filename) throws IOException {
    String userAgent = req.getHeader("User-Agent") != null ? req.getHeader("User-Agent").toLowerCase() : null;

    boolean rfc5987Supported = (null != userAgent) && (userAgent.contains("msie") || userAgent.contains(" chrome/") || userAgent.contains(" firefox/"));

    if (attach && rfc5987Supported) {
      streamContent(req, res, nodeRef, propertyQName, attach, filename, null);
    } else {
      streamContent(req, res, nodeRef, propertyQName, attach, null, null);
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

  private String extractFilename(NodeRef filenameNodeRef, NodeRef fileExtensionNodeRef, boolean quote) {
    final String extension = _serviceUtils.getFileExtension(fileExtensionNodeRef);

    String filename = (String) nodeService.getProperty(filenameNodeRef, VgrModel.PROP_TITLE_FILENAME);

    filename = FilenameUtils.getBaseName(filename) + extension;

    if (quote) {
      filename = "\"" + filename + "\"";
    }

    return filename;
  }

  private void streamRendition(WebScriptRequest req, WebScriptResponse res, NodeRef nodeRef, String streamId, boolean attach) throws IOException {
    try {
      final Method method = ReflectionUtils.findMethod(ContentGet.class, "streamRendition", WebScriptRequest.class, WebScriptResponse.class, NodeRef.class, String.class, Boolean.class);

      ReflectionUtils.makeAccessible(method);

      ReflectionUtils.invokeMethod(method, req, res, nodeRef, streamId, attach);
    } catch (final Exception ex) {
      throw new RuntimeException(ex);
    }
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

}