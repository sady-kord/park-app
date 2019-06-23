package com.eos.parkban.anpr.farsi_ocr_anpr;

/**
 * Created by kordbacheh.s on 24/10/2016.
 */
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.Arrays;

public class MCS {

    public enum ECombinationMethod{cmAvg, cmAdaBoostM1, cmAdaBoostM2, cmMajority, cmRanking, cmNone};
    final int MAX_CLASS = 100;
    final int MAX_CLASSIFIER = 20;	//Maximum number of classifiers combined in a single MCS


    public class SClassifierResult
    {
        public int winCode = 0;//winner code
        public double winVal = 0;//winner value
        public String winString = "";//winner string(e.g. "A" for 10)
        public double Certainty = 0;//Certainty Confidence, max1*(max1-max2), the ideal value is 1;
        public double Confidences[];//All output confidences including the winVal
    };

    public double RecResult[];
    public ECombinationMethod m_eCmbMethod;
    public short m_nClassifier; //number of MLP/RBF/WRBF Classifiers
    public short m_nClass; //number of classes
    public BaseClassifier NNList[]; //List of all RBF/WRBF/MLP Classifiers
    private byte[] fea_codes;//feature codes
    private double sum_beta; //used for boosting M1
    private double sum_Ln_1_beta; //used for boosting M2
    public String m_strComments, CodeNames[];


    SClassifierResult RecognizeBoostM2(double pattern[])
    {
        assert(m_nClassifier > 0);
        assert(pattern != null);

        double output[];
        SClassifierResult Result = new SClassifierResult();
        int i,j;
        Arrays.fill(RecResult, 0);
        double m1 = -1, m2 = -1;
        Result.winCode = -1;
        Result.winString = "";
        for(i = 0;  i < m_nClassifier; i++){
            output = NNList[i].fore_prop(pattern);
            for(j = 0; j < m_nClass; j++)
                RecResult[j] += output[j]*NNList[i].Ln_1_beta;
        }
        for(j = 0; j < m_nClass; j++){
            RecResult[j] /= sum_Ln_1_beta;//to be normalized to 1
            if(RecResult[j] > m1){
                Result.winCode = j;
                m2 = m1;
                m1 = RecResult[j];
                if(RecResult[j] > 0.9)//the situation in which two characters have confidences above 0.9 never occurs
                    break;
            }
            else if (RecResult[j] > m2){
                m2 = RecResult[j];
            }
        }
        for(j = j+1; j < m_nClass; j++)
            RecResult[j] /= sum_Ln_1_beta;//to be normalized to 1
        if(CodeNames.length > Result.winCode)
            Result.winString = CodeNames[Result.winCode];
        Result.Confidences = RecResult;//Dangerous Risk
        Result.winVal = m1;
        Result.Certainty = m1*(m1-m2);// ComputeCertainty();*/
        return Result;
    }

    //تابع زیر کامل شده است
    public int load(String FileName) throws IOException
    {
        FileInputStream fp = new FileInputStream(FileName);
        DataInputStream dis = new DataInputStream(fp);
        //Read a small header('MCS' + Version)
        byte str[] = new byte[7];//{"MCS 1.0"};//Multiple Classifier File
        fp.read(str, 0, 7);

        if(str[0] != 'M' || str[6] != '0'){
            dis.close();
            fp.close();
            throw new InvalidParameterException();
        }

        //byte ReservedHeader[] = new byte[400];//400 bytes reserved size
        //fp.read(ReservedHeader, 0, 400);

        fp.skip(400);//400 bytes reserved size
        byte CmbMethod = 0;
        CmbMethod = dis.readByte();//(byte)fp.read();
        m_eCmbMethod = ECombinationMethod.values()[CmbMethod];

        //////////////////////////////////////
        int length = FileUtil.read_int(fp);
        if(length > 0)
        {
            fea_codes = new byte[length];
            fp.read(fea_codes, 0, length);
        }

        sum_beta = 0;
        sum_Ln_1_beta = 0;
        m_nClassifier = FileUtil.read_short(fp);

        NNList = new BaseClassifier[m_nClassifier];

        for (int i = 0; i < m_nClassifier; i++){
            NNList[i] = new MLP();
            NNList[i].load_from_stream(fp);
            if(i == 0)
                m_nClass = NNList[i].get_output_size();
            else
            if(m_nClass != NNList[i].get_output_size())
                throw new InvalidParameterException();
            sum_beta += NNList[i].beta;
            sum_Ln_1_beta += NNList[i].Ln_1_beta;
        }

        if(m_nClass > 0)
            RecResult = new double[m_nClass];

        //////////////////////////////////////////////////////////////////////////
        //byte n[] = new byte[1];
        byte n = (byte)fp.read();;

        CodeNames = new String[n];

        for (int i = 0; i < n; i++)
        {
            short tmpW = FileUtil.read_short(fp);
            CodeNames[i] = "";
            for(int j = 0; j < tmpW; j++)
                CodeNames[i] += (char) FileUtil.read_short(fp);
        }
        //////////////////////////////////////////////////////////////////////////
        short tmpW = FileUtil.read_short(fp);
        m_strComments = "";
        for(int j = 0; j < tmpW; j++)
            m_strComments += (char) FileUtil.read_short(fp);

        //////////////////////////////////////////////////////////////////////////
        dis.close();
        fp.close();

        return 0;
    }
}
