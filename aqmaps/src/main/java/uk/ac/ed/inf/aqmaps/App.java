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

public class App {

    // Constant bounds for values of latitudes and longitudes with respect to cardinal directions
    private static final double BOUND_LATITUDE_NORTH = 55.946233;
    private static final double BOUND_LATITUDE_SOUTH = 55.942617;
    private static final double BOUND_LONGITUDE_EAST = -3.184319;
    private static final double BOUND_LONGITUDE_WEST = -3.192473;

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

    private static ArrayList<Polygon> loadNoFlyZonesFromURL(String url) {

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
    
    public static Drone initiate(String day, String month, String year, double startLatitude, double startLongitude, String port) {
        
        var start = Point.fromLngLat(startLongitude, startLatitude);

        var boundPoints = new ArrayList<Point>();
        boundPoints.add(Point.fromLngLat(BOUND_LONGITUDE_WEST, BOUND_LATITUDE_NORTH));
        boundPoints.add(Point.fromLngLat(BOUND_LONGITUDE_EAST, BOUND_LATITUDE_NORTH));
        boundPoints.add(Point.fromLngLat(BOUND_LONGITUDE_EAST, BOUND_LATITUDE_SOUTH));
        boundPoints.add(Point.fromLngLat(BOUND_LONGITUDE_WEST, BOUND_LATITUDE_SOUTH));
        var confinementArea = (Polygon.fromLngLats(List.of(boundPoints)));

        var noFlyZonesUrl = "http://localhost:" + port + "/buildings/no-fly-zones.geojson";
        var noflyZones = loadNoFlyZonesFromURL(noFlyZonesUrl);

        var sensorsUrl = "http://localhost:" + port + "/maps/" + year + "/" + month + "/" + day
                + "/air-quality-data.json";
        var sensors = AirQualitySensor.loadListFromURL(sensorsUrl, port);

        noflyZones.add(confinementArea);

        var drone = new Drone(start, noflyZones, sensors);
        drone.executeReadingRoutine();;

        return drone;
    }

    public static void main(String[] args) {
        var day = args[0];
        var month = args[1];
        var year = args[2];
        var startLatitude = Double.parseDouble(args[3]);
        var startLongitude = Double.parseDouble(args[4]);
        var seed = args[5];
        var port = args[6];

        var start = Point.fromLngLat(startLongitude, startLatitude);

        var boundPoints = new ArrayList<Point>();
        boundPoints.add(Point.fromLngLat(BOUND_LONGITUDE_WEST, BOUND_LATITUDE_NORTH));
        boundPoints.add(Point.fromLngLat(BOUND_LONGITUDE_EAST, BOUND_LATITUDE_NORTH));
        boundPoints.add(Point.fromLngLat(BOUND_LONGITUDE_EAST, BOUND_LATITUDE_SOUTH));
        boundPoints.add(Point.fromLngLat(BOUND_LONGITUDE_WEST, BOUND_LATITUDE_SOUTH));
        var confinementArea = (Polygon.fromLngLats(List.of(boundPoints)));

        var noFlyZonesUrl = "http://localhost:" + port + "/buildings/no-fly-zones.geojson";
        var noflyZones = loadNoFlyZonesFromURL(noFlyZonesUrl);

        var sensorsUrl = "http://localhost:" + port + "/maps/" + year + "/" + month + "/" + day
                + "/air-quality-data.json";
        var sensors = AirQualitySensor.loadListFromURL(sensorsUrl, port);

        /*
         * var features = new ArrayList<Feature>();
         * 
         * for (Polygon p : nfz) { var feature = Feature.fromGeometry((Geometry) p);
         * feature.addStringProperty("fill", "#ff0000");; features.add(feature); }
         */
        noflyZones.add(confinementArea);

        var drone = new Drone(start, noflyZones, sensors);
        drone.executeReadingRoutine();

        var flightPathLog = drone.getFlightPathLog();
        var fileName = "flightpath-" + day + "-" + month + "-" + year + ".txt";
        createAndWriteFile(fileName, flightPathLog);
        System.out.println("Finished routine on " + day + "/" + month + "/" + year + " with "
                + drone.getMoveCount() + " moves");

        var feature = drone.getFlightPathAsFeature();
        var features = drone.getReadingsAsFeatures();
        features.add(feature);

        var featureCollection = FeatureCollection.fromFeatures(features);
        var jsonString = featureCollection.toJson();
        fileName = "readings-" + day + "-" + month + "-" + year + ".geojson";
        createAndWriteFile(fileName, jsonString);
    }

}
