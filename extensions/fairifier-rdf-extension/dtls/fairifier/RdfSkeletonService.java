package org.dtls.fairifier;

import java.util.List;

/**
 * 
 * This service communicates with the API backend of openrefine and allows
 * direct access to rdf skeletons.
 * 
 * @author Shamanou van Leeuwen
 *
 */

interface RdfSkeletonService {
    public List<String> listModels(String fileType);
    public List<String> listModels();
    public void saveModel(String json, String fileType, String projectId);
    public String loadModel(String ProjectId);
}