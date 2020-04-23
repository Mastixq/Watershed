package watershed.algorithm;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import watershed.operations.Pixel;

import java.awt.*;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

public abstract class BaseWatershed {
    Mat processedImg;
    Mat srcImage;
    Pixel[][] pixelArray;

    int width, height;

    HashMap<Integer, Color> colorMap;
    public int nextSeed = 2;

    HashSet<Pixel> pixelSet;
    PriorityQueue<Pixel> queue;

    public BaseWatershed(String filename) {
        srcImage = Imgcodecs.imread(filename);
        if (srcImage.empty()) {
            System.err.println("Cannot read image: " + filename);
            System.exit(0);
        }
        width = (int) srcImage.size().width;
        height = (int) srcImage.size().height;

        pixelSet = new HashSet<>();

        colorMap = new HashMap();
        colorMap.put(0, Color.white);
        colorMap.put(1, Color.black);
    }

    public abstract void run() throws IOException, URISyntaxException;

    public abstract void calculate();

    abstract void startMarkers(Pixel[][] src);

    void addNeighbouringToQueue(int x, int y) {
        for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++) {
                if(i==j || i == -j)
                    continue;
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

    protected int newSeed() {
        Random gen = new Random();
        Color nextColor = new Color(gen.nextInt(256), gen.nextInt(256), gen.nextInt(256));
        colorMap.put(nextSeed, nextColor);
        return nextSeed++;
    }
}
