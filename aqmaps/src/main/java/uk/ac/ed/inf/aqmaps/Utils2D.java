package uk.ac.ed.inf.aqmaps;

import java.awt.geom.Line2D;
import java.util.ArrayList;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;

public class Utils2D {

    public static double distance(Point a, Point b) {

        return (Math.sqrt(Math.pow(a.latitude() - b.latitude(), 2)
                + Math.pow(a.longitude() - b.longitude(), 2)));
    }

    public static Point movePoint(Point start, double distance, double angleDegrees) {

        var radians = Math.toRadians(angleDegrees);
        var newLatitude = start.latitude() + distance * Math.sin(radians);
        var newLongitide = start.longitude() + distance * Math.cos(radians);

        return Point.fromLngLat(newLongitide, newLatitude);
    }

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
