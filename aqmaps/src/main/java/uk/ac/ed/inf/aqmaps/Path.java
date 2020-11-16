package uk.ac.ed.inf.aqmaps;

import java.util.ArrayList;

import com.mapbox.geojson.Point;

public class Path {
    private ArrayList<Point> points;
    private ArrayList<Integer> moveDirections;
    
    public Path(ArrayList<Point> points, ArrayList<Integer> moveDirections) {
        this.points = points;
        this.moveDirections = moveDirections;
    }

    public ArrayList<Point> getPoints() {
        return points;
    }

    public ArrayList<Integer> getMoves() {
        return moveDirections;
    }
    
    public Point getEndPoint() {
        return points.get(points.size() - 1);
    }


}
