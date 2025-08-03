package org.example.upload;

import com.fasterxml.jackson.databind.*;
import org.example.model.SectionRow;

import java.nio.file.*;
import java.util.*;

public class JsonFile implements UploadFile {
    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public List<SectionRow> parse(String path) throws Exception {

        List<SectionRow> list = new ArrayList<>();

        JsonNode root = mapper.readTree(Path.of(path).toFile());
        if (!root.isArray()) throw new IllegalArgumentException("Root must be a JSON array");

        for (JsonNode n : root) {
            String section   = n.get("sectionName").asText();
            int    row       = n.get("rowNumber").asInt();
            int    seats     = n.get("totalSeats").asInt();
            String seatType  = n.get("seatType").asText();

            list.add(new SectionRow(section, row, seats, seatType.trim()));

        }
        return list;
    }
}
