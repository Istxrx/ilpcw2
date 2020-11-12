package uk.ac.ed.inf.aqmaps;

import java.lang.reflect.Type;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class AirQualitySensor {
    
    private String location;
    private double battery;
    private double reading;
    
    @Override
    public String toString() {
        return(this.location + " " + this.battery + " " + this.reading);
    }

    public String getLocation() {
        return this.location;
    }

    public double getBattery() {
        return this.battery;
    }

    public double getReading() {
        return this.reading;
    }
    
    public static ArrayList<AirQualitySensor> loadListFromURL(String url) {
        
        var jsonString = App.readStringFromURL(url);
        Type listType = new TypeToken<ArrayList<AirQualitySensor>>(){}.getType();
        ArrayList<AirQualitySensor> sensors = new Gson().fromJson(jsonString, listType);
        
        return sensors;
    }
}
