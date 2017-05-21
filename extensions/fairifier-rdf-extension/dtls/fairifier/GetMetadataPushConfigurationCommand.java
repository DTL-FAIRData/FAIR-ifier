package org.dtls.fairifier;

import java.io.BufferedReader;
import java.io.FileReader;
import java.lang.Exception;
import java.io.IOException;
import org.deri.grefine.rdf.utils.HttpUtils;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONWriter;
import com.google.refine.commands.Command;
import org.deri.grefine.rdf.app.ApplicationContext;
import com.google.refine.browsing.Engine;
import com.google.refine.model.Project;
import org.openrdf.rio.RDFWriter; 
import java.lang.System;
import java.lang.Exception;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import java.io.File;
import java.lang.System;

/**
 * 
 * @author Shamanou van Leeuwen
 * @date 18-05-2017
 *
 */

public class GetMetadataPushConfigurationCommand extends Command{
    
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        StringBuilder contentBuilder = new StringBuilder();
        try {
            BufferedReader in = new BufferedReader(new FileReader(System.getProperty("user.dir") + "/extensions/grefine-rdf-extension/config.xml"));
            String str;
            while ((str = in.readLine()) != null) {
                contentBuilder.append(str);
            }
            in.close();
        } catch (IOException e) {}

        try{
            res.setCharacterEncoding("UTF-8");
            res.setHeader("Content-Type", "application/json");
            JSONWriter writer = new JSONWriter(res.getWriter());
            writer.object();
            writer.key("data"); writer.value(XML.toJSONObject(contentBuilder.toString()).toString());
            writer.endObject();
        }catch(Exception e){
            respondException(res, e);
        }
    }
}