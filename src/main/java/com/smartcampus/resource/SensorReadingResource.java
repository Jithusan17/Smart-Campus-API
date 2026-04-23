package com.smartcampus.resource;

import com.smartcampus.application.DataStore;
import com.smartcampus.exception.SensorUnavailableException;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private final Sensor sensor;
    private final DataStore store = DataStore.getInstance();

    public SensorReadingResource(Sensor sensor) {
        this.sensor = sensor;
    }

    @GET
    public Response getReadings() {
        List<SensorReading> readings = store.getReadingsForSensor(sensor.getId());
        return Response.ok(readings).build();
    }

    @POST
    public Response addReading(SensorReading reading) {
        if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException(
                "Sensor '" + sensor.getId() + "' is in MAINTENANCE and cannot accept readings."
            );
        }
        if ("OFFLINE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException(
                "Sensor '" + sensor.getId() + "' is OFFLINE and cannot accept readings."
            );
        }
        if (reading == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("message", "Reading body is required."))
                    .build();
        }
        SensorReading stamped = new SensorReading(reading.getValue());
        store.getReadingsForSensor(sensor.getId()).add(stamped);
        sensor.setCurrentValue(stamped.getValue());
        return Response.status(Response.Status.CREATED).entity(stamped).build();
    }
}