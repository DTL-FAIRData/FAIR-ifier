
package org.dtls.fairifier;

import java.io.IOException;

import org.apache.http.HttpException;

/**
 * A adapter which takes a resource and pushes it.
 * 
 * @author Shamanou van Leeuwen
 * @date 28-11-2016
 *
 */
public class PushFairDataToResourceAdapter {

    private Resource resource;

    public void push()
            throws IOException, HttpException {
        this.resource.push();
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }
}
