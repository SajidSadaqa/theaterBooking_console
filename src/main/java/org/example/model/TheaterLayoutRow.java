package org.example.model;


import java.util.List;

public record TheaterLayoutRow(
        String theaterName,
        String theaterDescription,
        String sectionName,
        int rowNumber,
        List<Integer> seatsConfiguration,
        String seatTypeName
) {

    public TheaterLayoutRow(String theaterName, String theaterDescription,
                            String sectionName, int rowNumber,
                            int seatsPerRow, String seatTypeName) {
        this(theaterName, theaterDescription, sectionName, rowNumber,
                List.of(seatsPerRow), seatTypeName);
    }

    public int seatsPerRow() {
        return seatsConfiguration.stream()
                .filter(seats -> seats > 0)
                .mapToInt(Integer::intValue)
                .sum();
    }

    public int getTotalSeats() {
        return seatsPerRow();
    }

    public boolean hasGaps() {
        return seatsConfiguration.stream().anyMatch(seats -> seats == 0);
    }

    public int getNumberOfSegments() {
        return (int) seatsConfiguration.stream().filter(seats -> seats > 0).count();
    }

    public String getConfigurationDisplay() {
        return seatsConfiguration.stream()
                .map(seats -> seats == 0 ? "aisle" : seats.toString())
                .reduce((a, b) -> a + "-" + b)
                .orElse("empty");
    }
}