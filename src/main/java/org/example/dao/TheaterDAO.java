package org.example.dao;

import org.example.config.TheaterConfig;
import org.example.model.*;
import java.util.List;
import java.util.Optional;

public interface TheaterDAO {

    // Theater management
    List<Theater> getAllTheaters();
    Optional<Theater> getTheaterById(int theaterId);
    Optional<Theater> getTheaterByName(String name);
    int createTheater(String name, String location);
    boolean updateTheater(int theaterId, String name, String location);
    boolean deleteTheater(int theaterId);

    // Seat Type management - now all methods include theaterId
    List<SeatType> getAllSeatTypes(int theaterId);
    Optional<SeatType> getSeatTypeById(int id, int theaterId);
    Optional<SeatType> getSeatTypeByName(String name, int theaterId);
    int createSeatType(String name,
                       String description,
                       double price,
                       int theaterId);
    boolean updateSeatType(int id, int theaterId, String name, String description, double price);
    boolean deleteSeatType(int id, int theaterId);

    // Section management - all methods updated with theaterId
    List<Section> getAllSections(int theaterId);
    List<Section> getActiveSections(int theaterId);
    Optional<Section> getSectionByName(String name, int theaterId);
    int createSection(int theaterId, String name, int seatTypeId, int rows, int seatsPerRow, String description);
    boolean updateSection(String name, int theaterId, int rows, int seatsPerRow, int seatTypeId);
    boolean deactivateSection(String name, int theaterId);
    boolean activateSection(String name, int theaterId);
    int generateSeatsForSection(String sectionName, int theaterId);

    // Seat management - all methods updated with theaterId
    List<Seat> getSeatsBySection(String sectionName, int theaterId);
    List<Seat> getAvailableSeatsBySection(String sectionName, int theaterId);
    List<Seat> getAvailableSeatsByRow(String sectionName, int row, int theaterId);
    Optional<Seat> getSeatByCode(String seatCode, int theaterId);
    boolean bookSeat(String seatCode, int theaterId, String customerName, String customerEmail, String customerPhone);
    List<Seat> getAllAvailableSeats(int theaterId);
    List<Seat> getAllBookedSeats(int theaterId);

    // Booking management - updated with theaterId
    List<Booking> getAllBookings(int theaterId);
    Optional<Booking> getBookingById(int id, int theaterId);
    boolean cancelBooking(int bookingId, int theaterId);

    // Configuration management - updated with theaterId
    List<TheaterConfig> getAllConfigs(int theaterId);
    Optional<TheaterConfig> getConfigByKey(String key, int theaterId);
    boolean updateConfig(String key, String value, int theaterId);

    // Statistics - all methods now theater-specific
    int getTotalSeats(int theaterId);
    int getAvailableSeatsCount(int theaterId);
    int getBookedSeatsCount(int theaterId);
    double getTotalRevenue(int theaterId);

    // Additional cross-theater statistics methods
    int getTotalSeatsAllTheaters();
    int getAvailableSeatsCountAllTheaters();
    int getBookedSeatsCountAllTheaters();
    double getTotalRevenueAllTheaters();
    List<Booking> getAllBookingsAllTheaters();
    Optional<Section> getSectionByNameAndTheater(String name, int theaterId);
    int createSection(String name, int theaterId, int seatTypeId,
                      int rows, int seatsPerRow, String description);
}

