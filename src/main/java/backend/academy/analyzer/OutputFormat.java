package backend.academy.analyzer;

import lombok.Getter;

@Getter
public enum OutputFormat implements ParsingOption {
    MARKDOWN("markdown"),
    ADOC("adoc");

    private final String stringValue;

    OutputFormat(String stringValue) {
        this.stringValue = stringValue;
    }
}
