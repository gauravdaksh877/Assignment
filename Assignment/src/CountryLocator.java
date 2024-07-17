
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class CountryLocator {
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

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter latitude: ");
        double latitude = scanner.nextDouble();

        System.out.println("Enter longitude: ");
        double longitude = scanner.nextDouble();

        CountryLocator locator = new CountryLocator();
        String[] countryDetails = locator.getCountryDetails(latitude, longitude);

        if (countryDetails[0] != null) {
            System.out.println("Country code: " + countryDetails[0]);
            System.out.println("Country name: " + countryDetails[1]);
        } else {
            System.out.println("Country not found for the given coordinates.");
        }

        scanner.close();
    }
}

