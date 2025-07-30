package org.example.dao;

import org.example.config.TheaterConfig;
import org.example.model.*;
import java.util.List;
import java.util.Optional;

public interface TheaterDAO {

    List<SeatType> getAllSeatTypes();
    Optional<SeatType> getSeatTypeById(int id);
    Optional<SeatType> getSeatTypeByName(String name);
    int createSeatType(String name, String description, double price);
    boolean updateSeatType(int id, String name, String description, double price);
    boolean deleteSeatType(int id);


    List<Section> getAllSections();
    List<Section> getActiveSections();
    Optional<Section> getSectionByName(String name);
    int createSection(String name, int seatTypeId, int rows, int seatsPerRow, String description);
    boolean updateSection(String name, int rows, int seatsPerRow, int seatTypeId);
    boolean deactivateSection(String name);
    boolean activateSection(String name);
    int generateSeatsForSection(String sectionName);


    List<Seat> getSeatsBySection(String sectionName);
    List<Seat> getAvailableSeatsBySection(String sectionName);
    List<Seat> getAvailableSeatsByRow(String sectionName, int row);
    Optional<Seat> getSeatByCode(String seatCode);
    boolean bookSeat(String seatCode, String customerName, String customerEmail, String customerPhone);
    List<Seat> getAllAvailableSeats();
    List<Seat> getAllBookedSeats();


    List<Booking> getAllBookings();
    Optional<Booking> getBookingById(int id);
    boolean cancelBooking(int bookingId);

    List<TheaterConfig> getAllConfigs();
    Optional<TheaterConfig> getConfigByKey(String key);
    boolean updateConfig(String key, String value);

    // Statistics
    int getTotalSeats();
    int getAvailableSeatsCount();
    int getBookedSeatsCount();
    double getTotalRevenue();
}

