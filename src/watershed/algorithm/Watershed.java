package watershed.algorithm;

import watershed.operations.Pixel;
import watershed.operations.ImageBase;

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
        processsed = ImageBase.initialProcess(srcOriginal);
        System.out.println("leaving initialProcess");
        pixelArray = ImageBase.toPixelArray(processsed);
        ImageBase.save(pixelArray,pixelArray.length,pixelArray[0].length, "processed.png");
        System.out.println("leaving to pixel array");
        startMarkers(pixelArray);
        System.out.println("end markers");
        HashMap<Integer,Color> colorMap = Pixel.colorMap;
        ImageBase.save(pixelArray,pixelArray.length,pixelArray[0].length, "marked.png");
        System.out.println(counterIsChecked);
        System.out.println(counterPixels);
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
                if(neighbouringPix.state==1)
                    continue;

                else
                {
                    //just in case it's false-positive max pixel, search neighbouring pixels for any other maxima
                    if(neighbouringPix.isChecked == false && neighbouringPix.distance == currPix.distance) {
                        if (!checkIfMax(src, x, y))
                            returnFlag = false;
                    }
                }
            }
        }


        return returnFlag;
    }

    private void watershedMarked(){

    }





}

