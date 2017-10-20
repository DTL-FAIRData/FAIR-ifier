
package org.dtls.fairifier;

import java.io.IOException;

import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.deri.grefine.rdf.utils.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Shamanou van Leeuwen
 * @date 20-02-2017
 *
 */

public class VirtuosoResource extends Resource {

    private String out;
    private String host;
    private String filename;
    private String directory;
    private String username;
    private String password;
    private static final Logger log = LoggerFactory.getLogger(VirtuosoResource.class);

    /**
     * 
     * This constructor takes in the parameters needed to push to a Virtuoso WEBDAV
     * directory.
     *
     * @param host
     *            the full hostname including the protocol, for example:
     *            http://dtls.nl
     * @param filename
     *            the name with which the file will be saved on the Virtuoso server.
     * @param username
     *            the Virtuoso username
     * @param password
     *            the Virtuoso password
     * @param directory
     *            the path where the file should be store on the Virtuoso server.
     */
    public VirtuosoResource(String host, String filename, String username, String password, String directory) {
        this.host = host;
        this.filename = filename;
        this.username = username;
        this.password = password;
        this.directory = directory;
    }

    /**
     * Check if there is a model and if so push it to Virtuoso.
     * 
     */
    public void push()
            throws IOException, HttpException {
        if (!this.hasModel()) {
            throw new IllegalArgumentException("Data of Resource object not set!");
        } else {
            this.out = this.getModelString();
            HttpResponse res = HttpUtils.put(this.host + this.directory + this.filename, this.out, this.username,
                    this.password);
            if (res.getStatusLine().getStatusCode() != 200) {
                throw new HttpException("Pushing data to Virtuso failed");
            }
        }
    }
}
