import java.util.Random;

public class Functions {
    public static double get_random_double(double min, double max, Config c) {
        Random r = new Random();
        r.setSeed(c.get_next_seed());
        return min + ((max - min) * r.nextDouble()); // returns random number between min and max of uniform distribution
    }

    public static double get_normal_number(int mean, int sds, Config c) {
        Random r = new Random();
        r.setSeed(c.get_next_seed());
        return r.nextGaussian() * sds + mean;  // works according to this website: https://stackoverflow.com/questions/31754209/can-random-nextgaussian-sample-values-from-a-distribution-with-different-mean
    }
}
