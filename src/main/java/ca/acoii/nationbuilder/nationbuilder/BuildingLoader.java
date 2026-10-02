package ca.acoii.nationbuilder.nationbuilder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class BuildingLoader {
    private final ObjectMapper mapper = new ObjectMapper();
    public Building load(String filename) {
        try {
            InputStream inStrm = getClass().getResourceAsStream("/buildings/" + filename);
            return mapper.readValue(inStrm, Building.class);
        } catch (IOException e) {
            throw new RuntimeException("Building file not found: " + filename);
        }
    }
    public List<Building> loadAll() {
        try {
            InputStream inStrm = getClass().getResourceAsStream("/buildings/index.json");
            List<String> filenames = mapper.readValue(inStrm, new TypeReference<>() {});
            List<Building> bldgs = new ArrayList<>();
            for (String name : filenames) {
                bldgs.add(load(name));
            }
            return bldgs;
        } catch (IOException e) {
            throw new RuntimeException("No index.json file found");
        }
    }
}
