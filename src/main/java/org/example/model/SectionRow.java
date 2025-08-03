package org.example.model;

public class SectionRow {
    private final String sectionName;
    private final int rowNumber;
    private final int totalSeats;
    private final String seatType;

    public SectionRow(String sectionName, int rowNumber, int totalSeats, String seatType) {
        this.sectionName = sectionName;
        this.rowNumber = rowNumber;
        this.totalSeats = totalSeats;
        this.seatType = String.valueOf(seatType);
    }

    // Getters
    public String getSectionName() { return sectionName; }
    public int getRowNumber() { return rowNumber; }
    public int getTotalSeats() { return totalSeats; }
    public String getSeatType() { return seatType; }

    @Override
    public String toString() {
        return sectionName + " Row " + rowNumber + " (" + totalSeats + " seats, " + seatType + ")";
    }
}
