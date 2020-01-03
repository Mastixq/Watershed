package watershed.algorithm;

import org.jetbrains.annotations.NotNull;
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
import java.awt.Point;

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
        //HighGui.imshow("After erode", dst);

        Mat test = new Mat();
        Imgproc.distanceTransform(dst, test, 2, 3);
        Mat normalized = new Mat();
        //Core.normalize(test, normalized, 0, 1., Core.NORM_MINMAX);
       // HighGui.imshow("After distancetrnsfm", normalized);
        Imgcodecs.imwrite("resources/dstTransform.jpg",test);
        //HighGui.waitKey( 0 );
        System.out.println(test.dump());
        return normalized;
    }
    private Pixel[][] toPixelArray(@NotNull Mat src){
        int width = (int)src.size().width;
        int height = (int)src.size().height;
        Pixel[][] tmpArray = new Pixel[width][height];
        for (int i = 0 ; i < width ; i++){
            for (int j = 0; j<height; j++){
               pixelArray[width][height] = new  Pixel(Pixel.EMPTY,
                                                (int)src.get(width,height)[0],
                                                new Point(width,height));
               if (pixelArray[width][height].distance == 0.) {
                   pixelArray[width][height].isMax = Pixel.NOTMAX;
               }
            }
        }
        return tmpArray;
    }
    private void startMarkers(@NotNull Pixel[][] src){ //seed it
        int width = src.length;
        int height = src[0].length;
        for (int i = 0 ; i < width ; i++){
            for (int j = 0; j<height; j++){

            }
        }

    }

    private boolean isMax(@NotNull Pixel[][] src, int x, int y) {
        int width = src.length;
        int height = src[0].length;


        if (x == width && y < height) {
          //  src[x][y].distance > src[x][y + 1].distance ?
        }
        return true;
    }

    boolean checkIfMax(Pixel[][] src, int p1, int p2, int prevMax){
        //just to clarify and hold position
        Pixel currPix = src[p1][p2];
        currPix.isChecked = true;
        if (currPix.isMax == Pixel.NOTMAX)
            return false;
        int sizeX = src.length;
        int sizeY = src[0].length;
        for(int i=-1;i<2;i++){
            for(int j=-1;j<2;j++){
                int x = p1 + i;
                int y = p2 + j;
                Pixel neighbouringPix = src[x][y];

                if(x<0 || x>sizeX-1 || y<0 || y>sizeY-1)
                    continue;
                else if(neighbouringPix.state==0 ||neighbouringPix.state==1)
                    continue;
                else{
                        if(neighbouringPix.isChecked == false || neighbouringPix.distance > currPix.distance) {
                            if (!checkIfMax(src, x, y, prevMax))
                                return false;
                        }
                    }

                }

            }
        return true;
    }


/*
        boolean checkifMax(array, position, prevMax)
        if(notMax)
            return true
        foreach()
                foreach(){
                if(currentValue > prevMax)
                    return false;
                else if currentValue == prevMax && isChecked
                   return checkIfMax(array, currentPosition, current);
                else
                    currPosition = checked,
                    notMax
                    return true


        }
    }

        return true;
    }
    */
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
