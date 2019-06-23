package com.eos.parkban.anpr.farsi_ocr_anpr;

/**
 * Created by kordbacheh.s on 24/10/2016.
 */

import java.io.FileInputStream;
import java.io.IOException;
import java.security.InvalidParameterException;


public class MLP extends BaseClassifier{

    public enum EActivationFunction{afSigmoid, afLinear};
    final int 	 DEFAULT_HIDDEN1		= 100;
    final double DEFAULT_INW_LR0		= 0.2;
    final double DEFAULT_INW_LR1		= 0.05;
    final double DEFAULT_weight_lr		= 0.3;
    final double DEFAULT_weight_lr_min	= 0.05;
    final double DEFAULT_mean_lr		= 0.01;
    final double DEFAULT_mean_lr_min	= 0.001;
    final double DEFAULT_spread_lr		= 0.005;
    final double DEFAULT_spread_lr_min	= 0.001;
    final int 	 DEFAULT_LR_CHANGE_EP	= 5;
    final double DEFAULT_LR_CHANGE_FACTOR = 0.9; //only one of this and next must be non zero
    final double DEFAULT_LR_DEC_VAL		= 0;
    final double DEFAULT_ERROR_GOAL		= 0.01;

    public class SMLPParams
    {
        SMLPParams(){
            nHidden = new int[3];
            nHidden[0] = DEFAULT_HIDDEN1;
            nHidden[1] = 0;
            nHidden[2] = 0;
            weight_lr 			= DEFAULT_weight_lr;
            weight_lr_min 		= DEFAULT_weight_lr_min;
            lr_change_factor 	= DEFAULT_LR_CHANGE_FACTOR;
            lr_dec_val			= DEFAULT_LR_DEC_VAL;
            lr_change_epochs 	= DEFAULT_LR_CHANGE_EP;
            error_goal       	= DEFAULT_ERROR_GOAL;
            Output_AF			= EActivationFunction.afSigmoid;
            squashing_factor 	= 1.0;
            mom 				= 0.5f;
            spo 				= 0;
        }

        public int nHidden[];// number of neurons in hidden layers
        public double weight_lr; // initial learning rate for weight adjustment (default = 0.3)
        public double weight_lr_min; // final learning rate for weight adjustment (default = 0.03)
        public double squashing_factor;
        public int lr_change_epochs; //change (usually decrease) learning rates after every rate_change_epochs
        public double lr_change_factor; //a number less than 1
        public double lr_dec_val; //may not be used! but if used, lr_change_factor must be zero
        public double error_goal;//
        public float mom; //momentum
        public float spo; //sigmoid prime offset
        public EActivationFunction Output_AF; //output Activation Function
    };

    final int MAX_LAYER = 5;
    private	short m_nClasses;//may differ from output layer size(when using ECOC)
    private short num_layer;
    private short layer_size[] = new short[MAX_LAYER];
    private byte fea_codes[];
    public SMLPParams params;
    public double weight [][][];
    public double bias [][];
    public double output [][];
    //delta for each neuron in each layer
    public double delta [][];
    //dw_olds are used only when using Momentum to hold previous weight adjustments
    public double dw_old [][][];
    public double dw_bias_old [][];


    static double sigmoid(double x)
    {
        return 1.0/(1.0 + Math.exp(-x));
    }

    public short get_output_size()
    {
        return layer_size[num_layer-1];
    }

    public String info()
    {
        String s = "";
        if(num_layer > 0)
            s = layer_size[0] + " : " + layer_size[1] + " : " + layer_size[2];
        return s;
    }
    public void load_from_stream(FileInputStream fp)throws IOException
    {
        //Read a small header('MLP' + Version)
        byte str[] = new byte[7];//{"MLP 1.0"}
        fp.read(str, 0, 7);

        if(str[0] != 'M' || str[6] != '0')
            throw new InvalidParameterException();

        num_layer = FileUtil.read_short(fp);

        for(short i = 0; i < MAX_LAYER; i++)
            layer_size[i] = FileUtil.read_short(fp);

        beta = FileUtil.read_double(fp);
        Ln_1_beta = Math.log(1/beta);

        rms_error = FileUtil.read_double(fp);

        ////////////////////////////////////////////////////////
        //SMLPParams 88 bytes
        params = new SMLPParams();

        params.nHidden[0] 		= FileUtil.read_int(fp);
        params.nHidden[1] 		=FileUtil.read_int(fp);
        params.nHidden[2] 		= FileUtil.read_int(fp);

        //dummy read for 8-byte struct alignment
        FileUtil.read_int(fp);

        params.weight_lr  		= FileUtil.read_double(fp);
        params.weight_lr_min  	= FileUtil.read_double(fp);
        params.squashing_factor = FileUtil.read_double(fp);
        params.lr_change_epochs = FileUtil.read_int(fp);

        //dummy read for 8-byte struct alignment
        FileUtil.read_int(fp);

        params.lr_change_factor = FileUtil.read_double(fp);
        params.lr_dec_val 		= FileUtil.read_double(fp);
        params.error_goal 		= FileUtil.read_double(fp);
        params.mom 				= FileUtil.read_float(fp);
        params.spo 				= FileUtil.read_float(fp);
        int af 					= FileUtil.read_int(fp);
        params.Output_AF		= EActivationFunction.values()[af];
        //dummy read for 8-byte struct alignment
        FileUtil.read_int(fp);
        /////////////////////////////////////////////////////////
        int length = FileUtil.read_int(fp);
        if(length > 0)
        {
            fea_codes = new byte[length];
            fp.read(fea_codes, 0, length);
        }

        load_weights(fp);

        output = new double[num_layer][];
        dw_old = new double[num_layer-1][][];
        dw_bias_old = new double[num_layer-1][];
        delta = new double[num_layer-1][];
        for (int i = 0; i < num_layer-1; i++)
        {
            output[i]  = new double[layer_size[i]];
            dw_old[i]  = new double[layer_size[i+1]] [layer_size[i]];
            dw_bias_old[i] = new double[layer_size[i+1]];
            delta[i] = new double[layer_size[i+1]];
        }

        output[num_layer-1] = new double[layer_size[num_layer-1]];

        //return 0;
    }

    private void load_weights(FileInputStream fp) throws IOException
    {
        weight = new double[num_layer][][];
        bias = new double[num_layer][];
        for (int i = 0; i < num_layer; i++)
        {
            int height = FileUtil.read_int(fp);
            int width = FileUtil.read_int(fp);
            weight[i] = new double [height][width];

            for(int y = 0; y < height; y++)
                for(int x = 0; x < width; x++)
                    weight[i][y][x] = FileUtil.read_double(fp);

            int len = FileUtil.read_int(fp);
            if(len > 0)
            {
                bias[i] = new double[len];
                for(int j = 0; j < height; j++)
                    bias[i][j] = FileUtil.read_double(fp);
            }
        }
    }

    public double [] fore_prop(double input[])
    {
        output[0] = input;//
        int i,y,x;
        double weighted_sum = 0;//it is very faster than using output[i+1][y] directly;
        for (i = 0; i < num_layer-1; i++){
            for (y = 0; y < weight[i].length; y++){//weight matrix between layers i and i+1
                weighted_sum = 0;//it is very faster than using output[i+1][y] directly;
                for (x = 0; x < weight[i][y].length; x++)
                    weighted_sum += output[i][x]*weight[i][y][x];//these operators are faster than Get
                weighted_sum += bias[i][y];//bias connect
                output[i+1][y] = sigmoid(weighted_sum);// sigmoid function
            }
        }

        return output[num_layer-1];
    }

}
