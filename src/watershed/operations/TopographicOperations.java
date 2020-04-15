package watershed.operations;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.highgui.HighGui;
import org.opencv.imgproc.Imgproc;
import org.opencv.photo.Photo;

import java.awt.*;

public class TopographicOperations extends BaseOperations {

    public TopographicOperations(int width, int height) {
        super(width, height);
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

        Mat gauss = new Mat();
        int kernelSize = 3;
        int sigma = 0;
        Imgproc.GaussianBlur(gray, gauss, new Size(kernelSize, kernelSize), sigma, sigma);

        Mat treshhold = new Mat();
        Imgproc.threshold(gauss, treshhold, 0, 255, Imgproc.THRESH_OTSU);
        Mat inverted = new Mat();
        Core.bitwise_not(treshhold, inverted);



        Mat kernel = Mat.ones(4,4, CvType.CV_32F);
        Mat morph = new Mat();
        Imgproc.morphologyEx(inverted, morph, Imgproc.MORPH_CLOSE, kernel);

        Mat dstTransform = new Mat();
        Imgproc.distanceTransform(morph, dstTransform, Imgproc.DIST_C, 3);




        HighGui.imshow("srcMat", srcMat);
        HighGui.moveWindow("srcMat", width, 0);
        HighGui.imshow("Treshhold", treshhold);
        HighGui.moveWindow("Treshhold", 0, height + 30);
        HighGui.imshow("Morph", morph);
      //  HighGui.moveWindow("Morph", 0, 0);
        HighGui.imshow("Inverted", inverted);
        HighGui.moveWindow("Inverted", width, height + 30);

        HighGui.waitKey(0);

        HighGui.destroyAllWindows();

        return dstTransform; //rozmycie
    }

    @Override
    public Mat preprocessOtsu(Mat processedImg) {
        return null;
    }
}
