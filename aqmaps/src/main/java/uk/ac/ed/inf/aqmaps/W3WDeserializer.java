package uk.ac.ed.inf.aqmaps;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

public class W3WDeserializer implements JsonDeserializer<What3Words>{

    @Override
    public What3Words deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        
        var words = json.getAsString();
        
        return new What3Words(words);
        
    }

}
