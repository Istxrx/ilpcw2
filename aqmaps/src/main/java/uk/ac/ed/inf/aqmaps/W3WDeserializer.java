package uk.ac.ed.inf.aqmaps;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

/**
 * Custom Json deserializer for What3Words to make the AirQualitySensor deserialization possible and
 * more compact
 */
public class W3WDeserializer implements JsonDeserializer<What3Words> {

    private String port;

    public W3WDeserializer(String port) {
        this.port = port;
    }

    @Override
    public What3Words deserialize(JsonElement json, Type typeOf, JsonDeserializationContext context)
            throws JsonParseException {

        var words = json.getAsString();

        return new What3Words(words, port);

    }

}
