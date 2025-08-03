package org.example.upload;

import org.example.model.SectionRow;
import java.nio.file.*;
import java.util.*;

public class TxtFile implements UploadFile {
    @Override
    public List<SectionRow> parse(String path) throws Exception {
        List<SectionRow> list = new ArrayList<>();

        for (String line : Files.readAllLines(Path.of(path))) {
            if (line.isBlank() || line.startsWith("#")) continue;

            String[] p = line.trim().split("\\s+");  // split on any whitespace
            if (p.length < 4) {
                System.err.println("Skipping malformed line: " + line);
                continue;
            }

            list.add(new SectionRow(
                    p[0],
                    Integer.parseInt(p[1]),
                    Integer.parseInt(p[2]),
                    p[3].trim()
            ));
        }
        return list;
    }
}
