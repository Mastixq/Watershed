package watershed.operations;

import java.awt.*;
import java.util.Comparator;

public class Pixel {
    public Point pos;
    public double value;
    public int state;
    public boolean isChecked;

    public Color color;

    public final static int EMPTY = 0;
    public final static int BORDER = 1;

    public Pixel(int state, double value, Point pos){
        this.isChecked = false;
        this.state = state;
        this.value = value;
        this.pos = pos;
    }

    public Pixel(int state, double value, Point pos, Color color){
        this.isChecked = false;
        this.state = state;
        this.value = value;
        this.pos = pos;
        this.color = color;
    }

    public static Comparator<Pixel> distanceComparator = new Comparator<Pixel>() {
        @Override
        public int compare(Pixel p1, Pixel p2) {
            return (int) (p2.value - p1.value);
        }
    };

    public static Comparator<Pixel> distanceMinimaComparator = new Comparator<Pixel>() {
        @Override
        public int compare(Pixel p1, Pixel p2) {
            return (int) (p1.value - p2.value);
        }
    };

}
