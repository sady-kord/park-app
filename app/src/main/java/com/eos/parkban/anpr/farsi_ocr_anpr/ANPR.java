package com.eos.parkban.anpr.farsi_ocr_anpr;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Logger;

public class ANPR {
    public final static boolean TRIAL_VERSION = true;
    public final static int TIME_LIMIT = 2016;
    public static int plate_counter = 0;
    final int MAX_THREADS = 4;

    Trd0 t0 = new Trd0();

    public class Result {
        public String str = "";
        public Rect rc = new Rect(0, 0, 1, 1);
        public float confidence = 0;
    }

    public class SPlate {
        public String str = "";
        public Rect rc = new Rect(0, 0, 1, 1);
        public int nChar = 0;
        public float cnf = 0;
        public float mincnf = 1.0f;
    }

    ;

    public class SRegion {
        public int x0 = 0, y0 = 0, W = 0, H = 0;//start and end of area that must be recognized
        public int nPxCount = 0;//Number of dark pixels in this region
        public int lbl = -1;
        public int code = -1;
    }

    ;
    public String LP_CharsEn[] = new String[]{
            "?", "1", "2", "3", "4", "5", "6", "7", "8", "9",
            "-Alef-", "-B-", "-Jim-", "-D-", "-Sin-", "-Sad-", "-Ta-", "-Ain-", "-Gh-",
            "-K-", "-L-", "-M-", "-N-", "-V-", "-H-", "-Y-", "IR", "-E-" /*E for Janbazan*/
    };

    String LP_WCharsFa[] = new String[]{
            "?", "۱", "۲", "۳", "۴", "۵", "۶", "۷", "۸", "۹",
            "ا", "ب", "ج", "د", "س", "ص", "ط", "ع", "ق",
            "ک", "ل", "م", "ن", "و", "ﻫ", "ی", "یر", "*"
    };
    public boolean _bestFound = false;
    public SPlate _bestPlate[] = new SPlate[MAX_THREADS]; //4 threads
    public Thread _hTrd[] = new Thread[MAX_THREADS];
    public byte _bestTrd;        //which thread produced the best results?
    public Mat _imGray = new Mat(), _imBlured = new Mat(), _imInput = new Mat();
    public float resize_thresh = 500;
    public float im_ratio = 1.0f;
    ////////////////////////////////////////
    //Thresholds:
    short _min_char_w = 5; //minimum with of characters
    short _min_char_h = 7; //minimum height of characters
    short _max_char_w = 100; //maximum with of characters
    short _max_char_h = 100; //maximum height of characters
    short _avg_char_height = 15;
    Context cntx;
    CFeature feature[] = new CFeature[4]; //4 threads, 4 feature extractor

    byte feature_codes[] = new byte[]{1, 14};
    byte feature_codes_fast[] = new byte[]{1, 10};

    MCS DigitMCS[] = new MCS[4];//Loci and WDC Binary
    MCS DigitMCS_fast[] = new MCS[4];//Loci and Zoning

    Boolean busy = false;
    public File pic_path;

    public ANPR(Context cntx) {
        this.cntx = cntx;
        pic_path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
    }

    public int Initialize(String pass) throws IOException {
        if (!pass.contains("ParkbanEos")) {
            return -1;
        }

        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
//        if(year > TIME_LIMIT) {
//            ShowToast.makeText(cntx, "برنامه منقضی شده است. نسخه جدید را از سایت برنامه یا بازار دانلود کنید.", ShowToast.LENGTH_LONG).show();
//            ShowToast.makeText(cntx, "Program expired. Please refer to www.FarsiOCR.ir or CafeBazaar for new version", ShowToast.LENGTH_SHORT).show();
//            return -2;
//        }

        for (int i = 0; i < 4; i++) {
            feature[i] = new CFeature();
            feature[i].params.width = 40;
            feature[i].params.height = 40;
            feature[i].params.norm = 2.0f;

            feature[i].params.norm_method = CFeature.ENormalizationMethod.DivideByNorm;

            DigitMCS[i] = new MCS();
            //it can not be accessed by path! only by asset manager!
            File dir = cntx.getExternalFilesDir(null);
            String mcs_path = dir.getPath() + "/LPCP.mcs";
            DigitMCS[i].load(mcs_path); //Precise classifier for FINE state
            //ShowToast.makeText(cntx, "Precise Classifier Loaded: " + DigitMCS[i].NNList[0].info(), ShowToast.LENGTH_SHORT).show();
            mcs_path = dir.getPath() + "/LPCF.mcs";
            DigitMCS_fast[i] = new MCS();
            DigitMCS_fast[i].load(mcs_path); //Fast classifier for COARSE state
            //ShowToast.makeText(cntx, "Fast Classifier Loaded: " + DigitMCS_fast[i].NNList[0].info(), ShowToast.LENGTH_SHORT).show();
            if (DigitMCS[i].m_nClassifier < 1) {
                throw new FileNotFoundException("ERROR >> File \"LPCF.MCS\" could not be loaded.");
            }

            if (DigitMCS_fast[i].m_nClassifier < 1) {
                throw new FileNotFoundException("ERROR >> File \"LPCF.MCS\" could not be loaded.");
            }
        }
//        if (TRIAL_VERSION) {
//           // ShowToast.makeText(cntx, "ANPR Lib TRIAL - www.FarsiOCR.ir", ShowToast.LENGTH_SHORT).show();
//            ShowToast.makeText(cntx, "نسخه آزمایشی پلاکهای دولتی را نمی خواند", ShowToast.LENGTH_SHORT).show();
//        } else
//            ShowToast.makeText(cntx, "ANPR Lib initialized", ShowToast.LENGTH_SHORT).show();
        return 0;
    }

