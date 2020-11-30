package uk.ac.ed.inf.aqmaps;

import java.util.ArrayList;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.Point;

public class AirQualitySensor {

    private static final double LOW_BATTERY_THRESHOLD = 10.0;
    
    private What3Words location;
    private double battery;
    private String reading;

    public String getLocation() {
        return this.location.getWords();
    }

    public Point getLocationAsPoint() {
        return this.location.toPoint();
    }

    public boolean hasLowBattery() {
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

    public static ArrayList<AirQualitySensor> loadListFromURL(String url, String port) {
        //TODO
        var gson = new GsonBuilder()
                .registerTypeAdapter(What3Words.class, new W3WDeserializer(port)).create();
        var jsonString = App.readStringFromURL(url);
        var listType = new TypeToken<ArrayList<AirQualitySensor>>() {}.getType();
        
        ArrayList<AirQualitySensor> sensors = gson.fromJson(jsonString, listType);
        
        return sensors;
    }
}
