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
        pixelArray = toPixelArray(srcOriginal);
        startMarkers(pixelArray);
        System.out.println(Pixel.nextSeed);
        System.out.println("end markers");
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

    private Pixel[][] toPixelArray(Mat src){
        int width = (int)src.size().width;
        int height = (int)src.size().height;
        System.out.println(width);
        System.out.println(height);
        Pixel[][] pixelArray = new Pixel[height][width];
        for (int i = 0 ; i < width ; i++){
            for (int j = 0; j<height; j++){
                int distance = (int)src.get(j,i)[0];
                Point tmpPoint = new Point(j,i);
                pixelArray[j][i] = new  Pixel(Pixel.EMPTY,              //all empty at initialize
                                                distance,  //
                                                tmpPoint);
                if (pixelArray[j][i].distance == 0.) {
                }
            }
        }
        return pixelArray;
    }

    /**
     * Function responsible for creating one point markers
     * for each local maximum in topographic distance
     * @param src 2dimensional array of pixel containing preprocessed image
     */
    private void startMarkers(Pixel[][] src){ //seed it
        int width = src.length;
        int height = src[0].length;
        for (int i = 0 ; i < width ; i++){
            for (int j = 0; j<height; j++){
                Pixel currPix = src[i][j];
                //we only need to check each pixel once
                if(currPix.isChecked) {
                    continue;
                }
                if(checkIfMax(src,i,j)){
                    System.out.println("Hello");
                    currPix.newSeed();
                }
            }
        }

    }

    /**
     * IMPORTANT isChecked flag must set to false before entering this function
     * because it's just a helper recurrence function
     * @param src
     * @param p1
     * @param p2
     * @return checked if given pixel is local maxima of topographic distance
     */
    boolean checkIfMax(Pixel[][] src, int p1, int p2)
    {
        Pixel currPix = src[p1][p2];
        currPix.isChecked = true;
        int sizeX = src.length;
        int sizeY = src[0].length;
        for(int i=-1;i<2;i++)
        {
            for(int j=-1;j<2;j++)
            {
                int x = p1 + i;
                int y = p2 + j;

                if(x<0 || x>sizeX-1 || y<0 || y>sizeY-1)
                    continue;

                Pixel neighbouringPix = src[x][y];
                if(neighbouringPix.state==0 || neighbouringPix.state==1)
                    continue;
                else
                    {
                        //just in case it's false-positive max pixel, search neighbouring pixels for any other maxima
                        if(neighbouringPix.isChecked == false || neighbouringPix.distance == currPix.distance) {
                            if (!checkIfMax(src, x, y))
                                return false;
                        }
                    }
                }
            }
        return true;
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
