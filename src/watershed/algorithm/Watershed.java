package watershed.algorithm;

import watershed.operations.Pixel;

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
import java.util.HashMap;
import java.util.Map;

//Image image = SwingFXUtils.toFXImage(capture, null);
public class Watershed {
    private Mat processImg;
    private Pixel[][] pixelArray;
    public int counterIsChecked;
    public int counterPixels;
    public Watershed(String filename) throws IOException {
        counterPixels = 0;
        counterIsChecked = 0;
        Mat srcOriginal = Imgcodecs.imread(filename);
        if (srcOriginal.empty()) {
            System.err.println("Cannot read image: " + filename);
            System.exit(0);
        }
        Mat processsed = new Mat();
        processsed = initialProcess(srcOriginal);
        System.out.println("leaving initialProcess");
        pixelArray = toPixelArray(processsed);
        save(pixelArray,pixelArray.length,pixelArray[0].length, "processed.png");
        System.out.println("leaving to pixel array");
        startMarkers(pixelArray);
        System.out.println("end markers");
        HashMap<Integer,Color> colorMap = Pixel.colorMap;
        save(pixelArray,pixelArray.length,pixelArray[0].length, "marked.png");
        System.out.println(counterIsChecked);
        System.out.println(counterPixels);
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

    private Pixel[][] toPixelArray(Mat src){
        int width = (int)src.size().width;
        int height = (int)src.size().height;
        //System.out.println("dumping over");
        Pixel[][] pixelArray = new Pixel[width][height];
        System.out.println("\n\n\n\n");
        for (int i = 0 ; i < width ; i++){
            for (int j = 0; j<height; j++){
                //System.out.print(i+ " "+j);
                counterPixels++;
                int distance = (int)(src.get(j,i)[0]*255);
                System.out.print(distance+" ");
                Point tmpPoint = new Point(i,j);
                pixelArray[i][j] = new  Pixel(Pixel.EMPTY,              //all empty at initialize
                                                distance,  //
                                                tmpPoint,
                        new Color(distance,distance,distance));
                if (pixelArray[i][j].distance == (double)0) {
                    //System.out.println("0.0");
                    pixelArray[i][j].isChecked = true;
                    counterIsChecked++;
                    pixelArray[i][j].state = Pixel.BORDER;
                }
            }
            System.out.println("");
        }
        return pixelArray;
    }

    /**
     * Function responsible for creating one point markers
     * for each local maximum in topographic distance
     * @param src 2dimensional array of pixel containing preprocessed image
     */
    private void startMarkers(Pixel[][] src){ //seed it
        int markersCount = 0;
        int width = src.length;
        int height = src[0].length;
        counterPixels = 0;
        for (int i = 0 ; i < width ; i++){
            for (int j = 0; j<height; j++){
                counterPixels++;
                Pixel currPix = src[i][j];
                //we only need to check each pixel once

                if(checkIfMax(src,i,j)){
                    boolean isPossible = true;
                    System.out.println("");


                    //System.out.println("Marking for distance: "+currPix.distance);
                    //System.out.println("With position x:"+i+" y:"+j);
                    markersCount++;
                    currPix.state=Pixel.newSeed();
                    for(int p1=-2;p1<3;p1++) {
                        for (int p2 = -2; p2 < 3; p2++) {


                            int x = p1 + i;
                            int y = p2 + j;
                            if(x<0 || x>src.length-1 || y<0 || y>src[0].length-1) {
                                System.out.print("NULL"+" ");
                            }
                            else {
                                System.out.print(src[x][y].distance + " " + src[x][y].state + " ");
                            }
                        }
                        System.out.println("");
                    }
                }
            }
        }
        System.out.println("Markers: "+markersCount);

    }

    /**
     * IMPORTANT isChecked flag be must set to false before entering this function
     * because it's just a helper recurrence function
     * @param src
     * @param p1
     * @param p2
     * @return checked if given pixel is local maxima of topographic distance
     */
    boolean checkIfMax(Pixel[][] src, int p1, int p2)
    {
        Pixel currPix = src[p1][p2];
        if (currPix.isChecked)
            return false;
        currPix.isChecked = true;
        counterIsChecked++;
        int sizeX = src.length;
        int sizeY = src[0].length;
        boolean returnFlag = true;
        int counter = 0;


        for(int i=-1;i<2;i++)
        {
            for(int j=-1;j<2;j++)
            {
                counter++;
                int x = p1 + i;
                int y = p2 + j;

                if(x<0 || x>sizeX-1 || y<0 || y>sizeY-1)
                    continue;

                if(i==0 && j == 0){
                    continue;
                }

                Pixel neighbouringPix = src[x][y];
                if (neighbouringPix.state > Pixel.BORDER)
                    return false;


                if(neighbouringPix.distance > currPix.distance) {

                    returnFlag = false;
                }

                if (neighbouringPix.distance < currPix.distance && !neighbouringPix.isChecked ){
                    neighbouringPix.isChecked = true;

                    counterIsChecked++;
                }
                if(neighbouringPix.state==0 || neighbouringPix.state==1)
                    continue;
                else
                {
                    //just in case it's false-positive max pixel, search neighbouring pixels for any other maxima
                    if(neighbouringPix.isChecked == false && neighbouringPix.distance >= currPix.distance) {
                        if (!checkIfMax(src, x, y))
                            returnFlag = false;
                    }
                }
            }
        }
        if (counter != 9) {
            System.out.println("DSADBJDLSABLDHJAS");
            System.out.println("Point:"+ currPix.pos.toString() );
        }


        return returnFlag;
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

    BufferedImage save(Pixel[][] src, int width, int height, String filename) throws IOException{
        BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int rgb=Pixel.colorMap.get((pixelArray[i][j].state)).getRGB();
                newImage.setRGB(i, j, rgb);
            }
        }
        File outfile = new File(filename);
        ImageIO.write(newImage, "png", outfile);
        return newImage;
    }
}

