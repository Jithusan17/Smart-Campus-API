package com.smartcampus.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.LinkedHashMap;
import java.util.Map;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class DiscoveryResource {

    @GET
    public Response discover() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("apiVersion", "1.0.0");
        response.put("title", "Smart Campus Sensor & Room Management API");
        response.put("description", "RESTful API for managing campus rooms and IoT sensors.");
        response.put("contact", Map.of(
            "name",  "Smart Campus Admin",
            "email", "smartcampus@university.ac.uk"
        ));
        response.put("resources", Map.of(
            "rooms",   "/api/v1/rooms",
            "sensors", "/api/v1/sensors"
        ));
        response.put("_links", Map.of(
            "self",    "/api/v1/",
            "rooms",   "/api/v1/rooms",
            "sensors", "/api/v1/sensors"
        ));
        return Response.ok(response).build();
    }
}