package uk.ac.ed.inf.aqmaps;

import com.mapbox.geojson.Point;

public class Pathfinding {
    
    public static double distance (Point a, Point b) {
        return(Math.sqrt(Math.pow(a.latitude()-b.latitude(),2)
                +Math.pow(a.longitude()-b.longitude(),2)));
    }
    
    
}
