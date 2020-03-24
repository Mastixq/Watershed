package watershed.algorithm;

import org.opencv.imgproc.Imgproc;
import watershed.operations.Pixel;
import watershed.operations.BaseOperations;

import org.opencv.core.*;
import watershed.operations.TopographicOperations;

import java.io.IOException;

//Image image = SwingFXUtils.toFXImage(capture, null);
public class TopographicWatershed extends BaseWatershed {

    public TopographicWatershed(String filename) throws IOException {
        super(filename);
        operations = new TopographicOperations(width,height);
        pixelArray = operations.toPixelArray(processedImg);
        operations.save(pixelArray, width, height, "processed.png", colorMap);
        startMarkers(pixelArray);
        calculate();
        operations.save(pixelArray, pixelArray.length, pixelArray[0].length, "marked.png", colorMap);

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
                if (checkIfMaxDistance(src, i, j)) {
                    currPix.state = newSeed();
                    addNeighbouringToQueue(i, j);
                }
            }
        }

    }

    /**
     * IMPORTANT isChecked flag be must set to false before entering this function
     * because it's just a helper recurrence function
     *
     * @param src
     * @param p1
     * @param p2
     * @return checked if given pixel is local maxima of topographic distance
     */
    boolean checkIfMaxDistance(Pixel[][] src, int p1, int p2) {
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
                        if (!checkIfMaxDistance(src, x, y))
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

    public Mat preprocess(Mat srcMat) {
        Mat processImg = new Mat();
        Mat gray = new Mat();
        Mat morph = new Mat();
        Mat laplace = new Mat();
        Mat blur = new Mat();

        Imgproc.blur(srcMat, blur, new Size(3, 3));
        // Imgproc.Laplacian(blur,laplace,srcOriginal.depth());
        //Imgproc.cvtColor(laplace,laplace,Imgproc.COLOR_BGR2GRAY);
        Imgproc.blur(blur, blur, new Size(3, 3));


        Imgproc.cvtColor(blur, gray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.threshold(gray, processImg, 0, 255, Imgproc.THRESH_OTSU);
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT,
                new Size(2, 2));
        Imgproc.dilate(processImg, morph, kernel);
        Mat dst = new Mat();
        Imgproc.erode(morph, dst, kernel);
        Mat inverted = new Mat();
        Core.bitwise_not(dst, inverted);
        Mat test = new Mat();
        Imgproc.distanceTransform(inverted, test, 1, 3);
        Mat normalized = new Mat();
//
//        HighGui.imshow("src",srcOriginal);
//        HighGui.imshow("blur",blur);
//        HighGui.imshow("laplace", laplace);
//        HighGui.imshow("After erode", dst);
//        HighGui.waitKey( 0 );
        Core.normalize(test, normalized, 0, 1., Core.NORM_MINMAX);
        return test;
    }

    private void watershedGradient() {

    }


    //TODO remove straight lines

    //TODO remove end of the lines (only 1 in moore)

    //TODO create markers on blured image, but place them on morphology separately
}

