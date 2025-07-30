package org.example.upload;


import org.example.model.SectionRow;
import java.util.List;

public interface UploadFile {
    List<SectionRow> parse(String path) throws Exception;
}
