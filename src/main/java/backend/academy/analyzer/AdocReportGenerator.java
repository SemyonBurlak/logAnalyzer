package backend.academy.analyzer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AdocReportGenerator extends FileReportGenerator {

    public AdocReportGenerator(Config config, LogReport logReport) {
        super(config, logReport);
    }

    @Override
    public void generateReportFile() {


        StringBuilder reportBuilder = new StringBuilder();

        String tableBorder = "|===\n";

        reportBuilder.append("= Total Log Report\n\n");
        reportBuilder.append("== General Information\n\n");

        // File names
        reportBuilder.append(tableBorder);
        reportBuilder.append("| Metric | Value\n\n");

        reportBuilder.append("| File(s) | `").append(String.join(", ", logReport.fileNames())).append("`\n");

        // Start DateTime
        reportBuilder.append("| Start DateTime | ")
            .append(config.fromTime().equals(LocalDateTime.MIN) ? "-" : config.fromTime()).append("\n");

        // End DateTime
        reportBuilder.append("| End DateTime | ")
            .append(config.toTime().equals(LocalDateTime.MAX) ? "-" : config.toTime()).append("\n");

        // Counter
        reportBuilder.append("| Request Count | ")
            .append(formatWithUnderscores(logReport.counter())).append("\n");

        // Avg Response Size
        reportBuilder.append("| Avg Response Size | ")
            .append(formatWithUnderscores(Math.round(logReport.averageResponseSize()))).append("b").append("\n");

        // Percentile
        reportBuilder.append("| 95th Percentile Response Size | ")
            .append(formatWithUnderscores(logReport.percentile())).append("b").append("\n");

        // Total transferred bytes
        reportBuilder.append("| Total transferred bytes | ")
            .append(formatWithUnderscores(logReport.totalTransferredBytes())).append("b").append("\n");

        reportBuilder.append(tableBorder).append("\n");

        reportBuilder.append("== Requested Resources\n\n");
        reportBuilder.append(tableBorder);
        reportBuilder.append("| Resource | Count\n\n");
        for (Map.Entry<String, Long> entry : logReport.mostFrequentResources()) {
            reportBuilder.append("|").append(entry.getKey())
                .append("|").append(formatWithUnderscores(entry.getValue())).append("\n");
        }
        reportBuilder.append(tableBorder).append("\n");

        reportBuilder.append("== Response Codes\n\n");
        reportBuilder.append(tableBorder);
        reportBuilder.append("| Code | Name | Count\n\n");
        for (Map.Entry<Integer, Long> entry : logReport.mostFrequentStatus()) {
            String statusName = FileReportGenerator.STATUS_CODE_MAP.get(entry.getKey());
            reportBuilder.append("|").append(entry.getKey())
                .append("|").append(statusName)
                .append("|").append(formatWithUnderscores(entry.getValue())).append("\n");
        }
        reportBuilder.append(tableBorder);

        reportBuilder.append("== Methods\n\n");
        reportBuilder.append(tableBorder);
        reportBuilder.append("| Method | Count\n\n");
        for (Map.Entry<String, Long> entry : logReport.mostFrequentMethods()) {
            reportBuilder.append("|").append(entry.getKey())
                .append("|").append(formatWithUnderscores(entry.getValue())).append("\n");
        }
        reportBuilder.append(tableBorder);

        Path path = Path.of("reports/" + logReport.fileNames().getFirst() + "_report.adoc");
        createFile(path, reportBuilder.toString(), "Created AsciiDoc report: ");
    }
}
