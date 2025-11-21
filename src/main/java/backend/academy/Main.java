package backend.academy;

import backend.academy.analyzer.AdocReportGenerator;
import backend.academy.analyzer.Config;
import backend.academy.analyzer.ConsoleArgumentsParser;
import backend.academy.analyzer.LogAnalyzer;
import backend.academy.analyzer.LogReport;
import backend.academy.analyzer.MarkdownReportGenerator;
import com.beust.jcommander.JCommander;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@UtilityClass @Slf4j
public class Main {
    public static void main(String[] args) throws IOException, URISyntaxException {
        ConsoleArgumentsParser parser = new ConsoleArgumentsParser();
        JCommander.newBuilder()
            .addObject(parser)
            .build()
            .parse(args);

        Config config = new Config(parser);

        List<LogAnalyzer> logAnalyzers = new ArrayList<>();

        for (Path path : config.pathList()) {
            logAnalyzers.add(new LogAnalyzer(path, config));
        }

        for (URI uri : config.uriList()) {
            logAnalyzers.add(new LogAnalyzer(uri, config));
        }

        LogReport logReport = LogReport.of(logAnalyzers);

        switch (config.outputFormat()) {
            case MARKDOWN -> new MarkdownReportGenerator(config, logReport).generateReportFile();
            case ADOC -> new AdocReportGenerator(config, logReport).generateReportFile();
            default -> throw new IllegalStateException("Unexpected value: " + config.outputFormat());
        }
    }
}
