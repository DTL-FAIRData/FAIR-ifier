
package org.dtls.fairifier;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.deri.grefine.rdf.utils.HttpUtils;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.json.JSONObject;
import org.json.JSONWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.refine.commands.Command;

import nl.dtl.fairmetadata4j.io.*;
import nl.dtl.fairmetadata4j.model.*;
import nl.dtl.fairmetadata4j.utils.*;

/**
 * 
 * @author Shamanou van Leeuwen
 * @date 7-11-2016
 *
 */

public class PostFairDataToFairDataPoint extends Command {

    private SimpleValueFactory f;
    private static final Logger LOGGER = LoggerFactory.getLogger(PostFairDataToFairDataPoint.class);

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        f = SimpleValueFactory.getInstance();

        ArrayList<IRI> datasetThemes = new ArrayList<IRI>();
        ArrayList<IRI> catalogThemes = new ArrayList<IRI>();
        String uuid_catalog = UUID.randomUUID().toString();
        String uuid_dataset = UUID.randomUUID().toString();
        String uuid_distribution = UUID.randomUUID().toString();
        ArrayList<Literal> keyWords = new ArrayList<Literal>();
        Agent agent = new Agent();
        Date date = new Date();

        String catalogString = null;
        String datasetString = null;
        String distributionString = null;

        Resource r = null;

        CatalogMetadata catalogMetadata = new CatalogMetadata();
        DatasetMetadata datasetMetadata = new DatasetMetadata();
        DistributionMetadata distributionMetadata = new DistributionMetadata();

