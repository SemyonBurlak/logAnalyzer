package backend.academy.analyzer;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
public class Config {
    public final static int MOST_FREQUENT_RESOURCES_LIMIT = 5;
    public final static int MOST_FREQUENT_STATUSES_LIMIT = 5;
    public final static int MOST_FREQUENT_METHODS_LIMIT = 5;

    public final static float PERCENTILE_RANK = 0.95f;

    private final static OutputFormat DEFAULT_OUTPUT_FORMAT = OutputFormat.MARKDOWN;

    private final static Pattern URL_PATH_PATTERN =
        Pattern.compile("^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]$");
    private final static Pattern LOCAL_PATH_PATTERN =
        Pattern.compile("^(?<baseDir>[^/\\\\]+)([/\\\\][^/\\\\]+)*$"); // relative glob to files

    private final ConsoleArgumentsParser parser;

    private final List<Path> pathList;
    private final List<URI> uriList;
    private final LocalDateTime fromTime;
    private final LocalDateTime toTime;

    private final OutputFormat outputFormat;

    private final FilterField filterField;
    private final String filterValue;

    public Config(ConsoleArgumentsParser parser) {
        this.parser = parser;
        uriList = new ArrayList<>();
        pathList = new ArrayList<>();

        if (parser.stringPaths() == null) {
            throw new IllegalArgumentException("Path list cannot be null or empty");
        }

        if (parser.stringFilterField() == null && parser.filterValue() != null) {
            throw new IllegalArgumentException("Filter value is set while filter field is not");
        }

        try {
            resolvePaths(parser.stringPaths());
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }

        if (parser.stringFromDateTime() == null) {
            this.fromTime = LocalDateTime.MIN;
        } else {
            this.fromTime = DateTimeConverter.convertArgumentDateTime(parser.stringFromDateTime());
        }

        if (parser.stringToDateTime() == null) {
            this.toTime = LocalDateTime.MAX;
        } else {
            this.toTime = DateTimeConverter.convertArgumentDateTime(parser.stringToDateTime());
        }

        if (parser.stringOutputFormat() == null) {
            this.outputFormat = DEFAULT_OUTPUT_FORMAT;
        } else {
            outputFormat =
                getParsingOption(Arrays.stream(OutputFormat.values()).toList(), parser().stringOutputFormat());
        }

        filterField = getParsingOption(Arrays.stream(FilterField.values()).toList(), parser().stringFilterField());

        this.filterValue = parser.filterValue();
    }

    private void resolvePaths(List<String> stringPathList) throws IOException, URISyntaxException {
        for (String path : stringPathList) {
            Matcher urlMatcher = URL_PATH_PATTERN.matcher(path);
            if (urlMatcher.matches()) {
                log.info("Resolved URI: {}", path);
                uriList.add(new URI(path));
            } else {
                log.info("Resolving glob: {}", path);
                resolveGlob(path);
            }
        }
    }

    private void resolveGlob(String glob) throws IOException {
        Matcher localPathMatcher = LOCAL_PATH_PATTERN.matcher(glob);

        if (!localPathMatcher.matches()) {
            log.error("Path not correct: {}", glob);
        }

        Path baseDir = Paths.get(localPathMatcher.group("baseDir"));

        PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher("glob:" + glob.replaceAll("\\\\", "/"));

        Files.walkFileTree(baseDir, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) {
                if (pathMatcher.matches(path)) {
                    log.info("Resolved file path: {}", path);
                    pathList.add(path);
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path path, IOException exc) {
                log.error("Failed to visit file: {}", path);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private <T extends ParsingOption> T getParsingOption(List<T> consoleValues, String value) {
        if (value == null) {
            return null;
        }
        for (T consoleValue : consoleValues) {
            if (consoleValue.stringValue().equals(value)) {
                return consoleValue;
            }
        }
        throw new IllegalArgumentException("Value " + value + " not found");
    }
}
