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
        int kernelSize = 5;
        int sigma = 0;
        Imgproc.GaussianBlur(clearMat, gauss, new Size(kernelSize, kernelSize), sigma, sigma);
        Mat gauss2 = new Mat();

        Imgproc.GaussianBlur(clearMat, gauss2, new Size(51, 31), sigma, sigma);

        HighGui.imshow("Source", srcMat);
        HighGui.imshow("Grayscale", gray);
        System.out.println(width);
        HighGui.moveWindow("Grayscale", width, 0);
        HighGui.imshow("Cleared", clearMat);
        HighGui.moveWindow("Cleared", 0, height + 30);
        HighGui.imshow("Gauss", gauss);
        HighGui.moveWindow("Gauss", width, height + 30);
        HighGui.imshow("Gauss2", gauss2);
        HighGui.moveWindow("Gauss2", width+width, height + 30);
        HighGui.waitKey(0);
        HighGui.destroyAllWindows();

        return gauss;
    }
}
