import java.io.*;
import java.util.Arrays;
import java.util.Random;
import java.util.ArrayList;
import java.util.HashMap;

class Functions {
    static double get_random_double(double min, double max, Config c) { // [min,max)
        Random r = new Random(c.get_next_seed());
        return min + ((max - min) * r.nextDouble()); // returns random number between min and max of uniform distribution
    }

    static double get_normal_number(double mean, double sds, Config c) {
        Random r = new Random(c.get_next_seed());
        // works according to this website: https://stackoverflow.com/questions/31754209/can-random-nextgaussian-sample-values-from-a-distribution-with-different-mean
        return r.nextGaussian() * sds + mean;
    }

    static int get_random_int(int min, int max, Config c) { // inclusive of min, exclusive of max --> [min, max)
        Random r = new Random(c.get_next_seed());
        return min + r.nextInt(max - min);
    }

    // retrieved from https://stackoverflow.com/questions/1241555/algorithm-to-generate-poisson-and-binomial-random-numbers
    static int poisson(double lambda, Config c) {
        Random r = new Random(c.get_next_seed());

        double L = Math.exp(-lambda);
        double p = 1.0;
        int k = 0;

        do {
            k++;
            p *= r.nextDouble();
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

    static double round_double(double x) { // rounds to nearest tenth
        return Math.round(x * 10)/10.0;
    }

    static void double_arraylist_2d_to_csv(ArrayList<ArrayList<Double>> arr, String filename, String col_title, Model model) {
        StringBuilder str1 = new StringBuilder(col_title);
        StringBuilder str2 = new StringBuilder();
        int max = 0;
        for (int i = 0; i < arr.size(); i++) {
            ArrayList<Double> a1 = arr.get(i);
            if (a1.size() > max) max = a1.size();
            str2.append(i);
            for (double x : a1) {
                str2.append(',').append(Functions.round_double(x)); // round to nearest tenth
            }
            str2.append(System.lineSeparator());
        }
        for (int i = 0; i < max; i++) {
            str1.append(",").append(i);
        }
        str1.append(System.lineSeparator()).append(str2.toString());
        write_csv(String.format("%s/data/model/%s.csv", model.config.parent_dir, filename), str1.toString());
    }

    static void int_arraylist_2d_to_csv(ArrayList<ArrayList<Integer>> arr, String filename, String col_title, Model model) {
        StringBuilder str1 = new StringBuilder(col_title);
        StringBuilder str2 = new StringBuilder();
        int max = 0;
        for (int i = 0; i < arr.size(); i++) {
            ArrayList<Integer> a1 = arr.get(i);
            if (a1.size() > max) max = a1.size();
            str2.append(i);
            for (int x : a1) {
                str2.append(',').append(x);
            }
            str2.append(System.lineSeparator());
        }
        for (int i = 0; i < max; i++) {
            str1.append(",").append(i);
        }
        str1.append(System.lineSeparator()).append(str2.toString());
        write_csv(String.format("%s/data/model/%s.csv", model.config.parent_dir, filename), str1.toString());
    }

    static void hashmap_to_csv(HashMap<String, String> map, int num_col, String filename, String col_title, Model model) {
        StringBuilder str = new StringBuilder(col_title);
        for (int i=0; i<num_col; i++) str.append(",").append(i);
        str.append(System.lineSeparator());
        for (String key : map.keySet()) {
            str.append(key).append(",").append(map.get(key)).append(System.lineSeparator());
        }
        write_csv(String.format("%s/data/model/%s.csv", model.config.parent_dir, filename), str.toString());
    }

    static String double_arraylist_to_csv_string(ArrayList<Double> arr) {
        StringBuilder str = new StringBuilder().append(arr.get(0));
        for (int i=1; i<arr.size(); i++) {
            str.append(",").append(Functions.round_double(arr.get(i))); // round to nearest tenth
        }
        return str.toString();
    }

    static String int_arraylist_to_csv_string(ArrayList<Integer> arr) {
        StringBuilder str = new StringBuilder().append(arr.get(0));
        for (int i=1; i<arr.size(); i++) {
            str.append(",").append(arr.get(i));
        }
        return str.toString();
    }

    static void write_csv(String path, String data) {
        BufferedWriter bw;
        try {
            bw = new BufferedWriter(new FileWriter(new File(path)));
            bw.write(data);
            bw.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    static ArrayList<ArrayList<Integer>> int_arr2d_to_list2d(int[][] a) {
        ArrayList<ArrayList<Integer>> out = new ArrayList<>();
        for (int[] a1 : a) {
            ArrayList<Integer> temp = new ArrayList<>();
            for (int x : a1) {
                temp.add(x);
            }
            out.add(temp);
        }
        return out;
    }

    static ArrayList<ArrayList<Double>> double_arr2d_to_list2d(double[][] a) {
        ArrayList<ArrayList<Double>> out = new ArrayList<>();
        for (double[] a1 : a) {
            ArrayList<Double> temp = new ArrayList<>();
            for (double x : a1) {
                temp.add(x);
            }
            out.add(temp);
        }
        return out;
    }

    static void string_to_csv(String data, String path, boolean append) {
        BufferedWriter bw; // leave this section of duplicated code because we are "appending" to the file
        try {
            bw = new BufferedWriter(new FileWriter(path, append));
            bw.write(data);
            bw.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    static void serialize_model(Model model, Config config) {
        String filename = String.format("%s/data/saved_objects/model.ser", config.parent_dir);
        try {
            //Saving of object in a file
            FileOutputStream file = new FileOutputStream(new File(filename));
            ObjectOutputStream out = new ObjectOutputStream(file);

            // Method for serialization of object
            out.writeObject(model);

            out.close();
            file.close();

            System.out.println("\nModel object has been serialized");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static Model deserialize_model(Config config) {
        Model object1 = null;
        String filename = String.format("%s/data/saved_objects/model.ser", config.parent_dir);

        try {
            // Reading the object from a file
            FileInputStream file = new FileInputStream(filename);
            ObjectInputStream in = new ObjectInputStream(file);

            // Method for deserialization of object
            object1 = (Model)in.readObject();

            in.close();
            file.close();

            System.out.println("Object has been deserialized ");
            return object1;
        } catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
