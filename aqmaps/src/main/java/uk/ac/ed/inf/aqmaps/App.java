package uk.ac.ed.inf.aqmaps;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Type;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;



public class App {
    
    //Constant bounds for values of latitudes and longitudes with respect to cardinal directions
    private static final BigDecimal BOUND_LATITUDE_NORTH = new BigDecimal("55.946233");
    private static final BigDecimal BOUND_LATITUDE_SOUTH = new BigDecimal("55.942617");
    private static final BigDecimal BOUND_LONGITUDE_EAST = new BigDecimal("-3.184319");
    private static final BigDecimal BOUND_LONGITUDE_WEST = new BigDecimal("-3.192473");
    
    public static String readStringFromURL(String url) {
        
        // Create a new HttpClient with default settings.
        var client = HttpClient.newHttpClient();
        
        // HttpClient assumes that it is a GET request by default.
        var request = HttpRequest.newBuilder().uri(URI.create(url)).build();
        
        try {
            // The response object is of class HttpResponse<String>
            var response = client.send(request, BodyHandlers.ofString());
            //System.out.println(response.statusCode());
            return(response.body());
            
        } catch (Exception e) {
            System.out.println("Fatal error: Unable to connect to " + url + ".");
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
    
    private static void createAndWriteFile (String fileName, String content) {

        try {
            var file = new File(fileName);
            file.createNewFile();
            var writer = new FileWriter(fileName);
            writer.write(content);
            writer.close();

        } catch (Exception e) {
            System.out.println("Error in createAndWriteFile.");
            e.printStackTrace();
        }
    }
    
    //Assigns color to corresponding value of pollution
    private static String pollutionValueColor (double pollutionValue) {

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
       
    public static void main(String[] args) {
        
        var nfz = loadNoFlyZonesFromURL("http://localhost:80/buildings/no-fly-zones.geojson");
        
        var aqsensors = AirQualitySensor.loadListFromURL("http://localhost:80/maps/2020/01/01/air-quality-data.json");
        System.out.println(aqsensors.get(9).toString());
                       
        var features = new ArrayList<Feature>();

        for (AirQualitySensor sensor : aqsensors) {
            features.add(Feature.fromGeometry((Geometry) sensor.getLocation().toPoint()));
        }
        
        for (int i = 0; i < features.size(); i++) {
            features.get(i).addStringProperty("marker-size", "medium");
            features.get(i).addStringProperty("location",aqsensors.get(i).getLocation().getWords());
            features.get(i).addStringProperty("rgb-string", pollutionValueColor(aqsensors.get(i).getReading()));
            features.get(i).addStringProperty("marker-color", pollutionValueColor(aqsensors.get(i).getReading()));
            features.get(i).addStringProperty("marker-symbol", "lighthouse");
        }
        
        for (Polygon p : nfz) {
            var feature = Feature.fromGeometry((Geometry) p);
            feature.addStringProperty("fill", "#ff0000");;
            features.add(feature);
        }
        
        var start = Point.fromLngLat(-3.1898, 55.9450);
        
        var drone = new Drone(start, nfz, aqsensors);
        drone.collectReadings(33);   
        System.out.println("OK");
       
        var f = drone.getFlightPath();
        features.add(f);
        
        var featureCollection = FeatureCollection.fromFeatures(features);
        createAndWriteFile("heatmap.geojson", featureCollection.toJson());
        
        
       
    }

    /*public static void main(String[] args) {
        
        Gson gson =
                new GsonBuilder()
                .registerTypeAdapter(What3Words.class, new W3WDeserializer())
                .create();
        
        var jsonString = App.readStringFromURL("http://localhost:80/maps/2020/01/01/air-quality-data.json");
        Type listType = new TypeToken<ArrayList<AirQualitySensor>>(){}.getType();
        ArrayList<AirQualitySensor> sensors = gson.fromJson(jsonString, listType);
        
        System.out.println(sensors.get(0).toString());
    
    }*/
}
