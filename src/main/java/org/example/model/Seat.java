package org.example.model;

public class Seat {
    private final int id;
    private final String seatCode;
    private final int sectionId;
    private final int rowNumber;
    private final int seatNumber;
    private final int seatTypeId;
    private final String status;
    private final boolean isActive;

    public Seat(int id, String seatCode, int sectionId, int rowNumber,
                int seatNumber, int seatTypeId, String status, boolean isActive) {
        this.id = id;
        this.seatCode = seatCode;
        this.sectionId = sectionId;
        this.rowNumber = rowNumber;
        this.seatNumber = seatNumber;
        this.seatTypeId = seatTypeId;
        this.status = status;
        this.isActive = isActive;
    }

    public boolean isAvailable() {
        return "AVAILABLE".equals(status) && isActive;
    }

    public int getId() { return id; }
    public String getSeatCode() { return seatCode; }
    public int getSectionId() { return sectionId; }
    public int getRowNumber() { return rowNumber; }
    public int getSeatNumber() { return seatNumber; }
    public int getSeatTypeId() { return seatTypeId; }
    public String getStatus() { return status; }
    public boolean isActive() { return isActive; }

    @Override
    public String toString() {
        return seatCode + " (" + status + ")";
    }
}
