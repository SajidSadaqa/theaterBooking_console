package org.example;

import org.example.dao.*;
import org.example.model.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;
import org.example.config.*;
import org.example.upload.*;
import org.example.upload.UploadFile;

import static org.example.dao.PostgreSQLTheaterDAO.*;

public class TheaterManagementSystem {
    private final Scanner scanner = new Scanner(System.in);
    private final TheaterDAO theaterDAO = new PostgreSQLTheaterDAO();
    private int currentTheaterId = -1;
    private String currentTheaterName = "";

    public void start() {
        System.out.println("======================================");
        System.out.println("  THEATER MANAGEMENT SYSTEM");
        System.out.println("======================================");
        System.out.println("Connected to PostgreSQL Database");

        // Select theater first
        if (!selectTheater()) {
            System.out.println("No theater selected. Exiting...");
            return;
        }

        // Main loop
        while (true) {
            showMainMenu();
            int choice = getIntInput();

            switch (choice) {
                case 1 -> manageTheaters();
                case 2 -> manageTheaterConfiguration();
                case 3 -> manageSeatTypes();
                case 4 -> manageSections();
                case 5 -> bookingSeason();
                case 6 -> viewReportsAndStatistics();
                case 7 -> manageBookings();
                case 8 -> importBookings();
                case 9 -> switchTheater();
                case 10 -> {
                    System.out.println("Thank you for using Theater Management System!");
                    return;
                }
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private boolean selectTheater() {
        System.out.println("\n========== SELECT THEATER ==========");
        List<Theater> theaters = theaterDAO.getAllTheaters();

        if (theaters.isEmpty()) {
            System.out.println("No theaters found. Would you like to create one? (y/N): ");
            String response = scanner.nextLine();
            if ("y".equalsIgnoreCase(response) || "yes".equalsIgnoreCase(response)) {
                return createFirstTheater();
            }
            return false;
        }

        System.out.println("Available theaters:");
        for (int i = 0; i < theaters.size(); i++) {
            Theater theater = theaters.get(i);
            System.out.printf("%d. %s (%s)%n", i + 1, theater.getName(), theater.getLocation());
        }

        System.out.print("Select theater (number): ");
        int choice = getIntInput();

        if (choice >= 1 && choice <= theaters.size()) {
            Theater selected = theaters.get(choice - 1);
            currentTheaterId = selected.getId();
            currentTheaterName = selected.getName();
            System.out.println("Selected theater: " + currentTheaterName);
            return true;
        }

        System.out.println("Invalid selection!");
        return selectTheater();
    }

    private boolean createFirstTheater() {
        System.out.println("\n=== Create Theater ===");
        System.out.print("Theater name: ");
        String name = scanner.nextLine();
        System.out.print("Location: ");
        String location = scanner.nextLine();

        int theaterId = theaterDAO.createTheater(name, location);
        if (theaterId > 0) {
            currentTheaterId = theaterId;
            currentTheaterName = name;
            System.out.println("Theater created successfully!");
            return true;
        } else {
            System.out.println("Failed to create theater!");
            return false;
        }
    }

    private void switchTheater() {
        if (selectTheater()) {
            System.out.println("Switched to theater: " + currentTheaterName);
        }
    }

    private void showMainMenu() {
        System.out.println("\n========== MAIN MENU ==========");
        System.out.println("Current Theater: " + currentTheaterName + " (ID: " + currentTheaterId + ")");
        System.out.println("1. Manage Theaters");
        System.out.println("2. Theater Configuration");
        System.out.println("3. Manage Seat Types");
        System.out.println("4. Manage Sections");
        System.out.println("5. Booking System");
        System.out.println("6. Reports & Statistics");
        System.out.println("7. Manage Bookings");
        System.out.println("8. Import bookings");
        System.out.println("9. Switch Theater");
        System.out.println("10. Exit");
        System.out.print("Choose an option: ");
    }

    // 1. Theater Management
    private void manageTheaters() {
        while (true) {
            System.out.println("\n========== MANAGE THEATERS ==========");
            System.out.println("1. View all theaters");
            System.out.println("2. Create new theater");
            System.out.println("3. Update theater");
            System.out.println("4. Delete theater");
            System.out.println("5. View cross-theater statistics");
            System.out.println("6. Back to main menu");
            System.out.print("Choose an option: ");

            int choice = getIntInput();
            switch (choice) {
                case 1 -> viewAllTheaters();
                case 2 -> createTheater();
                case 3 -> updateTheater();
                case 4 -> deleteTheater();
                case 5 -> viewCrossTheaterStatistics();
                case 6 -> {
                    return;
                }
                default -> System.out.println("Invalid choice!");
            }
        }
    }

    private void viewAllTheaters() {
        System.out.println("\n=== All Theaters ===");
        List<Theater> theaters = theaterDAO.getAllTheaters();
        if (theaters.isEmpty()) {
            System.out.println("No theaters found.");
        } else {
            theaters.forEach(theater -> {
                String current = (theater.getId() == currentTheaterId) ? " (CURRENT)" : "";
                System.out.println(theater + current);
            });
        }
    }

    private void createTheater() {
        System.out.println("\n=== Create New Theater ===");
        System.out.print("Theater name: ");
        String name = scanner.nextLine();
        System.out.print("Location: ");
        String location = scanner.nextLine();

        int id = theaterDAO.createTheater(name, location);
        if (id > 0) {
            System.out.println("Theater created successfully with ID: " + id);
        } else {
            System.out.println("Failed to create theater!");
        }
    }

    private void updateTheater() {
        System.out.println("\n=== Update Theater ===");
        viewAllTheaters();
        System.out.print("Enter theater ID to update: ");
        int theaterId = getIntInput();

        Optional<Theater> theater = theaterDAO.getTheaterById(theaterId);
        if (theater.isEmpty()) {
            System.out.println("Theater not found!");
            return;
        }

        Theater current = theater.get();
        System.out.println("Current: " + current);

        System.out.print("New name (press Enter to keep current): ");
        String newName = scanner.nextLine();
        if (newName.trim().isEmpty()) newName = current.getName();

        System.out.print("New location (press Enter to keep current): ");
        String newLocation = scanner.nextLine();
        if (newLocation.trim().isEmpty()) newLocation = current.getLocation();

        if (theaterDAO.updateTheater(theaterId, newName, newLocation)) {
            System.out.println("Theater updated successfully!");
            if (theaterId == currentTheaterId) {
                currentTheaterName = newName;
            }
        } else {
            System.out.println("Failed to update theater!");
        }
    }

    private void deleteTheater() {
        System.out.println("\n=== Delete Theater ===");
        viewAllTheaters();
        System.out.print("Enter theater ID to delete: ");
        int theaterId = getIntInput();

        if (theaterId == currentTheaterId) {
            System.out.println("Cannot delete the currently selected theater!");
            return;
        }

        Optional<Theater> theater = theaterDAO.getTheaterById(theaterId);
        if (theater.isEmpty()) {
            System.out.println("Theater not found!");
            return;
        }

        System.out.print("Are you sure you want to delete '" + theater.get().getName() + "'? (y/N): ");
        String confirm = scanner.nextLine();
        if ("y".equalsIgnoreCase(confirm) || "yes".equalsIgnoreCase(confirm)) {
            if (theaterDAO.deleteTheater(theaterId)) {
                System.out.println("Theater deleted successfully!");
            } else {
                System.out.println("Cannot delete theater - it may contain sections or bookings!");
            }
        }
    }

    private void viewCrossTheaterStatistics() {
        System.out.println("\n=== Cross-Theater Statistics ===");
        int totalSeats = theaterDAO.getTotalSeatsAllTheaters();
        int availableSeats = theaterDAO.getAvailableSeatsCountAllTheaters();
        int bookedSeats = theaterDAO.getBookedSeatsCountAllTheaters();
        double totalRevenue = theaterDAO.getTotalRevenueAllTheaters();

        System.out.printf("Total seats (all theaters): %d%n", totalSeats);
        System.out.printf("Available seats: %d%n", availableSeats);
        System.out.printf("Booked seats: %d%n", bookedSeats);
        System.out.printf("Total revenue: $%.2f%n", totalRevenue);

        List<Booking> allBookings = theaterDAO.getAllBookingsAllTheaters();
        System.out.printf("Total bookings: %d%n", allBookings.size());
    }

    // 2. Theater Configuration Management
    private void manageTheaterConfiguration() {
        while (true) {
            System.out.println("\n========== THEATER CONFIGURATION ==========");
            System.out.println("Theater: " + currentTheaterName);
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
        List<TheaterConfig> configs = theaterDAO.getAllConfigs(currentTheaterId);
        if (configs.isEmpty()) {
            System.out.println("No configurations found for this theater.");
        } else {
            configs.forEach(System.out::println);
        }
    }

    private void updateTheaterName() {
        System.out.print("Enter new theater name: ");
        String name = scanner.nextLine();

        if (theaterDAO.updateConfig("theater_name", name, currentTheaterId)) {
            System.out.println("Theater name updated successfully!");
            currentTheaterName = name;
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

        theaterDAO.updateConfig("max_sections", String.valueOf(maxSections), currentTheaterId);
        theaterDAO.updateConfig("max_rows_per_section", String.valueOf(maxRows), currentTheaterId);
        theaterDAO.updateConfig("max_seats_per_row", String.valueOf(maxSeats), currentTheaterId);

        System.out.println("Capacity limits updated successfully!");
    }

    private void toggleBookingSystem() {
        Optional<TheaterConfig> config = theaterDAO.getConfigByKey("booking_enabled", currentTheaterId);
        if (config.isPresent()) {
            boolean currentStatus = "true".equals(config.get().getConfigValue());
            boolean newStatus = !currentStatus;

            if (theaterDAO.updateConfig("booking_enabled", String.valueOf(newStatus), currentTheaterId)) {
                System.out.println("Booking system " + (newStatus ? "ENABLED" : "DISABLED"));
            }
        } else {
            System.out.println("Booking configuration not found!");
        }
    }

    // 3. Seat Types Management
    private void manageSeatTypes() {
        while (true) {
            System.out.println("\n========== MANAGE SEAT TYPES ==========");
            System.out.println("Theater: " + currentTheaterName);
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
        List<SeatType> seatTypes = theaterDAO.getAllSeatTypes(currentTheaterId);
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

        int id = theaterDAO.createSeatType(currentTheaterId, name, description, price);
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

        Optional<SeatType> seatType = theaterDAO.getSeatTypeByName(name, currentTheaterId);
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

        if (theaterDAO.updateSeatType(current.getId(), currentTheaterId, newName, newDescription, newPrice)) {
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

        Optional<SeatType> seatType = theaterDAO.getSeatTypeByName(name, currentTheaterId);
        if (seatType.isEmpty()) {
            System.out.println("Seat type not found!");
            return;
        }

        System.out.print("Are you sure you want to delete '" + name + "'? (y/N): ");
        String confirm = scanner.nextLine();
        if ("y".equalsIgnoreCase(confirm) || "yes".equalsIgnoreCase(confirm)) {
            if (theaterDAO.deleteSeatType(seatType.get().getId(), currentTheaterId)) {
                System.out.println("Seat type deleted successfully!");
            } else {
                System.out.println("Cannot delete seat type - it may be in use by sections!");
            }
        }
    }

    // 4. Section Management
    private void manageSections() {
        while (true) {
            System.out.println("\n========== MANAGE SECTIONS ==========");
            System.out.println("Theater: " + currentTheaterName);
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
        System.out.print("Enter path to CSV/TXT/JSON file: ");
        String path = scanner.nextLine().trim();

        UploadFile parser;
        String lower = path.toLowerCase();

        if (lower.endsWith(".csv")) {
            parser = new CsvFile();
        } else if (lower.endsWith(".json")) {
            parser = new JsonFile();
        } else {
            parser = new TxtFile();
        }

        try {
            List<SectionRow> rows = parser.parse(path);
            int created = 0, skipped = 0;

            for (SectionRow r : rows) {
                Optional<SeatType> st = r.getSeatType().matches("\\d+")
                        ? theaterDAO.getSeatTypeById(Integer.parseInt(r.getSeatType()), currentTheaterId)
                        : theaterDAO.getSeatTypeByName(r.getSeatType(), currentTheaterId);

                if (st.isEmpty()) {
                    System.out.println("Unknown seat type " + r.getSeatType() + " – skipped");
                    skipped++;
                    continue;
                }

                String sectionKey = r.getSectionName() + r.getRowNumber();

                if (theaterDAO.getSectionByName(sectionKey, currentTheaterId).isPresent()) {
                    skipped++;
                    continue;
                }

                theaterDAO.createSection(
                        currentTheaterId,
                        sectionKey,
                        st.get().getId(),
                        1,
                        r.getTotalSeats(),
                        "Imported row " + r.getRowNumber() + " of section " + r.getSectionName()
                );
                created++;
            }

            System.out.printf("Import complete – %d sections (rows) created, %d skipped.%n",
                    created, skipped);

        } catch (Exception ex) {
            System.err.println("Import failed: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void viewAllSections() {
        System.out.println("\n=== All Sections ===");
        List<Section> sections = theaterDAO.getAllSections(currentTheaterId);
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
        List<SeatType> seatTypes = theaterDAO.getAllSeatTypes(currentTheaterId);
        if (seatTypes.isEmpty()) {
            System.out.println("No seat types available. Please create seat types first.");
            return;
        }
        seatTypes.forEach(System.out::println);

        System.out.print("Section name (e.g., A, B, C): ");
        String name = scanner.nextLine().toUpperCase();

        System.out.print("Seat type name: ");
        String seatTypeName = scanner.nextLine().toUpperCase();

        Optional<SeatType> seatType = theaterDAO.getSeatTypeByName(seatTypeName, currentTheaterId);
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

        int id = theaterDAO.createSection(currentTheaterId, name, seatType.get().getId(), rows, seatsPerRow, description);
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

        Optional<Section> section = theaterDAO.getSectionByName(name, currentTheaterId);
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
        theaterDAO.getAllSeatTypes(currentTheaterId).forEach(System.out::println);
        System.out.print("New seat type name: ");
        String seatTypeName = scanner.nextLine().toUpperCase();

        Optional<SeatType> seatType = theaterDAO.getSeatTypeByName(seatTypeName, currentTheaterId);
        if (seatType.isEmpty()) {
            System.out.println("Invalid seat type!");
            return;
        }

        if (theaterDAO.updateSection(name, currentTheaterId, rows, seatsPerRow, seatType.get().getId())) {
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

        Optional<Section> section = theaterDAO.getSectionByName(name, currentTheaterId);
        if (section.isEmpty()) {
            System.out.println("Section not found!");
            return;
        }

        Section current = section.get();
        boolean newStatus = !current.isActive();

        boolean success = newStatus ?
                theaterDAO.activateSection(name, currentTheaterId) :
                theaterDAO.deactivateSection(name, currentTheaterId);

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
            int seatCount = theaterDAO.generateSeatsForSection(name, currentTheaterId);
            if (seatCount > 0) {
                System.out.println("Generated " + seatCount + " seats for section " + name);
            } else {
                System.out.println("Failed to generate seats!");
            }
        }
    }

    // 5. Booking System
    private void bookingSeason() {
        // Check if booking is enabled
        Optional<TheaterConfig> config = theaterDAO.getConfigByKey("booking_enabled", currentTheaterId);
        if (config.isPresent() && "false".equals(config.get().getConfigValue())) {
            System.out.println("Booking system is currently disabled!");
            return;
        }

        while (true) {
            System.out.println("\n========== BOOKING SYSTEM ==========");
            System.out.println("Theater: " + currentTheaterName);
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
        List<Section> sections = theaterDAO.getActiveSections(currentTheaterId);
        if (sections.isEmpty()) {
            System.out.println("No active sections available!");
            return;
        }
        sections.forEach(System.out::println);

        // Get section
        System.out.print("Choose your section: ");
        String sectionName = scanner.nextLine().toUpperCase();

        Optional<Section> section = theaterDAO.getSectionByName(sectionName, currentTheaterId);
        if (section.isEmpty() || !section.get().isActive()) {
            System.out.println("Invalid or inactive section!");
            return;
        }

        // Show available rows
        System.out.println("Available rows in section " + sectionName + ":");
        List<Seat> sectionSeats = theaterDAO.getAvailableSeatsBySection(sectionName, currentTheaterId);
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
        List<Seat> rowSeats = theaterDAO.getAvailableSeatsByRow(sectionName, chosenRow, currentTheaterId);
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

        Optional<Seat> seatOpt = theaterDAO.getSeatByCode(seatCode, currentTheaterId);
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

        boolean booked = theaterDAO.bookSeat(seatCode, currentTheaterId, customerName, customerEmail, customerPhone);
        System.out.println(booked ? "Seat booked successfully!" : "Failed to book seat!");
    }

    private void showAvailableSeats() {
        System.out.println("\n=== All Available Seats ===");
        List<Seat> seats = theaterDAO.getAllAvailableSeats(currentTheaterId);
        if (seats.isEmpty()) {
            System.out.println("No available seats.");
        } else {
            seats.forEach(System.out::println);
            System.out.println("Total available: " + seats.size());
        }
    }

    private void showBookedSeats() {
        System.out.println("\n=== All Booked Seats ===");
        List<Seat> seats = theaterDAO.getAllBookedSeats(currentTheaterId);
        if (seats.isEmpty()) {
            System.out.println("No booked seats.");
        } else {
            seats.forEach(System.out::println);
            System.out.println("Total booked: " + seats.size());
        }
    }

    // 6. Reports & Statistics
    private void viewReportsAndStatistics() {
        System.out.println("\n========== REPORTS & STATISTICS ==========");
        System.out.println("Theater: " + currentTheaterName);

        int total = theaterDAO.getTotalSeats(currentTheaterId);
        int available = theaterDAO.getAvailableSeatsCount(currentTheaterId);
        int booked = theaterDAO.getBookedSeatsCount(currentTheaterId);
        double revenue = theaterDAO.getTotalRevenue(currentTheaterId);

        System.out.printf("Total seats   : %d%n", total);
        System.out.printf("Available     : %d%n", available);
        System.out.printf("Booked        : %d%n", booked);
        System.out.printf("Total revenue : $%.2f%n", revenue);

        if (total > 0) {
            double occupancyRate = (double) booked / total * 100;
            System.out.printf("Occupancy rate: %.1f%%%n", occupancyRate);
        }
    }

    // 7. Manage Bookings
    private void manageBookings() {
        while (true) {
            System.out.println("\n========== MANAGE BOOKINGS ==========");
            System.out.println("Theater: " + currentTheaterName);
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
        List<Booking> bookings = theaterDAO.getAllBookings(currentTheaterId);
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

        boolean success = theaterDAO.cancelBooking(bookingId, currentTheaterId);
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

    record ImportStats(int created, int skipped, int errors) {
        static ImportStats combine(ImportStats a, ImportStats b) {
            return new ImportStats(
                    a.created  + b.created,
                    a.skipped  + b.skipped,
                    a.errors   + b.errors
            );
        }
    }

    private void importBookings() {
        System.out.print("Enter file path, comma list, or directory: ");
        String input = scanner.nextLine().trim();

        List<Path> files;
        try {
            files = resolveImportPaths(input);
            if (files.isEmpty()) {
                System.out.println("No booking files found.");
                return;
            }
        } catch (Exception e) {
            System.out.println("Invalid path: " + e.getMessage());
            return;
        }

        ImportStats total = files
                .parallelStream()
                .map(this::importSingleBookingFile)
                .reduce(new ImportStats(0, 0, 0), ImportStats::combine);

        System.out.printf(
                "Booking import complete: %d created, %d skipped, %d file errors%n",
                total.created(), total.skipped(), total.errors()
        );
    }

    private List<Path> resolveImportPaths(String input) throws Exception {
        if (input.contains(",")) {
            List<Path> list = new ArrayList<>();
            for (String s : input.split(",")) {
                String trimmed = s.trim();
                if (!trimmed.isEmpty()) {
                    list.add(Path.of(trimmed));
                }
            }
            return list;
        }

        Path p = Path.of(input);
        if (Files.isDirectory(p)) {
            try (var stream = Files.list(p)) {
                return stream
                        .filter(f -> {
                            String n = f.getFileName().toString().toLowerCase();
                            return n.endsWith(".csv") || n.endsWith(".json") || n.endsWith(".txt");
                        })
                        .toList();
            }
        }
        return List.of(p);
    }

    private ImportStats importSingleBookingFile(Path file) {
        int created = 0, skipped = 0, errors = 0;

        try {
            String lower = file.toString().toLowerCase();
            UploadFileBookings parser;
            if (lower.endsWith(".csv")) {
                parser = new BookingCsvFile();
            } else if (lower.endsWith(".json")) {
                parser = new BookingJsonFile();
            } else {
                parser = new BookingTxtFile();
            }

            List<BookingRow> rows = parser.parse(file.toString());

            try (Connection conn = DatabaseConfig.getDataSource().getConnection();
                 PreparedStatement claim = conn.prepareStatement(SQL_CLAIM_SEAT);
                 PreparedStatement priceQ = conn.prepareStatement(SQL_SEAT_TYPE_PRICE);
                 PreparedStatement ins = conn.prepareStatement(SQL_INSERT_BOOKING)) {

                conn.setAutoCommit(false);

                for (BookingRow r : rows) {
                    try {
                        // 1) Claim seat atomically (now with theater ID)
                        claim.setString(1, r.seatCode());
                        claim.setInt(2, currentTheaterId);
                        ResultSet rs = claim.executeQuery();
                        if (!rs.next()) {        // seat already reserved or not found
                            skipped++;
                            conn.rollback();
                            continue;
                        }

                        int seatId     = rs.getInt("id");
                        int seatTypeId = rs.getInt("seat_type_id");

                        // 2) Determine price (override > look-up)
                        double price = r.priceOverride();
                        if (price == 0.0) {
                            priceQ.setInt(1, seatTypeId);
                            priceQ.setInt(2, currentTheaterId);
                            try (ResultSet pr = priceQ.executeQuery()) {
                                if (pr.next()) price = pr.getDouble(1);
                            }
                        }

                        // 3) Insert booking
                        ins.setInt   (1, seatId);
                        ins.setString(2, r.customerName());
                        ins.setString(3, r.customerEmail());
                        ins.setString(4, r.customerPhone());
                        ins.setDouble(5, price);
                        ins.executeUpdate();

                        conn.commit();
                        created++;

                    } catch (SQLException ex) {
                        conn.rollback();
                        errors++;
                    }
                }
            }
        } catch (Exception e) {
            errors++;
        }
        return new ImportStats(created, skipped, errors);
    }

}