package org.example.upload;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.model.TheaterLayoutRow;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TheaterLayoutJsonFile implements LayoutUploadFile {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public List<TheaterLayoutRow> parse(String path) throws Exception {
        List<TheaterLayoutRow> list = new ArrayList<>();
        JsonNode root = MAPPER.readTree(Path.of(path).toFile());
        if (!root.isArray()) {
            throw new IllegalArgumentException("JSON root must be an array of layout entries");
        }
        for (JsonNode node : root) {
            String theaterName = node.path("theaterName").asText().trim();
            String theaterDesc = node.path("theaterDescription").asText().trim();
            String sectionName = node.path("sectionName").asText().trim();
            int rowNumber     = node.path("rowNumber").asInt();
            int seatsPerRow   = node.path("seatsPerRow").asInt();
            String seatType   = node.path("seatTypeName").asText().trim();

            // Skip entries missing required fields
            if (theaterName.isEmpty() || sectionName.isEmpty() || seatType.isEmpty()) {
                System.err.println("Skipping incomplete layout entry: " + node);
                continue;
            }

            list.add(new TheaterLayoutRow(
                    theaterName,
                    theaterDesc,
                    sectionName,
                    rowNumber,
                    Collections.singletonList(seatsPerRow),
                    seatType
            ));
        }
        return list;
    }
}

