package backend.academy.analyzer;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Slf4j
public class LogAnalyzerTest {

    private Config config;
    private ConsoleArgumentsParser parser;

    @BeforeEach
    public void setUp() {
        parser = mock(ConsoleArgumentsParser.class);
    }

    @Test
    void testAnalyzeLocalPathLogFile() {
        when(parser.stringPaths()).thenReturn(
            List.of("src/test/logs/test_logs.log"));
        config = new Config(parser);
        LogAnalyzer logAnalyzer = new LogAnalyzer(config.pathList().getFirst(), config);

        assertEquals(26, logAnalyzer.counter().getValue());
    }

    @Test
    void testAnalyzeURILocalLogFile() {
        when(parser.stringPaths()).thenReturn(
            List.of("file:///" + System.getProperty("user.dir") + "/src/test/logs/test_logs.log"));
        config = new Config(parser);
        LogAnalyzer logAnalyzer = new LogAnalyzer(config.uriList().getFirst(), config);

        assertEquals(26, logAnalyzer.counter().getValue());
    }

    @Test
    void testFilterLogsByTimeRange() {
        // Assuming some test log file exists that LogAnalyzer can process

        when(parser.stringPaths()).thenReturn(List.of("src/test/logs/test_logs.log"));
        when(parser.stringFromDateTime()).thenReturn("2024-11-17T12:00:05");
        when(parser.stringToDateTime()).thenReturn("2024-11-17T12:00:25");
        config = new Config(parser);

        LogAnalyzer logAnalyzer = new LogAnalyzer(config.pathList().getFirst(), config);

        assertEquals(3, (long) logAnalyzer.counter().getValue(), "Expected logs to be processed within time range");
    }

    @Test
    void testFilterLogsByFieldAndValue() {
        when(parser.stringPaths()).thenReturn(List.of("src/test/logs/test_logs.log"));
        when(parser.stringFilterField()).thenReturn("method");
        when(parser.filterValue()).thenReturn("GET");

        config = new Config(parser);

        LogAnalyzer logAnalyzer = new LogAnalyzer(config.pathList().getFirst(), config);
        assertEquals(16, logAnalyzer.counter().getValue());
    }

    @Test
    void testEmptyLogsDoNotCrashAnalyzer() {
        when(parser.stringPaths()).thenReturn(List.of("src/test/logs/empty_logs.log"));

        config = new Config(parser);

        LogAnalyzer logAnalyzer = new LogAnalyzer(config.pathList().getFirst(), config);

        assertEquals(0, logAnalyzer.counter().getValue(), "Expected no logs to be processed");
        assertTrue(logAnalyzer.mostFrequentResources().isEmpty(), "Expected no resources to be logged");
        assertTrue(logAnalyzer.mostFrequentStatuses().isEmpty(), "Expected no statuses to be logged");
    }

    @Test
    void testInvalidPathThrowsException() {
        when(parser.stringPaths()).thenReturn(List.of("invalid/path/logs.log"));

        config = new Config(parser);

        assertThrows(RuntimeException.class, () -> new LogAnalyzer(config.pathList().getFirst(), config));
    }
}
