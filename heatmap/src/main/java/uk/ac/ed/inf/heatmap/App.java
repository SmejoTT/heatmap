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
    
    private static final BigDecimal EAST_BOUND = new BigDecimal("-3.184319");
    private static final BigDecimal WEST_BOUND = new BigDecimal("-3.192473");
    private static final BigDecimal NORTH_BOUND = new BigDecimal("55.946233");
    private static final BigDecimal SOUTH_BOUND = new BigDecimal("55.942617");
    
    private static final String GREEN = "#00ff00";
    private static final String MEDIUM_GREEN = "#40ff00";
    private static final String LIGHT_GREEN = "#80ff00";
    private static final String LIME_GREEN = "#c0ff00";
    private static final String GOLD = "#ffc000";
    private static final String ORANGE = "#ff8000";
    private static final String RED_ORANGE = "#ff4000";
    private static final String RED = "#ff0000";
    
    
    
    private static Integer[][] predictionValues = new Integer[GRID_ROWS][GRID_COLS];
    private static Point[][] gridPoints = new Point[GRID_ROWS+1][GRID_COLS+1];
    private static String heatmapJson; 
    
    

    private static void readFile(String fileName) throws FileNotFoundException {
        var predictionsFile = new File(fileName);
        var scanner = new Scanner(predictionsFile);

        var iterator = 0;

        while (scanner.hasNextLine()) {
            var line = scanner.nextLine();
            var valuesArray = line.split(",");
            for (int i = 0; i < valuesArray.length; i++) {
                predictionValues[iterator][i]=Integer.parseInt(valuesArray[i].strip()); 
            }
            iterator++;
        }
        scanner.close();   
    }
    
    private static String getPolygonColor(int col,int row) {
        var predictionValue = predictionValues[row][col];
        if ((predictionValue >= 0) && (predictionValue < 32) ) {
            return GREEN;
        }
        
        if ((predictionValue >= 32) && (predictionValue < 64) ) {
            return MEDIUM_GREEN;
        }
        
        if ((predictionValue >= 64) && (predictionValue < 96) ) {
            return LIGHT_GREEN;
        }
        
        if ((predictionValue >= 96) && (predictionValue < 128) ) {
            return LIME_GREEN;
        }
        
        if ((predictionValue >= 128) && (predictionValue < 160) ) {
            return GOLD;
        }
        
        if ((predictionValue >= 160) && (predictionValue < 192) ) {
            return ORANGE;
        }
        
        if ((predictionValue >= 192) && (predictionValue < 224) ) {
            return RED_ORANGE;
        }
        
        if ((predictionValue >= 224) && (predictionValue < 256) ) {
            return RED;
        }
        
        return null;
    }
    
    private static void computeGridPoints() {
        var longStep = (EAST_BOUND.subtract(WEST_BOUND)).divide(BigDecimal.TEN);
        var latStep = (SOUTH_BOUND.subtract(NORTH_BOUND)).divide(BigDecimal.TEN);
        for (int row = 0; row < GRID_ROWS+1; row++) {
            var latitude = NORTH_BOUND.add(new BigDecimal(row).multiply(latStep)).doubleValue();
            for (int col = 0; col < GRID_COLS+1; col++) {
                var longitude = WEST_BOUND.add(new BigDecimal(col).multiply(longStep)).doubleValue();
                gridPoints[row][col]= Point.fromLngLat(longitude, latitude);
            }
        }
    }

    private static void createHeatmapJson() {
        var listOfFeatures= new ArrayList<Feature>();
        
        for (int row = 0; row < GRID_ROWS; row++) {
            for (int col = 0; col < GRID_COLS; col++) {
                var topLeftPoint = gridPoints[row][col];
                var topRightPoint = gridPoints[row][col+1];
                var bottomRightPoint = gridPoints[row+1][col+1];
                var bottomLeftPoint = gridPoints[row+1][col];
                
                var listOfPoints = new ArrayList<Point>();
                listOfPoints.add(topLeftPoint);
                listOfPoints.add(topRightPoint);
                listOfPoints.add(bottomRightPoint);
                listOfPoints.add(bottomLeftPoint);
                var polygon = Polygon.fromLngLats(List.of(listOfPoints));
                
                var feature = Feature.fromGeometry(polygon);
                feature.addNumberProperty("opacity", 0.75);
                feature.addStringProperty("rgb-string", getPolygonColor(col, row));
                feature.addStringProperty("fill", getPolygonColor(col, row));
                
                listOfFeatures.add(feature);   
            }   
        }
        var featureCollection = FeatureCollection.fromFeatures(listOfFeatures);
        heatmapJson = featureCollection.toJson();
    }
    
    private static void outputHeatmapFile() {
        try {
            var heatmapFile = new File("heatmap.geojson");
            heatmapFile.createNewFile();
            var writer = new FileWriter("heatmap.geojson");
            writer.write(heatmapJson);
            writer.close();
        } catch (IOException e) {
            System.out.println("An error has occurred.");
            e.printStackTrace();
        }    
    }
    
    public static void main(String[] args) throws FileNotFoundException {
        readFile(args[0]);
        computeGridPoints();
        createHeatmapJson();
        outputHeatmapFile();
    }
}
