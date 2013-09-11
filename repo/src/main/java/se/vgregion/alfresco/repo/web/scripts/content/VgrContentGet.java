/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package se.vgregion.alfresco.repo.web.scripts.content;

import java.io.IOException;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.web.scripts.content.ContentGet;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import se.vgregion.alfresco.repo.utils.ServiceUtils;

public class VgrContentGet extends ContentGet {

  protected ServiceUtils _serviceUtils;

  public void setServiceUtils(ServiceUtils serviceUtils) {
    _serviceUtils = serviceUtils;
  }

  @Override
  protected void streamContent(WebScriptRequest req, WebScriptResponse res, NodeRef nodeRef, QName propertyQName, boolean attach, String attachFileName) throws IOException {
    if (attach && StringUtils.isNotBlank(attachFileName)) {
      String filename = extractFilename(nodeRef);
      super.streamContent(req, res, nodeRef, propertyQName, attach, filename);
    } else {
      super.streamContent(req, res, nodeRef, propertyQName, attach, attachFileName);
    }
  }

  private String extractFilename(NodeRef nodeRef) {
    String filename = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);

    String extension = _serviceUtils.getFileExtension(nodeRef);

    if (".bin".equalsIgnoreCase(extension)) {
      extension = "." + FilenameUtils.getExtension(filename);
    }

    filename = FilenameUtils.getBaseName(filename);

    // return "\"" + filename + extension + "\"";
    return filename + extension;
  }

}
