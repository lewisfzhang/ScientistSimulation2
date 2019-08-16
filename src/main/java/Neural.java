import org.deeplearning4j.nn.modelimport.keras.KerasModelImport;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.io.ClassPathResource;
import java.util.*;

public class Neural {
    // indices by age
    ArrayList<NN_Func> V0_list = new ArrayList<>(); // q, T, max, mean, sds
    ArrayList<NN_Func> V1_list = new ArrayList<>(); // V0_idea1, V0_idea2, ..., V0_maxideas

    public Neural(Config config) {
        for (int i=0; i<config.tp_alive; i++) { // load all trained models
            V0_list.add(new NN_Func(0, i));
            V1_list.add(new NN_Func(1, i));
        }
    }
    public double predict(int type, int age, double[] in) {
        double out;

        if (type == 0) out = V0_list.get(age).predict(in);
        else out = V1_list.get(age).predict(in);

        return (out > 0) ? out : 0;
    }
}

class NN_Func {
    MultiLayerNetwork model;

    public NN_Func(int type, int age) { // import trained model
        try {
            String name = String.format("model_V%d_%d.h5", type, age);
            String simpleMlp = new ClassPathResource(name).getFile().getPath();
            model = KerasModelImport.importKerasSequentialModelAndWeights(simpleMlp);
            System.out.println("loaded "+name);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public double predict(double[] in) {
        INDArray input = Nd4j.zeros(1, in.length);
        for (int i=0; i<in.length; i++) input.putScalar(0, i, in[i]);
        return model.output(input).getDouble(0);
    }
}