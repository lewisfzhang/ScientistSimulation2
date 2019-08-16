import org.deeplearning4j.nn.modelimport.keras.KerasModelImport;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.io.ClassPathResource;
import java.util.*;

public class Neural {
    // indices by age
    ArrayList<NN_Func> V0_list = new ArrayList<>();
    ArrayList<NN_Func> V1_list = new ArrayList<>();

    public Neural(Config c) {
        // import function/array from python neural net
        // create separate function for each time period
        // store each function as element in array of functions

        // NOTE: MAYBE JUST USE TO SEND DATA TO A LOCALHOST SERVER that was started by python
    }
}

class NN_Func {
    Config config;

    public NN_Func(Config c) {
        config = c;

        try {
            String nn_path = String.format(config.parent_dir + "/data/nn/V%d_%d/model.h5", 0, 0);
            String simpleMlp = new ClassPathResource(nn_path).getFile().getPath();
            MultiLayerNetwork model = KerasModelImport.importKerasSequentialModelAndWeights(simpleMlp);

            int inputs = 5;
            INDArray features = Nd4j.zeros(inputs);
            for (int i=0; i<inputs; i++)
                features.putScalar(new int[] {i}, Math.random() < 0.5 ? 0 : 1);

            double prediction = model.output(features).getDouble(0);
            System.out.println(prediction);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}