
package org.dtls.fairifier;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Allows a developer to implement transformations from the openrefine provided
 * JSON blob and other formats.
 * 
 * @author Shamanou van Leeuwen
 * @see http://openrefine.org/
 */
public interface RdfSkeletonTransformer {

    /**
     * 
     * Should return a Model object based on the Path given.
     * 
     * @param path
     * @return Model
     * @throws -
     *             thrown when there is file available
     */
    public Model read(Path path)
            throws IOException;

    /**
     * Should write a model in a specific format to the specified path.
     * 
     * @param model
     *            - model to write
     * @param path
     *            - path to write to
     * @throws IOException
     *             - thrown when there is no file available
     */
    public void write(Model model, Path path)
            throws IOException;
}
