package watershed.operations;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.photo.Photo;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;

public class GradientOperations extends BaseOperations {

    private int filterWidth, filterHeight;
    private int ignoreGradientValue;
    public Mat otsuMask = null;

    public GradientOperations(int width, int height) {
        super(width, height);
        this.filterHeight = 6;
        this.filterWidth = 6;
        this.ignoreGradientValue = 15;
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
        Mat blur = new Mat();

        Imgproc.blur(gray, blur, new Size(filterWidth, filterHeight));
        Mat clearMat = new Mat();
        Photo.fastNlMeansDenoising(blur, clearMat);

        return blur;
    }

    public Mat applyOtsuMask(Mat srcMat) {
        Mat treshhold = new Mat();
        Imgproc.threshold(srcMat, treshhold, 0, 255, Imgproc.THRESH_OTSU);
        Mat toTransform = new Mat();
        if(!invertSelection) {
            Core.bitwise_not(treshhold, toTransform);
        }
        else {
            toTransform = treshhold;
        }

        otsuMask = toTransform;

        Mat laplace = new Mat(toTransform.height(), toTransform.width(), CvType.CV_8UC3);
        super.applyMask(srcMat, toTransform);
        Imgproc.Laplacian(srcMat, laplace, laplace.depth());

        return laplace;
    }

    @Override
    public Pixel[][] toPixelArray(Mat src) {
        int width = (int) src.size().width;
        int height = (int) src.size().height;
        Pixel[][] pixelArray = new Pixel[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int value = (int) (src.get(j, i)[0]);
                if (value < ignoreGradientValue)
                    value = 0;
                pixelArray[i][j] = new Pixel(Pixel.EMPTY,
                        value,
                        new Point(i, j),
                        new Color(value, value, value));
            }
        }
        return pixelArray;
    }

    public void updateParameters(int filterWidth, int filterHeight, int ignoreGradientValue) {
        this.filterWidth = filterWidth;
        this.filterHeight = filterHeight;
        this.ignoreGradientValue = ignoreGradientValue;
    }
}
