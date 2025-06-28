package filters;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class NoFoundExceptionMapper implements ExceptionMapper<NotFoundException> {

    @Override
    public Response toResponse(NotFoundException exception) {
        String path = exception.getMessage();
        String timestamp = ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        String json = String.format(
                "{\"timestamp\": \"%s\", \"status\": 404, \"error\": \"Not Found\", \"message\": \"Resource not found: %s\", \"path\": \"%s\"}",
                timestamp, path, path);
        return Response.status(Response.Status.NOT_FOUND)
                .type(MediaType.APPLICATION_JSON)
                .entity(json)
                .build();
    }
}