package ca.acoii.nationbuilder.nationbuilder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class LawLoader {
    private final ObjectMapper mapper = new ObjectMapper();
    public Law load(String filename) {
        try {
            InputStream inStrm = getClass().getResourceAsStream("/laws/" + filename);
            return mapper.readValue(inStrm, Law.class);
        } catch (IOException e) {
            throw new RuntimeException("Law file not found: " + filename + e.getMessage());
        }
    }
    public List<Law> loadAll() {
        try {
            InputStream inStrm = getClass().getResourceAsStream("/laws/index.json");
            List<String> filenames = mapper.readValue(inStrm, new TypeReference<>() {});
            List<Law> laws = new ArrayList<>();
            for (String name : filenames) {
                laws.add(load(name));
            }
            return laws;
        } catch (IOException e) {
            throw new RuntimeException("No index.json file found");
        }
    }
}
