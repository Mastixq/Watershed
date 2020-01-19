package watershed.operations;

import java.awt.*;
import java.util.HashMap;
import java.util.Random;

public class Pixel {
    public Point pos;
    public double distance;
    public int state;
    public boolean isChecked;

    public static int nextSeed = 2;
    public final static int EMPTY = 0;
    public final static int BORDER = 1;
    public static HashMap<Integer,Color> colorMap;

    static{
        colorMap = new HashMap();
        colorMap.put(0, Color.white);
        colorMap.put(1, Color.black);
    }

    public Pixel(int state, double distance, Point pos){
        this.isChecked = false;
        this.state = state;
        this.distance = distance;
        this.pos = pos;
    }
    public Pixel(int state, double distance, Point pos, Color color){
        this.isChecked = false;
        this.state = state;
        this.distance = distance;
        this.pos = pos;

    }
    public static int newSeed(Color color){
        colorMap.put(nextSeed,color);
        return nextSeed++;
    }


    public static int newSeed(){
        Random gen = new Random();
        Color nextColor = new Color(gen.nextInt(256),gen.nextInt(256),gen.nextInt(256));
        colorMap.put(nextSeed,nextColor);
        return nextSeed++;
    }

}
