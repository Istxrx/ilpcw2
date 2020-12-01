package uk.ac.ed.inf.aqmaps;

import java.util.ArrayList;
import com.mapbox.geojson.Point;

/**
 * Provides functionality to determine and possibly minimize a Hamiltonian cycle for a set of nodes
 * represented as points in an Euclidian 2D space.
 */
public class Graph {
    
    /**
     * Records the straight line distances between every pair of points. distanceMatrix[x][y] is the
     * distance between the two points x and y.
     */
    private double[][] distanceMatrix;
    /**
     * Records the order in which the nodes of the graph are visited. The first node is the
     * beginning and the end and its position is never changed. visitOrder[3] gives the index of a
     * node that is to be visited as 4th.
     */
    private int[] visitOrder;

    public Graph(ArrayList<Point> nodes) {

        this.distanceMatrix = new double[nodes.size()][nodes.size()];
        this.visitOrder = new int[nodes.size()];

        for (int i = 0; i < nodes.size(); i++) {
            for (int j = 0; j < nodes.size(); j++) {
                this.distanceMatrix[i][j] = Utils2D.distance(nodes.get(i), nodes.get(j));
            }
            this.visitOrder[i] = i;
        }
    }
    
    /**
     * @return current visit order of the nodes in this graph
     */
    public int[] getVisitOrder() {
        return this.visitOrder;
    }

    /**
     * Sets the visit order for the nodes in this graph so that at each step the next node that is
     * visited is the closest one from yet unvisited nodes.
     */
    public void toGreedyOrder() {

        for (int i = 1; i < this.visitOrder.length; i++) {
            var current = visitOrder[i - 1];
            var nearest = visitOrder[i];
            var minDistance = this.distanceMatrix[current][nearest];

            for (int j = i + 1; j < this.distanceMatrix.length; j++) {
                var candidate = visitOrder[j];
                var distance = this.distanceMatrix[current][candidate];
                
                if (distance < minDistance) {
                    minDistance = distance;
                    nearest = candidate;
                    this.visitOrder[j] = this.visitOrder[i];
                    this.visitOrder[i] = nearest;
                }
            }
        }
    }
    
    /**
     * Reverses a specified part of the visit order of this graph
     * 
     * @param startIndex the index of node in visit order that is the start of the part that we want
     *                   to reverse
     * @param endIndex   the index of node in visit order that is the end of the part that we want
     *                   to reverse
     */
    private void reversePartOfVisitOrder(int startIndex, int endIndex) {

        for (int i = 0; i <= (endIndex - startIndex) / 2; i++) {
            var temp = this.visitOrder[startIndex + i];
            this.visitOrder[startIndex + i] = this.visitOrder[endIndex - i];
            this.visitOrder[endIndex - i] = temp;
        }
    }
    
    /**
     * Attempts to optimize the current visit order of this graph by looking for a parts of the
     * visit order that if reversed result in a lower length of Hamiltonian cycle.
     * 
     * @param numberOfTries the maximum number of times the optimization is attempted
     */
    public void swapOptimizeOrder(int numberOfTries) {

        var optimized = true;
        int count = 0;
        
        while (count < numberOfTries && optimized) {
            optimized = false;
            
            for (int i = 1; i < this.visitOrder.length - 1; i++) {
                for (int j = i + 1; j < this.visitOrder.length; j++) {

                    var previousToStartNode = this.visitOrder[i - 1];
                    var startNode = this.visitOrder[i];
                    var endNode = this.visitOrder[j];
                    var nextToEndNode = this.visitOrder[(j + 1) % this.visitOrder.length];

                    var currentDistances = this.distanceMatrix[previousToStartNode][startNode]
                            + this.distanceMatrix[endNode][nextToEndNode];
                    var swappedDistances = this.distanceMatrix[previousToStartNode][endNode]
                            + this.distanceMatrix[startNode][nextToEndNode];

                    if (swappedDistances < currentDistances) {
                        this.reversePartOfVisitOrder(i, j);
                        optimized = true;
                    }
                }
            }
            count++;
        }
    }

}
