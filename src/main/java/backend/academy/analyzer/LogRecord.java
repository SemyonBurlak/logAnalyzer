package backend.academy.analyzer;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public final class LogRecord {
    private final String ip;
    private final String remoteUser;
    private final LocalDateTime timestamp;
    private final String method;
    private final String address;
    private final String protocol;
    private final int status;
    private final long bytes;
    private final String httpReferer;
    private final String userAgent;
}
