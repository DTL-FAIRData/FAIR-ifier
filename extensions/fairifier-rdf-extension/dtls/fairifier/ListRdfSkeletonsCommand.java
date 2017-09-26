package org.dtls.fairifier;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONWriter;

import com.google.refine.commands.Command;

public class ListRdfSkeletonsCommand extends Command{
    private static final RdfSkeletonService rdfSkeletonService = new FileSystemRdfSkeletonImpl();
    
    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setCharacterEncoding("UTF-8");
        res.setHeader("Content-Type", "application/json");
        JSONWriter writer = new JSONWriter(res.getWriter());
        try {
            writer.object();
            writer.key("list"); 
            writer.array();
        
            if (req.getParameter("fileType") == null) {
                List<String> list = rdfSkeletonService.listModels();
                for(String l : list) {
                    writer.value(l);
                }
            }else {
                List<String> list = rdfSkeletonService.listModels(req.getParameter("fileType"));
                for(String l : list) {
                    writer.value(l);
                }
            }
            writer.endArray();
            writer.endObject();
        }catch(JSONException e) {
            respondException(res, e);
        }
    }
}
