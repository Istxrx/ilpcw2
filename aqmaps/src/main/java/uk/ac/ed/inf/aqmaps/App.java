package uk.ac.ed.inf.aqmaps;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.FileWriter;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;

/**
 * Provides functionality for obtaining data from server and producing output files as well as some
 * utility functions for visualization of the data collected by the drone. Also handles the
 * preparation of data for a drone.
 */
public class App {

    // Constant bounds for values of latitudes and longitudes with respect to cardinal directions
    private static final double BOUND_LATITUDE_NORTH = 55.946233;
    private static final double BOUND_LATITUDE_SOUTH = 55.942617;
    private static final double BOUND_LONGITUDE_EAST = -3.184319;
    private static final double BOUND_LONGITUDE_WEST = -3.192473;

    /**
     * Connects to and copies the content of a URL address.
     * 
     * @param url an URL address of the server
     * @return string representing the content of the address
     */
    public static String readStringFromURL(String url) {

        // Create a new HttpClient with default settings.
        var client = HttpClient.newHttpClient();
        // HttpClient assumes that it is a GET request by default.
        var request = HttpRequest.newBuilder().uri(URI.create(url)).build();

        try {
            var response = client.send(request, BodyHandlers.ofString());
            return (response.body());
            
        } catch (Exception e) {
            System.out.println("Fatal error: Unable to connect to " + url + ".");
            e.printStackTrace();
            System.exit(1);
        }
        return null;
    }

    /**
     * Loads a list of polygons from a .geojson file on a server.
     * 
     * @param url an URL address of the server
     * @return list of polygons obtained from the server
     */
    private static ArrayList<Polygon> loadPolygonsFromURL(String url) {

        var geoJsonString = readStringFromURL(url);
        var featureCollection = FeatureCollection.fromJson(geoJsonString);
        var features = featureCollection.features();
        var polygons = new ArrayList<Polygon>();

        for (var feature : features) {
            var geometry = feature.geometry();
            polygons.add((Polygon) geometry);
        }
        return polygons;
    }
    
    /**
     * Creates and writes to a file in the current working directory.
     * 
     * @param fileName the name including extension of the file that is to be created
     * @param content  the text that will be written to the file
     */
    private static void createAndWriteFile(String fileName, String content) {

        try {
            var file = new File(fileName);
            file.createNewFile();
            var writer = new FileWriter(fileName);
            writer.write(content);
            writer.close();

        } catch (Exception e) {
            System.out.println("Error in createAndWriteFile.");
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Assigns color to corresponding value of pollution
     * 
     * @param pollutionValue the value of air pollution
     * @return the hexadecimal code of a color
     */
    public static String pollutionColor(double pollutionValue) {

        if (0 <= pollutionValue && pollutionValue < 32) {
            return "#00ff00";
        }
        if (32 <= pollutionValue && pollutionValue < 64) {
            return "#40ff00";
        }
        if (64 <= pollutionValue && pollutionValue < 96) {
            return "#80ff00";
        }
        if (96 <= pollutionValue && pollutionValue < 128) {
            return "#c0ff00";
        }
        if (128 <= pollutionValue && pollutionValue < 160) {
            return "#ffc000";
        }
        if (160 <= pollutionValue && pollutionValue < 192) {
            return "#ff8000";
        }
        if (192 <= pollutionValue && pollutionValue < 224) {
            return "#ff4000";
        }
        if (224 <= pollutionValue && pollutionValue < 256) {
            return "#ff0000";
        }
        return null;
    }

    /**
     * Assigns symbol to corresponding value of pollution
     * 
     * @param pollutionValue the value of air pollution
     * @return the name of a symbol
     */
    public static String pollutionSymbol(double pollutionValue) {

        if (0 <= pollutionValue && pollutionValue < 128) {
            return "lighthouse";
        }
        if (128 <= pollutionValue && pollutionValue < 256) {
            return "danger";
        }
        return null;
    }
    
    /**
     * Loads data from server, puts it in the right format and forwards it to the drone.
     * 
     * @param day            the day for which the data is obtained from server
     * @param month          the month for which the data is obtained from server
     * @param year           the year for which the data is obtained from server
     * @param startLatitude  the latitude of the starting point
     * @param startLongitude the longitude of the starting point
     * @param port           port the port at which the connection to server is established
     * @return drone loaded with the data it needs to complete the routine
     */
    // Should be done here since the Drone class does not handle loading data from server
    public static Drone initiateDrone(String day, String month, String year, double startLatitude,
            double startLongitude, String port) {
        
        // The starting location of the drone
        var start = Point.fromLngLat(startLongitude, startLatitude);
        
        // Creates the confinement area as a polygon
        var boundPoints = new ArrayList<Point>();
        boundPoints.add(Point.fromLngLat(BOUND_LONGITUDE_WEST, BOUND_LATITUDE_NORTH));
        boundPoints.add(Point.fromLngLat(BOUND_LONGITUDE_EAST, BOUND_LATITUDE_NORTH));
        boundPoints.add(Point.fromLngLat(BOUND_LONGITUDE_EAST, BOUND_LATITUDE_SOUTH));
        boundPoints.add(Point.fromLngLat(BOUND_LONGITUDE_WEST, BOUND_LATITUDE_SOUTH));
        var confinementArea = (Polygon.fromLngLats(List.of(boundPoints)));
        
        // Loads the no fly zones from server
        var noFlyZonesUrl = "http://localhost:" + port + "/buildings/no-fly-zones.geojson";
        var noflyZones = loadPolygonsFromURL(noFlyZonesUrl);
        
        // The drone will never cross the borders of any polygon in no-fly zones, if started inside,
        // it will never leave the polygon
        noflyZones.add(confinementArea);

        var sensorsUrl = "http://localhost:" + port + "/maps/" + year + "/" + month + "/" + day
                + "/air-quality-data.json";
        var sensors = AirQualitySensor.loadListFromURL(sensorsUrl, port);
        
        // Pass the data to the drone
        var drone = new Drone(start, noflyZones, sensors);

        return drone;
    }

    public static void main(String[] args) {
        var day = args[0];
        var month = args[1];
        var year = args[2];
        var startLatitude = Double.parseDouble(args[3]);
        var startLongitude = Double.parseDouble(args[4]);
        // var seed = args[5];
        var port = args[6];
        /*
         * var features = new ArrayList<Feature>();
         * 
         * for (Polygon p : nfz) { var feature = Feature.fromGeometry((Geometry) p);
         * feature.addStringProperty("fill", "#ff0000");; features.add(feature); }
         */
        
        // Initializes and feeds data to the drone
        var drone = initiateDrone(day, month, year, startLatitude, startLongitude, port);
        // Starts the routine
        drone.executeReadingRoutine();
        System.out.println("Finished routine on " + day + "/" + month + "/" + year + " with "
                + drone.getMoveCount() + " moves");
        
        // Creates the flight path log output file
        var flightPathLog = drone.getFlightPathLog();
        var fileName = "flightpath-" + day + "-" + month + "-" + year + ".txt";
        createAndWriteFile(fileName, flightPathLog);
        
        // Creates a visualization of flight path and sensor readings as a geojson file
        var feature = drone.getFlightPathAsFeature();
        var features = drone.getReadingsAsFeatures();
        features.add(feature);
        var featureCollection = FeatureCollection.fromFeatures(features);
        var jsonString = featureCollection.toJson();
        fileName = "readings-" + day + "-" + month + "-" + year + ".geojson";
        createAndWriteFile(fileName, jsonString);
    }

}
