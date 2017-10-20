
package org.dtls.fairifier;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.deri.grefine.rdf.utils.HttpUtils;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;
import org.eclipse.rdf4j.rio.turtle.TurtleParser;
import org.json.JSONException;
import org.json.JSONWriter;

import com.google.common.net.HttpHeaders;

import com.google.refine.commands.Command;

import nl.dtl.fairmetadata4j.io.CatalogMetadataParser;
import nl.dtl.fairmetadata4j.io.DatasetMetadataParser;
import nl.dtl.fairmetadata4j.io.FDPMetadataParser;
import nl.dtl.fairmetadata4j.model.CatalogMetadata;
import nl.dtl.fairmetadata4j.model.DatasetMetadata;
import nl.dtl.fairmetadata4j.utils.MetadataParserUtils;

/**
 * A command to get all the metadata from a FAIR Data Point(FDP)
 * 
 * @author Shamanou van Leeuwen
 * @date 1-11-2016
 *
 */

public class GetFairDataPointInfoCommand extends Command {

    private SimpleValueFactory f;

    /**
     * 
     * This method retrieves the catalog and dataset information depending on what
     * is specified in the POST call.
     * 
     * I returns the content with a "content" key.
     * 
     * When the method reports an error it returns this to the front-end.
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
        f = SimpleValueFactory.getInstance();
        res.setCharacterEncoding("UTF-8");
        res.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        JSONWriter writer = new JSONWriter(res.getWriter());

        try {
            if (req.getParameterMap().containsKey("uri")) {
                String uri = req.getParameter("uri");

                writer.object();
                if (req.getParameter("layer").equals("catalog")) {
                    writer.key("content");
                    writer.value(this.getFdpCatalogs(uri));
                } else if (req.getParameter("layer").equals("dataset")) {
                    writer.key("content");
                    writer.value(this.getFdpDatasets(uri));
                }
                writer.key("code");
                writer.value("ok");
                writer.endObject();
            } else {
                writer.object();
                writer.key("code");
                writer.value("ok");
                writer.key("content");
                writer.value(new ArrayList()); // empty list is needed for communication with
                                               // front end to work smoothly
                writer.endObject();
            }

        } catch (HttpException e) {
            respondException(res, e);
        } catch (JSONException e) {
            respondException(res, e);
        } catch (LayerUnavailableException e) {
            respondException(res, e);
        }
    }

    /**
     * 
     * Get all datasets in a FDP based on the URL of the FDP.
     * 
     * This method uses the FAIRMetadata4j library.
     * 
     * @param url
     * @return ArrayList<DatasetMetadata>
     * @throws IOException
     *             is thrown when metadata cannot be read from the URL provided
     * @throws RDFParseException
     *             is thrown when the metadata could not be parsed
     * @throws RDFHandlerException
     *             thrown when a incorrect rdf parser is provided
     * @throws LayerUnavailableException
     *             is thrown when no dataset metadata layer could be found at the
     *             provided URL
     */
    private ArrayList<DatasetMetadata> getFdpDatasets(String url)
            throws IOException, RDFParseException, RDFHandlerException, LayerUnavailableException, HttpException {
        f = SimpleValueFactory.getInstance();
        ArrayList<DatasetMetadata> out = new ArrayList<DatasetMetadata>();
        TurtleParser parser = new TurtleParser();
        StatementCollector rdfStatementCollector = new StatementCollector();
        parser.setRDFHandler(rdfStatementCollector);
        HttpResponse response = HttpUtils.get(url, RDFFormat.TURTLE.getDefaultMIMEType());

        if (response.getStatusLine().getStatusCode() == 200) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            parser.parse(reader, url);
            CatalogMetadataParser catalogMetadataParser = MetadataParserUtils.getCatalogParser();
            DatasetMetadataParser datasetMetadataParser = MetadataParserUtils.getDatasetParser();
            List<IRI> datasetUris = catalogMetadataParser
                    .parse(new ArrayList(rdfStatementCollector.getStatements()), f.createIRI(url)).getDatasets();
            for (IRI u : datasetUris) {
                response = HttpUtils.get(u.toString(), RDFFormat.TURTLE.getDefaultMIMEType());
                if (response.getStatusLine().getStatusCode() == 200) {
                    reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                    parser.parse(reader, u.toString());
                    out.add(datasetMetadataParser.parse(new ArrayList(rdfStatementCollector.getStatements()), u));
                } else {
                    throw new HttpException("Could not retrieve dataset(s)");
                }
            }
            return out;
        } else {
            throw new HttpException("Could not retrieve catalog(s)");
        }
    }

    /**
     * 
     * Get all catalogs in a FDP based on the url of the FDP.
     * 
     * This method uses the FAIRMetadata4j library.
     * 
     * 
     * @param url
     * @return ArrayList<CatalogMetadata>
     * @throws IOException
     *             is thrown when metadata cannot be read from the URL provided
     * @throws RDFParseException
     *             is thrown when the metadata could not be parsed
     * @throws RDFHandlerException
     *             thrown when a incorrect RDF parser is provided
     * @throws LayerUnavailableException
     *             is thrown when no dataset metadata layer could be found at the
     *             provided URL
     */
    private ArrayList<CatalogMetadata> getFdpCatalogs(String url)
            throws IOException, LayerUnavailableException, RDFParseException, RDFHandlerException, HttpException {
        f = SimpleValueFactory.getInstance();
        ArrayList<CatalogMetadata> out = new ArrayList<CatalogMetadata>();
        TurtleParser parser = new TurtleParser();
        StatementCollector rdfStatementCollector = new StatementCollector();
        parser.setRDFHandler(rdfStatementCollector);
        HttpResponse response = HttpUtils.get(url, RDFFormat.TURTLE.getDefaultMIMEType());

        if (response.getStatusLine().getStatusCode() == 200) {

            BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            parser.parse(reader, url);
            FDPMetadataParser fdpParser = MetadataParserUtils.getFdpParser();
            CatalogMetadataParser catalogMetadataParser = MetadataParserUtils.getCatalogParser();
            List<IRI> catalogUris = fdpParser
                    .parse(new ArrayList(rdfStatementCollector.getStatements()), f.createIRI(url)).getCatalogs();
            for (IRI u : catalogUris) {
                response = HttpUtils.get(u.toString(), RDFFormat.TURTLE.getDefaultMIMEType());

                if (response.getStatusLine().getStatusCode() == 200) {
                    reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                    parser.parse(reader, u.toString());
                    out.add(catalogMetadataParser.parse(new ArrayList(rdfStatementCollector.getStatements()), u));
                } else {
                    throw new HttpException("Could not retrieve catalogs(s)");
                }
            }
            return out;
        } else {
            throw new HttpException("Could not retrieve FDP metadata");
        }
    }
}
