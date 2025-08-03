package org.example.upload;

import org.example.model.BookingRow;

import java.nio.file.*;
import java.util.*;

public class BookingCsvFile implements UploadFileBookings {
    @Override
    public List<BookingRow> parse(String path) throws Exception {
        List<BookingRow> list = new ArrayList<>();

        for (String line : Files.readAllLines(Path.of(path))) {
            if (line.isBlank() || line.startsWith("#")) continue;
            String[] p = line.split(",");
            if (p.length < 4) continue;

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
