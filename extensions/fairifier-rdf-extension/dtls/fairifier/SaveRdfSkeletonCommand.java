package org.dtls.fairifier;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.refine.commands.Command;

public class SaveRdfSkeletonCommand extends Command{
    private static final RdfSkeletonService rdfSkeletonService = new FileSystemRdfSkeletonImpl();
    
    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        rdfSkeletonService.saveModel(req.getParameter("model"), req.getParameter("fileType"), req.getParameter("projectId"));
    }
}
