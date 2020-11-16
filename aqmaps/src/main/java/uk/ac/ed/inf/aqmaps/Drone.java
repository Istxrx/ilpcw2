package uk.ac.ed.inf.aqmaps;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;

public class Drone {
    
    private Point position;
    private int moveCount;
    private ArrayList<Polygon> noFlyZones;
    private ArrayList<What3Words> sensorLocations;
    private ArrayList<AirQualitySensor> sensors;
    
    private static final int DIRECTION_STEP = 10;
    private static final ArrayList<Integer> POSSIBLE_DIRECTIONS;
    static {
        POSSIBLE_DIRECTIONS = new ArrayList<Integer>();
        var direction = 0;
        
        while (direction < 360) {
            POSSIBLE_DIRECTIONS.add(direction);
            direction += DIRECTION_STEP;
        } 
    }
    private static final double MOVE_LENGTH = 0.0003;
    
    public Drone (Point position, 
            ArrayList<Polygon> noFlyZones, 
            ArrayList<AirQualitySensor> sensors) {
        
        this.position = position;
        this.moveCount = 0;
        this.noFlyZones = noFlyZones;
        this.sensors = sensors;    
        this.sensorLocations = new ArrayList<What3Words>();
        
        for (AirQualitySensor sensor : sensors) {
            sensorLocations.add(new What3Words(sensor.getLocation()));
        }
        
        
    }
    
    private void move() {}
    
    

    

}
