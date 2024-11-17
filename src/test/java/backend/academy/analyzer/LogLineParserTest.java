package backend.academy.analyzer;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

public class LogLineParserTest {

    @Test
    void testParseValidLogLine() {
        // Arrange
        String logLine = "192.168.0.1 - john.doe [10/Oct/2000:13:55:36 -0700] "
            + "\"GET /apache_pb.gif HTTP/1.0\" 200 2326 "
            + "\"http://www.example.com/start.html\" "
            + "\"Mozilla/4.08 [en] (Win98; I)\"";

        // Act
        LogRecord logRecord = LogLineParser.parseLineToLogRecord(logLine);

        // Assert
        assertEquals("192.168.0.1", logRecord.ip());
        assertEquals("john.doe", logRecord.remoteUser());
        assertEquals(LocalDateTime.of(2000, 10, 10, 13, 55, 36), logRecord.timestamp());
        assertEquals("GET", logRecord.method());
        assertEquals("/apache_pb.gif", logRecord.address());
        assertEquals("HTTP/1.0", logRecord.protocol());
        assertEquals(200, logRecord.status());
        assertEquals(2326L, logRecord.bytes());
        assertEquals("http://www.example.com/start.html", logRecord.httpReferer());
        assertEquals("Mozilla/4.08 [en] (Win98; I)", logRecord.userAgent());
    }

    @Test
    void testParseInvalidLogLine() {
        // Arrange
        String invalidLogLine = "Invalid log line format";

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
            LogLineParser.parseLineToLogRecord(invalidLogLine)
        );
        assertEquals("Invalid log line: " + invalidLogLine, exception.getMessage());
    }

    @Test
    void testParseLogLineWithEdgeCases() {
        // Arrange
        String logLine = "::1 - anonymous [10/Oct/2000:13:55:36 +0000] "
            + "\"POST /submit HTTP/1.1\" 404 0 "
            + "\"\" \"\"";

        // Act
        LogRecord logRecord = LogLineParser.parseLineToLogRecord(logLine);

        // Assert
        assertEquals("::1", logRecord.ip());
        assertEquals("anonymous", logRecord.remoteUser());
        assertEquals(LocalDateTime.of(2000, 10, 10, 13, 55, 36), logRecord.timestamp());
        assertEquals("POST", logRecord.method());
        assertEquals("/submit", logRecord.address());
        assertEquals("HTTP/1.1", logRecord.protocol());
        assertEquals(404, logRecord.status());
        assertEquals(0L, logRecord.bytes());
        assertEquals("", logRecord.httpReferer());
        assertEquals("", logRecord.userAgent());
    }

    @Test
    void testParseLogLineWithIPv6() {
        // Arrange
        String logLine = "2001:db8::ff00:42:8329 - user123 [10/Oct/2000:13:55:36 +0000] "
            + "\"PUT /update HTTP/1.1\" 201 1234 "
            + "\"http://example.com\" "
            + "\"curl/7.68.0\"";

        // Act
        LogRecord logRecord = LogLineParser.parseLineToLogRecord(logLine);

        // Assert
        assertEquals("2001:db8::ff00:42:8329", logRecord.ip());
        assertEquals("user123", logRecord.remoteUser());
        assertEquals(LocalDateTime.of(2000, 10, 10, 13, 55, 36), logRecord.timestamp());
        assertEquals("PUT", logRecord.method());
        assertEquals("/update", logRecord.address());
        assertEquals("HTTP/1.1", logRecord.protocol());
        assertEquals(201, logRecord.status());
        assertEquals(1234L, logRecord.bytes());
        assertEquals("http://example.com", logRecord.httpReferer());
        assertEquals("curl/7.68.0", logRecord.userAgent());
    }
}
