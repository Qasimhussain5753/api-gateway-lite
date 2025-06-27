package processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class NotFoundResponseProcessor implements Processor {

    @Override
    public void process(Exchange exchange) throws Exception {
        String path = exchange.getIn().getHeader(Exchange.HTTP_PATH, "N/A", String.class);
        String traceId = exchange.getIn().getHeader("X-Trace-ID", "N/A", String.class);
        String timestamp = ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        String jsonResponse = String.format(
            "{ \"timestamp\": \"%s\", \"status\": 404, \"error\": \"Not Found\", \"message\": \"The requested path '%s' was not found.\", \"path\": \"%s\", \"traceId\": \"%s\" }",
            timestamp, path, path, traceId
        );

        exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, 404);
        exchange.getMessage().setHeader(Exchange.CONTENT_TYPE, "application/json");
        exchange.getMessage().setBody(jsonResponse);
    }
}
