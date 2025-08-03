package org.example.upload;

import org.example.model.TheaterLayoutRow;

import java.nio.file.*;
import java.util.*;

public class TheaterLayoutCsvFile implements LayoutUploadFile {
    @Override
    public List<TheaterLayoutRow> parse(String path) throws Exception {
        List<TheaterLayoutRow> list = new ArrayList<>();
        List<String> lines = Files.readAllLines(Path.of(path));

        System.out.println("=== PARSING CSV FILE ===");
        System.out.println("File: " + path);
        System.out.println("Total lines: " + lines.size());

        int lineNumber = 0;
        int parsedCount = 0;
        int skippedCount = 0;
        int errorCount = 0;

        for (String line : lines) {
            lineNumber++;

            // Skip blank lines
            if (line.isBlank()) {
                System.out.println("Line " + lineNumber + ": Skipping blank line");
                skippedCount++;
                continue;
            }

            // Skip comment lines
            if (line.startsWith("#")) {
                System.out.println("Line " + lineNumber + ": Skipping comment line");
                skippedCount++;
                continue;
            }

            // Skip header lines (multiple possible formats)
            if (line.toLowerCase().startsWith("theatername") ||
                    line.toLowerCase().startsWith("theater_name") ||
                    line.toLowerCase().startsWith("theater name") ||
                    line.toLowerCase().contains("theater") && line.toLowerCase().contains("section")) {
                System.out.println("Line " + lineNumber + ": Skipping header line: " + line);
                skippedCount++;
                continue;
            }

            // Parse data line
            try {
                String[] p = line.split(",");

                if (p.length < 6) {
                    System.err.println("Line " + lineNumber + ": Insufficient columns (" + p.length + "/6): " + line);
                    errorCount++;
                    continue;
                }

                // Trim all parts
                for (int i = 0; i < p.length; i++) {
                    p[i] = p[i].trim();
                }

                // Validate required fields are not empty
                if (p[0].isEmpty() || p[2].isEmpty() || p[5].isEmpty()) {
                    System.err.println("Line " + lineNumber + ": Missing required fields (theater/section/seatType): " + line);
                    errorCount++;
                    continue;
                }

                // Parse and validate numbers
                int rowNumber;
                int seatsPerRow;

                try {
                    rowNumber = Integer.parseInt(p[3]);
                    if (rowNumber <= 0) {
                        System.err.println("Line " + lineNumber + ": Invalid row number (must be > 0): " + p[3]);
                        errorCount++;
                        continue;
                    }
                } catch (NumberFormatException e) {
                    System.err.println("Line " + lineNumber + ": Invalid row number format: '" + p[3] + "'");
                    System.err.println("  Full line: " + line);
                    errorCount++;
                    continue;
                }

                try {
                    seatsPerRow = Integer.parseInt(p[4]);
                    if (seatsPerRow <= 0) {
                        System.err.println("Line " + lineNumber + ": Invalid seats per row (must be > 0): " + p[4]);
                        errorCount++;
                        continue;
                    }
                } catch (NumberFormatException e) {
                    System.err.println("Line " + lineNumber + ": Invalid seats per row format: '" + p[4] + "'");
                    System.err.println("  Full line: " + line);
                    errorCount++;
                    continue;
                }

                // Create the row object
                TheaterLayoutRow row = new TheaterLayoutRow(
                        p[0],           // theaterName
                        p[1],           // theaterDescription
                        p[2],           // sectionName
                        rowNumber,      // rowNumber
                        seatsPerRow,    // seatsPerRow
                        p[5]            // seatTypeName
                );

                list.add(row);
                parsedCount++;

                System.out.println("Line " + lineNumber + ": ✓ Parsed - Theater: '" + p[0] +
                        "', Section: '" + p[2] + "', " + rowNumber + "x" + seatsPerRow +
                        " (" + p[5] + ")");

            } catch (Exception e) {
                System.err.println("Line " + lineNumber + ": Unexpected error parsing line: " + line);
                System.err.println("  Error: " + e.getMessage());
                errorCount++;
            }
        }

        System.out.println("\n=== PARSING SUMMARY ===");
        System.out.println("Total lines: " + lineNumber);
        System.out.println("Successfully parsed: " + parsedCount);
        System.out.println("Skipped: " + skippedCount);
        System.out.println("Errors: " + errorCount);

        if (list.isEmpty()) {
            System.err.println("⚠️ No valid data rows found!");
            System.err.println("Expected CSV format:");
            System.err.println("theaterName,theaterDescription,sectionName,rowNumber,seatsPerRow,seatTypeName");
            System.err.println("Main Theater,Downtown location,Orchestra,20,25,Standard");
        }

        return list;
    }

    // Alternative method for debugging - shows first few lines without parsing
    public void debugFile(String path) {
        try {
            List<String> lines = Files.readAllLines(Path.of(path));
            System.out.println("=== CSV FILE DEBUG ===");
            System.out.println("File: " + path);
            System.out.println("Total lines: " + lines.size());
            System.out.println();

            for (int i = 0; i < Math.min(lines.size(), 5); i++) {
                String line = lines.get(i);
                System.out.println("Line " + (i + 1) + ": '" + line + "'");

                if (!line.isBlank()) {
                    String[] parts = line.split(",");
                    System.out.println("  Columns: " + parts.length);
                    for (int j = 0; j < parts.length; j++) {
                        System.out.println("    [" + j + "] = '" + parts[j].trim() + "'");
                    }
                }
                System.out.println();
            }
        } catch (Exception e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }
}