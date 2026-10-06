package ca.acoii.nationbuilder.nationbuilder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class InfrastructureLoader {
    private final ObjectMapper mapper = new ObjectMapper();
    public Infrastructure load(String filename) {
        try {
            InputStream inStrm = getClass().getResourceAsStream("/infrastructure/" + filename);
            return mapper.readValue(inStrm, Infrastructure.class);
        } catch (IOException e) {
            throw new RuntimeException("Infrastructure file not found: " + filename);
        }
    }
    public List<Infrastructure> loadAll() {
        try {
            InputStream inStrm = getClass().getResourceAsStream("/infrastructure/index.json");
            List<String> filenames = mapper.readValue(inStrm, new TypeReference<>() {});
            List<Infrastructure> infras = new ArrayList<>();
            for (String name : filenames) {
                infras.add(load(name));
            }
            return infras;
        } catch (IOException e) {
            throw new RuntimeException("No index.json file found");
        }
    }
}
