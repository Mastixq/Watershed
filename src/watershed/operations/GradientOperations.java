package watershed.operations;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.highgui.HighGui;
import org.opencv.imgproc.Imgproc;
import org.opencv.photo.Photo;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;

public class GradientOperations extends BaseOperations{

    public GradientOperations(int width, int height) {
        super(width, height);
    }

    @Override
    public BufferedImage save(Pixel[][] src, int width, int height, String filename, Map<Integer, Color> colorMap) throws IOException {
        BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int rgb = src[i][j].color.getRGB();
                newImage.setRGB(i, j, rgb);
            }
        }
        File outfile = new File(filename);
        ImageIO.write(newImage, "png", outfile);
        return newImage;
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


        Mat laplace = new Mat();
        Imgproc.Laplacian(gauss,laplace,gauss.depth());

        HighGui.imshow("Source", srcMat);
        HighGui.imshow("Grayscale", gray);
        HighGui.moveWindow("Grayscale", width, 0);
        HighGui.imshow("Cleared", clearMat);
        HighGui.moveWindow("Cleared", 0, height + 30);
        HighGui.imshow("Gauss", gauss);
        HighGui.moveWindow("Gauss", width, height + 30);
        HighGui.imshow("Laplace", laplace);
        HighGui.moveWindow("Laplace", width+width, height + 30);
        HighGui.waitKey(0);
        HighGui.destroyAllWindows();

        return laplace;
    }

    public Mat preprocessTest(Mat srcMat) {
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

    @Override
    public Pixel[][] toPixelArray(Mat src) {
        int width = (int) src.size().width;
        int height = (int) src.size().height;
        Pixel[][] pixelArray = new Pixel[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                double value = (src.get(j, i)[0]);
                if (value < 2.)
                    value = 0;
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
