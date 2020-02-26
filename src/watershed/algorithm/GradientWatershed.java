package watershed.algorithm;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import watershed.operations.ImageBase;
import watershed.operations.Pixel;

import java.awt.*;
import java.io.IOException;

public class GradientWatershed extends BaseWatershed {

    public GradientWatershed(String filename) throws IOException {
        super(filename);
        pixelArray = toPixelArrayGradient(processedImg);
        ImageBase.saveGradient(pixelArray, width, height, "processed.png", colorMap);
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

    @Override
    protected Mat preprocess(Mat srcMat) {
        Mat processImg = new Mat();
        Mat gray = new Mat();
        Mat morph = new Mat();
        Mat laplace = new Mat();
        Mat blur = new Mat();

        Imgproc.blur(srcMat, blur, new Size(3, 3));

        Imgproc.Laplacian(blur,laplace,srcMat.depth());
        //Imgproc.cvtColor(laplace,laplace,Imgproc.COLOR_BGR2GRAY);


        Imgproc.cvtColor(laplace, gray, Imgproc.COLOR_BGR2GRAY);
     //   Imgproc.threshold(gray, processImg, 0, 255, Imgproc.THRESH_OTSU);
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT,
                new Size(2, 2));
//        Imgproc.dilate(processImg, morph, kernel);
//        Mat dst = new Mat();
//        Imgproc.erode(morph, dst, kernel);
        Mat inverted = new Mat();
        Core.bitwise_not(gray, inverted);

        //Mat normalized = new Mat();
//
//        HighGui.imshow("src",srcOriginal);
//        HighGui.imshow("blur",blur);
//        HighGui.imshow("laplace", laplace);
//        HighGui.imshow("After erode", dst);
//        HighGui.waitKey( 0 );
        //Core.normalize(test, inverted, 0, 1., Core.NORM_MINMAX);
        return inverted;
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

    private Pixel[][] toPixelArrayGradient(Mat src) {
        int width = (int) src.size().width;
        int height = (int) src.size().height;
        Pixel[][] pixelArray = new Pixel[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                double value = (src.get(j, i)[0]);
               // if (value < 10.)
               //    value = 0;
                System.out.println(value);
                Point tmpPoint = new Point(i, j);
                pixelArray[i][j] = new Pixel(Pixel.EMPTY,
                        value,
                        tmpPoint,
                        new Color((int)value, (int)value, (int)value));

            }
        }
        return pixelArray;
    }


}
