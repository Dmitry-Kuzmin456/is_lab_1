package lab.rest;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lab.dto.DragonRequest;
import lab.dto.DragonResponse;
import lab.dto.OptionsResponse;
import lab.dto.PageResponse;
import lab.service.DragonService;
import lab.ws.DragonSocket;

@Path("/dragons")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class DragonResource {

    @Inject
    private DragonService dragons;

    @GET
    public PageResponse list(@QueryParam("page") @DefaultValue("0") int page,
                             @QueryParam("size") @DefaultValue("5") int size,
                             @QueryParam("sort") String sort,
                             @QueryParam("dir") @DefaultValue("asc") String direction,
                             @QueryParam("field") String field,
                             @QueryParam("q") String query) {
        return dragons.page(page, size, sort, direction, field, query);
    }

    @GET
    @Path("options")
    public OptionsResponse options() {
        return dragons.options();
    }

    @GET
    @Path("{id:\\d+}")
    public DragonResponse get(@PathParam("id") int id) {
        return dragons.get(id);
    }

    @POST
    public Response create(DragonRequest request) {
        DragonResponse body = dragons.create(request);
        DragonSocket.broadcast();
        return Response.status(Response.Status.CREATED).entity(body).build();
    }

    @PUT
    @Path("{id:\\d+}")
    public DragonResponse update(@PathParam("id") int id, DragonRequest request) {
        DragonResponse body = dragons.update(id, request);
        DragonSocket.broadcast();
        return body;
    }

    @DELETE
    @Path("{id:\\d+}")
    public Response delete(@PathParam("id") int id) {
        dragons.delete(id);
        DragonSocket.broadcast();
        return Response.noContent().build();
    }
}
