package org.example.upload;

import org.example.model.TheaterLayoutRow;
import java.util.List;

public interface LayoutUploadFile {
    List<TheaterLayoutRow> parse(String path) throws Exception;
}
