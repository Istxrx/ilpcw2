package uk.ac.ed.inf.aqmaps;

import java.awt.geom.Line2D;
import java.util.ArrayList;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;

/**
 * Provides some basic functions for points and lines in 2D Euclidian space.
 */
public class Utils2D {
    
    /**
     * @param a the first point
     * @param b the second point
     * @return Euclidian distance between the 2 points
     */
    public static double distance(Point a, Point b) {

        return (Math.sqrt(Math.pow(a.latitude() - b.latitude(), 2)
                + Math.pow(a.longitude() - b.longitude(), 2)));
    }

    /**
     * Moves the point.
     * 
     * @param point         the point that is moved
     * @param distance      the distance by which the point is moved
     * @param angleDegrees  the angle in which the point is moved
     * @return result point after the move
     */
    public static Point movePoint(Point point, double distance, double angleDegrees) {

        var radians = Math.toRadians(angleDegrees);
        var newLatitude = point.latitude() + distance * Math.sin(radians);
        var newLongitide = point.longitude() + distance * Math.cos(radians);
        var movedPoint = Point.fromLngLat(newLongitide, newLatitude);

        return movedPoint;
    }
    
    /**
     * Determines if a line segment intersects a polygon in 2D Euclidian space.
     * 
     * @param start   the start point of a line segment
     * @param end     the end point of a line segment
     * @param polygon the polygon that is checked for intersection
     * @return true if the line segment intersects the polygon, false otherwise
     */
    public static boolean lineIntersectPolygon(Point start, Point end, Polygon polygon) {

        var intersect = false;
        var polygonPoints = polygon.coordinates().get(0);

        for (int i = 0; i < polygonPoints.size(); i++) {
            intersect = Line2D.linesIntersect(start.latitude(), start.longitude(), 
                    end.latitude(), end.longitude(), 
                    polygonPoints.get(i).latitude(),polygonPoints.get(i).longitude(),
                    polygonPoints.get((i + 1) % polygonPoints.size()).latitude(),
                    polygonPoints.get((i + 1) % polygonPoints.size()).longitude());
            
            if (intersect) {
                break;
            }
        }
        return intersect;
    }

    /**
     * Determines if a line segment intersects any of the polygons in 2D Euclidian space.
     * 
     * @param start    the start point of a line segment
     * @param end      the end point of a line segment
     * @param polygons the polygons that are checked for intersection
     * @return true if the line segment intersects any of the polygons in the list, false otherwise
     */
    public static boolean lineIntersectPolygons(Point start, Point end,
            ArrayList<Polygon> polygons) {

        var intersect = false;

        for (var polygon : polygons) {
            intersect = lineIntersectPolygon(start, end, polygon);
            if (intersect) {
                break;
            }
        }
        return intersect;
    }

}
