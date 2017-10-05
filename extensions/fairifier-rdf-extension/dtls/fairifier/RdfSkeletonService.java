
package org.dtls.fairifier;

import java.io.IOException;
import java.util.List;

/**
 * 
 * This service communicates with the API backend of openrefine and allows
 * direct access to rdf skeletons.
 * 
 * The implementations of this class enable a developer to use different ways of
 * persisting and reading rdf skeletons.
 * 
 * @author Shamanou van Leeuwen
 * @see http://openrefine.org/
 *
 */

interface RdfSkeletonService {

    /**
     * 
     * Returns a list of the rdf skeletons that belong to a specific filetype.
     * 
     * Should always return a list with available models, in case of a unavailable
     * file it should raise a IOException.
     * 
     * @param fileType
     *            - the requested filetpe
     * @return List<Model>
     * @throws IOException
     *             - thrown when there are no files available
     */
    public List<Model> listModels(String fileType)
            throws IOException;

    /**
     * Returns a list of all rdf skeletons.
     * 
     * Should always return a list with available models, in case of a unavailable
     * file it should raise a IOException.
     * 
     * @return List<Model>
     * @throws IOException
     *             - thrown when there are no files available
     */
    public List<Model> listModels()
            throws IOException;

    /**
     * 
     * Saves and persists the rdf skeleton. Rdf skeletons are persistent across
     * projects.
     * 
     * Should always return a list with available models, in case of a unavailable
     * file it should raise a IOException.
     * 
     * @param json
     *            - the json blob representing the rdf skeleton
     * @param fileType
     *            - the filetype of the file being FAIRified
     * @param title
     *            - the title of the rdf skeleton
     * @param projectId
     *            - the id of the project
     * @throws IOException
     *             - thrown when there is no file available
     */
    public void saveModel(String json, String fileType, String title, String projectId)
            throws IOException;

    /**
     * 
     * This method returns a model object when provided with a projectId.
     * 
     * Should always return a list with available models, in case of a unavailable
     * file it should raise a IOException.
     * 
     * @param projectId
     *            - the id of the project
     * @return Model
     * @throws IOException
     *             - thrown when there is no file available
     */
    public Model loadModel(String projectId)
            throws IOException;
}
