package org.example.upload;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.model.BookingRow;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class BookingJsonFile implements UploadFileBookings {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public List<BookingRow> parse(String path) throws Exception {
        List<BookingRow> list = new ArrayList<>();

        JsonNode root = MAPPER.readTree(Path.of(path).toFile());
        if (!root.isArray()) {
            throw new IllegalArgumentException("JSON root must be an array");
        }

        for (JsonNode node : root) {
            // mandatory fields
            String seatCode      = node.path("seatCode")     .asText().trim();
            String customerName  = node.path("customerName") .asText().trim();
            String customerEmail = node.path("customerEmail").asText().trim();
            String customerPhone = node.path("customerPhone").asText().trim();

            if (seatCode.isEmpty() || customerName.isEmpty()) {
                System.err.println("Skipping incomplete booking entry: " + node);
                continue;
            }

            // optional price override
            double price = node.path("price").isNumber()
                    ? node.get("price").asDouble()
                    : 0.0;

            list.add(new BookingRow(
                    seatCode,
                    customerName,
                    customerEmail,
                    customerPhone,
                    price
            ));
        }
        return list;
    }
}
