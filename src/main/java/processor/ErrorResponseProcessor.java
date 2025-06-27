package processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class ErrorResponseProcessor implements Processor {

    @Override
    public void process(Exchange exchange) throws Exception {
        Exception exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
        int statusCode = exchange.getIn().getHeader(Exchange.HTTP_RESPONSE_CODE, 500, Integer.class);
        String path = exchange.getIn().getHeader(Exchange.HTTP_PATH, "N/A", String.class);
        String traceId = exchange.getIn().getHeader("X-Trace-ID", "N/A", String.class);

        // Sanitize message for response (but not for logs)
        String message = getSafeMessage(exception);

        String errorType = getErrorType(statusCode);
        String timestamp = ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        String jsonResponse = String.format(
            "{ \"timestamp\": \"%s\", \"status\": %d, \"error\": \"%s\", \"path\": \"%s\", \"traceId\": \"%s\" }",
            timestamp, statusCode, errorType, path, traceId
        );

        exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, statusCode);
        exchange.getMessage().setHeader(Exchange.CONTENT_TYPE, "application/json");
        exchange.getMessage().setBody(jsonResponse);
    }

    private String getSafeMessage(Exception ex) {
        if (ex == null || ex.getMessage() == null) return "Unexpected error occurred.";
        return ex.getMessage()
                 .replaceAll("https?://[^\\s]+", "[REDACTED]")
                 .replaceAll("([0-9]{1,3}\\.){3}[0-9]{1,3}(:\\d+)?", "[REDACTED]");
    }

    private String getErrorType(int statusCode) {
        return switch (statusCode) {
            case 400 -> "Bad Request";
            case 401 -> "Unauthorized";
            case 403 -> "Forbidden";
            case 404 -> "Not Found";
            case 408 -> "Request Timeout";
            case 502 -> "Bad Gateway";
            case 503 -> "Service Unavailable";
            case 504 -> "Gateway Timeout";
            default -> "Internal Server Error";
        };
    }
}
