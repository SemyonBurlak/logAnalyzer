package backend.academy.analyzer;

import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.mutable.MutableLong;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

class LogReportTest {

    @Test void testLogReportCreationFromValidReports() {
        // Mock LogAnalyzer instances
        LogAnalyzer report1 = Mockito.mock(LogAnalyzer.class);
        when(report1.fileName()).thenReturn("log1.txt");
        when(report1.counter()).thenReturn(new MutableLong(100L));
        when(report1.mostFrequentResources()).thenReturn(
            Map.of("/resource1", 50L, "/resource2", 26L, "/resource3", 25L));
        when(report1.mostFrequentStatuses()).thenReturn(Map.of(200, 80L));
        when(report1.notZeroBytes()).thenReturn(List.of(1000L, 2000L, 3000L));

        LogAnalyzer report2 = Mockito.mock(LogAnalyzer.class);
        when(report2.fileName()).thenReturn("log2.txt");
        when(report2.counter()).thenReturn(new MutableLong(150L));
        when(report2.mostFrequentResources()).thenReturn(
            Map.of("/resource3", 100L, "/resource4", 15L, "/resource5", 24L, "/resource6", 10L));
        when(report2.mostFrequentStatuses()).thenReturn(Map.of(404, 50L));
        when(report2.notZeroBytes()).thenReturn(List.of(4000L, 5000L));

        // Create LogReport
        LogReport logReport = LogReport.of(List.of(report1, report2));

        // Verify LogReport contents
        assertEquals(List.of("log1.txt", "log2.txt"), logReport.fileNames());
        assertEquals(250L, logReport.counter());
        assertEquals(List.of(
                Map.entry("/resource3", 125L),
                Map.entry("/resource1", 50L),
                Map.entry("/resource2", 26L),
                Map.entry("/resource5", 24L),
                Map.entry("/resource4", 15L)),
            logReport.mostFrequentResources()
        );
        assertEquals(3000.0, logReport.averageResponseSize(), 0.01); // Average of all notZeroBytes
        assertTrue(logReport.percentile() > 0);
    }

    @Test void testLogReportCreationWithNoReports() {
        // Expect exception when creating LogReport with an empty list
        IllegalArgumentException exception =
            assertThrows(IllegalArgumentException.class, () -> LogReport.of(List.of()));
        assertEquals("No reports found", exception.getMessage());
    }

    @Test void testLogReportWithEdgeCases() {
        // Mock LogAnalyzer with no data
        LogAnalyzer report = Mockito.mock(LogAnalyzer.class);
        when(report.fileName()).thenReturn("emptyLog.txt");
        when(report.counter()).thenReturn(new MutableLong());
        when(report.mostFrequentResources()).thenReturn(Map.of());
        when(report.mostFrequentStatuses()).thenReturn(Map.of());
        when(report.notZeroBytes()).thenReturn(List.of());

        // Create LogReport
        LogReport logReport = LogReport.of(List.of(report));

        // Verify LogReport contents
        assertEquals(List.of("emptyLog.txt"), logReport.fileNames());
        assertEquals(0L, logReport.counter());
        assertTrue(logReport.mostFrequentResources().isEmpty());
        assertTrue(logReport.mostFrequentStatus().isEmpty());
        assertEquals(0.0, logReport.averageResponseSize());
        assertEquals(0L, logReport.percentile());
    }
}
