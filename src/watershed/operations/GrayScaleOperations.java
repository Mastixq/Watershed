package watershed.operations;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.highgui.HighGui;
import org.opencv.imgproc.Imgproc;
import org.opencv.photo.Photo;

public class GrayScaleOperations extends BaseOperations {

    public GrayScaleOperations(int width, int height) {
        super(width, height);
    }

    @Override
    public Mat preprocess(Mat srcMat) {
        Mat gray = new Mat();
        Imgproc.cvtColor(srcMat, gray, Imgproc.COLOR_BGR2GRAY);
        Mat clearMat = new Mat();
        Photo.fastNlMeansDenoising(gray, clearMat);
        Mat gauss = new Mat();
        int kernelSize = 29;
        int sigma = 0;
        //Imgproc.GaussianBlur(clearMat, gauss, new Size(kernelSize, kernelSize), sigma, sigma);
        Imgproc.medianBlur(clearMat, gauss, 5);
        HighGui.imshow("Source", srcMat);
        HighGui.imshow("Grayscale", gray);
        System.out.println(width);
        HighGui.moveWindow("Grayscale", width, 0);
        HighGui.imshow("Cleared", clearMat);
        HighGui.moveWindow("Cleared", 0, height + 30);
        HighGui.imshow("Gauss", gauss);
        HighGui.moveWindow("Gauss", width, height + 30);
        HighGui.waitKey(0);
        HighGui.destroyAllWindows();

        return gauss;
    }

    @Override
    public Mat preprocessOtsu(Mat processedImg) {
        return null;
    }
}
