
package org.dtls.fairifier;

import java.io.IOException;
import java.io.Writer;
import java.util.Properties;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.deri.grefine.rdf.RdfSchema;
import org.deri.grefine.rdf.operations.LoadRdfSchemaOperation;
import org.json.JSONObject;
import org.json.JSONWriter;
import com.google.refine.commands.Command;
import com.google.refine.history.HistoryEntry;
import com.google.refine.model.AbstractOperation;
import com.google.refine.model.Project;
import com.google.refine.process.Process;
import com.google.refine.util.ParsingUtilities;

public class LoadRdfSkeletonCommand extends Command {

    private static final RdfSkeletonService rdfSkeletonService = new FileSystemRdfSkeletonImpl();

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        res.setCharacterEncoding("UTF-8");
        res.setHeader("Content-Type", "application/json");
        JSONWriter writer = new JSONWriter(res.getWriter());
        try {
            // this needs the project parameter, indicating the project on which the skeleton should
            // be applied
            Project project = getProject(req);

            String jsonString = req.getParameter("schema");
            JSONObject json = ParsingUtilities.evaluateJsonStringToObject(jsonString);
            RdfSchema schema = RdfSchema.reconstruct(json);

            AbstractOperation op = new LoadRdfSchemaOperation(schema);
            Process process = op.createProcess(project, new Properties());

            performProcess(req, res, project, process);

        } catch (Exception e) {
            respondException(res, e);
        }
    }

    private void performProcess(HttpServletRequest request, HttpServletResponse response,
            Project project, Process process) throws Exception {
        Model out = rdfSkeletonService.loadModel(request.getParameter("projectId")); // this is the
                                                                                     // project
                                                                                     // where the
                                                                                     // skeleton
                                                                                     // comes from
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Type", "application/json");

        HistoryEntry historyEntry = project.processManager.queueProcess(process);
        if (historyEntry != null) {
            Writer w = response.getWriter();
            JSONWriter writer = new JSONWriter(w);
            Properties options = new Properties();

            writer.object();
            writer.key("code");
            writer.value("ok");
            writer.key("historyEntry");
            historyEntry.write(writer, options);
            writer.key("skeleton");
            writer.value(out.getJson());
            writer.endObject();

            w.flush();
            w.close();
        } else {
            respond(response, "{ \"code\" : \"pending\" }");
        }
    }
}
