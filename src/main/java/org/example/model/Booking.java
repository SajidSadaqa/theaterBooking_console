package org.example.model;

import java.time.LocalDateTime;

public class Booking {
    private final int id;
    private final int seatId;
    private final String seatCode;
    private final String customerName;
    private final String customerEmail;
    private final String customerPhone;
    private final LocalDateTime bookingTime;
    private final double totalPrice;
    private final String notes;
    private final String status;

    public Booking(int id, int seatId, String seatCode, String customerName,
                   String customerEmail, String customerPhone, LocalDateTime bookingTime,
                   double totalPrice, String notes, String status) {
        this.id = id;
        this.seatId = seatId;
        this.seatCode = seatCode;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.customerPhone = customerPhone;
        this.bookingTime = bookingTime;
        this.totalPrice = totalPrice;
        this.notes = notes;
        this.status = status;
    }

    // Getters
    public int getId() { return id; }
    public int getSeatId() { return seatId; }
    public String getSeatCode() { return seatCode; }
    public String getCustomerName() { return customerName; }
    public String getCustomerEmail() { return customerEmail; }
    public String getCustomerPhone() { return customerPhone; }
    public LocalDateTime getBookingTime() { return bookingTime; }
    public double getTotalPrice() { return totalPrice; }
    public String getNotes() { return notes; }
    public String getStatus() { return status; }

    @Override
    public String toString() {
        return seatCode + " - " + customerName + " ($" + String.format("%.2f", totalPrice) + ") - " + bookingTime;
    }
}