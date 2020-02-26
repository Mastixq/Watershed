package watershed.operations;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Map;

public class ImageBase {

    public static Mat BufferedImage2Mat(BufferedImage image) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", byteArrayOutputStream);
        byteArrayOutputStream.flush();
        return Imgcodecs.imdecode(new MatOfByte(byteArrayOutputStream.toByteArray()), -1);
    }

    public static BufferedImage Mat2BufferedImage(Mat matrix) throws IOException {
        MatOfByte mob = new MatOfByte();
        Imgcodecs.imencode(".jpg", matrix, mob);
        return ImageIO.read(new ByteArrayInputStream(mob.toArray()));
    }

    public static BufferedImage save(Pixel[][] src, int width, int height, String filename, Map<Integer, Color> colorMap) throws IOException {
        BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int rgb = colorMap.get((src[i][j].state)).getRGB();
                newImage.setRGB(i, j, rgb);
            }
        }
        File outfile = new File(filename);
        ImageIO.write(newImage, "png", outfile);
        return (BufferedImage) newImage;
    }

    public static BufferedImage saveGradient(Pixel[][] src, int width, int height, String filename, Map<Integer, Color> colorMap) throws IOException {
        BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int rgb = src[i][j].color.getRGB();
                newImage.setRGB(i, j, rgb);
            }
        }
        File outfile = new File(filename);
        ImageIO.write(newImage, "png", outfile);
        return (BufferedImage) newImage;
    }

    public static Pixel[][] toPixelArrayTopographic(Mat src) {
        int width = (int) src.size().width;
        int height = (int) src.size().height;
        Pixel[][] pixelArray = new Pixel[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                double value = (src.get(j, i)[0]);
                Point tmpPoint = new Point(i, j);
                pixelArray[i][j] = new Pixel(Pixel.EMPTY,
                        value,
                        tmpPoint);
                if (pixelArray[i][j].value == (double) 0) {
                    pixelArray[i][j].isChecked = true;
                    pixelArray[i][j].state = Pixel.BORDER;
                }
            }
        }
        return pixelArray;
    }


    public static void exportDataToExcel(String fileName, Pixel[][] data) throws FileNotFoundException, IOException {
        File file = new File(fileName);
        if (!file.isFile())
            file.createNewFile();


        int rowCount = data.length;

        for (int i = 0; i < rowCount; i++) {
            int columnCount = data[i].length;
            String[] values = new String[columnCount];
            for (int j = 0; j < columnCount; j++) {
                values[j] = data[i][j].value + "";
                if (data[i][j].state > 1) {
                    values[j] += "XDDDDDDD";
                }
            }
        }

    }
}
