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
    private int width, height;

    public Watershed(String filename) throws IOException {
        Mat srcImage = Imgcodecs.imread(filename);
        if (srcImage.empty()) {
            System.err.println("Cannot read image: " + filename);
            System.exit(0);
        }
        width = (int)srcImage.size().width;
        height = (int)srcImage.size().height;
        Mat processed = ImageBase.initialProcess(srcImage);
        pixelArray = ImageBase.toPixelArray(processed);
        ImageBase.save(pixelArray,pixelArray.length,pixelArray[0].length, "processed.png");
        startMarkers(pixelArray);
        System.out.println("end markers");

        HashMap<Integer,Color> colorMap = Pixel.colorMap;
        ImageBase.save(pixelArray,pixelArray.length,pixelArray[0].length, "marked.png");
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
                if(checkIfMax(src,i,j)){
                    currPix.state=Pixel.newSeed();
                }
            }
        }
    }

    /**
     * IMPORTANT isChecked flag be must set to false before entering this function
     * because it's just a helper recurrence function
     * @param src
     * @param p1
     * @param p2
     * @return checked if given pixel is local maxima of topographic distance
     */
    boolean checkIfMax(Pixel[][] src, int p1, int p2) {
        Pixel currPix = src[p1][p2];
        if (currPix.isChecked)
            return false;
        currPix.isChecked = true;
        int sizeX = src.length;
        int sizeY = src[0].length;
        boolean returnFlag = true;

        for(int i=-1;i<2;i++)
        {
            for(int j=-1;j<2;j++)
            {
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

