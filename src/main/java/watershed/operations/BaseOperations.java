package watershed.operations;

import org.opencv.core.*;
import watershed.application.MainWindow;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public abstract class BaseOperations {

    int width, height;
    boolean invertSelection = false;

    public BaseOperations(int width, int height) {
        this.width = width;
        this.height = height;
    }

    private BaseOperations() {

    }

    public BufferedImage save(Pixel[][] src, int width, int height, String filename, Map<Integer, Color> colorMap) throws IOException {
        BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Arrays.stream(src)
                .flatMap(pixels -> Arrays.stream(pixels)).forEach(pixel -> {
            int rgb = colorMap.get((pixel.state)).getRGB();
            newImage.setRGB(pixel.pos.x, pixel.pos.y, rgb);
        });

        File outfile = new File(filename);
        ImageIO.write(newImage, "png", outfile);
        return newImage;
    }

    public BufferedImage saveStateOverlay(Pixel[][] src, int width, int height, String filename, Map<Integer, Color> colorMap) throws IOException {
        BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Arrays.stream(src)
                .flatMap(pixels -> Arrays.stream(pixels)).forEach(pixel -> {
            int rgb;
            if (pixel.state > 0)
                rgb = colorMap.get((pixel.state)).getRGB();
            else
                rgb = new Color((int) pixel.value, (int) pixel.value, (int) pixel.value).getRGB();
            newImage.setRGB(pixel.pos.x, pixel.pos.y, rgb);
        });
        File outfile = new File(filename);
        ImageIO.write(newImage, "png", outfile);
        return newImage;
    }

    public BufferedImage saveOverCustom(Pixel[][] src, Pixel[][] custom, int width, int height, String filename, Map<Integer, Color> colorMap) throws IOException {
        BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                Pixel currPix = src[i][j];
                Pixel customPix = custom[i][j];
                int rgb;
                if (currPix.state == Pixel.BORDER)
                    rgb = Color.BLACK.getRGB();
                else
                    rgb = new Color((int) customPix.value, (int) customPix.value, (int) customPix.value).getRGB();
                newImage.setRGB(i, j, rgb);
            }
        }
        File outfile = new File(filename);
        ImageIO.write(newImage, "png", outfile);
        return newImage;
    }

    public BufferedImage saveBorderOverlay(Pixel[][] src, int width, int height, String filename, Map<Integer, Color> colorMap) throws IOException {
        BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                Pixel currPix = src[i][j];
                int rgb;
                if (currPix.state == Pixel.BORDER)
                    rgb = Color.GREEN.getRGB();
                else
                    rgb = new Color((int) currPix.value, (int) currPix.value, (int) currPix.value).getRGB();
                newImage.setRGB(i, j, rgb);
            }
        }
        File outfile = new File(filename);
        ImageIO.write(newImage, "png", outfile);
        return newImage;
    }

    public Pixel[][] toPixelArray(Mat src) {
        Pixel[][] pixelArray = new Pixel[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                double value = (src.get(j, i)[0]);
                Point tmpPoint = new Point(i, j);
                pixelArray[i][j] = new Pixel(Pixel.EMPTY,
                        value,
                        tmpPoint);
                if (pixelArray[i][j].value == (double) 0) {
                    pixelArray[i][j].isChecked = true;
                    pixelArray[i][j].state = Pixel.BORDER;
                }
            }
        }
        return pixelArray;
    }

    public void printPixel(Pixel[][] pixelArr, int x, int y) {
        for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++) {
                int p1 = x + i;
                int p2 = y + j;
                if (p1 < 0 || p1 > width - 1 || p2 < 0 || p2 > height - 1)
                    System.out.print("# ");
                else
                    System.out.print((int) pixelArr[p1][p2].value + " ");
            }
            System.out.println();
        }
    }

    public abstract Mat preprocess(Mat srcMat);

    public void applyMask(Mat src, Mat mask) {
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                double value = (mask.get(j, i)[0]);
                if (value == 0.)
                    src.put(j, i, value);
            }
        }
    }


    /**
     * Utility function to save histogram from given pixel array
     */
    public void saveHistogram(Pixel[][] src) throws IOException {
        int[] arr = new int[256];
        Arrays.stream(src)
                .flatMap(pixArr -> Arrays.stream(pixArr))
                .map(pix -> (int) ((Pixel) pix).value)
                .forEach(value -> arr[value]++);

        BufferedWriter outputWriter = null;
        outputWriter = new BufferedWriter(new FileWriter("saved.csv"));
        for (int i = 0; i < arr.length; i++) {
            outputWriter.write(arr[i] + ",");

        }
        outputWriter.flush();
        outputWriter.close();

    }

    public String prepareResultsFile() throws URISyntaxException {
        URI uri = MainWindow.class.getProtectionDomain().getCodeSource().getLocation().toURI();
        String path = Paths.get(uri).getParent().toString();
        DateFormat dateFormat = new SimpleDateFormat("MMddHHmmss");
        Date date = new Date();
        File newDirectory = new File(path + "/results" + dateFormat.format(date));
        System.out.println(newDirectory.toString());
        if (!newDirectory.exists()) {
            newDirectory.mkdir();
        }
        return newDirectory.toString();
    }

    public void setInvertSelection(boolean selection) {
        System.out.println(selection);
        invertSelection = selection;
    }

    public void saveStats(Pixel[][] src, String filename) throws IOException {
        BufferedWriter outputWriter;
        outputWriter = new BufferedWriter(new FileWriter(filename + ".csv"));

        Map<Integer, Integer> sizeStats = getSeedSize(src);

        outputWriter.write("Total" + "," + width * height + '\n');
        int totalFound = sizeStats.values().stream().reduce(0, (subtotal, value) -> subtotal + value);
        outputWriter.write("Found" + "," + totalFound + '\n');

        int size = sizeStats.size();
        DecimalFormat df2 = new DecimalFormat("#.##");
        outputWriter.write("Seeds:" + "," + size + '\n');
        outputWriter.write("Avg size:" + "," + df2.format((double) totalFound / size) + "\n\n");

        for (Integer key : sizeStats.keySet()) {
            outputWriter.write(key + "," + sizeStats.get(key) + '\n');
        }
        outputWriter.flush();
        outputWriter.close();

    }

    public Map<Integer, Integer> getSeedSize(Pixel[][] src) {
        HashMap<Integer, Integer> seedMap = new HashMap<>();
        Arrays.stream(src)
                .flatMap(pixArr -> Arrays.stream(pixArr))
                .filter(pixel -> pixel.state != Pixel.EMPTY)
                .filter(pixel -> pixel.state != Pixel.BORDER)
                .forEach(pixel -> {
                    if (seedMap.get(pixel.state) == null) {
                        seedMap.put(pixel.state, 1);
                    } else {
                        int prevValue = seedMap.get(pixel.state);
                        seedMap.put(pixel.state, ++prevValue);
                    }
                });


        return seedMap;
    }

}
