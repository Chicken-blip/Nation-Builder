package ca.acoii.nationbuilder.nationbuilder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ResearchLoader {
    private final ObjectMapper mapper = new ObjectMapper();
    public ResearchNode load(String filename) {
        try {
            InputStream inStrm = getClass().getResourceAsStream("/research/" + filename);
            return mapper.readValue(inStrm, ResearchNode.class);
        } catch (IOException e) {
            throw new RuntimeException("Research Node file not found: " + filename + e.getMessage());
        }
    }
    public List<ResearchNode> loadAll() {
        try {
            InputStream inStrm = getClass().getResourceAsStream("/research/index.json");
            List<String> filenames = mapper.readValue(inStrm, new TypeReference<>() {});
            List<ResearchNode> nodes = new ArrayList<>();
            for (String name : filenames) {
                nodes.add(load(name));
            }
            return nodes;
        } catch (IOException e) {
            throw new RuntimeException("No index.json file found");
        }
    }
}
