package backend.academy.analyzer;

import lombok.Getter;

@Getter
public enum FilterField implements ParsingOption {
    IP("ip"),
    REMOTE_USER("remote_user"),
    METHOD("method"),
    ADDRESS("address"),
    PROTOCOL("protocol"),
    STATUS("status"),
    REFERER("referer"),
    USER_AGENT("user_agent");

    private final String stringValue;

    FilterField(String stringValue) {
        this.stringValue = stringValue;
    }
}
