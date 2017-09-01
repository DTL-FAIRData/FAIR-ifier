package org.deri.grefine.reconcile.commands;


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
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.deri.grefine.reconcile.GRefineServiceManager;
import org.deri.grefine.reconcile.model.ReconciliationService;
import org.deri.grefine.reconcile.rdf.RdfReconciliationService;
import org.deri.grefine.reconcile.rdf.endpoints.QueryEndpoint;
import org.deri.grefine.reconcile.rdf.endpoints.QueryEndpointImpl;
import org.deri.grefine.reconcile.rdf.executors.DumpQueryExecutor;
import org.deri.grefine.reconcile.rdf.executors.QueryExecutor;
import org.deri.grefine.reconcile.rdf.factories.JenaTextSparqlQueryFactory;
import org.deri.grefine.reconcile.rdf.factories.SparqlQueryFactory;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;

import com.google.common.collect.ImmutableList;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class UploadFileAndAddServiceCommand extends AbstractAddServiceCommand{

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)throws ServletException, IOException {
		try{
			//write results
			response.setCharacterEncoding("UTF-8");
	        response.setHeader("Content-Type", "application/json");
	        PrintWriter out = response.getWriter();
			StringWriter sw = new StringWriter();
			JSONWriter w = new JSONWriter(sw);
			w.object();
	        w.key("code"); w.value("ok");
	        w.key("service");
	        ReconciliationService service = getReconciliationService(request);
	        service.writeAsJson(w);
	        w.endObject();
	        sw.flush();
			//out.print("<html><body><textarea>" + sw.toString() + "</textarea></body></html>");
	        out.print(sw.toString());
	        out.flush();
		} catch (Exception e) {
			respondError(response,e);
		}
	}

	@Override
	protected ReconciliationService getReconciliationService(HttpServletRequest request) throws JSONException, IOException {
		FileItemFactory factory = new DiskFileItemFactory();

		// Create a new file upload handler
		ServletFileUpload upload = new ServletFileUpload(factory);

		// Parse the request
		InputStream in = null;
		String name=null, id = null, props = null, format=null, filename = null;
		Model model = null;
		try{
			@SuppressWarnings("unchecked")
			List<FileItem> items = upload.parseRequest(request);
			for(FileItem item:items){
				if(item.getFieldName().equals("service_name")){
					name = item.getString(); 
					id = getIdForString(name);				
				}else if(item.getFieldName().equals("properties")){
					props = item.getString();
				}else if(item.getFieldName().equals("file_format")){
					format = item.getString();
					if(format.equals("text/turtle")){
						format = "TURTLE";
					}else if(format.equals("application/rdf+xml")){
						format = "RDF/XML";
					}else if(format.equals("text/rdf+n3")){
						format = "N3";
					}else if(format.equals("application/n-triples")){
						format = "N-TRIPLE";
					}
				}else if(item.getFieldName().equals("file_upload")){
					filename = item.getName();
					in = item.getInputStream();
				}
			}
			model = ModelFactory.createDefaultModel();

			System.out.println(format + " " + filename + " " + name);
			model.read(in, null ,format);
			
			ImmutableList<String> propUris = asImmutableList(props);
		
			if(GRefineServiceManager.singleton.hasService(id)){
				//id already exist
				throw new RuntimeException("A service with name '" + id + "' already exist!");
			}
			
			if(name.isEmpty() || propUris.size()==0){
				throw new RuntimeException("name and at least one label property ar needed");
			}
			
			SparqlQueryFactory queryFactory = new JenaTextSparqlQueryFactory();
			QueryExecutor queryExecutor;
			if(propUris.size()==1){
				queryExecutor = new DumpQueryExecutor(model,propUris.get(0));
			}else{
				queryExecutor = new DumpQueryExecutor(model);
			}
			QueryEndpoint queryEndpoint = new QueryEndpointImpl(queryFactory, queryExecutor);
			ReconciliationService service = new RdfReconciliationService(id, name, propUris, queryEndpoint, AddServiceCommand.DEFAULT_MATCH_THRESHOLD);
			GRefineServiceManager.singleton.addAndSaveService(service);
			return service;
		}catch(FileUploadException fe){
			throw new IOException(fe);
		}
	}

	private void respondError(HttpServletResponse response, Exception e) throws IOException, ServletException{
	    try {
	    JSONWriter writer = new JSONWriter(response.getWriter());
	    writer.object();
	    writer.key("code") ; writer.value("error");
	    writer.key("message") ; writer.value(e.getMessage());
	    writer.endObject();
        } catch (Exception ex) {
            respondException(response, ex);
        }

	}
}
