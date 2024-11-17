package backend.academy.analyzer;

import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@UtilityClass @Slf4j
public class LogLineParser {
    private final static Pattern LOG_LINE_PATTERN =
        Pattern.compile(
            // IPv4 pattern
            "(?<ip>(\\b((25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)\\.){3}(25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)\\b)"
                // IPv6 pattern
                + "|(([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}"
                + "|([0-9a-fA-F]{1,4}:){1,7}:"
                + "|([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}"
                + "|([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2}"
                + "|([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]{1,4}){1,3}"
                + "|([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4}"
                + "|([0-9a-fA-F]{1,4}:){1,2}(:[0-9a-fA-F]{1,4}){1,5}"
                + "|[0-9a-fA-F]{1,4}:((:[0-9a-fA-F]{1,4}){1,6})"
                + "|:((:[0-9a-fA-F]{1,4}){1,7}|:)"
                + "|fe80:(:[0-9a-fA-F]{0,4}){0,4}%[0-9a-zA-Z]+"
                + "|::(ffff(:0{1,4})?:)?((25[0-5]|2[0-4][0-9]|[0-1]?[0-9][0-9]?)\\\\.){3}"
                    + "(25[0-5]|2[0-4][0-9]|[0-1]?[0-9][0-9]?)"
                + "|([0-9a-fA-F]{1,4}:){1,4}:((25[0-5]|2[0-4][0-9]|[0-1]?[0-9][0-9]?)\\\\.){3}"
                    + "(25[0-5]|2[0-4][0-9]|[0-1]?[0-9][0-9]?)))"
                + " - "
                + "(?<remoteuser>[^\"]+) "
                + "\\[(?<time>(\\d{2})/(\\w{3})/(\\d{4}):(\\d{2}):(\\d{2}):(\\d{2}) ([+-]\\d{4}))] "
                + "\"(?<method>\\w+) (?<address>/[^\\s\"]+) (?<protocol>[^\\s\"]*)\" (?<status>\\d{3}) (?<bytes>\\d+)"
                + " \"(?<referer>[^\"]*)\" \"(?<userAgent>[^\"]*)\""
        );

    public static LogRecord parseLineToLogRecord(String line) {
        LogRecord logRecord;
        Matcher matcher = LOG_LINE_PATTERN.matcher(line);

        if (matcher.matches()) {
            String ip = matcher.group("ip");
            String remoteUser = matcher.group("remoteuser");
            LocalDateTime time = DateTimeConverter.convertLogDateTime(matcher.group("time"));
            String method = matcher.group("method");
            String address = matcher.group("address");
            String protocol = matcher.group("protocol");
            int status = Integer.parseInt(matcher.group("status"));
            long bytes = Long.parseLong(matcher.group("bytes"));
            String referer = matcher.group("referer");
            String userAgent = matcher.group("userAgent");

            logRecord = LogRecord.builder()
                .ip(ip)
                .remoteUser(remoteUser)
                .timestamp(time)
                .method(method)
                .address(address)
                .protocol(protocol)
                .status(status)
                .bytes(bytes)
                .httpReferer(referer)
                .userAgent(userAgent)
                .build();
            return logRecord;
        } else {
            throw new IllegalArgumentException("Invalid log line: " + line);
        }
    }
}
