package org.example;

import org.example.config.DatabaseConfig;
import org.example.dao.*;
import org.example.model.*;
import java.util.*;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        TheaterManagementSystem theater = new TheaterManagementSystem();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nShutting down Theater Management System...");
            DatabaseConfig.closeDataSource();
        }));

        theater.start();
    }
}