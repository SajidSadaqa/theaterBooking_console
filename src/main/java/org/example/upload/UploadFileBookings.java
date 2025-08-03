package org.example.upload;

import org.example.model.BookingRow;

import java.util.List;

public interface UploadFileBookings {
    List<BookingRow> parse(String path) throws Exception;
}
