package org.dtls.fairifier;

import java.lang.Exception;
import java.io.IOException;
import org.deri.grefine.rdf.utils.HttpUtils;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONWriter;
import com.google.refine.commands.Command;
import org.deri.grefine.rdf.app.ApplicationContext;
import com.google.refine.browsing.Engine;
import com.google.refine.model.Project;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.nquads.NQuadsParser;
import org.eclipse.rdf4j.rio.turtle.TurtleParser;
import org.eclipse.rdf4j.rio.rdfxml.RDFXMLParser;
import org.eclipse.rdf4j.rio.trig.TriGParser;
import org.eclipse.rdf4j.rio.ntriples.NTriplesParser;
import java.lang.System;
import java.lang.Exception;
import org.json.JSONException;
import org.json.JSONObject;
import org.eclipse.rdf4j.rio.Rio;
import java.io.ByteArrayInputStream;
import java.lang.Exception;
import java.nio.charset.StandardCharsets;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import java.io.InputStream;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import java.util.List;
import org.apache.commons.fileupload.FileUploadException;
import java.io.InputStreamReader;
import java.io.BufferedReader;

/**
 * 
 * @author Shamanou van Leeuwen
 * @date May 24 2017
 *
 */

public class DetectFileFormatCommand extends Command{
    RDFParser rdfParserttl = new TurtleParser();
    RDFParser rdfParserrdfxml = new RDFXMLParser();
    RDFParser rdfParserntripples = new NTriplesParser();

    RDFParser[] parsers = {rdfParserttl, rdfParserrdfxml, rdfParserntripples};

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        System.setProperty("org.xml.sax.driver", "org.apache.xerces.parsers.SAXParser");
        FileItemFactory factory = new DiskFileItemFactory();

        // Create a new file upload handler
        ServletFileUpload upload = new ServletFileUpload(factory);
        List<FileItem> items = null;
        String baseuri = null;
        String filename = null;
        InputStream in = null;
        String format = null;

        try{
            items = upload.parseRequest(req);
            for(FileItem item:items){
                if(item.getFieldName().equals("baseuri")){
                    baseuri = item.getString();
                }else if(item.getFieldName().equals("file_upload")){
                    filename = item.getName();
                    in = item.getInputStream();
                }
            }
        }catch(FileUploadException ex){
            respondException(res, ex);
        }

        for (RDFParser parser: parsers){
            try{
                BufferedReader reader =  new BufferedReader(new InputStreamReader(in));
                String firstLine = reader.readLine();
                if ( firstLine.substring(0,5).equals("<?xml") ){
                    format = "application/rdf+xml";
                    break;
                }
                parser.parse(in, baseuri);
                format = parser.getRDFFormat().getDefaultMIMEType();
                break;
            }catch(Exception e){
                System.out.println(e.toString());
                continue;
            }
        }

        try{
            res.setCharacterEncoding("UTF-8");
            res.setHeader("Content-Type", "application/json");
            JSONWriter writer = new JSONWriter(res.getWriter());
            writer.object();
            writer.key("RDFFormat"); writer.value(format);
            writer.endObject();
        }catch(Exception e){
            respondException(res, e);
        }
    }
}