        try {
            StringBuffer jb = new StringBuffer();
            String line = null;
            BufferedReader reader = req.getReader();
            while ((line = reader.readLine()) != null)
                jb.append(line);

            JSONObject fdp = new JSONObject(jb.toString()).getJSONObject("metadata");
            JSONObject catalog = fdp.getJSONObject("catalog");
            JSONObject dataset = fdp.getJSONObject("dataset");
            JSONObject distribution = fdp.getJSONObject("distribution");
            JSONObject uploadConfiguration = fdp.getJSONObject("uploadConfiguration");

            if (!catalog.getBoolean("_exists")) {

                // optional
                try {
                    catalogMetadata.setHomepage(f.createIRI(catalog.getString("http://xmlns.com/foaf/0.1/homepage")));
                } catch (Exception ex) {
                    LOGGER.warn(ex.getMessage());
                }
                catalogThemes.add(f.createIRI(catalog.getString("http://www.w3.org/ns/dcat#themeTaxonomy")));
                catalogMetadata.setThemeTaxonomys(catalogThemes);
                catalogMetadata.setTitle(f.createLiteral(catalog.getString("http://purl.org/dc/terms/title")));
                agent.setUri(f.createIRI(fdp.getString("baseUri")));
                agent.setName(
                        f.createLiteral(catalog.getJSONObject("http://purl.org/dc/terms/publisher").getString("url")));
                catalogMetadata.setPublisher(agent);
                try {
                    catalogMetadata.setRights(f.createIRI(catalog.getString("http://purl.org/dc/terms/rights")));
                    catalogMetadata
                            .setDescription(f.createLiteral(catalog.getString("http://purl.org/dc/terms/description")));
                    catalogMetadata.setLanguage(f.createIRI(catalog.getString("http://purl.org/dc/terms/language")));
                    catalogMetadata.setHomepage(f.createIRI(catalog.getString("http://xmlns.com/foaf/0.1/homepage")));
                } catch (Exception ex) {
                    LOGGER.warn(ex.getMessage());
                }
                catalogMetadata.setVersion(f.createLiteral(catalog.getString("http://purl.org/dc/terms/hasVersion")));
                catalogMetadata.setUri(f.createIRI(fdp.getString("baseUri")));
                catalogMetadata.setIssued(f.createLiteral(date));
                catalogMetadata.setModified(f.createLiteral(date));
                catalogString = MetadataUtils.getString(catalogMetadata, RDFFormat.TURTLE, false)
                        .replaceAll("\\<" + catalogMetadata.getUri() + "\\>", "<>");
            }
            if (!dataset.getBoolean("_exists")) {
                // optional
                try {
                    datasetMetadata
                            .setLandingPage(f.createIRI(dataset.getString("http://www.w3.org/ns/dcat#landingPage")));

                    datasetMetadata.setLanguage(f.createIRI(dataset.getString("http://purl.org/dc/terms/language")));

                } catch (Exception ex) {
                    LOGGER.warn(ex.getMessage());
                }

                for (int i = 0; i < dataset.getJSONArray("http://www.w3.org/ns/dcat#theme").length(); i++) {
                    datasetThemes
                            .add(f.createIRI(dataset.getJSONArray("http://www.w3.org/ns/dcat#theme").getString(i)));
                }
                datasetMetadata.setThemes(datasetThemes);
                // optional
                try {
                    for (int i = 0; i < dataset.getJSONArray("http://www.w3.org/ns/dcat#keyword").length(); i++) {
                        keyWords.add(f
                                .createLiteral(dataset.getJSONArray("http://www.w3.org/ns/dcat#keyword").getString(i)));
                    }
                    datasetMetadata.setKeywords(keyWords);
                } catch (Exception ex) {
                    LOGGER.warn(ex.getMessage());
                }
                // optional
                try {
                    datasetMetadata
                            .setContactPoint(f.createIRI(dataset.getString("http://www.w3.org/ns/dcat#contactPoint")));
                } catch (Exception ex) {
                    LOGGER.warn(ex.getMessage());
                }
                datasetMetadata.setTitle(f.createLiteral(dataset.getString("http://purl.org/dc/terms/title")));
                datasetMetadata.setIssued(f.createLiteral(date));
                datasetMetadata.setModified(f.createLiteral(date));
                datasetMetadata.setVersion(f.createLiteral(dataset.getString("http://purl.org/dc/terms/hasVersion")));
                try {
                    datasetMetadata.setLanguage(
                            f.createIRI(dataset.getJSONArray("http://purl.org/dc/terms/language").getString(0)));
                } catch (Exception ex) {
                    LOGGER.warn(ex.getMessage());
                }
                try {
                    datasetMetadata.setRights(f.createIRI(dataset.getString("http://purl.org/dc/terms/rights")));
                } catch (Exception ex) {
                    LOGGER.warn(ex.getMessage());
                }
                // optional

                try {
                    datasetMetadata
                            .setDescription(f.createLiteral(dataset.getString("http://purl.org/dc/terms/description")));
                } catch (Exception ex) {
                    LOGGER.warn(ex.getMessage());
                }
                if (catalog.getBoolean("_exists")) {
                    datasetMetadata.setParentURI(f.createIRI(
                            catalog.getString("http://rdf.biosemantics.org/ontologies/fdp-o#metadataIdentifier")));
                } else {
                    datasetMetadata.setParentURI(f.createIRI(fdp.getString("baseUri") + "/catalog/"
                            + catalog.getString("http://purl.org/dc/terms/title").replace(" ", "_") + "_"
                            + catalog.getString("http://purl.org/dc/terms/hasVersion").replace(" ", "_") + "_"
                            + uuid_catalog));
                }
                agent = new Agent();
                agent.setUri(f.createIRI(fdp.getString("baseUri") + "/datasetAgent/"
                        + dataset.getString("http://purl.org/dc/terms/title").replace(" ", "_") + "_"
                        + dataset.getString("http://purl.org/dc/terms/hasVersion").replace(" ", "_")));
                agent.setName(
                        f.createLiteral(dataset.getJSONObject("http://purl.org/dc/terms/publisher").getString("url")));
                datasetMetadata.setPublisher(agent);
                datasetMetadata.setUri(f.createIRI("http://base/catalog/dataset"));
                datasetString = MetadataUtils.getString(datasetMetadata, RDFFormat.TURTLE, false)
                        .replaceAll("\\<" + datasetMetadata.getUri() + "\\>", "<>");
            }

            if (fdp.getString("uploadtype").equals("ftp")) {
                distributionMetadata.setDownloadURL(f.createIRI("ftp://" + uploadConfiguration.getString("host")
                        + uploadConfiguration.getString("directory") + "FAIRdistribution_"
                        + distribution.getString("http://purl.org/dc/terms/title").replace(" ", "_") + "_"
                        + distribution.getString("http://purl.org/dc/terms/hasVersion").replace(" ", "_") + ".ttl"));
                // optional
                try {
                    distributionMetadata.setMediaType(f.createLiteral("text/turtle"));
                } catch (Exception ex) {
                    LOGGER.warn(ex.getMessage());
                }
                distributionMetadata.setByteSize(f.createLiteral(datasetString.getBytes("UTF-8").length));
            } else if (fdp.getString("uploadtype").equals("virtuoso")) {
                try {
                    distributionMetadata.setMediaType(f.createLiteral("text/turtle"));
                } catch (Exception ex) {
                    LOGGER.warn(ex.getMessage());
                }
                distributionMetadata.setDownloadURL(f.createIRI(uploadConfiguration.getString("host")
                        + uploadConfiguration.getString("directory") + "FAIRdistribution_"
                        + distribution.getString("http://purl.org/dc/terms/title").replace(" ", "_") + "_"
                        + distribution.getString("http://purl.org/dc/terms/hasVersion").replace(" ", "_") + ".ttl"));
            }

            distributionMetadata.setTitle(f.createLiteral(distribution.getString("http://purl.org/dc/terms/title")));
            if (dataset.getBoolean("_exists")) {
                distributionMetadata.setParentURI(f.createIRI(
                        dataset.getString("http://rdf.biosemantics.org/ontologies/fdp-o#metadataIdentifier")));
            } else {
                distributionMetadata.setParentURI(f.createIRI(fdp.getString("baseUri") + "/dataset/"
                        + dataset.getString("http://purl.org/dc/terms/title").replace(" ", "_") + "_"
                        + dataset.getString("http://purl.org/dc/terms/hasVersion").replace(" ", "_") + "_"
                        + uuid_dataset));
            }
            distributionMetadata
                    .setVersion(f.createLiteral(distribution.getString("http://purl.org/dc/terms/hasVersion")));
            // optional
            try {
                distributionMetadata
                        .setLicense(f.createIRI(distribution.getString("http://purl.org/dc/terms/license")));
                distributionMetadata.setLicense(f.createIRI(distribution.getString("http://purl.org/dc/terms/rights")));
            } catch (Exception ex) {
                LOGGER.warn(ex.getMessage());
            }
            distributionMetadata.setUri(f.createIRI("http://base/catalog/dataset/distribution"));
            distributionMetadata.setIssued(f.createLiteral(date));
            distributionMetadata.setModified(f.createLiteral(date));
            // optional
            try {
                distributionMetadata.setDescription(
                        f.createLiteral(distribution.getString("http://purl.org/dc/terms/description")));
            } catch (Exception ex) {
            }
            distributionString = MetadataUtils.getString(distributionMetadata, RDFFormat.TURTLE, false)
                    .replaceAll("\\<" + distributionMetadata.getUri() + "\\>", "<>");
            String catalogPost = null;
            String datasetPost = null;
            if (!catalog.getBoolean("_exists")) {
                catalogPost = IOUtils
                        .toString(
                                HttpUtils
                                        .post(fdp.getString("baseUri") + "/catalog?id="
                                                + catalog.getString("http://purl.org/dc/terms/title").replace(" ", "_")
                                                + "_"
                                                + catalog.getString("http://purl.org/dc/terms/hasVersion").replace(" ",
                                                        "_")
                                                + "_" + uuid_catalog, catalogString, "text/turtle")
                                        .getContent(),
                                "UTF-8");
            }
            if (!dataset.getBoolean("_exists")) {
                datasetPost = IOUtils
                        .toString(
                                HttpUtils
                                        .post(fdp.getString("baseUri") + "/dataset?id="
                                                + dataset.getString("http://purl.org/dc/terms/title").replace(" ", "_")
                                                + "_"
                                                + dataset.getString("http://purl.org/dc/terms/hasVersion").replace(" ",
                                                        "_")
                                                + "_" + uuid_dataset, datasetString, "text/turtle")
                                        .getContent(),
                                "UTF-8");
            }
            String distributionPost = IOUtils
                    .toString(
                            HttpUtils
                                    .post(fdp.getString("baseUri") + "/distribution?id="
                                            + distribution.getString("http://purl.org/dc/terms/title").replace(" ", "_")
                                            + "_"
                                            + distribution.getString("http://purl.org/dc/terms/hasVersion").replace(" ",
                                                    "_")
                                            + "_" + uuid_distribution, distributionString, "text/turtle")
                                    .getContent(),
                            "UTF-8");

            String data = new JSONObject(jb.toString()).getString("data");
            PushFairDataToResourceAdapter adapter = new PushFairDataToResourceAdapter();
            if (fdp.getString("uploadtype").equals("ftp")) {
                r = new FtpResource(uploadConfiguration.getString("host"), uploadConfiguration.getString("username"),
                        uploadConfiguration.getString("password"), uploadConfiguration.getString("directory"),
                        "FAIRdistribution_" + distribution.getString("http://purl.org/dc/terms/title").replace(" ", "_")
                                + "_" + distribution.getString("http://purl.org/dc/terms/hasVersion").replace(" ", "_")
                                + ".ttl");
            } else if (fdp.getString("uploadtype").equals("virtuoso")) {
                System.out.println(uploadConfiguration.getString("host"));
                r = new VirtuosoResource(uploadConfiguration.getString("host"),
                        "FAIRdistribution_" + distribution.getString("http://purl.org/dc/terms/title").replace(" ", "_")
                                + "_" + distribution.getString("http://purl.org/dc/terms/hasVersion").replace(" ", "_")
                                + ".ttl",
                        uploadConfiguration.getString("username"), uploadConfiguration.getString("password"),
                        uploadConfiguration.getString("directory"));
            }
            r.setFairData(data);
            adapter.setResource(r);
            adapter.push();

            res.setCharacterEncoding("UTF-8");
            res.setHeader("Content-Type", "application/json");
            JSONWriter writer = new JSONWriter(res.getWriter());
            writer.object();
            writer.key("code");
            writer.value("ok");
            if (!catalog.getBoolean("_exists")) {
                writer.key("catalogPost");
                writer.value(catalogPost);
            }
            if (!dataset.getBoolean("_exists")) {
                writer.key("datasetPost");
                writer.value(datasetPost);
            }
            writer.key("distributionPost");
            writer.value(distributionPost);
            writer.endObject();

        } catch (Exception ex) {
            respondException(res, ex);
        }
    }
}
