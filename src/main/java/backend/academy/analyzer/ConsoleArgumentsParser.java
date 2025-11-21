package backend.academy.analyzer;

import com.beust.jcommander.Parameter;
import java.util.List;
import lombok.Getter;

@Getter
public class ConsoleArgumentsParser {
    @Parameter(names = "--path", description = "Relative glob path or url to one or more NGINX log files",
        required = true,
        variableArity = true)
    private List<String> stringPaths;

    @Parameter(names = "--from", description = "Start date in ISO8601 format")
    private String stringFromDateTime;

    @Parameter(names = "--to", description = "End date in ISO8601 format")
    private String stringToDateTime;

    @Parameter(names = "--format", description = "Output format: markdown or adoc")
    private String stringOutputFormat;

    @Parameter(names = "--filter-field", description = "Filter result with field")
    private String stringFilterField;

    @Parameter(names = "--filter-value", description = "Value to filter")
    private String filterValue;
}
