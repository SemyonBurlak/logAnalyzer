package backend.academy.analyzer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.mutable.MutableLong;

@Getter
@Slf4j
public class LogAnalyzer {
    private final Config config;

    private final String fileName;
    private final MutableLong counter = new MutableLong();
    private final Map<String, Long> mostFrequentResources = new HashMap<>();
    private final Map<Integer, Long> mostFrequentStatuses = new HashMap<>();
    private final Map<String, Long> mostFrequentMethods = new HashMap<>();
    private final List<Long> notZeroBytes = new ArrayList<>();

    public LogAnalyzer(Path path, Config config) {
        this.config = config;

        this.fileName = path.getFileName().toString();
        analyzeLocalPathLogFile(path);
    }

    public LogAnalyzer(URI uri, Config config) {
        this.config = config;

        String[] segments = uri.getPath().split("/");
        this.fileName = segments[segments.length - 1];
        analyzeURILogFile(uri);
    }

    private void analyzeLocalPathLogFile(Path path) {
        log.info("Analyzing local path log file: {}", path);
        try (Stream<String> lines = Files.lines(path)) {
            analyzeStringStream(lines);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file " + path, e);
        }
    }

    private void analyzeURILogFile(URI uri) {
        log.info("Analyzing URI log file: {}", uri);
        if (uri.getScheme().equals("file")) {
            analyzeLocalPathLogFile(Paths.get(uri));
        } else {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(uri.toURL().openStream()))) {
                analyzeStringStream(reader.lines());
            } catch (MalformedURLException e) {
                throw new UncheckedIOException("Incorrect URL", e);
            } catch (IOException e) {
                throw new RuntimeException("Failed to read " + uri, e);
            }
        }
    }

    private void analyzeStringStream(Stream<String> lines) {
        Map<String, Long> resources = new HashMap<>();
        Map<Integer, Long> statuses = new HashMap<>();
        Map<String, Long> methods = new HashMap<>();

        lines.map(LogLineParser::parseLineToLogRecord)
            .filter(
                logRecord -> config.fromTime().isBefore(logRecord.timestamp())
                    && config.toTime().isAfter(logRecord.timestamp()))
            .filter(logRecord -> {
                if (config.filterField() == null) {
                    return true;
                }
                Pattern pattern = Pattern.compile(config.filterValue());
                Matcher matcher;
                switch (config.filterField()) {
                    case IP -> matcher = pattern.matcher(logRecord.ip());
                    case REMOTE_USER -> matcher = pattern.matcher(logRecord.remoteUser());
                    case METHOD -> matcher = pattern.matcher(logRecord.method());
                    case ADDRESS -> matcher = pattern.matcher(logRecord.address());
                    case PROTOCOL -> matcher = pattern.matcher(logRecord.protocol());
                    case STATUS -> matcher = pattern.matcher(String.valueOf(logRecord.status()));
                    case REFERER -> matcher = pattern.matcher(logRecord.httpReferer());
                    case USER_AGENT -> matcher = pattern.matcher(logRecord.userAgent());
                    default -> throw new RuntimeException("Unknown filter field " + config.filterField());
                }
                return matcher.find();
            })
            .forEach(logRecord -> {
                counter.increment();

                resources.putIfAbsent(logRecord.address(), 0L);
                resources.put(logRecord.address(), resources.get(logRecord.address()) + 1);
                statuses.putIfAbsent(logRecord.status(), 0L);
                statuses.put(logRecord.status(), statuses.get(logRecord.status()) + 1);
                methods.putIfAbsent(logRecord.method(), 0L);
                methods.put(logRecord.method(), methods.get(logRecord.method()) + 1);

                if (logRecord.bytes() > 0) {
                    notZeroBytes.add(logRecord.bytes());
                }
            });
        mostFrequentResources.putAll(getMostFrequentElements(resources, Config.MOST_FREQUENT_RESOURCES_LIMIT));

        mostFrequentStatuses.putAll(getMostFrequentElements(statuses, Config.MOST_FREQUENT_STATUSES_LIMIT));

        mostFrequentMethods.putAll(getMostFrequentElements(methods, Config.MOST_FREQUENT_METHODS_LIMIT));
    }

    private static <K, V extends Comparable<? super V>> Map<K, V> getMostFrequentElements(
        Map<K, V> statuses,
        int limit
    ) {
        return statuses.entrySet()
            .stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
            .limit(limit)
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

}
