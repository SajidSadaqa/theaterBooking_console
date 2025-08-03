package org.example.upload;

import org.example.model.BookingRow;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class BookingTxtFile implements UploadFileBookings {
    @Override
    public List<BookingRow> parse(String path) throws Exception {
        List<BookingRow> list = new ArrayList<>();

        for (String line : Files.readAllLines(Path.of(path))) {
            if (line.isBlank() || line.startsWith("#")) continue;

            String[] p = line.trim().split("\\s+");
            if (p.length < 4) {
                System.err.println("Skipping malformed line: " + line);
                continue;
            }

            double price = p.length >= 5 && !p[4].isBlank()
                    ? Double.parseDouble(p[4])
                    : 0.0;
            list.add(new BookingRow(
                    p[0].trim(), p[1].trim(), p[2].trim(), p[3].trim(), price
            ));
        }
        return list;
    }
}