    public Result Recognize(Mat img_input, Rect roi) {
        Log.i("====>", "in recognize method");
        Result result = new Result();
        plate_counter++;
//        if(TRIAL_VERSION && (plate_counter > 10)) {
//            ShowToast.makeText(cntx, "نسخه آزمایشی تنها 10 پلاک را می خواند", ShowToast.LENGTH_SHORT).show();
//            return result;
//        }
        if (busy) {
            Log.w("Warning", "** Recognize: Returning Busy!\n");
            return result;
        }
        busy = true;

        if ((DigitMCS[0].m_nClassifier < 1)) {
            Toast.makeText(cntx, "There is No Engine Loaded", Toast.LENGTH_LONG).show();
            result.str = "No Engine";
            busy = false;
            return result;
        }

        if (img_input.empty()) {
            Toast.makeText(cntx, "There is No Engine Loaded", Toast.LENGTH_LONG).show();
            result.str = "No Image";
            busy = false;
            return result;
        }

        Mat img_temp = img_input;
        Mat img = new Mat();
        if (roi != null) {
            img_temp = img_input.submat(roi);
        }

        if (img_temp.cols() > resize_thresh) {
            im_ratio = resize_thresh / (float) img_temp.cols();
            Imgproc.resize(img_temp, img, new Size(resize_thresh, img_temp.rows() * resize_thresh / img_temp.cols()), 0, 0, Imgproc.INTER_LINEAR);
        } else {
            img = img_temp.clone();
            im_ratio = 1.0f;
        }

        Imgproc.cvtColor(img, _imGray, Imgproc.COLOR_RGB2GRAY);
        Imgproc.medianBlur(_imGray, _imGray, 3);
        Imgproc.blur(_imGray, _imBlured, new Size(13, 13));

        ////////////////////////////////////////////////
        if (false) {
            _bestFound = false;
            for (int i = 0; i < 4; i++)
                _bestPlate[i] = new SPlate();
            //(new Trd2()).run();
            (new Trd1()).run();
        }
        ////////////////////////////////////////////////
        else {
            RunThreads();
            try {
                Log.i("====>", "before _htrd");
                _hTrd[1].join(2000);
                Log.i("====>", "after _htrd");
//                if(!TRIAL_VERSION) {
//                    _hTrd[2].join(2000);
//                    _hTrd[3].join(2000);
//                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        SPlate bestPlate = _bestPlate[0];
        //float bestCnf = 0;
        for (int i = 1; i < 4; i++)
            if (_bestPlate[i].cnf > bestPlate.cnf)
                bestPlate = _bestPlate[i];

        if (im_ratio != 1.0) {
            result.rc.x = (int) (bestPlate.rc.x / im_ratio);
            result.rc.y = (int) (bestPlate.rc.y / im_ratio);

            result.rc.width = (int) (bestPlate.rc.width / im_ratio);
            result.rc.height = (int) (bestPlate.rc.height / im_ratio);
        } else
            result.rc = bestPlate.rc;
        result.str = bestPlate.str;
        result.confidence = bestPlate.cnf;
        return result;
    }

    private void RunThreads() {
        _bestFound = false;
        Log.i("====>", "in RunThreads");
        for (int i = 0; i < 4; i++)
            _bestPlate[i] = new SPlate();

        //Thread 0 will be called normally to work in main program and at the same time, the other threads finish their works
        _hTrd[0] = new Thread(t0);
        _hTrd[0].start();
        Log.i("====>", "_htrd0 start");
        _hTrd[1] = new Thread(new Trd1());
        _hTrd[1].start();
        Log.i("====>", "_htrd1 start");
        if (!TRIAL_VERSION) {
            _hTrd[2] = new Thread(new Trd2());
            _hTrd[2].start();
            Log.i("====>", "_htrd2 start");
            _hTrd[3] = new Thread(new Trd3());
            _hTrd[3].start();
            Log.i("====>", "_htrd3 start");
        }
        //(new Trd0()).run();
        t0.run();
        Log.i("====>", "new Trd0()).run()");
    }

    private void ProcessBinaryImage(Mat imBin, int trd_idx) {

        //List<SPlate> plt;
        ArrayList<SPlate> plt = new ArrayList<SPlate>();
        short mxLbl[][];
        //Mat2CMatrix(imBin, mxLbl);

        int best = FindPlateFast(imBin, plt, trd_idx);

        Log.i("====>","bestFound = " +_bestFound );
        if (_bestFound)//may be found in another thread!
            return;

        Log.i("====>","plt.size() =" + plt.size());
        if (plt.size() == 0)
            return;//nothing found
        SPlate plt_best = plt.get(best);
        if (plt_best.nChar > 3)// always refine result (@940204 4 --> 3)
        {
            Log.i("====>","ifplt_best.nChar > 3" );
            _bestPlate[trd_idx] = plt_best;

            Rect roi, rc = plt_best.rc;
            if (plt_best.nChar >= 8) {
                Log.i("====>","plt_best.nChar >= 8" );
                roi = new Rect();
                Log.i("====>","roi = new Rect()" );
                roi.x = Math.max(0, rc.x - 5);
                Log.i("====>","x" );
                roi.width = Math.min(_imGray.cols() - roi.x - 1, rc.width + 10);
                Log.i("====>","width" );
                roi.y = Math.max(0, rc.y - 5);
                Log.i("====>","y" );
                roi.height = Math.min(_imGray.rows() - roi.y - 1, rc.height + 10);
                Log.i("====>","roi.height" );
                /*roi = rc;
                if (roi.br().x >= _imGray.cols())
                    roi.width = _imGray.cols() - roi.x - 1;
                if (roi.br().y >= _imGray.rows())
                    roi.height = _imGray.rows() - roi.y - 1;*/
            } else {
                Log.i("====>","else)" );
                roi = new Rect();
                roi.x = Math.max(0, rc.x - rc.width / 2);
                roi.width = Math.min(_imGray.cols() - roi.x - 1, rc.width * 4 / 2);
                roi.y = Math.max(0, rc.y - rc.height / 3);
                roi.height = Math.min(_imGray.rows() - roi.y - 1, rc.height * 5 / 3);
            }

            SPlate plt_refine = RefineChars(trd_idx > 1, roi, trd_idx);
            Log.i("====>","SPlate plt_refine" );

            if (plt_refine.cnf > _bestPlate[trd_idx].cnf) {
                Log.i("====>","if bade >8" );
                _bestPlate[trd_idx] = plt_refine;
                if (plt_refine.nChar == 8 && plt_refine.cnf > 0.95)
                    _bestFound = true;
            }
            //Can not create toast inside thread!
            //ShowToast.makeText(cntx, plt_best.str, ShowToast.LENGTH_LONG).show();
        } else {
            _bestPlate[trd_idx] = plt_best;
            //ShowToast.makeText(cntx, "یافت نشد", ShowToast.LENGTH_LONG).show();
        }
    }

    private SPlate RefineChars(boolean invert_colors, Rect roi, int trd_idx) {
        Rect tmp = roi;
        Mat imTmp = new Mat();
        Mat im = new Mat(), im0 = new Mat(_imGray, tmp);
        Mat imb = new Mat(), imb0 = new Mat(_imBlured, tmp);
        Rect rc = new Rect(0, 0, roi.width, roi.height);
        Rect rcBest = rc.clone(), rcMax = rc.clone();

        //CVMAT gray, bin;

        final int nStep = 4;
        Mat imBin[] = new Mat[nStep];
        for (int i = 0; i < 4; i++)
            imBin[i] = new Mat();

        //@940614 When plate is too small, its a good idea to resize it
        boolean doubled = false;
        if (im0.rows() < 50) {
            doubled = true;
            Imgproc.resize(im0, im, new Size(im0.width() * 2, im0.height() * 2), 0, 0, Imgproc.INTER_LINEAR);
            Imgproc.resize(imb0, imb, new Size(imb0.width() * 2, imb0.height() * 2), 0, 0, Imgproc.INTER_LINEAR);
        } else {
            im = im0;
            imb = imb0;
        }

        if (invert_colors) {
            Core.multiply(imb, new Scalar(1.0), imTmp);
            Core.compare(im, imTmp, imBin[0], Core.CMP_LE);
            //Imgcodecs.imwrite(pic_path + "/rbin0.bmp", imBin[0]);

            Core.multiply(imb, new Scalar(0.92), imTmp);
            Core.compare(im, imTmp, imBin[1], Core.CMP_LE);
            //Imgcodecs.imwrite(pic_path + "/rbin1.bmp", imBin[1]);

            Core.multiply(imb, new Scalar(0.85), imTmp);
            Core.compare(im, imTmp, imBin[2], Core.CMP_LE);
            //Imgcodecs.imwrite(pic_path + "/rbin2.bmp", imBin[2]);

            Core.multiply(imb, new Scalar(0.8), imTmp);
            Core.compare(im, imTmp, imBin[3], Core.CMP_LE);
            //Imgcodecs.imwrite(pic_path + "/rbin3.bmp", imBin[3]);

            /*imBin[0] = (im > (imb*0.97 + 255 * 0.03)) / 255;
            imBin[1] = (im > (imb*0.92 + 255*0.08)) / 255;
            imBin[2] = (im > (imb*0.85 + 255*0.15)) / 255;
            imBin[3] = (im > (imb*0.80 + 255*0.20)) / 255;*/
        } else {
            Core.multiply(imb, new Scalar(1.0), imTmp);
            Core.compare(im, imTmp, imBin[0], Core.CMP_GE);
            //Imgcodecs.imwrite(pic_path + "/rbin0.bmp", imBin[0]);

            Core.multiply(imb, new Scalar(0.92), imTmp);
            Core.compare(im, imTmp, imBin[1], Core.CMP_GE);
            //Imgcodecs.imwrite(pic_path + "/rbin1.bmp", imBin[1]);

            Core.multiply(imb, new Scalar(0.85), imTmp);
            Core.compare(im, imTmp, imBin[2], Core.CMP_GE);
            //Imgcodecs.imwrite(pic_path + "/rbin2.bmp", imBin[2]);

            Core.multiply(imb, new Scalar(0.8), imTmp);
            Core.compare(im, imTmp, imBin[3], Core.CMP_GE);
            //Imgcodecs.imwrite(pic_path + "/rbin3.bmp", imBin[3]);

            /*imBin[0] = (im <= (imb*1.0)) / 255;
            imBin[1] = (im <= (imb*0.92)) / 255;
            imBin[2] = (im <= (imb*0.85))/255;
            imBin[3] = (im <= (imb*0.8))/255;*/
        }

        SRegion rgn[] = new SRegion[0];

        MCS.SClassifierResult res;
        String str[] = new String[]{"", "", "", ""};
        float cnf[] = new float[nStep];// = {0};
        float mincnf[] = new float[]{1, 1, 1, 1};
        int nValid[] = new int[nStep];

        float maxCnf = 0;
        int maxIdx = 0;
        int maxChar = 0;
        int maxCharIdx = 0; //index with max number of characters
        short avg_char_height = 0;

        SPlate result = new SPlate();

        for (int step = 0; step < nStep; step++) {
            rgn = FastBwLbl(imBin[step].clone());
            if (rgn.length < 3)
                break;
            int lefts[] = new int[rgn.length];
            int indices[] = new int[rgn.length];

            for (int j = 0; j < rgn.length; j++) {
                lefts[j] = rgn[j].x0;
                indices[j] = j;
            }

            //sort components from left to right
            Utils.QuickSortWithIndex(lefts, indices, 0, rgn.length - 1);

            //letterPos = -1;
            //s1 = ''; s2 = ''; s3 = ''; s4 = '';
            int nPx = 0;
            rc.x = rc.y = 10000;
            rc.width = rc.height = 0;
            boolean letter_detected = false;
            float sum_height = 0;

            int th_width = Math.min(_max_char_w, imBin[step].width() / 3);
            int th_height = Math.min(_max_char_h, imBin[step].width() / 3);

            int lbl_cnt = 0;
            for (int j = 0; j < rgn.length; j++) {
                int idx = indices[j];
                nPx = rgn[idx].nPxCount;
                ////////////////////////////////////////////
                if ((rgn[idx].W < _min_char_w - 1) ||
                        (rgn[idx].H < _min_char_h - 3) ||
                        (rgn[idx].W > th_width) ||
                        (rgn[idx].H > th_height))
                    continue;
                //findContours returns padded labels so it must be removed (+1 and -2)
                Mat sub = imBin[step].submat(new Rect(rgn[idx].x0 + 1, rgn[idx].y0 + 1, rgn[idx].W - 2, rgn[idx].H - 2));
                nPx = 0;
                byte mx[][] = new byte[sub.height()][sub.width()];
                for (int y = 0; y < sub.height(); y++)
                    for (int x = 0; x < sub.width(); x++) {
                        float f = (float) sub.get(y, x)[0];
                        if (f == 0) {
                            mx[y][x] = 1;//(byte)f;//(mxLbl[y][x] == rgn[idx].lbl);
                            nPx++;
                        }
                    }

                rgn[idx].nPxCount = nPx;
                //Imgcodecs.imwrite(pic_path + "/" + j + ".bmp", sub);
                //Filter according to dimension of components
                if ((nPx > imBin[step].width() * imBin[step].height() / 5) || (nPx < 20))
                    continue;

                ////////////////////////////////////////////

                //Extract Fast Features
                feature[trd_idx].GetFeatures(mx, feature_codes);

                //Recognize digits by Boosted Neural Network
                res = DigitMCS[trd_idx].RecognizeBoostM2(feature[trd_idx]._Features);

                if (res.winCode > 0 && res.winCode != 26)//26 is یـر in ایران
                {
                    //extra stroke at the beginning or end of the plate, recognized as 1 or 9
                    if ((res.winCode == 1 || res.winCode == 9) && (nValid[step] == 0 || nValid[step] >= 7))
                        if (rgn[idx].H < 0.8 * _avg_char_height)
                            continue;
                    if (res.winCode > 9 && letter_detected)
                        continue;//its usually ن of ایران
                    if (res.winCode > 9) {
                        if (nValid[step] != 2 && res.winVal < 0.8)
                            continue;
                        letter_detected = true;
                        //دومین حرف پلاک باید غیرعدد باشد، در غیر این صورت اطمینان پلاک باید کاهش یابد
                        if (nValid[step] != 2)
                            cnf[step] -= 0.1f;//

                        if (res.winCode == 11)//Code of letter ب or ت or پ
                        {
                            //Update pixel counts for remaining regions and then check for ب, پ or ت
                            for (int k = j + 1; k < rgn.length; k++) {
                                int idx2 = indices[k];
                                if (rgn[idx2].nPxCount == 0 && rgn[idx2].W > 2 && rgn[idx2].H > 2) {
                                    Mat sub2 = imBin[step].submat(new Rect(rgn[idx2].x0 + 1, rgn[idx2].y0 + 1, rgn[idx2].W - 2, rgn[idx2].H - 2));
                                    rgn[idx2].nPxCount = sub2.cols() * sub2.rows() - (int) Core.sumElems(sub2).val[0] / 255;//
                                }
                            }
                            ProcessDotsBPT(rgn, idx, res);
                        }
                    }
                    str[step] += LP_WCharsFa[res.winCode];

                    cnf[step] += (float) res.Certainty;
                    if (res.Certainty < mincnf[step])
                        mincnf[step] = (float) res.Certainty;
                    nValid[step]++;

                    if (rgn[idx].x0 < rc.x) {
                        if (rc.width > 0)
                            rc.width += (rc.x - rgn[idx].x0);
                        rc.x = rgn[idx].x0;
                    }
                    if (rgn[idx].y0 < rc.y) {
                        if (rc.height > 0)
                            rc.height += (rc.y - rgn[idx].y0);
                        rc.y = rgn[idx].y0;
                    }
                    if (rgn[idx].x0 + rgn[idx].W > rc.x + rc.width)
                        rc.width = rgn[idx].x0 + rgn[idx].W - rc.x;
                    if (rgn[idx].y0 + rgn[idx].H > rc.y + rc.height)
                        rc.height = rgn[idx].y0 + rgn[idx].H - rc.y;

                    sum_height += rgn[idx].H;
                }
                //else if(res.winCode == 26)
                //ir_rgn = rgn[idx];
                //else if(res.Certainty < 0.3)
                //cdb.AddCharacter(mx.GetSMatrix(), res.winCode, res.Certainty*1000);
            }
            cnf[step] /= (Math.abs(nValid[step] - 8) + nValid[step]); // The most confidence must be occur for 8 components
            if (nValid[step] > maxChar) {
                maxChar = nValid[step];
                rcMax = rc.clone();
                maxCharIdx = step;
            }
            if (cnf[step] > maxCnf) {
                maxCnf = cnf[step];
                rcBest = rc.clone();
                maxIdx = step;
                avg_char_height = (short) (sum_height / nValid[step]);
                if (nValid[step] > 6 && maxCnf > 0.7 && avg_char_height > 10)
                    _avg_char_height = avg_char_height;
            }

            //if(nValid[step] < 2) //nothing found --> not an LP
            //break;

            //شرط 0.7 حتما باشد، چرا که گاهی خط بین دو بخش پلاک به عنوان عدد 1 شناخته شده
            // و در مقابل یک نویسه از طرفین خوانده نشده است
            if (nValid[step] == 8 && cnf[step] > 0.9 && mincnf[step] > 0.7) { //true LP detected
                maxCnf = cnf[step];
                maxIdx = step;
                break;
            }
        }

        result.cnf = maxCnf;
        result.nChar = nValid[maxIdx];
        result.str = str[maxIdx];
        if (result.nChar < 8 && maxChar >= 8) {
            result.cnf = cnf[maxCharIdx];
            result.nChar = maxChar;
            result.str = str[maxCharIdx];
            rcBest = rcMax;
        }
        if (doubled) {
            rcBest.x /= 2;
            rcBest.y /= 2;
            rcBest.width /= 2;
            rcBest.height /= 2;
        }
        result.rc = rcBest;
        result.rc.x += roi.x;
        result.rc.y += roi.y;

        result.mincnf = mincnf[maxIdx];

        return result;
    }

    //returns bestplate index
    private int FindPlateFast(Mat imBin, ArrayList<SPlate> plt, int trd_idx) {
        SRegion rgn[] = new SRegion[0];
        SRegion rgnValid[] = new SRegion[10];//rgnValid will keep valid characters
        MCS.SClassifierResult res;
        String str = "";

        int nValid = 0;

        float maxCnf = 0;
        int maxIdx = 0;
        float avg_char_height = 0;

        rgn = FastBwLbl(imBin.clone());

        //Core.multiply(imBin, new Scalar(1.0/255), imBin);

        int lefts[] = new int[rgn.length];
        int indices[] = new int[rgn.length];

        for (int j = 0; j < rgn.length; j++) {
            lefts[j] = rgn[j].x0;
            indices[j] = j;
        }

        //sort components from left to right
        Utils.QuickSortWithIndex(lefts, indices, 0, rgn.length - 1);

        int nPx = 0;
        boolean letter_detected = false;
        float sum_height = 0;
        //تمام مولفه های پیوسته را برای یافتن ارقام و حروف بررسی کن
        int th_width = Math.min(_max_char_w, imBin.width() / 5);
        int th_height = Math.min(_max_char_h, imBin.width() / 5);

        int lbl_cnt = 0;
        for (int j = 0; j < rgn.length; j++) {
            //If best result found in another thread, break.
            if (_bestFound)
                break;
            int idx = indices[j];
            if ((rgn[idx].W < _min_char_w) ||
                    (rgn[idx].H < _min_char_h) ||
                    (rgn[idx].W > th_width) ||
                    (rgn[idx].H > th_height) ||
                    (rgn[idx].W > 2.5 * rgn[idx].H))
                continue;
            Mat sub = imBin.submat(new Rect(rgn[idx].x0 + 1, rgn[idx].y0 + 1, rgn[idx].W - 2, rgn[idx].H - 2));
            nPx = 0;
            /*int length = (int) (sub.total() * sub.elemSize());
            byte buffer[] = new byte[length];
            sub.get(0, 0, buffer);*/
            byte mx[][] = new byte[sub.height()][sub.width()];
            for (int y = 0; y < sub.height(); y++)
                for (int x = 0; x < sub.width(); x++) {
                    float f = (float) sub.get(y, x)[0];
                    if (f == 0) {
                        mx[y][x] = 1;//(byte)f;//(mxLbl[y][x] == rgn[idx].lbl);
                        nPx++;
                    }
                }

            //if(rgn[idx].nPxCount == 0)
            //  rgn[idx].nPxCount = (int)Core.sumElems(sub).val[0];//
            rgn[idx].nPxCount = nPx;

            //Filter according to dimension of components
            if ((nPx > imBin.width() * imBin.height() / 10) || (nPx < 20))
                continue;

            //Extract Fast Features
            feature[trd_idx].GetFeatures(mx, feature_codes_fast);

            //Recognize digits by Boosted Neural Network
            res = DigitMCS_fast[trd_idx].RecognizeBoostM2(feature[trd_idx]._Features);

            //code 0 means no character
            if (res.winCode > 0 && res.winCode != 26)//26 is یـر in ایران
            {
                if (res.winCode == 22 && letter_detected)
                    continue;//its usually ن of ایران

                if (res.winCode > 9) {
                    letter_detected = true;
                    if (res.winCode == 11)//Code of letter ب or ت or پ
                    {
                        //Update pixel counts for remaining regions and then check for ب, پ or ت
                        for (int k = j + 1; k < rgn.length; k++) {
                            int idx2 = indices[k];
                            if (rgn[idx2].nPxCount == 0 && rgn[idx2].W > 2 && rgn[idx2].H > 2) {
                                Mat sub2 = imBin.submat(new Rect(rgn[idx2].x0 + 1, rgn[idx2].y0 + 1, rgn[idx2].W - 2, rgn[idx2].H - 2));
                                rgn[idx2].nPxCount = sub2.cols() * sub2.rows() - (int) Core.sumElems(sub2).val[0] / 255;//
                            }
                        }
                        ProcessDotsBPT(rgn, idx, res);
                    }
                }
                str += LP_WCharsFa[res.winCode];

                sum_height += rgn[idx].H;
                rgn[idx].code = -1;//to be used if kmeans clustering is required later
                rgn[idx].lbl = (int) (res.winVal * 10000); //confidence to be used if kmeans clustering is required later
                if (nValid > rgnValid.length - 1) //resize it
                {
                    SRegion tmp[] = new SRegion[rgnValid.length + 8];
                    //use ArrayCopy instead!
                    for (int r = 0; r < rgnValid.length; r++)
                        tmp[r] = rgnValid[r];
                    rgnValid = tmp;
                }
                rgnValid[nValid++] = rgn[idx];
            }
        }

        int bestPlate = 0;
        if (nValid > 0) {
            avg_char_height = sum_height / nValid;
        }
        if (nValid < 4)
            return 0;
        else {
            //first find and combine characters which are horizontally in the same row
            //vector<int> rows(0);//center of plates (or combination of characters) in different vertical positions
            ArrayList<Integer> rows = new ArrayList<Integer>();
            for (int i = 0; i < nValid; i++) {
                int y_cur = rgnValid[i].y0 + rgnValid[i].H / 2;
                int j = 0;
                for (j = 0; j < rows.size(); j++) {
                    if (Math.abs(y_cur - rows.get(j)) < 1.4 * avg_char_height)
                        break;
                }
                rgnValid[i].code = j; //row index is saved to ".code" which has no other use here
                if (j >= rows.size())
                    rows.add(y_cur); //add new row
            }

            //now find the gaps and separate plates
            for (int r = 0; r < rows.size(); r++) {
                int x1_prev = -1;
                SPlate p = new SPlate();
                for (int i = 0; i < nValid; i++) {
                    if (rgnValid[i].code != r)
                        continue;
                    if (x1_prev == -1)//beginning of a new plate
                    {
                        x1_prev = rgnValid[i].x0 + rgnValid[i].W - 1;
                        p.rc.x = rgnValid[i].x0;
                        p.rc.y = rgnValid[i].y0;
                        p.rc.width = rgnValid[i].W;
                        p.rc.height = rgnValid[i].H;
                        p.str = str.substring(i, i + 1);
                        p.nChar = 1;
                        p.cnf = (float) rgnValid[i].lbl; //confidence is saved to ".lbl" an alternative use of this variable
                        p.mincnf = (float) rgnValid[i].lbl;
                    } else {
                        if (Math.abs(x1_prev - rgnValid[i].x0) < 1.9 * avg_char_height  /*&& p.nChar < 8*/)//still on the same plate (@940204 1.1 --> 1.9)
                        {
                            x1_prev = rgnValid[i].x0 + rgnValid[i].W - 1;

                            p.rc.y = Math.min(p.rc.y, rgnValid[i].y0);
                            p.rc.width = x1_prev + 1 - p.rc.x;
                            int y1 = (int) Math.max(p.rc.br().y, rgnValid[i].y0 + rgnValid[i].H);
                            p.rc.height = y1 - p.rc.y;
                            p.str += str.substring(i, i + 1);
                            p.nChar++;
                            p.cnf += rgnValid[i].lbl;
                            if (rgnValid[i].lbl < p.mincnf)
                                p.mincnf = (float) rgnValid[i].lbl;
                        } else {
                            if (p.nChar > 3) {
                                plt.add(p);// push_back(p);
                                p = new SPlate();
                            }
                            x1_prev = -1;
                            i--;//check this region again, since it is for a new plate
                        }
                    }
                }

                if (x1_prev != -1 && p.nChar > 3)
                    plt.add(p);
            }

            maxCnf = 0;
            for (int c = 0; c < plt.size(); c++) {
                SPlate p = plt.get(c);
                p.cnf = p.cnf / 10000 / (Math.abs(p.nChar - 8) + p.nChar);
                //دومین حرف پلاک باید غیرعدد باشد، در غیر این صورت اطمینان پلاک باید کاهش یابد
                if (p.nChar > 2 && p.str.charAt(2) >= '۱')
                    p.cnf -= 0.1f;//
                p.mincnf = p.mincnf / 10000;
                if (p.cnf > maxCnf) {
                    maxCnf = p.cnf;
                    bestPlate = c;
                }
                //plt.set(c, p);
            }
        }

        return bestPlate;
    }

    private void ProcessDotsBPT(SRegion[] rgn, int idx, MCS.SClassifierResult res) {
        int nTop = 0;//number of dots on top
        int nBot = 0;
        //Check if some dots or on top of it or at the bottom of it (ت or ب or پ)
        int c1 = rgn[idx].x0 + rgn[idx].W / 2;//center of the character in x direction
        for (int k = 0; k < rgn.length; k++) {
            float nPx = (float) rgn[k].nPxCount;

            //If dots are too further from top or bottom of the body, its noise
            if ((k == idx) || (rgn[k].y0 < rgn[idx].y0 - 0.2 * rgn[idx].H) || (rgn[k].y0 > rgn[idx].y0 + 1.7 * rgn[idx].H))
                continue;

            //Some limits to be a dot
            if ((nPx > 0.2 * rgn[idx].W * rgn[idx].H) || (nPx < 7) || rgn[k].W < 3 || rgn[k].H < 3)
                continue;

            //Check horizontal center of dots and body
            int c2 = rgn[k].x0 + rgn[k].W / 2;
            if (Math.abs(c1 - c2) > Math.max(rgn[idx].H / 2, 8))//Center of dots and body must be almost equal
                continue;

            if (rgn[k].y0 < rgn[idx].y0 + rgn[idx].H)
                nTop++;
            else {
                if ((float) rgn[k].W / rgn[k].H > 1.8)
                    nBot += 2;
                else if (nPx / (rgn[k].W * rgn[k].H) > 0.5)
                    nBot++;
                else if (nPx > rgn[idx].nPxCount * 0.2)
                    nBot += 3;//3 dots
            }
        }

        if (nTop > nBot)
            LP_WCharsFa[res.winCode] = "ت";
        else if (nBot > 2)
            LP_WCharsFa[res.winCode] = "پ";
        else
            LP_WCharsFa[res.winCode] = "ب";
    }

    private SRegion[] FastBwLbl(Mat imBin) {
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();

        Imgproc.findContours(imBin, contours, new Mat(), Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);

        //boolean b = Imgcodecs.imwrite(pic_path+"/bin2.jpg", imBin);

        SRegion[] rgn = new SRegion[contours.size()];
        int i = 0;
        for (MatOfPoint cnt : contours) {
            Rect rc = Imgproc.boundingRect(cnt);
            rgn[i] = new SRegion();
            rgn[i].x0 = rc.x;
            rgn[i].y0 = rc.y;
            rgn[i].W = rc.width;
            rgn[i].H = rc.height;
            //Mat sub = imBin.submat(rc);
            //rgn[i].nPxCount
            i++;
        }
        return rgn;
    }

    class Trd0 implements Runnable {

        @Override
        public void run() {
            Log.i("====>","in run t0");
            Mat imBin = new Mat();
            Log.i("====>","after mat1");
            Mat imTmp = new Mat();
            Log.i("====>","after mat2");
            Core.multiply(_imBlured, new Scalar(0.95), imTmp);
            Log.i("====>","after Core1");
            Core.compare(_imGray, imTmp, imBin, Core.CMP_GE);
            Log.i("====>","after Core2");
            //boolean b = Imgcodecs.imwrite(pic_path+"/bin0.jpg", imBin);
            Log.i("====>"," before ProcessBinaryImage T0");
            try{
                ProcessBinaryImage(imBin, 0);
            }
            catch (Exception e){
                Log.i("====>","Exception ProcessBinaryImage T0 " +  e.getMessage());
            }

            Log.i("====>"," after ProcessBinaryImage T0");
            busy = false;
        }
    }

    class Trd1 implements Runnable {

        @Override
        public void run() {
            Log.i("====>","in run T1");
            Mat imBin = new Mat();
            Mat imTmp = new Mat();
            Core.multiply(_imBlured, new Scalar(0.9), imTmp);
            Core.compare(_imGray, imTmp, imBin, Core.CMP_GE);
            //boolean b = Imgcodecs.imwrite(pic_path+"/bin1.jpg", imBin);
            Log.i("====>"," before ProcessBinaryImage T1");
            try{
                ProcessBinaryImage(imBin, 1);
            }
            catch (Exception e){
                Log.i("====>","Exception ProcessBinaryImage T1 " +  e.getMessage());
            }
            Log.i("====>"," after ProcessBinaryImage T1");
        }
    }

    class Trd2 implements Runnable {

        @Override
        public void run() {
            Mat imBin = new Mat();
            Mat imTmp = new Mat();
            //imBin = (_imGray > (_imBlured*0.95 + 255 * 0.05)) / 255;
            //Core.addWeighted(_imBlured, 0.95, new Mat(), 0, 255*0.05, imTmp);
            Core.multiply(_imBlured, new Scalar(0.95), imTmp);
            Core.compare(_imGray, imTmp, imBin, Core.CMP_LE);
            //boolean b = Imgcodecs.imwrite(pic_path+"/bin2.jpg", imBin);
            ProcessBinaryImage(imBin, 2);
        }
    }

    class Trd3 implements Runnable {

        @Override
        public void run() {
            Mat imBin = new Mat();
            Mat imTmp = new Mat();
            //imBin = (_imGray > (_imBlured*0.9 + 255 * 0.05)) / 255;
            Core.multiply(_imBlured, new Scalar(0.9), imTmp);
            Core.compare(_imGray, imTmp, imBin, Core.CMP_LE);
            //boolean b = Imgcodecs.imwrite(pic_path+"/bin3.jpg", imBin);
            ProcessBinaryImage(imBin, 3);
        }
    }
}



