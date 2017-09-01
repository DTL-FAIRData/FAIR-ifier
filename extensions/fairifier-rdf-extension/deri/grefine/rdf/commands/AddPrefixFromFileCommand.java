package org.deri.grefine.rdf.commands;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.deri.grefine.rdf.app.ApplicationContext;
import org.deri.grefine.rdf.vocab.VocabularyImporter;
import org.json.JSONException;
import org.json.JSONObject;
import org.eclipse.rdf4j.repository.Repository;;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.sail.inferencer.fc.ForwardChainingRDFSInferencer;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.json.JSONWriter;

public class AddPrefixFromFileCommand extends RdfCommand{

    public AddPrefixFromFileCommand(ApplicationContext ctxt) {
		super(ctxt);
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)throws ServletException, IOException {
		try {
			FileItemFactory factory = new DiskFileItemFactory();

			// Create a new file upload handler
			ServletFileUpload upload = new ServletFileUpload(factory);

			String uri = null, prefix = null, format = null, projectId = null, filename="";
			InputStream in = null;
			@SuppressWarnings("unchecked")
			List<FileItem> items = upload.parseRequest(request);
			for(FileItem item:items){
				if(item.getFieldName().equals("vocab-prefix")){
					prefix = item.getString(); 
				}else if(item.getFieldName().equals("vocab-uri")){
					uri = item.getString();
				}else if(item.getFieldName().equals("project")){
					projectId = item.getString();
				}else if(item.getFieldName().equals("file_format")){
					format = item.getString();
					if(format.equals("text/turtle")){
						format = "TTL";
					}else if(format.equals("application/rdf+xml")){
						format = "RDF/XML";
					}else if(format.equals("text/rdf+n3")){
						format = "N3";
					}else if(format.equals("application/n-triples")){
						format = "NTRIPLE";
					}
				}else{
					filename = item.getName();
					in = item.getInputStream();
				}
			}

			Repository repository = new SailRepository(new ForwardChainingRDFSInferencer(new MemoryStore()));
			repository.initialize();
			RepositoryConnection con = repository.getConnection();
			RDFFormat rdfFormat;
			if(format.equals("TTL")){
				rdfFormat = RDFFormat.TURTLE;
			}else if(format.equals("N3")){
				rdfFormat = RDFFormat.N3;
			}else if(format.equals("NTRIPLE")) {
				rdfFormat = RDFFormat.NTRIPLES;
			}else{
				rdfFormat = RDFFormat.RDFXML;
			}
			con.add(in, "", rdfFormat);
			con.close();

			getRdfSchema(projectId).addPrefix(prefix, uri);

        	getRdfContext().getVocabularySearcher().importAndIndexVocabulary(prefix, uri, repository, projectId, new VocabularyImporter());
        	//success
                JSONWriter writer = new JSONWriter(response.getWriter());
                writer.object();
                writer.key("code") ; writer.value("ok");
                writer.endObject();

		} catch (Exception e) {
		    respondException(response, e);
		}

	}
}
