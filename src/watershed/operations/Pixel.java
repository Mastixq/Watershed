package watershed.operations;

import java.awt.*;
import java.util.HashMap;
import java.util.Random;

public class Pixel {
    public Point pos;
    public double distance;
    public int state;
    public int isMax;
    public boolean isChecked;
    public final static int UNDEF = 0;
    public final static int NOTMAX = 1;
    public final static int MAX = 2;

    public static int nextSeed = 2;
    public final static int EMPTY = 0;
    public final static int BORDER = 1;
    static HashMap<Integer,Color> colorMap;

    static{
        colorMap = new HashMap();
        colorMap.put(0, Color.white);
        colorMap.put(1, Color.black);
    }

    public Pixel(int state, int distance, Point pos){
        this.isChecked = false;
        this.isMax = UNDEF;
        this.state = state;
        this.distance = distance;
        this.pos = pos;
    }



    public int newSeed(){
        Random gen = new Random();
        Color nextColor = new Color(gen.nextInt(256),gen.nextInt(256),gen.nextInt(256));
        colorMap.put(nextSeed,nextColor);
        return nextSeed++;
    }

}
