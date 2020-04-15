package watershed.operations;

import org.opencv.core.*;
import org.opencv.highgui.HighGui;
import org.opencv.imgproc.Imgproc;
import org.opencv.photo.Photo;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

public class GradientOperations extends BaseOperations {

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
        int kernelSize = 21;
        int sigma = 0;
//        Imgproc.GaussianBlur(clearMat, gauss, new Size(kernelSize, kernelSize), sigma, sigma);

        Imgproc.medianBlur(clearMat, gauss, 5);
        Mat laplace = new Mat(gauss.height(), gauss.width(), CvType.CV_8UC3);
        // Imgproc.Laplacian(gauss,laplace,gauss.depth());
        Imgproc.Laplacian(gauss, laplace, laplace.depth());
        HighGui.imshow("Source", srcMat);
        HighGui.imshow("Grayscale", gray);
        HighGui.moveWindow("Grayscale", width, 0);
        HighGui.imshow("Cleared", clearMat);
        HighGui.moveWindow("Cleared", 0, height + 30);
        HighGui.imshow("Gauss", gauss);
        HighGui.moveWindow("Gauss", width, height + 30);
        HighGui.imshow("Laplace", laplace);
        HighGui.moveWindow("Laplace", width + width, height + 30);
        HighGui.waitKey(0);
        HighGui.destroyAllWindows();

        return gauss; //rozmycie
    }

    public Mat preprocessOtsu(Mat srcMat) {
        Mat treshhold = new Mat();
        double q = Imgproc.threshold(srcMat, treshhold, 0, 255, Imgproc.THRESH_OTSU);
        System.out.println("OTSU: "+ q);
        Mat inverted = new Mat();
        Core.bitwise_not(treshhold, inverted);

        Mat laplace = new Mat(treshhold.height(), treshhold.width(), CvType.CV_8UC3);
        super.applyMask(srcMat,inverted);
        Imgproc.Laplacian(srcMat,laplace,laplace.depth());



        HighGui.imshow("srcMat", srcMat);
        HighGui.moveWindow("srcMat", width, 0);
        HighGui.imshow("Treshhold", treshhold);
        HighGui.moveWindow("Treshhold", 0, height + 30);
        HighGui.imshow("Inverted", inverted);
        HighGui.moveWindow("Inverted", width, height + 30);
        HighGui.imshow("Laplace", laplace);
        HighGui.moveWindow("Laplace", width+width+30, height + 30);
        HighGui.waitKey(0);

        HighGui.destroyAllWindows();
        return laplace;
    }

    @Override
    public Pixel[][] toPixelArray(Mat src) {
        int width = (int) src.size().width;
        int height = (int) src.size().height;
        Pixel[][] pixelArray = new Pixel[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                double value = (src.get(j, i)[0]);
                if (value < 15.)
                    value = 0;
                Point tmpPoint = new Point(i, j);
                pixelArray[i][j] = new Pixel(Pixel.EMPTY,
                        value,
                        tmpPoint,
                        new Color((int) value, (int) value, (int) value));

            }
        }
        return pixelArray;
    }




}
