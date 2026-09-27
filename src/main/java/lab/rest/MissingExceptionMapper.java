package lab.rest;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lab.dto.ErrorResponse;
import lab.service.MissingException;

@Provider
public class MissingExceptionMapper implements ExceptionMapper<MissingException> {

    @Override
    public Response toResponse(MissingException exception) {
        String message = exception.getMessage() == null ? "Не найдено" : exception.getMessage();
        return Response.status(Response.Status.NOT_FOUND)
                .type(MediaType.APPLICATION_JSON)
                .entity(new ErrorResponse(message))
                .build();
    }
}
