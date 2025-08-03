package org.example.model;

public record BookingRow(
        String seatCode,
        String customerName,
        String customerEmail,
        String customerPhone,
        double  priceOverride
) {}
