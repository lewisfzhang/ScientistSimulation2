import java.io.IOException;
import java.util.Random;
import java.util.ArrayList;

class Functions {
    static double get_random_double(double min, double max, Config c) {
        Random r = new Random();
        r.setSeed(c.get_next_seed());
        return min + ((max - min) * r.nextDouble()); // returns random number between min and max of uniform distribution
    }

    static double get_normal_number(double mean, double sds, Config c) {
        Random r = new Random();
        r.setSeed(c.get_next_seed());
        // works according to this website: https://stackoverflow.com/questions/31754209/can-random-nextgaussian-sample-values-from-a-distribution-with-different-mean
        return r.nextGaussian() * sds + mean;
    }

    static int get_random_int(int min, int max, Config c) { // inclusive of min, exclusive of max --> [min, max)
        Random r = new Random();
        r.setSeed(c.get_next_seed());
        return min + r.nextInt(max - min);
    }

    // retrieved from https://stackoverflow.com/questions/1241555/algorithm-to-generate-poisson-and-binomial-random-numbers
    static int poisson(double lambda) {
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
    static void arr_increment_int(ArrayList<Integer> arr, int idx, int value) {
        int curr_val = arr.get(idx);
        arr.set(idx, curr_val + value);
    }

    // arr[idx] += val
    static void arr_increment_double(ArrayList<Double> arr, int idx, double value) {
        double curr_val = arr.get(idx);
        arr.set(idx, curr_val + value);
    }

    // sets all elements from idx to end to 0
    static void set_remain_zero(ArrayList<Integer> arr, int idx) {
        for (int i=idx; i<arr.size(); i++) {
            arr.set(i, 0);
        }
    }

    static void execCmd(String cmd) { // run shell commands
        try {
            Process proc = Runtime.getRuntime().exec(cmd);
        } catch (IOException e) {
            // exception handling
        }
    }

    static ArrayList<Integer> arr_double_to_int(ArrayList<Double> arr) {
        ArrayList<Integer> out = new ArrayList<Integer>();
        for(Double d : arr){
            out.add(d.intValue());
        }
        return out;
    }
}
