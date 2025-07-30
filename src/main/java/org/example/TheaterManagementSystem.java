package org.example;

import org.example.dao.*;
import org.example.model.*;
import java.util.*;
import java.util.stream.Collectors;
import org.example.config.*;
import org.example.upload.CsvFile;
import org.example.upload.TxtFile;
import org.example.upload.UploadFile;

public class TheaterManagementSystem {
    private final Scanner scanner = new Scanner(System.in);
    private final TheaterDAO theaterDAO = new PostgreSQLTheaterDAO();

    public void start() {
        System.out.println("======================================");
        System.out.println("  THEATER MANAGEMENT SYSTEM");
        System.out.println("======================================");
        System.out.println("Connected to PostgreSQL Database");

        // Main loop
        while (true) {
            showMainMenu();
            int choice = getIntInput();

            switch (choice) {
                case 1 -> manageTheaterConfiguration();
                case 2 -> manageSeatTypes();
                case 3 -> manageSections();
                case 4 -> bookingSeason();
                case 5 -> viewReportsAndStatistics();
                case 6 -> manageBookings();
                case 7 -> {
                    System.out.println("Thank you for using Theater Management System!");
                    return;
                }
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private void showMainMenu() {
        System.out.println("\n========== MAIN MENU ==========");
        System.out.println("1. Theater Configuration");
        System.out.println("2. Manage Seat Types");
        System.out.println("3. Manage Sections");
        System.out.println("4. Booking System");
        System.out.println("5. Reports & Statistics");
        System.out.println("6. Manage Bookings");
        System.out.println("7. Exit");
        System.out.print("Choose an option: ");
    }

    // 1. Theater Configuration Management
    private void manageTheaterConfiguration() {
        while (true) {
            System.out.println("\n========== THEATER CONFIGURATION ==========");
            System.out.println("1. View current configuration");
            System.out.println("2. Update theater name");
            System.out.println("3. Update capacity limits");
            System.out.println("4. Toggle booking system");
            System.out.println("5. Back to main menu");
            System.out.print("Choose an option: ");

            int choice = getIntInput();
            switch (choice) {
                case 1 -> viewTheaterConfiguration();
                case 2 -> updateTheaterName();
                case 3 -> updateCapacityLimits();
                case 4 -> toggleBookingSystem();
                case 5 -> {
                    return;
                }
                default -> System.out.println("Invalid choice!");
            }
        }
    }

    private void viewTheaterConfiguration() {
        System.out.println("\n=== Current Theater Configuration ===");
        List<TheaterConfig> configs = theaterDAO.getAllConfigs();
        configs.forEach(System.out::println);
    }

    private void updateTheaterName() {
        System.out.print("Enter new theater name: ");
        String name = scanner.nextLine();

        if (theaterDAO.updateConfig("theater_name", name)) {
            System.out.println("Theater name updated successfully!");
        } else {
            System.out.println("Failed to update theater name!");
        }
    }

    private void updateCapacityLimits() {
        System.out.println("\n=== Update Capacity Limits ===");
        System.out.print("Maximum sections allowed: ");
        int maxSections = getIntInput();
        System.out.print("Maximum rows per section: ");
        int maxRows = getIntInput();
        System.out.print("Maximum seats per row: ");
        int maxSeats = getIntInput();

        theaterDAO.updateConfig("max_sections", String.valueOf(maxSections));
        theaterDAO.updateConfig("max_rows_per_section", String.valueOf(maxRows));
        theaterDAO.updateConfig("max_seats_per_row", String.valueOf(maxSeats));

        System.out.println("Capacity limits updated successfully!");
    }

    private void toggleBookingSystem() {
        Optional<TheaterConfig> config = theaterDAO.getConfigByKey("booking_enabled");
        if (config.isPresent()) {
            boolean currentStatus = "true".equals(config.get().getConfigValue());
            boolean newStatus = !currentStatus;

            if (theaterDAO.updateConfig("booking_enabled", String.valueOf(newStatus))) {
                System.out.println("Booking system " + (newStatus ? "ENABLED" : "DISABLED"));
            }
        }
    }

    // 2. Seat Types Management
    private void manageSeatTypes() {
        while (true) {
            System.out.println("\n========== MANAGE SEAT TYPES ==========");
            System.out.println("1. View all seat types");
            System.out.println("2. Create new seat type");
            System.out.println("3. Update seat type");
            System.out.println("4. Delete seat type");
            System.out.println("5. Back to main menu");
            System.out.print("Choose an option: ");

            int choice = getIntInput();
            switch (choice) {
                case 1 -> viewAllSeatTypes();
                case 2 -> createSeatType();
                case 3 -> updateSeatType();
                case 4 -> deleteSeatType();
                case 5 -> {
                    return;
                }
                default -> System.out.println("Invalid choice!");
            }
        }
    }

    private void viewAllSeatTypes() {
        System.out.println("\n=== All Seat Types ===");
        List<SeatType> seatTypes = theaterDAO.getAllSeatTypes();
        if (seatTypes.isEmpty()) {
            System.out.println("No seat types found.");
        } else {
            seatTypes.forEach(System.out::println);
        }
    }

    private void createSeatType() {
        System.out.println("\n=== Create New Seat Type ===");
        System.out.print("Seat type name: ");
        String name = scanner.nextLine().toUpperCase();
        System.out.print("Description: ");
        String description = scanner.nextLine();
        System.out.print("Price: $");
        double price = getDoubleInput();

        int id = theaterDAO.createSeatType(name, description, price);
        if (id > 0) {
            System.out.println("Seat type created successfully with ID: " + id);
        } else {
            System.out.println("Failed to create seat type!");
        }
    }

    private void updateSeatType() {
        System.out.println("\n=== Update Seat Type ===");
        viewAllSeatTypes();
        System.out.print("Enter seat type name to update: ");
        String name = scanner.nextLine().toUpperCase();

        Optional<SeatType> seatType = theaterDAO.getSeatTypeByName(name);
        if (seatType.isEmpty()) {
            System.out.println("Seat type not found!");
            return;
        }

        SeatType current = seatType.get();
        System.out.println("Current: " + current);

        System.out.print("New name (press Enter to keep current): ");
        String newName = scanner.nextLine();
        if (newName.trim().isEmpty()) newName = current.getName();

        System.out.print("New description (press Enter to keep current): ");
        String newDescription = scanner.nextLine();
        if (newDescription.trim().isEmpty()) newDescription = current.getDescription();

        System.out.print("New price (press Enter to keep current): $");
        String priceInput = scanner.nextLine();
        double newPrice = priceInput.trim().isEmpty() ? current.getPrice() : Double.parseDouble(priceInput);

        if (theaterDAO.updateSeatType(current.getId(), newName, newDescription, newPrice)) {
            System.out.println("Seat type updated successfully!");
        } else {
            System.out.println("Failed to update seat type!");
        }
    }

    private void deleteSeatType() {
        System.out.println("\n=== Delete Seat Type ===");
        viewAllSeatTypes();
        System.out.print("Enter seat type name to delete: ");
        String name = scanner.nextLine().toUpperCase();

        Optional<SeatType> seatType = theaterDAO.getSeatTypeByName(name);
        if (seatType.isEmpty()) {
            System.out.println("Seat type not found!");
            return;
        }

        System.out.print("Are you sure you want to delete '" + name + "'? (y/N): ");
        String confirm = scanner.nextLine();
        if ("y".equalsIgnoreCase(confirm) || "yes".equalsIgnoreCase(confirm)) {
            if (theaterDAO.deleteSeatType(seatType.get().getId())) {
                System.out.println("Seat type deleted successfully!");
            } else {
                System.out.println("Cannot delete seat type - it may be in use by sections!");
            }
        }
    }

    // 3. Section Management
    private void manageSections() {
        while (true) {
            System.out.println("\n========== MANAGE SECTIONS ==========");
            System.out.println("1. View all sections");
            System.out.println("2. Create new section");
            System.out.println("3. Update section configuration");
            System.out.println("4. Activate/Deactivate section");
            System.out.println("5. Regenerate seats for section");
            System.out.println("6. Import sections from file");
            System.out.println("7. Back to main menu");
            System.out.print("Choose an option: ");

            int choice = getIntInput();
            switch (choice) {
                case 1 -> viewAllSections();
                case 2 -> createSection();
                case 3 -> updateSection();
                case 4 -> toggleSectionStatus();
                case 5 -> regenerateSeats();
                case 6 -> importSectionsFromFile();
                case 7 -> {
                    return;
                }
                default -> System.out.println("Invalid choice!");
            }
        }
    }

    private void importSectionsFromFile() {
        System.out.print("Enter path to CSV/TXT file: ");
        String path = scanner.nextLine().trim();

        UploadFile parser = path.toLowerCase().endsWith(".csv") ? new CsvFile()
                : new TxtFile();
        try {
            List<SectionRow> rows = parser.parse(path);
            int created = 0;

            for (SectionRow r : rows) {
                // 1. Resolve seat‑type: by name or id
                Optional<SeatType> st = r.getSeatType().matches("\\d+")
                        ? theaterDAO.getSeatTypeById(Integer.parseInt(r.getSeatType()))
                        : theaterDAO.getSeatTypeByName(r.getSeatType());

                if (st.isEmpty()) {
                    System.out.println("Unknown seat type " + r.getSeatType() + " – skipped");
                    continue;
                }

                // 2. Ensure Section exists (create if needed)
                if (theaterDAO.getSectionByName(r.getSectionName()).isEmpty()) {
                    theaterDAO.createSection(
                            r.getSectionName(),
                            st.get().getId(),
                            r.getRowNumber(),
                            r.getTotalSeats(),
                            "Imported"
                    );
                }
            }
        } catch(Exception ex){
                System.err.println("Import failed: " + ex.getMessage());
                ex.printStackTrace();
            }
        }

    private void viewAllSections() {
        System.out.println("\n=== All Sections ===");
        List<Section> sections = theaterDAO.getAllSections();
        if (sections.isEmpty()) {
            System.out.println("No sections found.");
        } else {
            sections.forEach(section -> {
                System.out.println(section + " - " + (section.isActive() ? "ACTIVE" : "INACTIVE"));
            });
        }
    }

    private void createSection() {
        System.out.println("\n=== Create New Section ===");

        // Show available seat types
        System.out.println("Available seat types:");
        List<SeatType> seatTypes = theaterDAO.getAllSeatTypes();
        seatTypes.forEach(System.out::println);

        System.out.print("Section name (e.g., A, B, C): ");
        String name = scanner.nextLine().toUpperCase();

        System.out.print("Seat type name: ");
        String seatTypeName = scanner.nextLine().toUpperCase();

        Optional<SeatType> seatType = theaterDAO.getSeatTypeByName(seatTypeName);
        if (seatType.isEmpty()) {
            System.out.println("Invalid seat type!");
            return;
        }

        System.out.print("Number of rows: ");
        int rows = getIntInput();
        System.out.print("Seats per row: ");
        int seatsPerRow = getIntInput();
        System.out.print("Description: ");
        String description = scanner.nextLine();

        int id = theaterDAO.createSection(name, seatType.get().getId(), rows, seatsPerRow, description);
        if (id > 0) {
            System.out.println("Section created successfully with " + (rows * seatsPerRow) + " seats!");
        } else {
            System.out.println("Failed to create section!");
        }
    }

    private void updateSection() {
        System.out.println("\n=== Update Section Configuration ===");
        viewAllSections();
        System.out.print("Enter section name to update: ");
        String name = scanner.nextLine().toUpperCase();

        Optional<Section> section = theaterDAO.getSectionByName(name);
        if (section.isEmpty()) {
            System.out.println("Section not found!");
            return;
        }

        Section current = section.get();
        System.out.println("Current: " + current);

        System.out.print("New number of rows: ");
        int rows = getIntInput();
        System.out.print("New seats per row: ");
        int seatsPerRow = getIntInput();

        System.out.println("Available seat types:");
        theaterDAO.getAllSeatTypes().forEach(System.out::println);
        System.out.print("New seat type name: ");
        String seatTypeName = scanner.nextLine().toUpperCase();

        Optional<SeatType> seatType = theaterDAO.getSeatTypeByName(seatTypeName);
        if (seatType.isEmpty()) {
            System.out.println("Invalid seat type!");
            return;
        }

        if (theaterDAO.updateSection(name, rows, seatsPerRow, seatType.get().getId())) {
            System.out.println("Section updated successfully! Seats have been regenerated.");
        } else {
            System.out.println("Failed to update section!");
        }
    }

    private void toggleSectionStatus() {
        System.out.println("\n=== Activate/Deactivate Section ===");
        viewAllSections();
        System.out.print("Enter section name: ");
        String name = scanner.nextLine().toUpperCase();

        Optional<Section> section = theaterDAO.getSectionByName(name);
        if (section.isEmpty()) {
            System.out.println("Section not found!");
            return;
        }

        Section current = section.get();
        boolean newStatus = !current.isActive();

        boolean success = newStatus ?
                theaterDAO.activateSection(name) :
                theaterDAO.deactivateSection(name);

        if (success) {
            System.out.println("Section " + name + " " + (newStatus ? "ACTIVATED" : "DEACTIVATED"));
        } else {
            System.out.println("Failed to update section status!");
        }
    }

    private void regenerateSeats() {
        System.out.println("\n=== Regenerate Seats ===");
        viewAllSections();
        System.out.print("Enter section name: ");
        String name = scanner.nextLine().toUpperCase();

        System.out.print("This will delete all existing seats and bookings for this section. Continue? (y/N): ");
        String confirm = scanner.nextLine();
        if ("y".equalsIgnoreCase(confirm) || "yes".equalsIgnoreCase(confirm)) {
            int seatCount = theaterDAO.generateSeatsForSection(name);
            if (seatCount > 0) {
                System.out.println("Generated " + seatCount + " seats for section " + name);
            } else {
                System.out.println("Failed to generate seats!");
            }
        }
    }

    // 4. Booking System
    private void bookingSeason() {
        // Check if booking is enabled
        Optional<TheaterConfig> config = theaterDAO.getConfigByKey("booking_enabled");
        if (config.isPresent() && "false".equals(config.get().getConfigValue())) {
            System.out.println("Booking system is currently disabled!");
            return;
        }

        while (true) {
            System.out.println("\n========== BOOKING SYSTEM ==========");
            System.out.println("1. Book a seat");
            System.out.println("2. Show available seats");
            System.out.println("3. Show booked seats");
            System.out.println("4. Back to main menu");
            System.out.print("Choose an option: ");

            int choice = getIntInput();
            switch (choice) {
                case 1 -> processBooking();
                case 2 -> showAvailableSeats();
                case 3 -> showBookedSeats();
                case 4 -> {
                    return;
                }
                default -> System.out.println("Invalid choice!");
            }
        }
    }

    private void processBooking() {
        System.out.println("\n=== Seat Booking ===");

        // Show active sections
        System.out.println("Available sections:");
        List<Section> sections = theaterDAO.getActiveSections();
        if (sections.isEmpty()) {
            System.out.println("No active sections available!");
            return;
        }
        sections.forEach(System.out::println);

        // Get section
        System.out.print("Choose your section: ");
        String sectionName = scanner.nextLine().toUpperCase();

        Optional<Section> section = theaterDAO.getSectionByName(sectionName);
        if (section.isEmpty() || !section.get().isActive()) {
            System.out.println("Invalid or inactive section!");
            return;
        }

        // Show available rows
        System.out.println("Available rows in section " + sectionName + ":");
        List<Seat> sectionSeats = theaterDAO.getAvailableSeatsBySection(sectionName);
        Set<Integer> availableRows = sectionSeats.stream()
                .map(Seat::getRowNumber)
                .collect(Collectors.toCollection(TreeSet::new));

        if (availableRows.isEmpty()) {
            System.out.println("No available rows in this section!");
            return;
        }
        System.out.println("Rows with available seats: " + availableRows);

        System.out.print("Choose your row: ");
        int chosenRow = getIntInput();
        if (!availableRows.contains(chosenRow)) {
            System.out.println("Invalid row selection!");
            return;
        }

        // Show available seats in the chosen row
        List<Seat> rowSeats = theaterDAO.getAvailableSeatsByRow(sectionName, chosenRow);
        if (rowSeats.isEmpty()) {
            System.out.println("No available seats in row " + chosenRow);
            return;
        }
        String seatCodes = rowSeats.stream()
                .map(Seat::getSeatCode)
                .collect(Collectors.joining(", "));
        System.out.println("Available seats: " + seatCodes);

        System.out.print("Enter seat code to book: ");
        String seatCode = scanner.nextLine().toUpperCase();

        Optional<Seat> seatOpt = theaterDAO.getSeatByCode(seatCode);
        if (seatOpt.isEmpty() || !seatOpt.get().isAvailable()) {
            System.out.println("Seat not available!");
            return;
        }

        // Collect customer information
        System.out.print("Customer name: ");
        String customerName = scanner.nextLine();
        System.out.print("Customer email: ");
        String customerEmail = scanner.nextLine();
        System.out.print("Customer phone: ");
        String customerPhone = scanner.nextLine();

        boolean booked = theaterDAO.bookSeat(seatCode, customerName, customerEmail, customerPhone);
        System.out.println(booked ? "Seat booked successfully!" : "Failed to book seat!");
    }

    private void showAvailableSeats() {
        System.out.println("\n=== All Available Seats ===");
        List<Seat> seats = theaterDAO.getAllAvailableSeats();
        if (seats.isEmpty()) {
            System.out.println("No available seats.");
        } else {
            seats.forEach(System.out::println);
            System.out.println("Total available: " + seats.size());
        }
    }

    private void showBookedSeats() {
        System.out.println("\n=== All Booked Seats ===");
        List<Seat> seats = theaterDAO.getAllBookedSeats();
        if (seats.isEmpty()) {
            System.out.println("No booked seats.");
        } else {
            seats.forEach(System.out::println);
            System.out.println("Total booked: " + seats.size());
        }
    }

    // 5. Reports & Statistics
    private void viewReportsAndStatistics() {
        System.out.println("\n========== REPORTS & STATISTICS ==========");
        int total = theaterDAO.getTotalSeats();
        int available = theaterDAO.getAvailableSeatsCount();
        int booked = theaterDAO.getBookedSeatsCount();
        double revenue = theaterDAO.getTotalRevenue();

        System.out.printf("Total seats   : %d%n", total);
        System.out.printf("Available     : %d%n", available);
        System.out.printf("Booked        : %d%n", booked);
        System.out.printf("Total revenue : $%.2f%n", revenue);
    }

    // 6. Manage Bookings
    private void manageBookings() {
        while (true) {
            System.out.println("\n========== MANAGE BOOKINGS ==========");
            System.out.println("1. View all bookings");
            System.out.println("2. Cancel booking");
            System.out.println("3. Back to main menu");
            System.out.print("Choose an option: ");

            int choice = getIntInput();
            switch (choice) {
                case 1 -> viewAllBookings();
                case 2 -> cancelBooking();
                case 3 -> { return; }
                default -> System.out.println("Invalid choice!");
            }
        }
    }

    private void viewAllBookings() {
        System.out.println("\n=== All Bookings ===");
        List<Booking> bookings = theaterDAO.getAllBookings();
        if (bookings.isEmpty()) {
            System.out.println("No bookings found.");
        } else {
            bookings.forEach(System.out::println);
            System.out.println("Total bookings: " + bookings.size());
        }
    }

    private void cancelBooking() {
        viewAllBookings();
        System.out.print("Enter booking ID to cancel: ");
        int bookingId = getIntInput();

        boolean success = theaterDAO.cancelBooking(bookingId);
        System.out.println(success ? "Booking cancelled." : "Failed to cancel booking!");
    }

    private int getIntInput() {
        while (true) {
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.print("Invalid number, try again: ");
            }
        }
    }

    private double getDoubleInput() {
        while (true) {
            try {
                return Double.parseDouble(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.print("Invalid amount, try again: ");
            }
        }
    }
}

