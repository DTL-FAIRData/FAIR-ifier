
package org.deri.grefine.rdf.commands;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.deri.grefine.rdf.app.ApplicationContext;
import org.deri.grefine.rdf.vocab.PrefixExistException;
import org.deri.grefine.rdf.vocab.VocabularyImportException;
import org.deri.grefine.rdf.vocab.VocabularyImporter;
import org.deri.grefine.rdf.vocab.VocabularyIndexException;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.sail.inferencer.fc.ForwardChainingRDFSInferencer;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.json.JSONException;
import org.json.JSONWriter;

public class AddPrefixFromFileCommand extends RdfCommand {

    public AddPrefixFromFileCommand(final ApplicationContext ctxt) {
        super(ctxt);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        final FileItemFactory factory = new DiskFileItemFactory();

        // Create a new file upload handler
        final ServletFileUpload upload = new ServletFileUpload(factory);

        String uri = null, prefix = null, projectId = null;
        Optional<RDFFormat> format = null;
        InputStream in = null;
        List<FileItem> items = null;
        try {
            items = upload.parseRequest(request);
        } catch (FileUploadException e) {
            respondException(response, e);
        }
        for (FileItem item : items) {
            if (item.getFieldName().equals("vocab-prefix")) {
                prefix = item.getString();
            } else if (item.getFieldName().equals("vocab-uri")) {
                uri = item.getString();
            } else if (item.getFieldName().equals("project")) {
                projectId = item.getString();
            } else if (item.getFieldName().equals("file_format")) {
                format = Rio.getParserFormatForMIMEType(item.getString());
            } else {
                try {
                    in = item.getInputStream();
                } catch (IOException e) {
                    respondException(response, e);
                }
            }
        }

        final Repository repository = new SailRepository(new ForwardChainingRDFSInferencer(new MemoryStore()));
        repository.initialize();
        try (RepositoryConnection con = repository.getConnection()) {
            con.add(in, "", format.get());
            con.close();
            getRdfSchema(projectId).addPrefix(prefix, uri);

            getRdfContext().getVocabularySearcher().importAndIndexVocabulary(prefix, uri, repository, projectId,
                    new VocabularyImporter());
        } catch (VocabularyImportException e) {
            respondException(response, e);
        } catch (PrefixExistException e) {
            respondException(response, e);
        } catch (VocabularyIndexException e) {
            respondException(response, e);
        }
        try {
            // success
            JSONWriter writer = new JSONWriter(response.getWriter());
            writer.object();
            writer.key("code");
            writer.value("ok");
            writer.endObject();
        } catch (JSONException e) {
            respondException(response, e);
        }
    }
}
