package org.example.model;

public class Theater {
    private final int id;
    private final String name;
    private final String location;

    public Theater(int id, String name, String location) {
        this.id       = id;
        this.name     = name;
        this.location = location;
    }
    public int getId()             { return id; }
    public String getName()        { return name; }
    public String getLocation()    { return location; }

    @Override
    public String toString() {
        return String.format("[%d] %s (%s)", id, name, location);
    }
}
