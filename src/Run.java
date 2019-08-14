public class Run {
    public static void main(String[] args) {
        boolean run_model = true;
        boolean save_model = true;
        boolean use_collect = true;
        boolean append_data = false;
        Config config = new Config();
        Time tot = new Time("total time");
        Model model;
        Collect collect;

        if (run_model) {
            model = new Model(config);

            for (int i = 0; i < config.time_periods; i++) {
                System.out.printf("%nstep%d", i);
                Time t = new Time("model step");
                model.step();
                t.stop_time();
            }

            if (save_model) Functions.serialize_model(model, config);
        } else model = Functions.deserialize_model(config);

        if (use_collect) {
            collect = new Collect(model);
            Time t = new Time("collect");
            collect.collect_data();
            collect.neural_net_data(append_data);
            t.stop_time();

            tot.stop_time();
        }
    }
}