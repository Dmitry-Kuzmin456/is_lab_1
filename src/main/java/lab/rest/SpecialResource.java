package lab.rest;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lab.dto.DragonResponse;
import lab.dto.KillerRequest;
import lab.dto.PersonOption;
import lab.dto.SendTeamRequest;
import lab.dto.TeamRequest;
import lab.dto.TeamResponse;
import lab.service.DragonService;
import lab.service.SpecialService;
import lab.ws.DragonSocket;

import java.util.List;

@Path("/special")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class SpecialResource {

    @Inject
    private SpecialService specials;

    @Inject
    private DragonService dragons;

    @GET
    @Path("max-date")
    public DragonResponse maxDate() {
        return specials.maxCreationDate();
    }

    @GET
    @Path("by-name")
    public List<DragonResponse> byName(@QueryParam("part") String part) {
        return specials.byName(part);
    }

    @GET
    @Path("by-character")
    public List<DragonResponse> byCharacter(@QueryParam("character") String character) {
        return specials.byCharacter(character);
    }

    @POST
    @Path("killers")
    public Response createKiller(KillerRequest request) {
        PersonOption body = dragons.createKiller(request);
        DragonSocket.broadcast();
        return Response.status(Response.Status.CREATED).entity(body).build();
    }

    @GET
    @Path("teams")
    public List<TeamResponse> teams() {
        return specials.teams();
    }

    @POST
    @Path("teams")
    public Response createTeam(TeamRequest request) {
        TeamResponse body = specials.createTeam(request);
        DragonSocket.broadcast();
        return Response.status(Response.Status.CREATED).entity(body).build();
    }

    @POST
    @Path("teams/send")
    public TeamResponse send(SendTeamRequest request) {
        TeamResponse body = specials.send(request);
        DragonSocket.broadcast();
        return body;
    }
}
