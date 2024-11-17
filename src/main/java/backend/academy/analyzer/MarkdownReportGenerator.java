package backend.academy.analyzer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MarkdownReportGenerator extends FileReportGenerator {

    public MarkdownReportGenerator(Config config, LogReport logReport) {
        super(config, logReport);
    }

    @Override
    public void generateReportFile() {
        StringBuilder reportBuilder = new StringBuilder();
        final String tableSeparator = "|:---:|---:|\n";
        final String newLine = "|\n";

        reportBuilder.append("### General Information\n\n");
        reportBuilder.append("|Metric|Value|\n");
        reportBuilder.append(tableSeparator);

        // File names
        reportBuilder.append("|File(s)|");
        reportBuilder.append("`").append(String.join(", ", logReport.fileNames())).append("`").append(newLine);
        // Start DateTime
        reportBuilder.append("|Start DateTime|");
        reportBuilder.append(config.fromTime().equals(LocalDateTime.MIN) ? "-" : config.fromTime()).append(newLine);

        // End DateTime
        reportBuilder.append("|End DateTime|");
        reportBuilder.append(config.toTime().equals(LocalDateTime.MAX) ? "-" : config.toTime()).append(newLine);

        // Counter
        reportBuilder.append("|Request Count| ")
            .append(formatWithUnderscores(logReport.counter())).append(newLine);

        // Avg Response Size
        reportBuilder.append("|Avg Response Size|")
            .append(formatWithUnderscores(Math.round(logReport.averageResponseSize()))).append("b").append(newLine);

        // Percentile
        reportBuilder.append("|95th Percentile Response Size|")
            .append(formatWithUnderscores(logReport.percentile())).append("b").append(newLine);

        // Total transferred bytes
        reportBuilder.append("|Total transferred bytes|")
            .append(formatWithUnderscores(logReport.totalTransferredBytes())).append("b").append(newLine);

        reportBuilder.append("\n### Requested Resources\n\n");
        reportBuilder.append("|Resource|Count").append(newLine);
        reportBuilder.append(tableSeparator);
        for (Map.Entry<String, Long> entry : logReport.mostFrequentResources()) {
            reportBuilder.append("|").append(entry.getKey())
                .append("|").append(formatWithUnderscores(entry.getValue())).append(newLine);
        }

        reportBuilder.append("\n### Response Codes\n\n");
        reportBuilder.append("|Code|Name|Count").append(newLine);
        reportBuilder.append("|:---:|:---:|---:").append(newLine);
        for (Map.Entry<Integer, Long> entry : logReport.mostFrequentStatus()) {
            String statusName = FileReportGenerator.STATUS_CODE_MAP.get(entry.getKey());
            reportBuilder.append("|").append(entry.getKey())
                .append("|").append(statusName)
                .append("|").append(formatWithUnderscores(entry.getValue())).append(newLine);
        }

        reportBuilder.append("\n### Methods\n\n");
        reportBuilder.append("|Method|Count").append(newLine);
        reportBuilder.append(tableSeparator);
        for (Map.Entry<String, Long> entry : logReport.mostFrequentMethods()) {
            reportBuilder.append("|").append(entry.getKey())
                .append("|").append(formatWithUnderscores(entry.getValue())).append(newLine);
        }

        Path path = Path.of("reports/" + logReport.fileNames().getFirst() + "_report.md");

        createFile(path, reportBuilder.toString(), "Created Markdown report: ");
    }
}
