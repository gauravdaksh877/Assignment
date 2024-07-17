# GeoLocation to Country Code Application

## Overview

This application accepts latitude and longitude as input and returns the country name in ISO 3166-1 alpha-2 format (e.g., IN, US, etc.). The solution does not rely on any external API calls or HTTP requests to get this information. The application includes:
- Data source/database setup
- A Java program (`CountryLocator`) to determine the country code
- JUnit tests to validate the functionality
- Performance testing to measure execution time

## Data Source / Database

The application uses a MySQL database to store geographical data, specifically country boundaries.

1. **Database Setup:**
   - Create a MySQL database named `GeoData`.
   - Create a table named `CountryBoundaries` with the following schema:
     ```sql
     CREATE TABLE CountryBoundaries (
         id INT AUTO_INCREMENT PRIMARY KEY,
         latitude DOUBLE NOT NULL,
         longitude DOUBLE NOT NULL,
         country_code VARCHAR(2) NOT NULL,
         country_name VARCHAR(100) NOT NULL
     );
     ```

2. **Data Population:**
   - Populate the `CountryBoundaries` table with relevant data, mapping latitude and longitude to country codes and names.

## Java Program - `CountryLocator`

The `CountryLocator` class is responsible for mapping the input coordinates to the corresponding country code and name.

1. **CountryLocator.java:**
   ```java
   import java.sql.Connection;
   import java.sql.DriverManager;
   import java.sql.PreparedStatement;
   import java.sql.ResultSet;
   import java.sql.SQLException;

   public class CountryLocator {
       private static final String JDBC_URL = "jdbc:mysql://localhost:3306/GeoData";
       private static final String DB_USER = "your_username";
       private static final String DB_PASSWORD = "your_password";

       private Connection connect() {
           Connection connection = null;
           try {
               connection = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD);
           } catch (Exception e) {
               e.printStackTrace();
           }
           return connection;
       }

       public String[] getCountryDetails(double latitude, double longitude) {
           String[] countryDetails = new String[2]; // Index 0: country code, Index 1: country name
           String query = "SELECT country_code, country_name FROM CountryBoundaries WHERE latitude = ? AND longitude = ? LIMIT 1";

           try (Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(query)) {

               pstmt.setDouble(1, latitude);
               pstmt.setDouble(2, longitude);

               ResultSet rs = pstmt.executeQuery();

               if (rs.next()) {
                   countryDetails[0] = rs.getString("country_code");
                   countryDetails[1] = rs.getString("country_name");
               }
           } catch (SQLException e) {
               e.printStackTrace();
           }
           return countryDetails;
       }
   }
   
   ## JUnit Testing
   import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class CountryLocatorTest {

    private CountryLocator locator;

    @Before
    public void setUp() throws Exception {
        locator = new CountryLocator();
    }

    @Test
    public void testValidCoordinates() {
        // Example test case with valid coordinates
        String[] result = locator.getCountryDetails(28.6139, 77.2090); // New Delhi, India
        assertNotNull("Country details should not be null", result);
        assertEquals("IN", result[0]);
        assertEquals("India", result[1]);
    }

    @Test
    public void testInvalidCoordinates() {
        // Example test case with invalid coordinates (ocean or undefined region)
        String[] result = locator.getCountryDetails(0.0, 0.0); // Null Island (not a real country)
        assertNull("Country code should be null for invalid coordinates", result[0]);
        assertNull("Country name should be null for invalid coordinates", result[1]);
    }

    @Test
    public void testEdgeCaseCoordinates() {
        // Example test case with edge case coordinates (border regions)
        String[] result = locator.getCountryDetails(51.5074, -0.1278); // London, UK
        assertNotNull("Country details should not be null", result);
        assertEquals("GB", result[0]);
        assertEquals("United Kingdom", result[1]);
    }

    @Test
    public void testCoordinatesWithNoCountry() {
        // Example test case with coordinates in an undefined or empty region
        String[] result = locator.getCountryDetails(-91.0, -181.0); // Invalid latitude and longitude
        assertNull("Country code should be null for out of range coordinates", result[0]);
        assertNull("Country name should be null for out of range coordinates", result[1]);
    }
}

## Performance Testing

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class PerformanceTest {

    private static final int REQUESTS_PER_SECOND = 100;
    private static final int DURATION_SECONDS = 10;

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        CountryLocator locator = new CountryLocator();
        ExecutorService executor = Executors.newFixedThreadPool(REQUESTS_PER_SECOND);

        List<Callable<Long>> tasks = new ArrayList<>();
        for (int i = 0; i < REQUESTS_PER_SECOND * DURATION_SECONDS; i++) {
            tasks.add(() -> {
                long startTime = System.nanoTime();
                locator.getCountryDetails(28.6139, 77.2090); // New Delhi, India
                long endTime = System.nanoTime();
                return endTime - startTime;
            });
        }

        List<Future<Long>> results = executor.invokeAll(tasks);
        executor.shutdown();

        long totalTime = 0;
        for (Future<Long> result : results) {
            totalTime += result.get();
        }

        double averageTime = (double) totalTime / results.size();
        System.out.println("Average execution time: " + averageTime / 1_000_000 + " ms");
    }
}