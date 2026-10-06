package ca.acoii.nationbuilder.nationbuilder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class AppointeeLoader {
    private final ObjectMapper mapper = new ObjectMapper();
    public RoleFile load(String filename) {
        try {
            InputStream inStrm = getClass().getResourceAsStream("/appointees/" + filename);
            return mapper.readValue(inStrm, RoleFile.class);
        } catch (IOException e) {
            throw new RuntimeException("Role file not found: " + filename + "\n" + e.getMessage());
        }
    }
    public List<RoleFile> loadAll() {
        try {
            InputStream inStrm = getClass().getResourceAsStream("/appointees/index.json");
            List<String> filenames = mapper.readValue(inStrm, new TypeReference<>() {});
            List<RoleFile> roles = new ArrayList<>();
            for (String name : filenames) {
                roles.add(load(name));
            }
            return roles;
        } catch (IOException e) {
            throw new RuntimeException("No index.json file found");
        }
    }
}
