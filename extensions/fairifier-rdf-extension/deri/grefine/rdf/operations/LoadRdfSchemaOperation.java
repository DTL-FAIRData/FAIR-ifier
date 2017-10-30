package org.deri.grefine.rdf.operations;

import java.util.Properties;
import org.deri.grefine.rdf.RdfSchema;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;
import com.google.refine.history.Change;
import com.google.refine.history.HistoryEntry;
import com.google.refine.model.AbstractOperation;
import com.google.refine.model.Project;
import com.google.refine.operations.OperationRegistry;

public class LoadRdfSchemaOperation extends AbstractOperation {

    final protected RdfSchema _schema;

    public LoadRdfSchemaOperation(RdfSchema schema) {
        this._schema = schema;
    }

    static public AbstractOperation reconstruct(Project project, JSONObject obj) throws Exception {
        return new LoadRdfSchemaOperation(RdfSchema.reconstruct(obj.getJSONObject("schema")));
    }

    @Override
    public void write(JSONWriter writer, Properties options) throws JSONException {
        writer.object();
        writer.key("op");
        writer.value(OperationRegistry.s_opClassToName.get(this.getClass()));
        writer.key("description");
        writer.value("Load RDF schema skeleton");
        writer.key("schema");
        _schema.write(writer, options);
        writer.endObject();

    }

    @Override
    protected String getBriefDescription(Project project) {
        return "Load RDF schema skelton";
    }

    @Override
    protected HistoryEntry createHistoryEntry(Project project, long historyEntryID)
            throws Exception {
        String description = "Load RDF schema skeleton";

        Change change = new SaveRdfSchemaOperation.RdfSchemaChange(_schema);

        return new HistoryEntry(historyEntryID, project, description, LoadRdfSchemaOperation.this,
                change);
    }
}
