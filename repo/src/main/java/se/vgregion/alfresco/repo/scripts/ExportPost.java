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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.repo.exporter.ACPExportPackageHandler;
import org.alfresco.repo.web.scripts.content.StreamACP;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.view.ExporterCrawlerParameters;
import org.alfresco.service.cmr.view.Location;
import org.alfresco.util.GUID;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * Creates an RM specific ACP file of nodes to export then streams it back
 * to the client.
 * 
 * @author Gavin Cornwell
 */
 
public class ExportPost extends StreamACP

{
    private static Log logger = LogFactory.getLog(ExportPost.class);

    protected static final String PARAM_TRANSFER_FORMAT = "transferFormat";
            
//    @SuppressWarnings("deprecation")
    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException
    {
        File tempACPFile = null;
        try
        {
            NodeRef[] nodeRefs = null;
            String contentType = req.getContentType();
            if (MULTIPART_FORMDATA.equals(contentType))
            {
                // get nodeRefs parameter from form
                nodeRefs = getNodeRefs(req.getParameter(PARAM_NODE_REFS));
            }
            else
            {
                // presume the request is a JSON request so get nodeRefs from JSON body
                nodeRefs = getNodeRefs(new JSONObject(new JSONTokener(req.getContent().getContent())));
            }
            
            // setup the ACP parameters
            ExporterCrawlerParameters params = new ExporterCrawlerParameters();
            params.setCrawlSelf(true);
            params.setCrawlChildNodes(true);
            params.setExportFrom(new Location(nodeRefs));
            
            // create an ACP of the nodes
            tempACPFile = createACP(params, "mets", false);
                
            // stream the ACP back to the client as an attachment (forcing save as)
            streamContent(req, res, tempACPFile, true, tempACPFile.getName());
        } 
        catch (IOException ioe)
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                    "Could not read content from req.", ioe);
        }
        catch (JSONException je)
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                        "Could not parse JSON from req.", je);
        }
        finally
        {
           // try and delete the temporary file
           if (tempACPFile != null)
           {
               if (logger.isDebugEnabled())
                   logger.debug("Deleting temporary archive: " + tempACPFile.getAbsolutePath());
               
               tempACPFile.delete();
           }
        }
    }
    
    
    protected File createACP(ExporterCrawlerParameters params, String extension, boolean keepFolderStructure)
    {
        try
        {
            // generate temp file and folder name
            File dataFile = new File(GUID.generate());
            File contentDir = new File(GUID.generate());
            
            // setup export package handler
            File acpFile = TempFileProvider.createTempFile(TEMP_FILE_PREFIX, "." + extension);
            ACPExportPackageHandler handler = new ACPExportPackageHandler(new FileOutputStream(acpFile), 
                 dataFile, contentDir, this.mimetypeService);
            handler.setExportAsFolders(keepFolderStructure);
            handler.setNodeService(this.nodeService);

            // perform the actual export
            this.exporterService.exportView(handler, params, null);
            
            if (logger.isDebugEnabled())
                logger.debug("Created temporary archive: " + acpFile.getAbsolutePath());
            
            return acpFile;
        }
        catch (FileNotFoundException fnfe)
        {
            throw new WebScriptException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                        "Failed to create archive", fnfe);
        }
    }
    
}