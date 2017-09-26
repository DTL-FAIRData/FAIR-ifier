package org.dtls.fairifier;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;

public class FileSystemRdfSkeletonImpl implements RdfSkeletonService {
    private static final Path SAVELOCATION = Paths.get(System.getProperty("user.home") + "/.local/share/openrefine/");
    private static final ObjectMapper mapper = new ObjectMapper();

    
    @Override
    public List<String> listModels(final String fileType)  throws IOException {
        return Files.list(SAVELOCATION)
                .filter(Files::isRegularFile)
                .map(this::getStringAndFiletype)
                .filter(val -> val[0] != null)
                .filter(val -> val[1].equals(fileType) )
                .map(element -> element[0])
                .collect(Collectors.toList());
    }

    @Override
    public List<String> listModels()  throws IOException {
        return Files.list(SAVELOCATION)
                .filter(Files::isRegularFile)
                .map(this::getStringAndFiletype)
                .filter(val -> val[0] != null)
                .map(element -> element[0])
                .collect(Collectors.toList());
    }
    
    @Override
    public void saveModel(String json, String fileType, String projectId)  throws IOException {
        Path fileLocation = Paths.get(SAVELOCATION.toString() + File.separator + projectId + ".skeleton.json");        
        Files.write(fileLocation, json.getBytes(StandardCharsets.UTF_8));
        fileLocation = Paths.get(SAVELOCATION.toString() + File.separator + projectId + ".metadata.skeleton.json");
        try (BufferedWriter writer = Files.newBufferedWriter(fileLocation)) {
            SkeletonMetadata metadata = new SkeletonMetadata();
            metadata.setFileType(fileType);
            mapper.writeValue(writer, metadata);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public String loadModel(String projectId)  throws IOException {
        return new String(Files.readAllBytes(Paths.get(SAVELOCATION.toString() + File.separator + projectId + ".skeleton.json" )));
    }
    
    private String[] getStringAndFiletype(Path element){
        String name = element.getFileName().toString().split("\\.")[0];
        String[] out = new String[2];
        try {
            if (new File(element.getParent() + File.separator + element.getFileName().toString()).isFile() && element.getFileName().toString().contains("metadata")) {
                SkeletonMetadata metadata = mapper.readValue(new File(element.getParent().toString() + File.separator + name + ".metadata.skeleton.json"), SkeletonMetadata.class);
                
                out[0] = name;
                out[1] = metadata.getFileType();
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return out;
    }
}
