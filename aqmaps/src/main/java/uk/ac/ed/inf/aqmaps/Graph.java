package uk.ac.ed.inf.aqmaps;

import java.util.ArrayList;

import com.mapbox.geojson.Point;

public class Graph {
    
    private double[][] distanceMatrix;
    private int[] visitOrder;
    
    public Graph (ArrayList<Point> nodes) {
        
        this.distanceMatrix = new double[nodes.size()][nodes.size()];
        this.visitOrder = new int[nodes.size()];
        
        for (int i = 0; i < nodes.size(); i++) {
            for (int j = 0; j < nodes.size(); j++) {
                this.distanceMatrix[i][j] = Utils2D.distance(nodes.get(i), nodes.get(j)); 
                //System.out.print(this.distanceMatrix[i][j] + ", ");
            }
            //System.out.println();
            this.visitOrder[i] = i;
        }
        
    }
    
    public int[] getVisitOrder() {
        return this.visitOrder;
    }
    
    public void swapOrder (int startIndex, int endIndex) {
               
        for (int i = 0; i <= (endIndex - startIndex) / 2; i++) {
            var temp = this.visitOrder[startIndex + i];
            this.visitOrder[startIndex + i] = this.visitOrder[endIndex - i];
            this.visitOrder[endIndex - i] = temp;
        }
        
    }
    
    public void greedyOrder () {       
       
       for (int i = 1; i < this.visitOrder.length; i++) {
           var current = visitOrder[i - 1];
           var nearest = visitOrder[i];
           var minDistance = this.distanceMatrix[current][nearest];
           
           for (int j = i + 1; j < this.distanceMatrix.length; j++) {
               var candidate = visitOrder[j];
               var distance = this.distanceMatrix[current][candidate];
               //System.out.println("Comparing " + current + " to " + candidate);
               if (distance < minDistance) {
                   //System.out.println("yes");
                   minDistance = distance;
                   nearest = candidate;
                   this.visitOrder[j] = this.visitOrder[i];
                   this.visitOrder[i] = nearest;
               }
           }
       }
    }
    
    public void swapOptimizeOrder () {
        
        for (int i = 1; i < this.visitOrder.length - 1; i++) {
            for (int j = i + 1; j < this.visitOrder.length; j++) {
                
                var currentDistances = this.distanceMatrix[this.visitOrder[i-1]][this.visitOrder[i]] 
                        + this.distanceMatrix[this.visitOrder[j]][this.visitOrder[(j + 1) % this.visitOrder.length]];
                
                var swappedDistances = this.distanceMatrix[this.visitOrder[i-1]][this.visitOrder[j]] 
                        + this.distanceMatrix[this.visitOrder[i]][this.visitOrder[(j + 1) % this.visitOrder.length]];
                        
                if (swappedDistances < currentDistances) {
                    this.swapOrder(i, j);
                }
            }
        }
    }

}
