package watershed.algorithm;

import com.sun.prism.paint.Gradient;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import watershed.operations.BaseOperations;
import watershed.operations.GradientOperations;
import watershed.operations.Pixel;

import java.awt.*;
import java.io.IOException;

public class GradientWatershed extends BaseWatershed {

    public GradientWatershed(String filename) throws IOException {
        super(filename);
        operations = new GradientOperations();
        pixelArray = operations.toPixelArray(processedImg);
        operations.save(pixelArray, width, height, "processed.png", colorMap);
        //startGradientMarkers(pixelArray);
       // calculate();
       // ImageBase.save(pixelArray, pixelArray.length, pixelArray[0].length, "marked.png", colorMap);
    }

    @Override
    public void calculate() {
        while (!queue.isEmpty()) {
            Pixel currPix = queue.remove();
            int stateCandidate = -1;
            for (int i = -1; i < 2; i++) {
                for (int j = -1; j < 2; j++) {
                    int p1 = currPix.pos.x + i;
                    int p2 = currPix.pos.y + j;

                    if (p1 < 0 || p1 > width - 1 || p2 < 0 || p2 > height - 1)
                        continue;
                    if (i == 0 && j == 0)
                        continue;
                    Pixel processingPix = pixelArray[p1][p2];
                    if (processingPix.state == Pixel.EMPTY || processingPix.state == Pixel.BORDER)
                        continue;
                    if (stateCandidate == -1)
                        stateCandidate = processingPix.state;
                    if (stateCandidate != processingPix.state)
                        stateCandidate = processingPix.BORDER;
                }
            }
            if (stateCandidate == -1) {
                stateCandidate = Pixel.EMPTY;
            }
            currPix.state = stateCandidate;
            addNeighbouringToQueue(currPix.pos.x, currPix.pos.y);


        }

    }


    private void startGradientMarkers(Pixel[][] src) { //seed it
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                Pixel currPix = src[i][j];
                if (checkIfMaxGradient(src, i, j)) {
                    currPix.state = newSeed();
                    addNeighbouringToQueue(i, j);
                }
            }
        }

    }

    boolean checkIfMaxGradient(Pixel[][] src, int x, int y) {
        Pixel currPix = src[x][y];
        if (currPix.isChecked)
            return false;
        currPix.isChecked = true;
        boolean returnFlag = true;

        for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++) {
                int p1 = x + i;
                int p2 = y + j;

                if (p1 < 0 || p1 > width - 1 || p2 < 0 || p2 > height - 1)
                    continue;
                if (i == 0 && j == 0) {
                    continue;
                }

                Pixel neighbouringPix = src[p1][p2];

                if (neighbouringPix.state > Pixel.BORDER)
                    return false;
                if (neighbouringPix.value > currPix.value) {

                    returnFlag = false;
                }

                if (neighbouringPix.value < currPix.value && !neighbouringPix.isChecked) {
                    neighbouringPix.isChecked = true;
                }
                if (neighbouringPix.state == 1)
                    continue;
                else {
                    //just in case it's false-positive max pixel, search neighbouring pixels for any other maxima
                    if (neighbouringPix.isChecked == false && neighbouringPix.value == currPix.value) {
                        if (!checkIfMaxGradient(src, p1, p2))
                            returnFlag = false;
                    }
                }
            }
        }
        return returnFlag;
    }



}
