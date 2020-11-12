package uk.ac.ed.inf.aqmaps;

import java.util.ArrayList;
import java.util.Iterator;

import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;

public class Drone {
    
    private Point position;
    private int moveCount;
    private double distanceTraveled;
    private ArrayList<Polygon> noFlyZones;
    private ArrayList<What3Words> sensorLocations;
    private ArrayList<AirQualitySensor> sensors;
    
    public Drone (Point position, 
            ArrayList<Polygon> noFlyZones, 
            ArrayList<AirQualitySensor> sensors) {
        
        this.position = position;
        this.moveCount = 0;
        this.distanceTraveled = 0;
        this.noFlyZones = noFlyZones;
        this.sensors = sensors;    
        this.sensorLocations = new ArrayList<What3Words>();
        
        for (AirQualitySensor sensor : sensors) {
            sensorLocations.add(new What3Words(sensor.getLocation()));
        }
    }
    
    private void move() {}
    

}
