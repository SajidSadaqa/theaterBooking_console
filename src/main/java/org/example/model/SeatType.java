package org.example.model;


public class SeatType {
    private final int id;
    private final int theaterId;
    private final String name;
    private final String description;
    private final double price;

    public SeatType(int id, int theaterId,String name, String description, double price) {
        this.id = id;
        this.theaterId = theaterId;
        this.name = name;
        this.description = description;
        this.price = price;
    }

    public int getTheaterId()    { return theaterId; }
    public int getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }

    @Override
    public String toString() {
        return name + " - $" + String.format("%.2f", price) + " (" + description + ")";
    }
}
