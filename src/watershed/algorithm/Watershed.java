package watershed.algorithm;

import watershed.operations.Pixel;
import watershed.operations.ImageBase;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;

import java.awt.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;

//Image image = SwingFXUtils.toFXImage(capture, null);
public class Watershed {
    private Mat processImg;
    private Pixel[][] pixelArray;
    private int width, height;

    HashSet<Pixel> pixelSet;
    PriorityQueue<Pixel> queue;
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
        ImageBase.save(pixelArray,width,height, "processed.png");

        pixelSet = new HashSet<>();
        queue = new PriorityQueue<>(Pixel.distanceComparator);
        startDistanceMarkers(pixelArray);
        ImageBase.exportDataToExcel("test.csv", pixelArray);
        watershedMarked();
        System.out.println("end markers");

        HashMap<Integer,Color> colorMap = Pixel.colorMap;
        ImageBase.save(pixelArray,pixelArray.length,pixelArray[0].length, "marked.png");

    }

    /**
     * Function responsible for creating one point markers
     * for each local maximum in topographic distance
     * @param src 2-dimensional array of pixel containing preprocessed image
     */
    private void startDistanceMarkers(Pixel[][] src){ //seed it
        int width = src.length;
        int height = src[0].length;
        int counter = 0;
        for (int i = 0 ; i < width ; i++){
            for (int j = 0; j<height; j++){
                Pixel currPix = src[i][j];
                if(checkIfMaxDistance(src,i,j)){
                    currPix.state=Pixel.newSeed();
                    counter++;
                    addNeighbouringToQueue(i,j);
                }
            }
        }
        System.out.println(counter);
        System.out.println("Last seed: "+Pixel.nextSeed);

    }

    /**
     * IMPORTANT isChecked flag be must set to false before entering this function
     * because it's just a helper recurrence function
     * @param src
     * @param p1
     * @param p2
     * @return checked if given pixel is local maxima of topographic distance
     */
    boolean checkIfMaxDistance(Pixel[][] src, int p1, int p2) {
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
                        if (!checkIfMaxDistance(src, x, y))
                            returnFlag = false;
                    }
                }
            }
        }
        return returnFlag;
    }

    boolean checkIfMaxGradient(Pixel[][] src, int x, int y) {
        Pixel currPix = src[x][y];
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
                int p1 = x + i;
                int p2 = y + j;

                if(p1<0 || p1>sizeX-1 || p2<0 || p2>sizeY-1)
                    continue;
                if(i==0 && j == 0){
                    continue;
                }

                Pixel neighbouringPix = src[p1][p2];

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
                        if (!checkIfMaxDistance(src, p1, p2))
                            returnFlag = false;
                    }
                }
            }
        }
        return returnFlag;
    }

    private void watershedMarked(){
        while(!queue.isEmpty()){
            Pixel currPix = queue.remove();
            int stateCandidate = -1;
            for(int i=-1;i<2;i++) {
                for (int j = -1; j < 2; j++) {
                    int p1 = currPix.pos.x + i;
                    int p2 = currPix.pos.y + j;
                    if(p1<0 || p1>width-1 || p2<0 || p2>height-1)
                        continue;
                    if(i==0 && j == 0)
                        continue;
                    Pixel processingPix = pixelArray[p1][p2];
                    if (processingPix.state == Pixel.EMPTY || processingPix.state == Pixel.BORDER)
                        continue;
                    if (stateCandidate == -1)
                        stateCandidate = processingPix.state;
                    if (stateCandidate != processingPix.state)
                        stateCandidate = processingPix.BORDER;
                }
            }
            if (stateCandidate == -1){
                stateCandidate = Pixel.EMPTY;
            }
            currPix.state = stateCandidate;
            addNeighbouringToQueue(currPix.pos.x, currPix.pos.y);


        }

    }
    private void watershedGradient(){

    }

    //TODO dodawanie do kolejki pustych pixeli wokol ziaren
    //Priority queue by distance
    private void addNeighbouringToQueue(int x, int y){
        for(int i=-1;i<2;i++)
        {
            for(int j=-1;j<2;j++)
            {
                int p1 = x + i;
                int p2 = y + j;

                if(p1<0 || p1>width-1 || p2<0 || p2>height-1)
                    continue;
                if(i==0 && j == 0){
                    continue;
                }
                Pixel currPix = pixelArray[p1][p2];

                if ( pixelArray[p1][p2].state == 0) {
                    if (pixelSet.add(currPix)){
                        queue.add(currPix);
                    }
                    else{
                        continue;
                    }
                }

            }
        }
    }
    //TODO remove straight lines

    //TODO remove end of the lines (only 1 in moore)

    //TODO create markers on blured image, but place them on morphology separately
}

