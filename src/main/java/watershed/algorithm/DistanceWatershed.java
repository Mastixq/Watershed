package watershed.algorithm;

import watershed.operations.DistanceOperations;
import watershed.operations.Pixel;

import java.io.IOException;
import java.util.PriorityQueue;


public class DistanceWatershed extends BaseWatershed {
    DistanceOperations operations;

    public DistanceWatershed(String filename) {
        super(filename);
        queue = new PriorityQueue<>(Pixel.distanceComparator);
        operations = new DistanceOperations(width, height);
    }

    public void run() throws IOException {
        processedImg = operations.preprocess(srcImage);
        pixelArray = operations.toPixelArray(processedImg);
        operations.save(pixelArray, width, height, "processed.png", colorMap);
        startMarkers(pixelArray);
        calculate();
        operations.save(pixelArray, width, height, "watershed.png", colorMap);
        Pixel[][] customSrc = operations.toPixelArray(srcImage);
        operations.saveOverCustom(pixelArray, customSrc, width, height, "testSaveOverCustom.png", colorMap);
    }

    public void updateParameters(int elementWidth, int elementHeight){
        operations.updateParameters(elementWidth,elementHeight);
    }

    /**
     * Function responsible for creating one point markers
     * for each local maximum in topographic distance
     *
     * @param src 2-dimensional array of pixel containing preprocessed image
     */
    @Override
    void startMarkers(Pixel[][] src) {
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                Pixel currPix = src[i][j];
                if (isLocalMaximum(src, i, j)) {
                    currPix.state = newSeed();
                    addNeighbouringToQueue(i, j);
                }
            }
        }

    }

    boolean isLocalMaximum(Pixel[][] src, int p1, int p2) {
        Pixel currPix = src[p1][p2];
        if (currPix.isChecked)
            return false;
        currPix.isChecked = true;
        int sizeX = src.length;
        int sizeY = src[0].length;
        boolean returnFlag = true;

        for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++) {
                int x = p1 + i;
                int y = p2 + j;

                if (x < 0 || x > sizeX - 1 || y < 0 || y > sizeY - 1)
                    continue;
                if (i == 0 && j == 0) {
                    continue;
                }

                Pixel neighbouringPix = src[x][y];

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
                        if (!isLocalMaximum(src, x, y))
                            returnFlag = false;
                    }
                }
            }
        }
        return returnFlag;
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

}

