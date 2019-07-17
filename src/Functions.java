import java.util.Random;
import java.util.ArrayList;

public class Functions {
    public static double get_random_double(double min, double max, Config c) {
        Random r = new Random();
        r.setSeed(c.get_next_seed());
        return min + ((max - min) * r.nextDouble()); // returns random number between min and max of uniform distribution
    }

    public static double get_normal_number(int mean, int sds, Config c) {
        Random r = new Random();
        r.setSeed(c.get_next_seed());
        // works according to this website: https://stackoverflow.com/questions/31754209/can-random-nextgaussian-sample-values-from-a-distribution-with-different-mean
        return r.nextGaussian() * sds + mean;
    }

    // retrieved from https://stackoverflow.com/questions/1241555/algorithm-to-generate-poisson-and-binomial-random-numbers
    public static int poisson(double lambda) {
        double L = Math.exp(-lambda);
        double p = 1.0;
        int k = 0;

        do {
            k++;
            p *= Math.random();
        } while (p > L);

        return k - 1;
    }

    // arr[idx] += val
    public static void arr_increment_int(ArrayList<Integer> arr, int idx, int value) {
        int curr_val = arr.get(idx);
        arr.set(idx, curr_val + value);
    }

    // arr[idx] += val
    public static void arr_increment_double(ArrayList<Double> arr, int idx, double value) {
        double curr_val = arr.get(idx);
        arr.set(idx, curr_val + value);
    }
}