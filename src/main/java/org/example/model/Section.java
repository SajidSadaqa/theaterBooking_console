package org.example.model;

public class Section {
    private final int id;
    private final String name;
    private final int seatTypeId;
    private final String seatTypeName;
    private final int rows;
    private final int seatsPerRow;
    private final String description;
    private final boolean isActive;

    public Section(int id, String name, int seatTypeId, String seatTypeName,
                   int rows, int seatsPerRow, String description, boolean isActive) {
        this.id = id;
        this.name = name;
        this.seatTypeId = seatTypeId;
        this.seatTypeName = seatTypeName;
        this.rows = rows;
        this.seatsPerRow = seatsPerRow;
        this.description = description;
        this.isActive = isActive;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public int getSeatTypeId() { return seatTypeId; }
    public String getSeatTypeName() { return seatTypeName; }
    public int getRows() { return rows; }
    public int getSeatsPerRow() { return seatsPerRow; }
    public String getDescription() { return description; }
    public boolean isActive() { return isActive; }

    public int getTotalSeats() { return rows * seatsPerRow; }

    @Override
    public String toString() {
        return name + " (" + seatTypeName + ") - " + rows + " rows × " +
                seatsPerRow + " seats = " + getTotalSeats() + " total";
    }
}
