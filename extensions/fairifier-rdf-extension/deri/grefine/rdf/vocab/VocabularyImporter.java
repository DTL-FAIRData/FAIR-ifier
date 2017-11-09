package org.deri.grefine.rdf.vocab;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import org.apache.http.HttpHeaders;
import org.apache.http.ProtocolException;
import org.apache.tika.mime.MediaType;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleNamespace;
import org.eclipse.rdf4j.model.impl.ValueFactoryImpl;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.RDFParserRegistry;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.jsonld.JSONLDParser;
import org.eclipse.rdf4j.rio.nquads.NQuadsParser;
import org.eclipse.rdf4j.rio.ntriples.NTriplesParser;
import org.eclipse.rdf4j.rio.rdfjson.RDFJSONParser;
import org.eclipse.rdf4j.rio.rdfxml.RDFXMLParser;
import org.eclipse.rdf4j.rio.trig.TriGParser;
import org.eclipse.rdf4j.rio.trix.TriXParser;
import org.eclipse.rdf4j.rio.turtle.TurtleParser;
import org.eclipse.rdf4j.sail.inferencer.fc.ForwardChainingRDFSInferencer;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class VocabularyImporter {

    private final static Logger logger = LoggerFactory.getLogger(VocabularyImporter.class);
    private static final String PREFIXES = "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> "
            + "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
            + "PREFIX skos:<http://www.w3.org/2004/02/skos/core#> ";
    private static final String CLASSES_QUERY_P1 = PREFIXES
            + "SELECT ?resource ?label ?en_label ?description ?en_description ?definition ?en_definition "
            + "WHERE { " + "?resource rdf:type rdfs:Class. "
            + "OPTIONAL {?resource rdfs:label ?label.} "
            + "OPTIONAL {?resource rdfs:label ?en_label. FILTER langMatches( lang(?en_label), \"EN\" )  } "
            + "OPTIONAL {?resource rdfs:comment ?description.} "
            + "OPTIONAL {?resource rdfs:comment ?en_description. FILTER langMatches( lang(?en_description), \"EN\" )  } "
            + "OPTIONAL {?resource skos:definition ?definition.} "
            + "OPTIONAL {?resource skos:definition ?en_definition. FILTER langMatches( lang(?en_definition), \"EN\" )  } "
            + "FILTER regex(str(?resource), \"^";
    private static final String CLASSES_QUERY_P2 = "\")}";

    private static final String PROPERTIES_QUERY_P1 = PREFIXES
            + "SELECT ?resource ?label ?en_label ?description ?en_description ?definition ?en_definition "
            + "WHERE { " + "?resource rdf:type rdf:Property. "
            + "OPTIONAL {?resource rdfs:label ?label.} "
            + "OPTIONAL {?resource rdfs:label ?en_label. FILTER langMatches( lang(?en_label), \"EN\" )  } "
            + "OPTIONAL {?resource rdfs:comment ?description.} "
            + "OPTIONAL {?resource rdfs:comment ?en_description. FILTER langMatches( lang(?en_description), \"EN\" )  } "
            + "OPTIONAL {?resource skos:definition ?definition.} "
            + "OPTIONAL {?resource skos:definition ?en_definition. FILTER langMatches( lang(?en_definition), \"EN\" )  } "
            + "FILTER regex(str(?resource), \"^";
    private static final String PROPERTIES_QUERY_P2 = "\")}";
    private static final ArrayList<RDFParser> PARSERS = new ArrayList<RDFParser>();
    static {
        PARSERS.add(new TurtleParser());
        PARSERS.add(new NTriplesParser());
        PARSERS.add(new RDFXMLParser());
        PARSERS.add(new JSONLDParser());
        PARSERS.add(new NQuadsParser());
        PARSERS.add(new RDFJSONParser());
        PARSERS.add(new TriXParser());
        PARSERS.add(new TriGParser());
    }


    public void importVocabulary(String name, String uri, String fetchUrl, List<RDFSClass> classes,
            List<RDFSProperty> properties) throws VocabularyImportException {
        try {
            Repository repos = getModel(fetchUrl, name);
            getTerms(repos, name, classes, properties);
        } catch (IOException e) {
            throw new VocabularyImportException("Unable to import vocabulary from " + fetchUrl, e);
        }
    }

    public void importVocabulary(String name, String uri, Repository repository,
            List<RDFSClass> classes, List<RDFSProperty> properties)
            throws VocabularyImportException {
        try {
            getTerms(repository, name, classes, properties);
        } catch (IOException e) {
            throw new VocabularyImportException("Unable to import vocabulary from " + uri, e);
        }
    }

    private Repository getModel(String url, String name) throws VocabularyImportException {
        try {
            final Repository repository =
                    new SailRepository(new ForwardChainingRDFSInferencer(new MemoryStore()));
            repository.initialize();
            try (RepositoryConnection con = repository.getConnection()) {
                final ValueFactory F = new ValueFactoryImpl();
                Model model = getModelFromUrl(url);
                if (model.getNamespaces().contains(new SimpleNamespace(name, url))) {
                    con.add(model, F.createIRI(model.getNamespace(name).get().getName()));
                } else {
                    try {
                        con.add(model, F.createIRI(model.getNamespace("").get().getName()));
                    } catch (NoSuchElementException ex) {
                        // needed in case the base uri and location are equal, and the base uri is
                        // not in a name space
                        con.add(model, F.createIRI(url));
                    }
                }
                con.close();
                return repository;
            }
        } catch (Exception e) {
            throw new VocabularyImportException("Unable to import vocabulary from " + url, e);
        }
    }

    private Model getModelFromUrl(String url)
            throws IOException, MalformedURLException, IOException, ProtocolException {
        RDFParser parser = getParserFromUrl(url);
        HttpURLConnection httpConnection = (HttpURLConnection) new URL(url).openConnection();
        httpConnection.setRequestMethod("GET");
        httpConnection.setRequestProperty(HttpHeaders.ACCEPT,
                parser.getRDFFormat().getDefaultMIMEType());
        return Rio.parse(httpConnection.getInputStream(), "", parser.getRDFFormat());
    }

    private RDFParser getParserFromUrl(String url) throws IOException, MalformedURLException {
        StringBuffer response = new StringBuffer();
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        Set<RDFFormat> rdfFormats = RDFParserRegistry.getInstance().getKeys();
        List<String> acceptHeaders = RDFFormat.getAcceptParams(rdfFormats, false, RDFFormat.RDFXML);
        StringBuilder acceptHeaderStrBuilder = new StringBuilder();


        for (String acceptHeader : acceptHeaders) {
            String acceptHeaderFirstPart = acceptHeader.split(";")[0].trim();
            if (!acceptHeaderFirstPart.equals(MediaType.TEXT_PLAIN.toString())) {
                acceptHeaderStrBuilder.append(acceptHeader);
                acceptHeaderStrBuilder.append(",");
            }
        }
        String acceptHeaderStr = acceptHeaderStrBuilder.toString();
        con.setRequestProperty(HttpHeaders.ACCEPT,
                acceptHeaderStr.substring(0, acceptHeaderStr.length() - 1));
        HttpURLConnection.setFollowRedirects(true);
        con.connect();

        String firstContentTypeHeader = con.getContentType().split(";")[0].trim();
        if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
            if (firstContentTypeHeader.equals(MediaType.TEXT_HTML.toString())
                    || firstContentTypeHeader.equals(MediaType.TEXT_PLAIN.toString())) {

                for (RDFParser parser : PARSERS) {
                    try {
                        parser.parse(con.getInputStream(), url);
                        return parser;
                    } catch (IOException | RDFParseException | RDFHandlerException e) {
                        continue;
                    }
                }
            } else {
                Optional<RDFFormat> rdfFormat =
                        Rio.getParserFormatForMIMEType(con.getContentType());
                if (rdfFormat.isPresent()) {
                    return Rio.createParser(
                            Rio.getParserFormatForMIMEType(con.getContentType()).get());
                }
            }
        }
        return Rio.createParser(RDFFormat.RDFXML);
    }

    protected void getTerms(Repository repos, String name, List<RDFSClass> classes,
            List<RDFSProperty> properties) throws VocabularyImportException, IOException {
        String uri = null;
        try {
            RepositoryConnection con = repos.getConnection();
            if (con.getNamespaces().asList().contains(name)) {
                uri = con.getNamespace(name);
            } else {
                uri = "";
            }
            try {
                TupleQuery query = con.prepareTupleQuery(QueryLanguage.SPARQL,
                        CLASSES_QUERY_P1 + uri + CLASSES_QUERY_P2);
                TupleQueryResult res = query.evaluate();

                Set<String> seen = new HashSet<String>();
                while (res.hasNext()) {
                    BindingSet solution = res.next();
                    String clazzURI = solution.getValue("resource").stringValue();

                    if (seen.contains(clazzURI)) {
                        continue;
                    }
                    seen.add(clazzURI);
                    String label = getFirstNotNull(new Value[] {solution.getValue("en_label"),
                            solution.getValue("label")});
                    String description = getFirstNotNull(new Value[] {
                            solution.getValue("en_definition"), solution.getValue("definition"),
                            solution.getValue("en_description"), solution.getValue("description")});
                    RDFSClass clazz = new RDFSClass(clazzURI, label, description, name, uri);
                    classes.add(clazz);
                }

                query = con.prepareTupleQuery(QueryLanguage.SPARQL,
                        PROPERTIES_QUERY_P1 + uri + PROPERTIES_QUERY_P2);
                res = query.evaluate();

                seen = new HashSet<String>();
                while (res.hasNext()) {
                    BindingSet solution = res.next();
                    String propertyUri = solution.getValue("resource").stringValue();

                    if (!seen.contains(propertyUri)) {
                        seen.add(propertyUri);
                        String label = getFirstNotNull(new Value[] {solution.getValue("en_label"),
                                solution.getValue("label")});
                        String description = getFirstNotNull(new Value[] {
                                solution.getValue("en_definition"), solution.getValue("definition"),
                                solution.getValue("en_description"),
                                solution.getValue("description")});
                        RDFSProperty prop =
                                new RDFSProperty(propertyUri, label, description, name, uri);
                        properties.add(prop);
                    }
                }

            } catch (Exception ex) {
                throw new VocabularyImportException(
                        "Error while processing vocabulary retrieved from " + uri, ex);
            } finally {
                con.close();
            }
        } catch (RepositoryException ex) {
            throw new VocabularyImportException(
                    "Error while processing vocabulary retrieved from " + uri, ex);
        }
    }

    private String getFirstNotNull(Value[] values) {
        String s = null;
        for (int i = 0; i < values.length; i++) {
            s = getString(values[i]);
            if (s != null) {
                break;
            }
        }
        return s;
    }

    private String getString(Value v) {
        if (v != null) {
            return v.stringValue();
        }
        return null;
    }
}
