package dtls.fairifier;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

public class FileSystemRdfSkeletonImpl implements RdfSkeletonService {
    private static final Path SAVELOCATION = Paths.get(System.getProperty("user.home") + "/.local/share/openrefine/");
    
    @Override
    public List<String> listModels(final String fileType) {
        Stream<Path> files = Files.list(SAVELOCATION);
        ArrayList<String> out = new ArrayList<String>();
        files.forEach(element -> {
                String name = element.getFileName().toString().split(".")[0];
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    SkeletonMetadata metadata = mapper.readValue(new File(element.getParent() + File.separator + name + "metadata.skeleton.json"), SkeletonMetadata.class);
                    if (metadata.getFileType().equals(fileType)) {
                        out.add(new String(Files.readAllBytes(element)));
                    }
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        return out;
    }

    @Override
    public List<String> listModels() {
        Stream<Path> files = Files.list(SAVELOCATION);
        ArrayList<String> out = new ArrayList<String>();
        files.forEach(element -> {
            String name = element.getFileName().toString().split(".")[0];
            try {
                ObjectMapper mapper = new ObjectMapper();
                SkeletonMetadata metadata = mapper.readValue(new File(element.getParent() + File.separator + name + "metadata.skeleton.json"), SkeletonMetadata.class);
                out.add(new String(Files.readAllBytes(element)));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
        return out;
    }
    
    @Override
    public void saveModel(String json, String fileType, String projectId) {
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(SAVELOCATION.toString() + File.separator + projectId + ".skeleton.json"));
            out.write(json);
            out.close();
            ObjectMapper mapper = new ObjectMapper();
            SkeletonMetadata metadata = new SkeletonMetadata();
            metadata.setFileType(fileType);
            mapper.writeValue(new File(SAVELOCATION.toString() + File.separator  + projectId + ".metadata.skeleton.json"), metadata);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public String loadModel(String projectId) {
        Stream<Path> files = Files.list(SAVELOCATION);
        String out;
        files.forEach(element -> {
            String name = element.getFileName().toString().split(".")[0];
            if(name.equals(projectId)) {
                try {
                    out = new String(Files.readAllBytes(element));
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        });
        return out;
    }
}
