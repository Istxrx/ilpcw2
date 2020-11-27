package uk.ac.ed.inf.aqmaps;

import java.lang.reflect.Type;
import java.util.ArrayList;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.Point;

public class AirQualitySensor {
    
    private What3Words location;
    private double battery;
    private String reading;
    
    private static double LOW_BATTERY_THRESHOLD = 10;
    
    @Override
    public String toString() {
        return(this.location + " " + this.battery + " " + this.reading);
    }

    public String getLocation() {
        return this.location.getWords();
    }
    
    public Point getLocationAsPoint() {
        return this.location.toPoint();
    }

    public double getBattery() {
        return this.battery;
    }
    
    private boolean hasLowBattery() {
        return this.battery < LOW_BATTERY_THRESHOLD;
    }

    public double getReading() {
        return Double.parseDouble(this.reading);
    }
    
    public Feature toFeature(boolean visited) {
        var feature = Feature.fromGeometry((Geometry) this.getLocationAsPoint());
        if (visited) {
            if (!this.hasLowBattery()) {
                feature.addStringProperty("rgb-string", App.pollutionColor(this.getReading()));
                feature.addStringProperty("marker-color", App.pollutionColor(this.getReading()));
                feature.addStringProperty("marker-symbol", App.pollutionSymbol(this.getReading()));
            } else {
                feature.addStringProperty("rgb-string", "#000000");
                feature.addStringProperty("marker-color", "#000000");
                feature.addStringProperty("marker-symbol", "cross");
            }
        } else {
            feature.addStringProperty("rgb-string", "#aaaaaa");
            feature.addStringProperty("marker-color", "#aaaaaa");
        }
        return feature;
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
