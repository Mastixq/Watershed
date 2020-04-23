package watershed.operations;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.awt.*;

public class DistanceOperations extends BaseOperations {

    private int elementWidth, elementHeight;

    public DistanceOperations(int width, int height) {
        super(width, height);
        this.elementWidth = 3;
        this.elementHeight = 3;
    }

    @Override
    public Pixel[][] toPixelArray(Mat src) {
        int width = (int) src.size().width;
        int height = (int) src.size().height;
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

    @Override
    public Mat preprocess(Mat srcMat) {
        Mat gray = new Mat();
        Imgproc.cvtColor(srcMat, gray, Imgproc.COLOR_BGR2GRAY);

        Mat otsu = new Mat();
        Imgproc.threshold(gray, otsu, 0, 255, Imgproc.THRESH_OTSU);

        Mat kernel = Mat.ones(elementWidth,elementHeight, CvType.CV_32F);

        Mat morphOpen = new Mat();
        Imgproc.morphologyEx(otsu, morphOpen, Imgproc.MORPH_OPEN, kernel);

        Mat morphClose = new Mat();
        Imgproc.morphologyEx(morphOpen, morphClose, Imgproc.MORPH_CLOSE, kernel);


        Mat toTransform = new Mat();
        if(!invertSelection) {
            Core.bitwise_not(morphClose, toTransform);
        }
        else {
            toTransform = morphOpen;
        }

        Mat dstTransform = new Mat();
        Imgproc.distanceTransform(toTransform, dstTransform, Imgproc.CV_DIST_C, 3);

        return dstTransform;
    }

    public void updateParameters(int elementWidth, int elementHeight) {
        this.elementWidth = elementWidth;
        this.elementHeight = elementHeight;
    }

}
