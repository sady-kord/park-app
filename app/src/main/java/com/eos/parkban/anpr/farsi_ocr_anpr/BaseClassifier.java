package com.eos.parkban.anpr.farsi_ocr_anpr;

/**
 * Created by kordbacheh.s on 24/10/2016.
 */
import java.io.FileInputStream;
import java.io.IOException;


public abstract class BaseClassifier {
    public BaseClassifier()
    {
        beta = 1.0;
        Ln_1_beta = 1.0;
        rms_error = 0;
    }
    public abstract void load_from_stream(FileInputStream fp)throws IOException;
    public abstract String info();
    public abstract short get_output_size();
    public abstract double [] fore_prop(double input[]);

    public double beta, Ln_1_beta;//used for boosting
    public double rms_error;

}