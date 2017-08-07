package org.dtls.fairifier;

import java.lang.IllegalArgumentException;
import java.io.File;
import java.net.URL;
import java.net.HttpURLConnection;
import java.io.OutputStreamWriter;
import org.deri.grefine.rdf.utils.HttpUtils;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 * @author Shamanou van Leeuwen
 * @date 20-02-2017
 *
*/

public class VirtuosoResource extends Resource{
    private String out;
    private String host;
    private String filename;
    private String directory;
    private String username;
    private String password;
    private static final Logger log = LoggerFactory.getLogger(VirtuosoResource.class);


    public VirtuosoResource(String host, String filename, String username, String password, String directory){
        this.host = host;
        this.filename = filename;
        this.username = username;
        this.password = password;
        this.directory = directory;
    }
    
    public void push(){
        if (!this.hasModel()){
            throw new IllegalArgumentException("Data of Resource object not set!");
        }else{
            try{
                this.out = this.getModelString();
                HttpUtils.put(this.host + this.directory + this.filename, this.out, this.username, this.password);
            }catch(Exception e){ log.error(e.getMessage()); }
        }
    }
}