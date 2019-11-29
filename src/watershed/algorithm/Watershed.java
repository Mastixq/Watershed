package watershed.algorithm;


import org.opencv.core.*;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.photo.Photo;

import java.util.Arrays;

public class Watershed {
    private Mat srcOriginal;
    private Mat processImg;
    public Watershed(String filename)
    {
        srcOriginal = Imgcodecs.imread(filename);
        if (srcOriginal.empty()) {
            System.err.println("Cannot read image: " + filename);
            System.exit(0);
        }
        initialProcess();
    };


    private void initialProcess()
    {
        processImg = srcOriginal.clone();
        Mat gray = new Mat(srcOriginal.size(), CvType.CV_8U);
        Mat morph = new Mat(srcOriginal.size(), CvType.CV_8U);
        int kernelSize = 1;
        Mat element = Imgproc.getStructuringElement(Imgproc.CV_SHAPE_RECT, new Size(2 * kernelSize + 1, 2 * kernelSize + 1),
                new Point(kernelSize, kernelSize));




//        Imgproc.dilate(srcOriginal,processImg,element);
//        Imgproc.dilate(processImg,processImg,element);
//        Imgproc.dilate(processImg,processImg,element);
//        Imgproc.dilate(processImg,processImg,element);
//        Imgproc.dilate(processImg,processImg,element);
        Imgproc.cvtColor(srcOriginal,gray,Imgproc.COLOR_BGR2GRAY);
        HighGui.imshow("Color2GRAY", gray);
        Imgproc.threshold(gray,processImg,0,255,Imgproc.THRESH_OTSU);
        HighGui.imshow("THRESHOLD OTSU", processImg);
        int array[][] = {{2,2,2},{2,2,2},{2,2,2}};
        Mat kernelMat = new Mat(3,3,CvType.CV_8UC1);
        Imgproc.morphologyEx(processImg,morph,Imgproc.MORPH_OPEN,kernelMat);
        //cvtColor(processImg, gray, Imgproc.COLOR_BGR2GRAY);
        HighGui.imshow("MorphologyEx", morph);
        processImg = grayHistogram(srcOriginal);
       // HighGui.imshow("Hist", processImg);
        HighGui.waitKey( 0 );
    }
    private Mat grayHistogram(Mat src){
        Mat histMat = new Mat();
        Imgproc.cvtColor(src,src,Imgproc.COLOR_BGR2GRAY);
        int array[][] = {{2,2,2},{2,2,2},{2,2,2}};
        Mat kernelMat = new Mat(3,3,CvType.CV_8UC1);

        Mat hist_1 = new Mat();

        MatOfFloat ranges = new MatOfFloat(0f, 256f);
        MatOfInt histSize = new MatOfInt(25);

        Imgproc.calcHist(Arrays.asList(src), new MatOfInt(0),
                new Mat(), hist_1, histSize, ranges);

        //HighGui.imshow("histogram", hist_1);
        return hist_1;
    }





}
