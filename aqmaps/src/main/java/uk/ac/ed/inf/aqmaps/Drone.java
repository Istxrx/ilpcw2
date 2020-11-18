package uk.ac.ed.inf.aqmaps;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;

public class Drone {
    
    private Point position;
    private int moveCount;
    private Path flightPath;
    private String flightPathLog;
    private ArrayList<Polygon> noFlyZones;
    private ArrayList<What3Words> sensorLocations;
    private ArrayList<AirQualitySensor> sensors;
    
    private static final int MAX_MOVE_COUNT = 150;
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
    private static final double READING_RANGE = 0.0002;
    
    public Drone (Point position, 
            ArrayList<Polygon> noFlyZones, 
            ArrayList<AirQualitySensor> sensors) {
        
        this.position = position;
        this.moveCount = 0;
        this.flightPath = new Path(position);
        this.noFlyZones = noFlyZones;
        this.sensors = sensors;    
        this.sensorLocations = new ArrayList<What3Words>();
        
        for (AirQualitySensor sensor : sensors) {
            sensorLocations.add(new What3Words(sensor.getLocation()));
        }
          
    }
    
    public boolean move (int direction) {
        
        if (this.moveCount < MAX_MOVE_COUNT) {
            this.position = Utils2D.movePoint(this.position, MOVE_LENGTH, direction);
            this.flightPath.addMove(this.position, direction);
            this.moveCount += 1;
            return true;
        }
        return false;  
    }
    
    public void move (Path path) {
        
        for (Integer direction : path.getMoveDirections()) {
            if (!this.move(direction)) {
                break;
            }
        }
    }
    
    public Feature getFlightPath () {
        return this.flightPath.toFeature();
    }
    
    public void moveToNearestSensor () {
        
        var startingPath = new Path(this.flightPath.getEndPoint());
        var possiblePaths = startingPath.findContinuations(MOVE_LENGTH, POSSIBLE_DIRECTIONS, noFlyZones);
        var found = false;
        
        while (!found) {
            var path = possiblePaths.get(0);

            for (What3Words location : sensorLocations) {
                if (Utils2D.distance(path.getEndPoint(), location.toPoint()) < READING_RANGE) {
                    this.move(path);
                    found = true;
                    break;
                }
            }
            possiblePaths.addAll(path.findContinuations(MOVE_LENGTH, POSSIBLE_DIRECTIONS, noFlyZones));
            possiblePaths.remove(0);

        }

    }
    

    

}
