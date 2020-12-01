package uk.ac.ed.inf.aqmaps;

import java.util.ArrayList;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.Point;

/**
 * A representation of a sensor which monitors the quality of the air.
 */
public class AirQualitySensor {

    private static final double LOW_BATTERY_THRESHOLD = 10.0;
    
    private What3Words location;
    private double battery;
    private String reading;
    
    /**
     * @return location of this sensor given in a what 3 words format
     */
    public String getLocation() {
        return this.location.getWords();
    }

    /**
     * @return location of this sensor as a point
     */
    public Point getLocationAsPoint() {
        return this.location.toPoint();
    }
    
    /**
     * @return true if the battery of this sensor is low, false otherwise
     */
    public boolean hasLowBattery() {
        return this.battery < LOW_BATTERY_THRESHOLD;
    }

    /**
     * @return level of air pollution
     */
    public double getReading() {
        return Double.parseDouble(this.reading);
    }
    
    /**
     * @param visited if true includes the reading representing pollution level
     * @return point marker feature representing this sensor and its location
     */
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

    /**
     * @param sensors the sensors of which locations are returned
     * @return coordinate locations of the sensors represented as points 
     */
    public static ArrayList<Point> toPoints(ArrayList<AirQualitySensor> sensors) {
        
        var points = new ArrayList<Point>();

        for (AirQualitySensor sensor : sensors) {
            points.add(sensor.getLocationAsPoint());
        }
        return points;
    }

    /**
     * Loads a list of sensors from .json file on a server
     * 
     * @param url  an URL address of the server
     * @param port the port at which the connection to server is established
     * @return list of sensors obtained from the server
     */
    public static ArrayList<AirQualitySensor> loadListFromURL(String url, String port) {

        var gson = new GsonBuilder()
                .registerTypeAdapter(What3Words.class, new W3WDeserializer(port)).create();
        var jsonString = App.readStringFromURL(url);
        var listType = new TypeToken<ArrayList<AirQualitySensor>>() {}.getType();
        
        ArrayList<AirQualitySensor> sensors = gson.fromJson(jsonString, listType);
        
        return sensors;
    }
    
}
