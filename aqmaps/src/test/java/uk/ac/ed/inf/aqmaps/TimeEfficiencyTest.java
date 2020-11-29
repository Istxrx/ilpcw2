package uk.ac.ed.inf.aqmaps;



public class TimeEfficiencyTest {
    
    private static void droneTime () {

        var args = new String[] {"02", "02", "2021", "55.9444", "-3.1878", "5678", "80"};
        
        long startTime = System.nanoTime() / 1000000;
        App.main(args);
        long stopTime = System.nanoTime() / 1000000;
        System.out.println(stopTime - startTime + " miliseconds");
    }
    
    public static void main(String[] args) {
        droneTime();
    }
}
