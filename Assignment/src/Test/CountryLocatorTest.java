package Test;

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
        String[] result = locator.getCountryDetails(40.069099, 45.038189); // New Delhi, India
        assertNotNull("Country details should not be null", result);
        assertEquals("AM", result[0]);
        assertEquals("Armenia", result[1]);
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
        String[] result = locator.getCountryDetails(41.153332, 20.168331); // London, UK
        assertNotNull("Country details should not be null", result);
        assertEquals("AL", result[0]);
        assertEquals("Albania", result[1]);
    }

    @Test
    public void testCoordinatesWithNoCountry() {
        // Example test case with coordinates in an undefined or empty region
        String[] result = locator.getCountryDetails(-91.0, -181.0); // Invalid latitude and longitude
        assertNull("Country code should be null for out of range coordinates", result[0]);
        assertNull("Country name should be null for out of range coordinates", result[1]);
    }
}

