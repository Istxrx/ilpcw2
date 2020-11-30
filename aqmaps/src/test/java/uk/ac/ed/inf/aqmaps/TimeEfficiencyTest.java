package uk.ac.ed.inf.aqmaps;



public class TimeEfficiencyTest {
    
    private static void appTime () {

        var args = new String[] {"04", "08", "2021", "55.9444", "-3.1878", "5678", "80"};
        
        var startTime = System.nanoTime() / 1000000;
        App.main(args);
        var stopTime = System.nanoTime() / 1000000;
        System.out.println(stopTime - startTime + " miliseconds");
    }
    
    public static void main(String[] args) {
        appTime();
    }
}
