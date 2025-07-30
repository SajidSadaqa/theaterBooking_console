package org.example.dao;

import org.example.config.DatabaseConfig;
import org.example.config.TheaterConfig;
import org.example.model.*;


import java.sql.*;
import java.util.*;

public class PostgreSQLTheaterDAO implements TheaterDAO {

    // Seat Type Management
    @Override
    public List<SeatType> getAllSeatTypes() {
        List<SeatType> seatTypes = new ArrayList<>();
        String sql = "SELECT * FROM seat_types ORDER BY name";

        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                seatTypes.add(mapSeatType(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching seat types", e);
        }

        return seatTypes;
    }

    @Override
    public Optional<SeatType> getSeatTypeById(int id) {
        String sql = "SELECT * FROM seat_types WHERE id = ?";

        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
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
    public Optional<SeatType> getSeatTypeByName(String name) {
        String sql = "SELECT * FROM seat_types WHERE name = ?";

        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
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
    public int createSeatType(String name, String description, double price) {
        String sql = "INSERT INTO seat_types (name, description, price) VALUES (?, ?, ?) RETURNING id";

        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            stmt.setString(2, description);
            stmt.setDouble(3, price);

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
    public boolean updateSeatType(int id, String name, String description, double price) {
        String sql = "UPDATE seat_types SET name = ?, description = ?, price = ? WHERE id = ?";

        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            stmt.setString(2, description);
            stmt.setDouble(3, price);
            stmt.setInt(4, id);

            int updated = stmt.executeUpdate();
            conn.commit();
            return updated > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating seat type", e);
        }
    }

    @Override
    public boolean deleteSeatType(int id) {
        String sql = "DELETE FROM seat_types WHERE id = ? AND NOT EXISTS (SELECT 1 FROM sections WHERE seat_type_id = ?)";

        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.setInt(2, id);

            int deleted = stmt.executeUpdate();
            conn.commit();
            return deleted > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting seat type", e);
        }
    }

    // Section Management
    @Override
    public List<Section> getAllSections() {
        List<Section> sections = new ArrayList<>();
        String sql = """
            SELECT s.*, st.name as seat_type_name 
            FROM sections s 
            JOIN seat_types st ON s.seat_type_id = st.id 
            ORDER BY s.name
            """;

        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                sections.add(mapSection(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching sections", e);
        }

        return sections;
    }

    @Override
    public List<Section> getActiveSections() {
        List<Section> sections = new ArrayList<>();
        String sql = """
            SELECT s.*, st.name as seat_type_name 
            FROM sections s 
            JOIN seat_types st ON s.seat_type_id = st.id 
            WHERE s.is_active = true
            ORDER BY s.name
            """;

        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                sections.add(mapSection(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching active sections", e);
        }

        return sections;
    }

    @Override
    public Optional<Section> getSectionByName(String name) {
        String sql = """
            SELECT s.*, st.name as seat_type_name 
            FROM sections s 
            JOIN seat_types st ON s.seat_type_id = st.id 
            WHERE s.name = ?
            """;

        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
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
    public int createSection(String name, int seatTypeId, int rows, int seatsPerRow, String description) {
        String sql = "INSERT INTO sections (name, seat_type_id, rows, seats_per_row, description) VALUES (?, ?, ?, ?, ?) RETURNING id";

        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            stmt.setInt(2, seatTypeId);
            stmt.setInt(3, rows);
            stmt.setInt(4, seatsPerRow);
            stmt.setString(5, description);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int sectionId = rs.getInt("id");
                conn.commit();

                // Generate seats for the new section
                generateSeatsForSection(name);

                return sectionId;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error creating section", e);
        }

        return -1;
    }

    @Override
    public boolean updateSection(String name, int rows, int seatsPerRow, int seatTypeId) {
        String sql = "SELECT update_section_config(?, ?, ?, (SELECT name FROM seat_types WHERE id = ?))";

        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            stmt.setInt(2, rows);
            stmt.setInt(3, seatsPerRow);
            stmt.setInt(4, seatTypeId);

            ResultSet rs = stmt.executeQuery();
            conn.commit();
            return rs.next();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating section", e);
        }
    }

    @Override
    public boolean deactivateSection(String name) {
        String sql = "UPDATE sections SET is_active = false WHERE name = ?";

        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            int updated = stmt.executeUpdate();
            conn.commit();
            return updated > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error deactivating section", e);
        }
    }

    @Override
    public boolean activateSection(String name) {
        String sql = "UPDATE sections SET is_active = true WHERE name = ?";

        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            int updated = stmt.executeUpdate();
            conn.commit();
            return updated > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error activating section", e);
        }
    }

    @Override
    public int generateSeatsForSection(String sectionName) {
        String sql = "SELECT generate_seats_for_section(?)";

        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, sectionName);
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
    public List<Seat> getSeatsBySection(String sectionName) {
        List<Seat> seats = new ArrayList<>();
        String sql = """
            SELECT s.* FROM seats s 
            JOIN sections sec ON s.section_id = sec.id 
            WHERE sec.name = ? AND s.is_active = true
            ORDER BY s.row_number, s.seat_number
            """;

        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, sectionName);
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
    public List<Seat> getAvailableSeatsBySection(String sectionName) {
        List<Seat> seats = new ArrayList<>();
        String sql = """
            SELECT s.* FROM seats s 
            JOIN sections sec ON s.section_id = sec.id 
            WHERE sec.name = ? AND s.status = 'AVAILABLE' AND s.is_active = true
            ORDER BY s.row_number, s.seat_number
            """;

        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, sectionName);
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
    public List<Seat> getAvailableSeatsByRow(String sectionName, int row) {
        List<Seat> seats = new ArrayList<>();
        String sql = """
            SELECT s.* FROM seats s 
            JOIN sections sec ON s.section_id = sec.id 
            WHERE sec.name = ? AND s.row_number = ? AND s.status = 'AVAILABLE' AND s.is_active = true
            ORDER BY s.seat_number
            """;

        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, sectionName);
            stmt.setInt(2, row);
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
    public Optional<Seat> getSeatByCode(String seatCode) {
        String sql = "SELECT * FROM seats WHERE seat_code = ?";

        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, seatCode);
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
    public boolean bookSeat(String seatCode, String customerName, String customerEmail, String customerPhone) {
        String updateSeatSQL = "UPDATE seats SET status = 'RESERVED' WHERE seat_code = ? AND status = 'AVAILABLE' AND is_active = true";
        String insertBookingSQL = """
            INSERT INTO bookings (seat_id, customer_name, customer_email, customer_phone, total_price) 
            VALUES (?, ?, ?, ?, (SELECT st.price FROM seats s JOIN seat_types st ON s.seat_type_id = st.id WHERE s.seat_code = ?))
            """;
        String getSeatIdSQL = "SELECT id FROM seats WHERE seat_code = ?";

        try (Connection conn = DatabaseConfig.getDataSource().getConnection()) {
            // Update seat status
            try (PreparedStatement updateStmt = conn.prepareStatement(updateSeatSQL)) {
                updateStmt.setString(1, seatCode);
                int updated = updateStmt.executeUpdate();

                if (updated == 0) {
                    conn.rollback();
                    return false; // Seat not available
                }
            }

            // Get seat ID
            int seatId;
            try (PreparedStatement getSeatStmt = conn.prepareStatement(getSeatIdSQL)) {
                getSeatStmt.setString(1, seatCode);
                ResultSet rs = getSeatStmt.executeQuery();
                if (rs.next()) {
                    seatId = rs.getInt("id");
                } else {
                    conn.rollback();
                    return false;
                }
            }

            // Insert booking record
            try (PreparedStatement insertStmt = conn.prepareStatement(insertBookingSQL)) {
                insertStmt.setInt(1, seatId);
                insertStmt.setString(2, customerName);
                insertStmt.setString(3, customerEmail);
                insertStmt.setString(4, customerPhone);
                insertStmt.setString(5, seatCode);
                insertStmt.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            throw new RuntimeException("Error booking seat", e);
        }
    }

    @Override
    public List<Seat> getAllAvailableSeats() {
        List<Seat> seats = new ArrayList<>();
        String sql = """
            SELECT s.* FROM seats s 
            JOIN sections sec ON s.section_id = sec.id 
            WHERE s.status = 'AVAILABLE' AND s.is_active = true AND sec.is_active = true
            ORDER BY sec.name, s.row_number, s.seat_number
            """;

        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                seats.add(mapSeat(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching all available seats", e);
        }

        return seats;
    }

    @Override
    public List<Seat> getAllBookedSeats() {
        List<Seat> seats = new ArrayList<>();
        String sql = """
            SELECT s.* FROM seats s 
            JOIN sections sec ON s.section_id = sec.id 
            WHERE s.status = 'RESERVED' AND s.is_active = true AND sec.is_active = true
            ORDER BY sec.name, s.row_number, s.seat_number
            """;

        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

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
    public List<Booking> getAllBookings() {
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
            throw new RuntimeException("Error fetching bookings", e);
        }

        return bookings;
    }

    @Override
    public Optional<Booking> getBookingById(int id) {
        String sql = """
            SELECT b.*, s.seat_code
            FROM bookings b
            JOIN seats s ON b.seat_id = s.id
            WHERE b.id = ?
            """;

        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
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
    public boolean cancelBooking(int bookingId) {
        String updateSeatSQL = """
            UPDATE seats SET status = 'AVAILABLE' 
            WHERE id = (SELECT seat_id FROM bookings WHERE id = ?)
            """;
        String updateBookingSQL = "UPDATE bookings SET status = 'CANCELLED' WHERE id = ?";

        try (Connection conn = DatabaseConfig.getDataSource().getConnection()) {
            // Update seat status
            try (PreparedStatement updateSeatStmt = conn.prepareStatement(updateSeatSQL)) {
                updateSeatStmt.setInt(1, bookingId);
                updateSeatStmt.executeUpdate();
            }

            // Update booking status
            try (PreparedStatement updateBookingStmt = conn.prepareStatement(updateBookingSQL)) {
                updateBookingStmt.setInt(1, bookingId);
                int updated = updateBookingStmt.executeUpdate();

                if (updated > 0) {
                    conn.commit();
                    return true;
                }
            }

            conn.rollback();
            return false;
        } catch (SQLException e) {
            throw new RuntimeException("Error cancelling booking", e);
        }
    }

    // Configuration Management
    @Override
    public List<TheaterConfig> getAllConfigs() {
        List<TheaterConfig> configs = new ArrayList<>();
        String sql = "SELECT * FROM theater_config ORDER BY config_key";

        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                configs.add(mapTheaterConfig(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching configurations", e);
        }

        return configs;
    }

    @Override
    public Optional<TheaterConfig> getConfigByKey(String key) {
        String sql = "SELECT * FROM theater_config WHERE config_key = ?";

        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, key);
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
    public boolean updateConfig(String key, String value) {
        String sql = "UPDATE theater_config SET config_value = ?, updated_at = CURRENT_TIMESTAMP WHERE config_key = ?";

        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, value);
            stmt.setString(2, key);

            int updated = stmt.executeUpdate();
            conn.commit();
            return updated > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating configuration", e);
        }
    }

    // Statistics
    @Override
    public int getTotalSeats() {
        String sql = "SELECT COUNT(*) FROM seats WHERE is_active = true";

        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error getting total seats count", e);
        }

        return 0;
    }

    @Override
    public int getAvailableSeatsCount() {
        String sql = "SELECT COUNT(*) FROM seats WHERE status = 'AVAILABLE' AND is_active = true";

        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error getting available seats count", e);
        }

        return 0;
    }

    @Override
    public int getBookedSeatsCount() {
        String sql = "SELECT COUNT(*) FROM seats WHERE status = 'RESERVED' AND is_active = true";

        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error getting booked seats count", e);
        }

        return 0;
    }

    @Override
    public double getTotalRevenue() {
        String sql = "SELECT COALESCE(SUM(total_price), 0) FROM bookings WHERE status = 'CONFIRMED'";

        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error getting total revenue", e);
        }

        return 0.0;
    }

    // Helper methods for mapping ResultSet to objects
    private SeatType mapSeatType(ResultSet rs) throws SQLException {
        return new SeatType(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getDouble("price")
        );
    }

    private Section mapSection(ResultSet rs) throws SQLException {
        return new Section(
                rs.getInt("id"),
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
                rs.getString("config_key"),
                rs.getString("config_value"),
                rs.getString("description")
        );
    }
}

