package watershed.operations;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Arrays;
import java.util.Map;

public abstract class BaseOperations {

    int width, height;

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
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                Pixel currPix = src[i][j];
                int rgb;
                if (currPix.state > 0)
                    rgb = colorMap.get((src[i][j].state)).getRGB();
                else
                    rgb = new Color((int) currPix.value, (int) currPix.value, (int) currPix.value).getRGB();
                newImage.setRGB(i, j, rgb);
            }
        }
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
                    rgb = Color.GREEN.getRGB();
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

    public Mat preprocessOtsu(Mat processedImg){return null;}

    public void applyMask(Mat src, Mat mask) {
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                double value = (mask.get(j, i)[0]);
                if (value == 0.)
                    src.put(j, i, value);
            }
        }
    }

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
}
