package uk.ac.ed.inf.aqmaps;

import java.lang.reflect.Type;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mapbox.geojson.Point;

public class AirQualitySensor {
    
    private What3Words location;
    private double battery;
    private double reading;
    
    @Override
    public String toString() {
        return(this.location + " " + this.battery + " " + this.reading);
    }

    public What3Words getLocation() {
        return this.location;
    }

    public double getBattery() {
        return this.battery;
    }

    public double getReading() {
        return this.reading;
    }
    
    public Point getLocationAsPoint() {
        return this.location.toPoint();
    }
    
    public static ArrayList<Point> toPoints(ArrayList<AirQualitySensor> sensors) {
        var points = new ArrayList<Point>();
        
        for (AirQualitySensor sensor : sensors) {
            points.add(sensor.getLocationAsPoint());
        }
        return points;
    }
    
    public static ArrayList<AirQualitySensor> loadListFromURL(String url) {
        
        var gson = new GsonBuilder()
                .registerTypeAdapter(What3Words.class, new W3WDeserializer("80"))
                .create();
        
        var jsonString = App.readStringFromURL(url);
        Type listType = new TypeToken<ArrayList<AirQualitySensor>>(){}.getType();
        ArrayList<AirQualitySensor> sensors = gson.fromJson(jsonString, listType);
        
        return sensors;
    }
}
