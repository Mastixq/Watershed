package watershed.algorithm;

import org.opencv.core.Mat;
import watershed.operations.GrayScaleOperations;
import watershed.operations.Pixel;

import java.io.IOException;
import java.util.PriorityQueue;

public class GrayScaleWatershed extends BaseWatershed {

    public GrayScaleWatershed(String filename) throws IOException {
        super(filename);
        queue = new PriorityQueue<>(Pixel.distanceMinimaComparator);
        operations = new GrayScaleOperations(width,height);
        processedImg = operations.preprocess(srcImage);
        pixelArray = operations.toPixelArray(processedImg);
        System.out.println("xd");
        startMarkers(pixelArray);
        //calculate();
        operations.save(pixelArray,width,height,"test.png",colorMap);
        operations.saveStateOverlay(pixelArray,width,height,"testOverlay.png",colorMap);

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
    @Override
    void startMarkers(Pixel[][] src) {
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                Pixel currPix = src[i][j];
                if (markLocalMinima(i, j)) {
                    currPix.state = newSeed();
                    addNeighbouringToQueue(i, j);
                }
            }
        }
    }

    private boolean markLocalMinima(int px, int py) {
        Pixel currPix = pixelArray[px][py];

        if (currPix.isChecked)
            return false;
        currPix.isChecked = true;

        boolean returnFlag = true;

        for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++) {
                int x = px + i;
                int y = py + j;

                if (x < 0 || x > width - 1 || y < 0 || y > height - 1)
                    continue;
                if (i == 0 && j == 0) {
                    continue;
                }

                Pixel neighbouringPix = pixelArray[x][y];

                if (neighbouringPix.state > Pixel.BORDER)
                    return false;
                if (neighbouringPix.value < currPix.value) {
                    returnFlag = false;
                }

                if (neighbouringPix.value > currPix.value && !neighbouringPix.isChecked) {
                    neighbouringPix.isChecked = true;
                }
                if (neighbouringPix.state == 1)
                    continue;
                else {
                    //just in case it's false-positive max pixel, search neighbouring pixels for any other maxima
                    if (neighbouringPix.isChecked == false && neighbouringPix.value == currPix.value) {
                        if (!markLocalMinima(x, y))
                            returnFlag = false;
                    }
                }
            }
        }
        return returnFlag;
    }


}
