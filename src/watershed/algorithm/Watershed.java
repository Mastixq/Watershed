package watershed.algorithm;

import watershed.operations.Pixel;

import org.opencv.core.*;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

//Image image = SwingFXUtils.toFXImage(capture, null);
public class Watershed {
    private Mat processImg;
    private Pixel[][] pixelArray;

    public Watershed(String filename) {
        Mat srcOriginal = Imgcodecs.imread(filename);
        if (srcOriginal.empty()) {
            System.err.println("Cannot read image: " + filename);
            System.exit(0);
        }
        Mat processsed = new Mat();
        processsed = initialProcess(srcOriginal);
        transformToPixelArray(srcOriginal);
    }
    private void transformToPixelArray(Mat src) {

    }
    private Mat initialProcess(Mat srcOriginal) {
        processImg = srcOriginal.clone();
        Mat gray = new Mat(srcOriginal.size(), CvType.CV_8U);
        Mat morph = new Mat(srcOriginal.size(), CvType.CV_8U);


        Mat test1 = new Mat(srcOriginal.size(), CvType.CV_8U);
        Mat test2 = new Mat(srcOriginal.size(), CvType.CV_8U);
        Mat test3 = new Mat(srcOriginal.size(), CvType.CV_8U);
        int kernelSize = 1;
        Mat element = Imgproc.getStructuringElement(Imgproc.CV_SHAPE_RECT, new Size(2 * kernelSize + 1, 2 * kernelSize + 1),
                new Point(kernelSize, kernelSize));

        Imgproc.cvtColor(srcOriginal,gray,Imgproc.COLOR_BGR2GRAY);
        //HighGui.imshow("Color2GRAY", gray);
        Imgproc.threshold(gray,processImg,0,255,Imgproc.THRESH_OTSU);
        //HighGui.imshow("THRESHOLD OTSU", processImg);
        int morphKernel[][] = {{1,1,1},
                    {1,1,1},
                    {1,1,1}};
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT,
                new  Size(2, 2));
        //Imgproc.morphologyEx(processImg,morph,Imgproc.MORPH_OPEN,kernelMat);

        Imgproc.dilate(processImg,morph,kernel);
        Mat dst = new Mat();
        //HighGui.imshow("After dilate", morph);


        Imgproc.erode(morph,dst,kernel);
        //HighGui.imshow("After erode", dst);

        Mat test = new Mat();
        Imgproc.distanceTransform(dst, test, 2, 3);
        Mat normalized = new Mat();
        //Core.normalize(test, normalized, 0, 1., Core.NORM_MINMAX);
       // HighGui.imshow("After distancetrnsfm", normalized);
        Imgcodecs.imwrite("resources/dstTransform.jpg",normalized);
        //HighGui.waitKey( 0 );
        System.out.println(test.get(23,231)[0]);
        return normalized;
    }
    private Pixel[][] startMarkers(Mat src){
        int width = (int)src.size().width;
        int height = (int)src.size().height;
        Pixel[][] tmpArray = new Pixel[width][height];
        for (int i = 0 ; i < width ; i++){
            for (int j = 0; j<height; j++){
//                double[] test = src.get(width,height);
                tmpArray[width][height] = new Pixel(Pixel.EMPTY, (int)src.get(width,height)[0],new java.awt.Point(width,height));
            }
        }
        return new Pixel[2][3];
    }
    private void watershedMarked(){}

    public static Mat BufferedImage2Mat(BufferedImage image) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", byteArrayOutputStream);
        byteArrayOutputStream.flush();
        return Imgcodecs.imdecode(new MatOfByte(byteArrayOutputStream.toByteArray()),  -1);
    }
    public static BufferedImage Mat2BufferedImage(Mat matrix)throws IOException {
        MatOfByte mob=new MatOfByte();
        Imgcodecs.imencode(".jpg", matrix, mob);
        return ImageIO.read(new ByteArrayInputStream(mob.toArray()));
    }
}
