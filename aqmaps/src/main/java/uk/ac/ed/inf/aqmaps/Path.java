package uk.ac.ed.inf.aqmaps;

import java.util.ArrayList;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;

/**
 * Encapsulates sequence of moves as both-ways linked list structure. Provides functionality to
 * search for a path to a target point in 2D space.
 */
public class Path {
    

    //Used as a weight in heuristic function
    private static final double EPSILON = 2.0;
    
    private Point point;
    private Integer usedDirection;
    private double length;

    private Path previous;
    private Path next;

    private Path(Point point, Integer moveDirection, double length, Path previous,
            Path next) {

        this.point = point;
        this.usedDirection = moveDirection;
        this.length = length;
        this.previous = previous;
        this.next = next;
    }

    public Path(Point startPoint) {
        this(startPoint, null, 0, null, null);
    }

    /**
     * Assumes this is reference to start of the path and lists all move directions used in it
     * onwards.
     * 
     * @return list of move directions that in this path
     */
    public ArrayList<Integer> getMoveDirections() {
        
        var moveDirections = new ArrayList<Integer>();
        var path = this;
        
        // Loop through the path until we reach the end
        while (path.next != null) {
            path = path.next;
            moveDirections.add(path.usedDirection);
        }
        return moveDirections;
    }

    /**
     * Assumes this is reference to start of the path and lists all points in it onwards.
     * 
     * @return list of points that are in this path
     */
    public ArrayList<Point> getPoints() {
        
        var points = new ArrayList<Point>();
        var path = this;

        // Loop through the path until we reach the end
        while (true) {
            points.add(path.point);
            if (path.next != null) {
                path = path.next;
            } else {
                break;
            }
        }
        return points;
    }

    /**
     * @return the first point in this path
     */
    public Point getStartPoint() {
        
        var path = this;
        
        // Loop through the path until we reach the start
        while (path.previous != null) {
            path = path.previous;
        }
        return path.point;
    }

    /**
     * Reconstructs the path from end to start.
     * 
     * @return reference to the start of this path
     */
    private Path getStart() {
        
        var path = this;
        
        // Loop through the path until we reach the start
        while (path.previous != null) {
            path.previous.next = path;
            path = path.previous;
        }
        return path;
    }

    /**
     * Extends the end of this path by a new move. Does not keep track of length.
     *
     * @param end       the new end point of the path
     * @param direction the direction that was used to get to this point
     */
    public void addMove(Point end, int direction) {
        
        var path = this;
        
        // Loop through the path until we reach the end
        while (path.next != null) {
            path = path.next;
        }
        // Insert the new end
        var next = new Path(end, direction, 0, path, null);
        path.next = next;
    }

    /**
     * @return line string feature representing the current path from here onwards
     */
    public Feature toFeature() {
        
        var lineString = LineString.fromLngLats(this.getPoints());
        var feature = Feature.fromGeometry((Geometry) lineString);

        return feature;
    }

    /**
     * Finds legal continuations of this path. Legal continuation can not cross obstacle and is
     * result of moving this point attribute by move length in a one of directions.
     * 
     * @param moveLength the length of each move in each path
     * @param directions the list of directions in degrees that will be explored
     * @param obstacles  the list of polygons that should not be crossed
     * @return list of legal continuations of this path
     */
    private ArrayList<Path> findBranches(double moveLength, ArrayList<Integer> directions,
            ArrayList<Polygon> obstacles) {
        
        var branches = new ArrayList<Path>();
        var start = this.point;

        // Finds the end points resulting from moving the current point in the given directions by a
        // given length
        for (Integer direction : directions) {
            var end = Utils2D.movePoint(start, moveLength, direction);

            // If the line between start and end point intersects any of the obstacles, this
            // continuation of the path is not involved in output
            if (!Utils2D.lineIntersectPolygons(start, end, obstacles)) {
                var branch = new Path(end, direction, this.length + moveLength, this, null);
                branches.add(branch);
            }
        }
        return branches;
    }

    /**
     * Assigns value based on length of this path and estimated distance to target point.
     *
     * @param target  the point that the path should lead to
     * @param epsilon the weight, epsilon > 1 favors path length over distance to target
     * @return heuristic value = path length + epsilon * distance to target point
     */
    private double weightedHeuristicValue(Point target, double epsilon) {
        
        var pathLength = this.length;
        var remainingDistance = Utils2D.distance(this.point, target);

        return pathLength + epsilon * remainingDistance;
    }

    /**
     * Chooses the best path according to heuristic function
     * 
     * @param paths  the paths to choose from
     * @param target the point that each path should lead to
     * @return path from the list that has the lowest (best) heuristic value
     */
    private static Path chooseBestPath(ArrayList<Path> paths, Point target) {
        
        var bestPath = paths.get(0);
        var bestHeuristicValue = bestPath.weightedHeuristicValue(target, EPSILON);
        
        // Finds the minimum heuristic value and corresponding path
        for (Path path : paths) {
            var heuristicValue = path.weightedHeuristicValue(target, EPSILON);

            if (heuristicValue < bestHeuristicValue) {
                bestPath = path;
                bestHeuristicValue = heuristicValue;
            }
        }
        return bestPath;
    }

    /**
     * Performs an variation of A* search.
     *
     * @param start      the start point of the path
     * @param target     target the point that the path should lead to
     * @param range      the range around the target within which the path should end
     * @param moveLength the length of each move in the path
     * @param directions the list of allowed directions for the moves in degrees
     * @param obstacles  the list of polygons that should not be crossed
     * @return path to the target point within range while avoiding obstacles
     */
    public static Path findPathToPoint(Point start, Point target, double range, double moveLength,
            ArrayList<Integer> directions, ArrayList<Polygon> obstacles) {

        var startingPath = new Path(start);
        // The search space
        var paths = startingPath.findBranches(moveLength, directions, obstacles);
        
        // Terminates if there is no path left to explore = there is no path to target
        while (paths.size() > 0) {
            // Choose the most promising path to reach the target 
            var path = chooseBestPath(paths, target);

            // If the target is reached, returns the start of the path that it found
            if (Utils2D.distance(path.point, target) < range) {
                return path.getStart();
            }
            // The most promising path did not yet reach the target so it is expanded and its
            // continuations are added to the search space
            paths.addAll(path.findBranches(moveLength, directions, obstacles));
            paths.remove(path);
        }
        return null;
    }
}
