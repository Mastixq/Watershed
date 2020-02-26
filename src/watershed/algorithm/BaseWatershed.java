package watershed.algorithm;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import watershed.operations.ImageBase;
import watershed.operations.Pixel;

import java.awt.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Random;

public abstract class BaseWatershed {
    protected Mat processedImg;
    protected Mat srcImage;
    protected Pixel[][] pixelArray;

    protected int width, height;

    public HashMap<Integer, Color> colorMap;
    public int nextSeed = 2;

    protected HashSet<Pixel> pixelSet;
    protected PriorityQueue<Pixel> queue;

    private BaseWatershed() {
        System.out.println("0 arguments constructor can't be called");
    }

    public BaseWatershed(String filename) {
        srcImage = Imgcodecs.imread(filename);
        if (srcImage.empty()) {
            System.err.println("Cannot read image: " + filename);
            System.exit(0);
        }
        width = (int) srcImage.size().width;
        height = (int) srcImage.size().height;

        processedImg = preprocess(srcImage);


        pixelSet = new HashSet<>();
        queue = new PriorityQueue<>(Pixel.distanceComparator);

        colorMap = new HashMap();
        colorMap.put(0, Color.white);
        colorMap.put(1, Color.black);
    }

    public abstract void calculate();

    protected abstract Mat preprocess(Mat srcMat);


    //TODO dodawanie do kolejki pustych pixeli wokol ziaren
    //Priority queue by distance
    protected void addNeighbouringToQueue(int x, int y) {
        for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++) {
                int p1 = x + i;
                int p2 = y + j;

                if (p1 < 0 || p1 > width - 1 || p2 < 0 || p2 > height - 1)
                    continue;
                if (i == 0 && j == 0) {
                    continue;
                }
                Pixel currPix = pixelArray[p1][p2];

                if (pixelArray[p1][p2].state == 0) {
                    if (pixelSet.add(currPix)) {
                        queue.add(currPix);
                    } else {
                        continue;
                    }
                }

            }
        }
    }

    protected int newSeed(Color color) {
        colorMap.put(nextSeed, color);
        return nextSeed++;
    }

    protected int newSeed() {
        Random gen = new Random();
        Color nextColor = new Color(gen.nextInt(256), gen.nextInt(256), gen.nextInt(256));
        colorMap.put(nextSeed, nextColor);
        return nextSeed++;
    }
}