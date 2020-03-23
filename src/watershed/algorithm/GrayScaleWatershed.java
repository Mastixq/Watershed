package watershed.algorithm;

import org.opencv.core.Mat;

public class GrayScaleWatershed extends BaseWatershed {

    public GrayScaleWatershed(String filename) {
        super(filename);
    }

    @Override
    public void calculate() {

    }

    @Override
    protected Mat preprocess(Mat srcMat) {
        return null;
    }
}
