package Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
                locator.getCountryDetails(40.069099, 45.038189); // New Delhi, India
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

class CountryLocator {
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/GeoData";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "mysql";

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

