package ca.acoii.nationbuilder.nationbuilder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class NationLoader {
    private final ObjectMapper mapper = new ObjectMapper();
    public Nation load(String filename) {
        try {
            InputStream inStrm = getClass().getResourceAsStream("/nations/" + filename);
            return mapper.readValue(inStrm, Nation.class);
        } catch (IOException e) {
            throw new RuntimeException("Nation file not found: " + filename + "\n" + e.getMessage());
        }
    }
    public List<Nation> loadAll() {
        try {
            InputStream inStrm = getClass().getResourceAsStream("/nations/index.json");
            List<String> filenames = mapper.readValue(inStrm, new TypeReference<>() {});
            List<Nation> nations = new ArrayList<>();
            for (String name : filenames) {
                nations.add(load(name));
            }
            return nations;
        } catch (IOException e) {
            throw new RuntimeException("No index.json file found");
        }
    }
}
