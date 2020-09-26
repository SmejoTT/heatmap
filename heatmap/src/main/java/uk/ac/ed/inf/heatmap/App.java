package uk.ac.ed.inf.heatmap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;

public class App {
    
    private static final int GRID_ROWS = 10;
    private static final int GRID_COLS = 10;
    
    private static final String GREEN = "#00ff00";
    private static final String MEDIUM_GREEN = "#40ff00";
    private static final String LIGHT_GREEN = "#80ff00";
    private static final String LIME_GREEN = "#c0ff00";
    private static final String GOLD = "#ffc000";
    private static final String ORANGE = "#ff8000";
    private static final String RED_ORANGE = "#ff4000";
    private static final String RED = "#ff0000";
    
    
    
    private static Integer[][] predictionValues = new Integer[GRID_ROWS][GRID_COLS];
    private static String heatmapJson; 
    
    

    private static void readFile(String fileName) throws FileNotFoundException {
        File predictions = new File(fileName);
        Scanner scanner = new Scanner(predictions);

        int iterator = 0;

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String[] splitValues = line.split(",");
            for (int i = 0; i < splitValues.length; i++) {
                predictionValues[iterator][i]=Integer.parseInt(splitValues[i].trim()); 
            }
            iterator++;
        }
        scanner.close();   
    }
    
    private static String getPolygonColor(int col,int row) {
        if ((predictionValues[row][col] >= 0) && (predictionValues[row][col] < 32) ) {
            return GREEN;
        }
        
        if ((predictionValues[row][col] >= 32) && (predictionValues[row][col] < 64) ) {
            return MEDIUM_GREEN;
        }
        
        if ((predictionValues[row][col] >= 64) && (predictionValues[row][col] < 96) ) {
            return LIGHT_GREEN;
        }
        
        if ((predictionValues[row][col] >= 96) && (predictionValues[row][col] < 128) ) {
            return LIME_GREEN;
        }
        
        if ((predictionValues[row][col] >= 128) && (predictionValues[row][col] < 160) ) {
            return GOLD;
        }
        
        if ((predictionValues[row][col] >= 160) && (predictionValues[row][col] < 192) ) {
            return ORANGE;
        }
        
        if ((predictionValues[row][col] >= 192) && (predictionValues[row][col] < 224) ) {
            return RED_ORANGE;
        }
        
        if ((predictionValues[row][col] >= 224) && (predictionValues[row][col] < 256) ) {
            return RED;
        }
        
        return "";
    }

    private static void createHeatmapJson() {
        BigDecimal eastBound = new BigDecimal("-3.184319");
        BigDecimal westBound = new BigDecimal("-3.192473");
        BigDecimal northBound = new BigDecimal("55.946233");
        BigDecimal southBound = new BigDecimal("55.942617");

        BigDecimal longStep = (eastBound.subtract(westBound)).divide(BigDecimal.TEN);
        BigDecimal latStep = (southBound.subtract(northBound)).divide(BigDecimal.TEN);
        System.out.println(longStep);
        System.out.println(latStep);
        
        
        List<Feature> listOfFeatures= new ArrayList<>();
        
        for (int row = 0; row < GRID_ROWS; row++) {
            for (int col = 0; col < GRID_COLS; col++) {
                
                List<Point> points = new ArrayList<>();
                double longitude;
                double latitude;
                //Compute LONGITUDE LATITUDE for each POINT separately
                
                longitude = westBound.add(new BigDecimal(col).multiply(longStep)).doubleValue();
                latitude = northBound.add(new BigDecimal(row).multiply(latStep)).doubleValue();
                Point topLeft = Point.fromLngLat(longitude, latitude);
                
                longitude = westBound.add(new BigDecimal(col+1).multiply(longStep)).doubleValue();
                latitude = northBound.add(new BigDecimal(row).multiply(latStep)).doubleValue();
                Point topRight = Point.fromLngLat(longitude, latitude);
                
                longitude = westBound.add(new BigDecimal(col+1).multiply(longStep)).doubleValue();
                latitude = northBound.add(new BigDecimal(row+1).multiply(latStep)).doubleValue();
                Point bottomRight = Point.fromLngLat(longitude, latitude);
                
                longitude = westBound.add(new BigDecimal(col).multiply(longStep)).doubleValue();
                latitude = northBound.add(new BigDecimal(row+1).multiply(latStep)).doubleValue();
                Point bottomLeft = Point.fromLngLat(longitude, latitude);
                
                points.add(topLeft);
                points.add(topRight);
                points.add(bottomRight);
                points.add(bottomLeft);
                
                Polygon polygon = Polygon.fromLngLats(List.of(points));
                
                Feature feature = Feature.fromGeometry(polygon);
                feature.addNumberProperty("opacity", 0.75);
                feature.addStringProperty("rgb-string", getPolygonColor(col, row));
                feature.addStringProperty("fill", getPolygonColor(col, row));
                
                listOfFeatures.add(feature);   
            }   
        }
        FeatureCollection featureCollection = FeatureCollection.fromFeatures(listOfFeatures);
        heatmapJson = featureCollection.toJson();
    }
    
    private static void outputHeatmapFile() {
        try {
            File heatmap = new File("heatmap.geojson");
            heatmap.createNewFile();
            FileWriter writer = new FileWriter("heatmap.geojson");
            writer.write(heatmapJson);
            writer.close();
        } catch (IOException e) {
            System.out.println("An error has occurred.");
            e.printStackTrace();
        }    
    }
    
    

    public static void main(String[] args) throws FileNotFoundException {
        readFile(args[0]);
        createHeatmapJson();
        outputHeatmapFile();
    }
}
