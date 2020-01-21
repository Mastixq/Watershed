package watershed.operations;

import org.opencv.core.*;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class ImageBase {
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

    public static BufferedImage save(Pixel[][] src, int width, int height, String filename) throws IOException{
        BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int rgb=Pixel.colorMap.get((src[i][j].state)).getRGB();
                newImage.setRGB(i, j, rgb);
            }
        }
        File outfile = new File(filename);
        ImageIO.write(newImage, "png", outfile);
        return newImage;
    }

    public static Pixel[][] toPixelArray(Mat src){
        int width = (int)src.size().width;
        int height = (int)src.size().height;
        Pixel[][] pixelArray = new Pixel[width][height];
        for (int i = 0 ; i < width ; i++){
            for (int j = 0; j<height; j++){
                int distance = (int)(src.get(j,i)[0]*255);
                System.out.print(distance+" ");
                Point tmpPoint = new Point(i,j);
                pixelArray[i][j] = new  Pixel(Pixel.EMPTY,              //all empty at initialize
                        distance,  //
                        tmpPoint,
                        new Color(distance,distance,distance));
                if (pixelArray[i][j].distance == (double)0) {
                    pixelArray[i][j].isChecked = true;
                    pixelArray[i][j].state = Pixel.BORDER;
                }
            }
        }
        return pixelArray;
    }
    public static Mat initialProcess(Mat srcOriginal) {
        Mat processImg = new Mat();
        Mat gray = new Mat();
        Mat morph = new Mat();
        Mat laplace = new Mat();



        Mat blur = new Mat();
        Imgproc.blur(srcOriginal,blur,new Size(3,3));
        // Imgproc.blur(blur,blur,new Size(3,3));
        // Imgproc.blur(blur,blur,new Size(3,3));
        HighGui.imshow("src",srcOriginal);
        HighGui.imshow("blur",blur);
        Imgproc.Laplacian(blur,laplace,srcOriginal.depth());

        Imgproc.cvtColor(laplace,laplace,Imgproc.COLOR_BGR2GRAY);
        //Imgproc.threshold(laplace,blur,0,255,Imgproc.THRESH_OTSU);
        HighGui.imshow("laplace", laplace);
        Imgproc.cvtColor(srcOriginal,gray,Imgproc.COLOR_BGR2GRAY);
        //HighGui.imshow("Color2GRAY", gray);
        Imgproc.threshold(gray,processImg,0,255,Imgproc.THRESH_OTSU);
        //HighGui.imshow("THRESHOLD OTSU", processImg);
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT,
                new  Size(2, 2));
        Imgproc.dilate(processImg,morph,kernel);
        Mat dst = new Mat();
        //HighGui.imshow("After dilate", morph);

        Imgproc.erode(morph,dst,kernel);
        HighGui.imshow("After erode", dst);
        Mat inverted = new Mat();
        Core.bitwise_not(dst,inverted);
        Mat test = new Mat();

        Imgproc.distanceTransform(inverted, test, 1, 3);
        Mat normalized = new Mat();
        Core.normalize(test, normalized, 0, 1., Core.NORM_MINMAX);

        //Imgproc.sqrBoxFilter(normalized, );
        //HighGui.imshow("After distancetrnsfm", normalized);
        Imgcodecs.imwrite("resources/dstTransform.jpg",test);
        HighGui.waitKey( 0 );
        System.out.println("Normalized: ");
        System.out.println(normalized.dump());
        return normalized;
    }
}
