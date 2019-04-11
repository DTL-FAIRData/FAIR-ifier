
package org.dtls.fairifier;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;

public class FileSystemRdfSkeletonImpl implements RdfSkeletonService {

    private static final Path SAVELOCATION = Paths.get(System.getProperty("user.home") + "/.local/share/openrefine/");
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final RdfSkeletonTransformer rdfSkeletonJsonTransformerImpl = new RdfSkeletonJsonTransformerImpl();

    @Override
    public List<Model> listModels(final String fileType)
            throws IOException {
        return Files.list(SAVELOCATION).filter(Files::isRegularFile).map(this::getStringAndFiletype)
                .filter(val -> val.getMetadata() != null)
                .filter(val -> val.getMetadata().getFileType().equals(fileType)).collect(Collectors.toList());
    }

    @Override
    public List<Model> listModels()
            throws IOException {
        return Files.list(SAVELOCATION).filter(Files::isRegularFile).map(this::getStringAndFiletype)
                .filter(val -> val.getMetadata() != null).collect(Collectors.toList());
    }

    @Override
    public void saveModel(String json, String fileType, String title, String projectId)
            throws IOException {

        Path path = Paths.get(SAVELOCATION.toString(), projectId + ".skeleton.json");
        SkeletonMetadata metadata = new SkeletonMetadata();
        metadata.setFileName(path.toString());
        metadata.setFileType(fileType);
        metadata.setTitle(title);
        metadata.setProjectId(projectId);
        Model model = new Model();
        model.setJson(json);
        model.setMetadata(metadata);
        rdfSkeletonJsonTransformerImpl.write(model, path);
    }

    @Override
    public Model loadModel(String projectId)
            throws IOException {
        return rdfSkeletonJsonTransformerImpl.read(Paths.get(SAVELOCATION.toString(), projectId + ".skeleton.json"));
    }

    private Model getStringAndFiletype(Path element) {
        String name = element.getFileName().toString().split("\\.")[0];
        Model model = new Model();
        try {
            if (element.getFileName().toString().contains("metadata")) {
                SkeletonMetadata metadata = mapper.readValue(
                        new File(SAVELOCATION.toString() + File.separator + name + ".metadata.skeleton.json"),
                        SkeletonMetadata.class);
                String json = new String(
                        Files.readAllBytes(Paths.get(SAVELOCATION.toString(), name + ".skeleton.json")));

                model.setMetadata(metadata);
                model.setJson(json);
                return model;
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return model;
    }
}
