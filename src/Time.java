import java.lang.Math;

public class Time {
    public long runtime;
    public long pause;
    public String name;

    public Time(String name) {
        this.runtime = System.currentTimeMillis();
        this.pause = 0;
        this.name = name;
    }

    public void stop_time() {
        long stop = System.currentTimeMillis();
        double time_elapsed = (stop - this.runtime)/1000.0;
        System.out.printf("%s elapsed runtime: %.3f seconds", this.name, time_elapsed);
        this.runtime = stop;
    }

    public void pause_time() {
        this.pause = System.currentTimeMillis();
    }

    public void resume_time() {
        long time = System.currentTimeMillis();
        if (this.pause == 0) {
            this.runtime = time;
        } else {
            long pause_length = time - this.pause;
            this.runtime += pause_length;
        }
    }

}
