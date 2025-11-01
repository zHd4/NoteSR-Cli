package app.notesr.cli.core.util;

import app.notesr.cli.core.util.VersionComparator;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VersionComparatorTest {
    private final VersionComparator comparator = new VersionComparator();

    @ParameterizedTest
    @CsvSource({
        //equal versions
        "1.0, 1.0, 0",
        "2.5.3, 2.5.3, 0",
        "1.0, 1.0.0, 0",
        "4.0.0.0, 4, 0",

        // first bigger
        "1.1, 1.0, 1",
        "2.0.1, 2.0.0, 1",
        "10.0, 2.9.9, 1",
        "1.0.1, 1.0, 1",
        "1.2.10, 1.2.2, 1",

        // second bigger
        "1.0, 1.1, -1",
        "2.0.0, 2.0.1, -1",
        "2.9.9, 10.0, -1",
        "1.0, 1.0.1, -1",
        "3.4.5.6, 3.4.5.7, -1"
    })
    void testCompareVersions(String v1, String v2, int expectedSign) {
        int result = comparator.compare(v1, v2);
        assertEquals(expectedSign, Integer.signum(result));
    }
}
