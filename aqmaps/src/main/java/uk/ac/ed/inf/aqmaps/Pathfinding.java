package uk.ac.ed.inf.aqmaps;

import java.awt.geom.Line2D;
import java.util.ArrayList;

import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;

public class Pathfinding {
    
    public static double distance (Point a, Point b) {
        
        return(Math.sqrt(Math.pow(a.latitude() - b.latitude(), 2)
                + Math.pow(a.longitude() - b.longitude(), 2)));
    }
    
    public static Point movePoint (Point start, double distance, double angleDegrees) {
        
        var radians = Math.toRadians(angleDegrees);
        var newLatitude = start.latitude() + distance * Math.cos(radians);
        var newLongitide = start.longitude() + distance * Math.sin(radians);
        
        return Point.fromLngLat(newLongitide, newLatitude);
    }
    
    public static int findNearestPoint (Point a, ArrayList<Point> points) {
        
        var minDistance = distance(a, points.get(0));
        int index = 0;
        
        for (int i = 1; i < points.size(); i++) {
           if (distance(a, points.get(i)) < minDistance) {
               minDistance = distance(a, points.get(i));
               index = i;
           }
        }
        
        return index;
    }
    
    public static boolean intersectPolygon (Point start, Point end, Polygon polygon) {
        
        var intersect = false;
        var polygonPoints = polygon.coordinates().get(0);
        
        for (int i = 0; i < polygonPoints.size(); i++) {
            
            intersect = Line2D.linesIntersect(
                    start.latitude(), 
                    start.longitude(),
                    end.latitude(), 
                    end.longitude(), 
                    polygonPoints.get(i).latitude(), 
                    polygonPoints.get(i).longitude(), 
                    polygonPoints.get((i + 1) % polygonPoints.size()).latitude(), 
                    polygonPoints.get((i + 1) % polygonPoints.size()).longitude());
                
            if (intersect) {
                break;
            }
        }
        
        return intersect;
    }
    
}
