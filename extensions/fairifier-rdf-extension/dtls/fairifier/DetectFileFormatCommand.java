
package org.dtls.fairifier;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.http.HttpHeaders;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.RDFParserRegistry;
import org.eclipse.rdf4j.rio.jsonld.JSONLDParser;
import org.eclipse.rdf4j.rio.nquads.NQuadsParser;
import org.eclipse.rdf4j.rio.ntriples.NTriplesParser;
import org.eclipse.rdf4j.rio.rdfjson.RDFJSONParser;
import org.eclipse.rdf4j.rio.rdfxml.RDFXMLParser;
import org.eclipse.rdf4j.rio.trig.TriGParser;
import org.eclipse.rdf4j.rio.trix.TriXParser;
import org.eclipse.rdf4j.rio.turtle.TurtleParser;
import org.json.JSONException;
import org.json.JSONWriter;

import com.google.refine.commands.Command;

/**
 * Enables automatic file format detection
 * 
 * @author Shamanou van Leeuwen
 * @date May 24 2017
 *
 */

/**
 * A command which detects the file format, should be configured in the
 * controller and called from the javascript front-end.
 * 
 */
public class DetectFileFormatCommand extends Command {

    private final static ArrayList<RDFParser> PARSERS = new ArrayList<RDFParser>();

    static {

        PARSERS.add(new TurtleParser());
        PARSERS.add(new NTriplesParser());
        PARSERS.add(new RDFXMLParser());
        PARSERS.add(new JSONLDParser());
        PARSERS.add(new NQuadsParser());
        PARSERS.add(new RDFJSONParser());
        PARSERS.add(new TriXParser());
        PARSERS.add(new TriGParser());

    };

    /**
     * This method takes a request containing RDF and tries to iterate over
     * different parsers to see which one is able to parse the data. If it is able
     * to be parsed a format string is returned by passing it to the response object
     * in JSON-format.
     * 
     * URLs are parsed by checking the content-type of the HTTP response.
     * 
     * @param req
     *            a request object
     * @param res
     *            a response object
     * @see com.google.refine.commands.Command#doPost(javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse)
     */
    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        FileItemFactory factory = new DiskFileItemFactory();

        // Create a new file upload handler
        final ServletFileUpload upload = new ServletFileUpload(factory);
        String baseuri = null;
        FileItem fileContent = null;
        String format = null;
        String url = null;

        try {
            List<FileItem> items = upload.parseRequest(req);
            for (final FileItem item : items) {
                if (item.getFieldName().equals("baseuri")) {
                    baseuri = item.getString();
                } else if (item.getFieldName().equals("file_source")) {
                    url = item.getString();
                } else if (item.getFieldName().equals("file_upload")) {
                    fileContent = item;
                }
            }
        } catch (FileUploadException ex) {
            respondException(res, ex);
        }

        if ((url != null) && !url.trim().equals("") && !url.trim().equals("url")) {

            try {
                format = getFormatFromUrl(url);
            } catch (IOException e) {
                respondException(res, e);
            }
        } else {
            format = parseFullFile(fileContent, baseuri);
        }
        try {
            res.setCharacterEncoding("UTF-8");
            res.setHeader("Content-Type", "application/json");
            JSONWriter writer = new JSONWriter(res.getWriter());
            writer.object();
            writer.key("RDFFormat");
            writer.value(format);
            writer.endObject();
        } catch (JSONException e) {
            respondException(res, e);
        }
    }

    private String parseFullFile(FileItem file, String baseuri) {
        String format = null;
        for (RDFParser parser : PARSERS) {
            try {
                if (baseuri == null) {
                    baseuri = "";
                }
                parser.parse(new BufferedInputStream(file.getInputStream()), baseuri);
                format = parser.getRDFFormat().getDefaultMIMEType();
                break;
            } catch (IOException e) {
                continue;
            } catch (RDFParseException e) {
                continue;
            } catch (RDFHandlerException e) {
                continue;
            }
        }
        return format;

    }

    private String getFormatFromUrl(String url)
            throws IOException, MalformedURLException {
        StringBuffer response = new StringBuffer();
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        Set<RDFFormat> rdfFormats = RDFParserRegistry.getInstance().getKeys();
        List<String> acceptHeaders = RDFFormat.getAcceptParams(rdfFormats, false, RDFFormat.RDFXML);

        for (String acceptHeader : acceptHeaders) {
            con.setRequestProperty(HttpHeaders.ACCEPT, acceptHeader);
        }
        con.connect();
        if (con.getResponseCode() == 200) {
            return con.getContentType();
        }
        return RDFFormat.RDFXML.getDefaultMIMEType();
    }
}
