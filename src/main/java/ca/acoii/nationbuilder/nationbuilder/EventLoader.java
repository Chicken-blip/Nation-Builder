package ca.acoii.nationbuilder.nationbuilder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class EventLoader {
    private final ObjectMapper mapper = new ObjectMapper();
    public GameEvent load(String filename) {
        try {
            InputStream inStrm = getClass().getResourceAsStream("/events/" + filename);
            return mapper.readValue(inStrm, GameEvent.class);
        } catch (IOException e) {
            throw new RuntimeException("Event file not found: " + filename);
        }
    }
    public List<GameEvent> loadAll() {
        try {
            InputStream inStrm = getClass().getResourceAsStream("/events/index.json");
            List<String> filenames = mapper.readValue(inStrm, new TypeReference<>() {});
            List<GameEvent> gameEvts = new ArrayList<>();
            for (String name : filenames) {
                gameEvts.add(load(name));
            }
            return gameEvts;
        } catch (IOException e) {
            throw new RuntimeException("No index.json file found");
        }
    }

}
