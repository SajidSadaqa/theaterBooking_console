package org.example.dao;

import org.example.config.DatabaseConfig;
import org.example.config.TheaterConfig;
import org.example.model.*;

import java.sql.*;
import java.util.*;

public class PostgreSQLTheaterDAO implements TheaterDAO {

    // Theater Management
    @Override
    public List<Theater> getAllTheaters() {
        List<Theater> theaters = new ArrayList<>();
        String sql = "SELECT * FROM theaters ORDER BY name";

        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                theaters.add(mapTheater(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching theaters", e);
        }

        return theaters;
    }

    @Override
    public Optional<Theater> getTheaterById(int theaterId) {
        String sql = "SELECT * FROM theaters WHERE id = ?";

        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, theaterId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapTheater(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching theater", e);
        }

        return Optional.empty();
    }

    @Override
    public int createTheater(String name, String location) {
        String sql = "INSERT INTO theaters (name, location) VALUES (?, ?) RETURNING id";

        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            stmt.setString(2, location);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                conn.commit();
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error creating theater", e);
        }

        return -1;
    }

    @Override
    public boolean updateTheater(int theaterId, String name, String location) {
        String sql = "UPDATE theaters SET name = ?, location = ? WHERE id = ?";

        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            stmt.setString(2, location);
            stmt.setInt(3, theaterId);

            int updated = stmt.executeUpdate();
            conn.commit();
            return updated > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating theater", e);
        }
    }

    @Override
    public boolean deleteTheater(int theaterId) {
        String sql = "DELETE FROM theaters WHERE id = ? AND NOT EXISTS (SELECT 1 FROM sections WHERE theater_id = ?)";

        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, theaterId);
            stmt.setInt(2, theaterId);

            int deleted = stmt.executeUpdate();
            conn.commit();
            return deleted > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting theater", e);
        }
    }

    // Seat Type Management
    @Override
    public List<SeatType> getAllSeatTypes(int theaterId) {
        List<SeatType> seatTypes = new ArrayList<>();
        String sql = "SELECT * FROM seat_types WHERE theater_id = ? ORDER BY name";

        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, theaterId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                seatTypes.add(mapSeatType(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching seat types", e);
        }

        return seatTypes;
    }

    @Override
    public Optional<SeatType> getSeatTypeById(int id, int theaterId) {
        String sql = "SELECT * FROM seat_types WHERE id = ? AND theater_id = ?";

        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.setInt(2, theaterId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapSeatType(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching seat type", e);
        }

        return Optional.empty();
    }

    @Override
    public Optional<SeatType> getSeatTypeByName(String name, int theaterId) {
        String sql = "SELECT * FROM seat_types WHERE name = ? AND theater_id = ?";

        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            stmt.setInt(2, theaterId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapSeatType(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching seat type", e);
        }

        return Optional.empty();
    }

    @Override
    public int createSeatType(int theaterId, String name, String description, double price) {
        String sql = "INSERT INTO seat_types (theater_id, name, description, price) VALUES (?, ?, ?, ?) RETURNING id";

        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, theaterId);
            stmt.setString(2, name);
            stmt.setString(3, description);
            stmt.setDouble(4, price);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                conn.commit();
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error creating seat type", e);
        }

        return -1;
    }

    @Override
    public boolean updateSeatType(int id, int theaterId, String name, String description, double price) {
        String sql = "UPDATE seat_types SET name = ?, description = ?, price = ? WHERE id = ? AND theater_id = ?";

        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            stmt.setString(2, description);
            stmt.setDouble(3, price);
            stmt.setInt(4, id);
            stmt.setInt(5, theaterId);

            int updated = stmt.executeUpdate();
            conn.commit();
            return updated > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating seat type", e);
        }
    }

    @Override
    public boolean deleteSeatType(int id, int theaterId) {
        String sql = "DELETE FROM seat_types WHERE id = ? AND theater_id = ? AND NOT EXISTS (SELECT 1 FROM sections WHERE seat_type_id = ?)";

        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.setInt(2, theaterId);
            stmt.setInt(3, id);

            int deleted = stmt.executeUpdate();
            conn.commit();
            return deleted > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting seat type", e);
        }
    }

    // Section Management
    @Override
    public List<Section> getAllSections(int theaterId) {
        List<Section> sections = new ArrayList<>();
        String sql = """
            SELECT s.*, st.name as seat_type_name 
            FROM sections s 
            JOIN seat_types st ON s.seat_type_id = st.id 
            WHERE s.theater_id = ?
            ORDER BY s.name
            """;

        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, theaterId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                sections.add(mapSection(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching sections", e);
        }

        return sections;
    }

    @Override
    public List<Section> getActiveSections(int theaterId) {
        List<Section> sections = new ArrayList<>();
        String sql = """
            SELECT s.*, st.name as seat_type_name 
            FROM sections s 
            JOIN seat_types st ON s.seat_type_id = st.id 
            WHERE s.theater_id = ? AND s.is_active = true
            ORDER BY s.name
            """;

        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, theaterId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                sections.add(mapSection(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching active sections", e);
        }

        return sections;
    }

    @Override
    public Optional<Section> getSectionByName(String name, int theaterId) {
        String sql = """
            SELECT s.*, st.name as seat_type_name 
            FROM sections s 
            JOIN seat_types st ON s.seat_type_id = st.id 
            WHERE s.name = ? AND s.theater_id = ?
            """;

        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            stmt.setInt(2, theaterId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapSection(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching section", e);
        }

        return Optional.empty();
    }

    @Override
    public int createSection(int theaterId, String name, int seatTypeId, int rows, int seatsPerRow, String description) {
        String sql = "INSERT INTO sections (theater_id, name, seat_type_id, rows, seats_per_row, description) VALUES (?, ?, ?, ?, ?, ?) RETURNING id";

        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, theaterId);
            stmt.setString(2, name);
            stmt.setInt(3, seatTypeId);
            stmt.setInt(4, rows);
            stmt.setInt(5, seatsPerRow);
            stmt.setString(6, description);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int sectionId = rs.getInt("id");
                conn.commit();

                // Generate seats for the new section
                generateSeatsForSection(name, theaterId);

                return sectionId;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error creating section", e);
        }

        return -1;
    }

    @Override
    public boolean updateSection(String name, int theaterId, int rows, int seatsPerRow, int seatTypeId) {
        String sql = "SELECT update_section_config(?, ?, ?, ?, (SELECT name FROM seat_types WHERE id = ? AND theater_id = ?))";

        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            stmt.setInt(2, theaterId);
            stmt.setInt(3, rows);
            stmt.setInt(4, seatsPerRow);
            stmt.setInt(5, seatTypeId);
            stmt.setInt(6, theaterId);

            ResultSet rs = stmt.executeQuery();
            conn.commit();
            return rs.next();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating section", e);
        }
    }

    @Override
    public boolean deactivateSection(String name, int theaterId) {
        String sql = "UPDATE sections SET is_active = false WHERE name = ? AND theater_id = ?";

        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            stmt.setInt(2, theaterId);
            int updated = stmt.executeUpdate();
            conn.commit();
            return updated > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error deactivating section", e);
        }
    }

    @Override
    public boolean activateSection(String name, int theaterId) {
        String sql = "UPDATE sections SET is_active = true WHERE name = ? AND theater_id = ?";

        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            stmt.setInt(2, theaterId);
            int updated = stmt.executeUpdate();
            conn.commit();
            return updated > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error activating section", e);
        }
    }

    @Override
    public int generateSeatsForSection(String sectionName, int theaterId) {
        String sql = "SELECT generate_seats_for_section(?, ?)";

        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, sectionName);
            stmt.setInt(2, theaterId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int seatCount = rs.getInt(1);
                conn.commit();
                return seatCount;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error generating seats", e);
        }

        return 0;
    }

    // Seat Management
    @Override
    public List<Seat> getSeatsBySection(String sectionName, int theaterId) {
        List<Seat> seats = new ArrayList<>();
        String sql = """
            SELECT s.* FROM seats s 
            JOIN sections sec ON s.section_id = sec.id 
            WHERE sec.name = ? AND sec.theater_id = ? AND s.is_active = true
            ORDER BY s.row_number, s.seat_number
            """;

        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, sectionName);
            stmt.setInt(2, theaterId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                seats.add(mapSeat(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching seats", e);
        }

        return seats;
    }

    @Override
    public List<Seat> getAvailableSeatsBySection(String sectionName, int theaterId) {
        List<Seat> seats = new ArrayList<>();
        String sql = """
            SELECT s.* FROM seats s 
            JOIN sections sec ON s.section_id = sec.id 
            WHERE sec.name = ? AND sec.theater_id = ? AND s.status = 'AVAILABLE' AND s.is_active = true
            ORDER BY s.row_number, s.seat_number
            """;

        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, sectionName);
            stmt.setInt(2, theaterId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                seats.add(mapSeat(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching available seats", e);
        }

        return seats;
    }

    @Override
    public List<Seat> getAvailableSeatsByRow(String sectionName, int row, int theaterId) {
        List<Seat> seats = new ArrayList<>();
        String sql = """
            SELECT s.* FROM seats s 
            JOIN sections sec ON s.section_id = sec.id 
            WHERE sec.name = ? AND sec.theater_id = ? AND s.row_number = ? AND s.status = 'AVAILABLE' AND s.is_active = true
            ORDER BY s.seat_number
            """;

        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, sectionName);
            stmt.setInt(2, theaterId);
            stmt.setInt(3, row);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                seats.add(mapSeat(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching available seats by row", e);
        }

        return seats;
    }

    @Override
    public Optional<Seat> getSeatByCode(String seatCode, int theaterId) {
        String sql = """
            SELECT s.* FROM seats s 
            JOIN sections sec ON s.section_id = sec.id 
            WHERE s.seat_code = ? AND sec.theater_id = ?
            """;

        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, seatCode);
            stmt.setInt(2, theaterId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapSeat(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching seat", e);
        }

        return Optional.empty();
    }

    @Override
    public boolean bookSeat(String seatCode, int theaterId, String customerName, String customerEmail, String customerPhone) {
        String updateSeatSQL = """
            UPDATE seats SET status = 'RESERVED' 
            FROM sections sec 
            WHERE seats.section_id = sec.id 
              AND seats.seat_code = ? 
              AND sec.theater_id = ? 
              AND seats.status = 'AVAILABLE' 
              AND seats.is_active = true
            """;
        String insertBookingSQL = """
            INSERT INTO bookings (seat_id, customer_name, customer_email, customer_phone, total_price) 
            SELECT s.id, ?, ?, ?, st.price 
            FROM seats s 
            JOIN sections sec ON s.section_id = sec.id
            JOIN seat_types st ON s.seat_type_id = st.id 
            WHERE s.seat_code = ? AND sec.theater_id = ?
            """;

        try (Connection conn = DatabaseConfig.getDataSource().getConnection()) {
            // Update seat status
            try (PreparedStatement updateStmt = conn.prepareStatement(updateSeatSQL)) {
                updateStmt.setString(1, seatCode);
                updateStmt.setInt(2, theaterId);
                int updated = updateStmt.executeUpdate();

                if (updated == 0) {
                    conn.rollback();
                    return false; // Seat not available
                }
            }

            // Insert booking record
            try (PreparedStatement insertStmt = conn.prepareStatement(insertBookingSQL)) {
                insertStmt.setString(1, customerName);
                insertStmt.setString(2, customerEmail);
                insertStmt.setString(3, customerPhone);
                insertStmt.setString(4, seatCode);
                insertStmt.setInt(5, theaterId);
                insertStmt.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            throw new RuntimeException("Error booking seat", e);
        }
    }

    @Override
    public List<Seat> getAllAvailableSeats(int theaterId) {
        List<Seat> seats = new ArrayList<>();
        String sql = """
            SELECT s.* FROM seats s 
            JOIN sections sec ON s.section_id = sec.id 
            WHERE sec.theater_id = ? AND s.status = 'AVAILABLE' AND s.is_active = true AND sec.is_active = true
            ORDER BY sec.name, s.row_number, s.seat_number
            """;

        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, theaterId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                seats.add(mapSeat(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching all available seats", e);
        }

        return seats;
    }

    @Override
    public List<Seat> getAllBookedSeats(int theaterId) {
        List<Seat> seats = new ArrayList<>();
        String sql = """
            SELECT s.* FROM seats s 
            JOIN sections sec ON s.section_id = sec.id 
            WHERE sec.theater_id = ? AND s.status = 'RESERVED' AND s.is_active = true AND sec.is_active = true
            ORDER BY sec.name, s.row_number, s.seat_number
            """;

        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, theaterId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                seats.add(mapSeat(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching all booked seats", e);
        }

        return seats;
    }

    // Booking Management
    @Override
    public List<Booking> getAllBookings(int theaterId) {
        List<Booking> bookings = new ArrayList<>();
        String sql = """
            SELECT b.*, s.seat_code
            FROM bookings b
            JOIN seats s ON b.seat_id = s.id
            JOIN sections sec ON s.section_id = sec.id
            WHERE sec.theater_id = ?
            ORDER BY b.booking_time DESC
            """;

        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, theaterId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                bookings.add(mapBooking(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching bookings", e);
        }

        return bookings;
    }

    @Override
    public Optional<Booking> getBookingById(int id, int theaterId) {
        String sql = """
            SELECT b.*, s.seat_code
            FROM bookings b
            JOIN seats s ON b.seat_id = s.id
            JOIN sections sec ON s.section_id = sec.id
            WHERE b.id = ? AND sec.theater_id = ?
            """;

        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.setInt(2, theaterId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapBooking(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching booking", e);
        }

        return Optional.empty();
    }

    @Override
    public boolean cancelBooking(int bookingId, int theaterId) {
        String getSeatIdSQL = """
            SELECT b.seat_id 
            FROM bookings b
            JOIN seats s ON b.seat_id = s.id
            JOIN sections sec ON s.section_id = sec.id
            WHERE b.id = ? AND sec.theater_id = ?
            """;
        String deleteBookingSQL = "DELETE FROM bookings WHERE id = ?";
        String updateSeatSQL = "UPDATE seats SET status = 'AVAILABLE' WHERE id = ?";

        try (Connection conn = DatabaseConfig.getDataSource().getConnection()) {
            // First get the seat_id
            int seatId;
            try (PreparedStatement getSeatStmt = conn.prepareStatement(getSeatIdSQL)) {
                getSeatStmt.setInt(1, bookingId);
                getSeatStmt.setInt(2, theaterId);
                ResultSet rs = getSeatStmt.executeQuery();
                if (rs.next()) {
                    seatId = rs.getInt("seat_id");
                } else {
                    return false; // Booking not found
                }
            }

            // Delete the booking
            try (PreparedStatement deleteStmt = conn.prepareStatement(deleteBookingSQL)) {
                deleteStmt.setInt(1, bookingId);
                int deleted = deleteStmt.executeUpdate();
                if (deleted == 0) {
                    conn.rollback();
                    return false;
                }
            }

            // Update seat status
            try (PreparedStatement updateSeatStmt = conn.prepareStatement(updateSeatSQL)) {
                updateSeatStmt.setInt(1, seatId);
                updateSeatStmt.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            throw new RuntimeException("Error cancelling booking", e);
        }
    }

    // Configuration Management
    @Override
    public List<TheaterConfig> getAllConfigs(int theaterId) {
        List<TheaterConfig> configs = new ArrayList<>();
        String sql = "SELECT * FROM theater_config WHERE theater_id = ? ORDER BY config_key";

        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, theaterId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                configs.add(mapTheaterConfig(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching configurations", e);
        }

        return configs;
    }

    @Override
    public Optional<TheaterConfig> getConfigByKey(String key, int theaterId) {
        String sql = "SELECT * FROM theater_config WHERE config_key = ? AND theater_id = ?";

        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, key);
            stmt.setInt(2, theaterId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapTheaterConfig(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching configuration", e);
        }

        return Optional.empty();
    }

    @Override
    public boolean updateConfig(String key, String value, int theaterId) {
        String sql = "UPDATE theater_config SET config_value = ?, updated_at = CURRENT_TIMESTAMP WHERE config_key = ? AND theater_id = ?";

        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, value);
            stmt.setString(2, key);
            stmt.setInt(3, theaterId);

            int updated = stmt.executeUpdate();
            conn.commit();
            return updated > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating configuration", e);
        }
    }

    // Statistics - Theater-specific
    @Override
    public int getTotalSeats(int theaterId) {
        String sql = """
            SELECT COUNT(*) FROM seats s 
            JOIN sections sec ON s.section_id = sec.id 
            WHERE sec.theater_id = ? AND s.is_active = true
            """;

        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, theaterId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error getting total seats count", e);
        }

        return 0;
    }

    @Override
    public int getAvailableSeatsCount(int theaterId) {
        String sql = """
            SELECT COUNT(*) FROM seats s 
            JOIN sections sec ON s.section_id = sec.id 
            WHERE sec.theater_id = ? AND s.status = 'AVAILABLE' AND s.is_active = true
            """;

        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, theaterId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error getting available seats count", e);
        }

        return 0;
    }

    @Override
    public int getBookedSeatsCount(int theaterId) {
        String sql = """
            SELECT COUNT(*) FROM seats s 
            JOIN sections sec ON s.section_id = sec.id 
            WHERE sec.theater_id = ? AND s.status = 'RESERVED' AND s.is_active = true
            """;

        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, theaterId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error getting booked seats count", e);
        }

        return 0;
    }

    @Override
    public double getTotalRevenue(int theaterId) {
        String sql = """
            SELECT COALESCE(SUM(b.total_price), 0) FROM bookings b
            JOIN seats s ON b.seat_id = s.id
            JOIN sections sec ON s.section_id = sec.id
            WHERE sec.theater_id = ? AND b.status = 'CONFIRMED'
            """;

        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, theaterId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error getting total revenue", e);
        }

        return 0.0;
    }

    // Statistics - Cross-theater
    @Override
    public int getTotalSeatsAllTheaters() {
        String sql = "SELECT COUNT(*) FROM seats WHERE is_active = true";

        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error getting total seats count for all theaters", e);
        }

        return 0;
    }

    @Override
    public int getAvailableSeatsCountAllTheaters() {
        String sql = "SELECT COUNT(*) FROM seats WHERE status = 'AVAILABLE' AND is_active = true";

        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error getting available seats count for all theaters", e);
        }

        return 0;
    }

    @Override
    public int getBookedSeatsCountAllTheaters() {
        String sql = "SELECT COUNT(*) FROM seats WHERE status = 'RESERVED' AND is_active = true";

        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error getting booked seats count for all theaters", e);
        }

        return 0;
    }

    @Override
    public double getTotalRevenueAllTheaters() {
        String sql = "SELECT COALESCE(SUM(total_price), 0) FROM bookings WHERE status = 'CONFIRMED'";

        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error getting total revenue for all theaters", e);
        }

        return 0.0;
    }

    @Override
    public List<Booking> getAllBookingsAllTheaters() {
        List<Booking> bookings = new ArrayList<>();
        String sql = """
            SELECT b.*, s.seat_code
            FROM bookings b
            JOIN seats s ON b.seat_id = s.id
            ORDER BY b.booking_time DESC
            """;

        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                bookings.add(mapBooking(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching all bookings for all theaters", e);
        }

        return bookings;
    }

    // Helper methods for mapping ResultSet to objects
    private Theater mapTheater(ResultSet rs) throws SQLException {
        return new Theater(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("location")
        );
    }

    private SeatType mapSeatType(ResultSet rs) throws SQLException {
        return new SeatType(
                rs.getInt("id"),
                rs.getInt("theater_id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getDouble("price")
        );
    }

    private Section mapSection(ResultSet rs) throws SQLException {
        return new Section(
                rs.getInt("id"),
                rs.getInt("theater_id"),
                rs.getString("name"),
                rs.getInt("seat_type_id"),
                rs.getString("seat_type_name"),
                rs.getInt("rows"),
                rs.getInt("seats_per_row"),
                rs.getString("description"),
                rs.getBoolean("is_active")
        );
    }

    private Seat mapSeat(ResultSet rs) throws SQLException {
        return new Seat(
                rs.getInt("id"),
                rs.getString("seat_code"),
                rs.getInt("section_id"),
                rs.getInt("row_number"),
                rs.getInt("seat_number"),
                rs.getInt("seat_type_id"),
                rs.getString("status"),
                rs.getBoolean("is_active")
        );
    }

    private Booking mapBooking(ResultSet rs) throws SQLException {
        return new Booking(
                rs.getInt("id"),
                rs.getInt("seat_id"),
                rs.getString("seat_code"),
                rs.getString("customer_name"),
                rs.getString("customer_email"),
                rs.getString("customer_phone"),
                rs.getTimestamp("booking_time") != null ? rs.getTimestamp("booking_time").toLocalDateTime() : null,
                rs.getDouble("total_price"),
                rs.getString("notes"),
                rs.getString("status")
        );
    }

    private TheaterConfig mapTheaterConfig(ResultSet rs) throws SQLException {
        return new TheaterConfig(
                rs.getInt("id"),
                rs.getInt("theater_id"),
                rs.getString("config_key"),
                rs.getString("config_value"),
                rs.getString("description")
        );
    }

    // SQL Constants for booking operations
    public static final String SQL_CLAIM_SEAT = """
        UPDATE seats
        SET status = 'RESERVED'
        FROM sections sec
        WHERE seats.section_id = sec.id
          AND seats.seat_code = ?          -- 1
          AND sec.theater_id = ?           -- 2
          AND seats.status = 'AVAILABLE'
          AND seats.is_active = true
        RETURNING seats.id, seats.seat_type_id
        """;

    public static final String SQL_INSERT_BOOKING = """
        INSERT INTO bookings
          (seat_id, customer_name, customer_email, customer_phone, total_price, status)
        VALUES
          (?, ?, ?, ?, ?, 'CONFIRMED')
        """;

    public static final String SQL_SEAT_TYPE_PRICE = """
        SELECT price FROM seat_types WHERE id = ? AND theater_id = ?
        """;
}