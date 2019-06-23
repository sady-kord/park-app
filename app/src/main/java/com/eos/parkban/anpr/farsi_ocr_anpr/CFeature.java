package com.eos.parkban.anpr.farsi_ocr_anpr;

/**
 * Created by kordbacheh.s on 24/10/2016.
 */
import java.util.Arrays;


public class CFeature {

    public enum ENormalizationMethod{DivideByMax, DivideByNorm, None};
    public final double SQRT_2 = 1.4142135623730950488016887242097; //Sqrt(2)
    public class SFeatureParams{
        SFeatureParams(){
            norm = 2.0f; //desired norm
            width = height = cell_width = cell_height = 0;
            norm_method = ENormalizationMethod.DivideByNorm;
            back = 0;
            fore = 1;
        }
        public float norm; //desired norm of feature vector
        public short width;	//if input pattern should be normalized, this params will be used
        public short height;
        public short cell_width; //number of cells = _width/_cell_width * _height/_cell_height
        public short cell_height;
        public ENormalizationMethod norm_method;
        public byte back; //background of input image
        public byte fore; //foreground of input image
    };

    short _feature_idx; //position within _Features vector. used when extracting more than one feature

    public SFeatureParams params;
    public double _Features[];

    public static final int FEATURE_COUNT = 23;

    public CFeature() {
        _Features = new double[1000];
        params = new SFeatureParams();
    }

    //Something like function pointers in C++
    public interface Feature {
        int extract(byte mx[][]);
    };

    public final Feature Loci81 = new Feature()
    {
        public int extract(byte mx[][])
        {
            return Loci(mx, 2, 2);
        }
    };

    public final Feature Loci144 = new Feature()
    {
        public int extract(byte mx[][])
        {
            return Loci(mx, 3, 2);
        }
    };

    public final Feature Loci256 = new Feature()
    {
        public int extract(byte mx[][])
        {
            return Loci(mx, 3, 3);
        }
    };

    public final Feature Zoning64 = new Feature()
    {
        public int extract(byte mx[][])
        {
            return Zoning(mx, 8, 8, 5);
        }
    };

    public final Feature Zoning72 = new Feature()
    {
        public int extract(byte mx[][])
        {
            return Zoning(mx, 12, 6, 10);
        }
    };

    public final Feature WDC_Binary1 = new Feature()
    {
        public int extract(byte mx[][])
        {
            return WDC_Binary(mx, 4, 4, 10);
        }
    };

    public final Feature WDC_Binary2 = new Feature()
    {
        public int extract(byte mx[][])
        {
            return WDC_Binary(mx, 6, 3, 12);
        }
    };

    public final Feature ExtractFea[] = new Feature[] {null, Loci81, Loci144, Loci256, null, null, null, null, null, null,
            Zoning64, Zoning72, null, null, WDC_Binary1, WDC_Binary2, null, null, null, null,
            null, null, null};/* {
      public double eval(double x) {
        return x*5+Math.sin(x);
      }
    };

    public static final Feature f2 = new Feature() {
      public double eval(double x) {
        return Math.pow(x*f1.eval(-x),x);
      }
    };*/

    //////////////////////////////////////////////////////////////////////////
    int WDC_Binary(byte inMx[][], int nCol/* = 4*/, int nRow/* = 4*/, int CellSize/* = 16*/)
    {
        int x, y, ind, k, l, j, i, W, H;
        int DV,DU,x1y1,x1y2,x2y1,x2y2;
        int FeaLen = 9*nCol*nRow;
        if (inMx == null)
            return FeaLen;

        int offset = _feature_idx;
        Arrays.fill(_Features, offset, offset + FeaLen + 1, 0);
        H = nRow*CellSize;
        W = nCol*CellSize;
        byte nMx[][];
        if(inMx.length != H || inMx[0].length != W){
            nMx = MyMatrix.resample(inMx, W, H, params.back, MyMatrix.EResampleMethod.rmBiLinear, true, (byte) 1);
            //nMx.SaveToCSV(L"test.csv");
        }
        else
        {
            nMx = inMx;
        }
        for (i = 0; i < nRow; i++){
            for (j = 0; j < nCol; j++){
                for(k = 0; k < CellSize; k++){
                    for (l = 0; l < CellSize; l++){
                        y = (i * CellSize) + k;
                        x = (j * CellSize) + l;
                        x1y1 = nMx[y][x];
                        x2y2 = ((x+1) < W && (y+1) < H) ? nMx[y+1][x+1] : 0;
                        x2y1 = ((x+1) < W             ) ? nMx[y  ][x+1] : 0;
                        x1y2 = (             (y+1) < H) ? nMx[y+1][ x ] : 0;
                        DV = x1y1 - x2y2;
                        DU = x2y1 - x1y2;
                        //4 3 2
                        //5 0 1
                        //6 7 8

                        if ((DV == 0) && (DU == 0)) //center
                        {
                            ind = ((i * nCol) + j) * 9 + 0;
                            _Features[offset + ind] += 0.01;
                        }
                        else if ((DV == 1) && (DU == 0)) //0 degree
                        {
                            ind = ((i * nCol) + j) * 9 + 1;
                            _Features[offset + ind] += 1.0;
                        }
                        else if ((DV == 1) && (DU == 1)) //45
                        {
                            ind = ((i * nCol) + j) * 9 + 2;
                            _Features[offset + ind] += SQRT_2; //sqrt(2)
                        }
                        else if ((DV == 0) && (DU == 1)) //90
                        {
                            ind = ((i * nCol) + j) * 9 + 3;
                            _Features[offset + ind] += 1.0;
                        }
                        else if ((DV == -1) && (DU == 1)) //135
                        {
                            ind = ((i * nCol) + j) * 9 + 4;
                            _Features[offset + ind] += SQRT_2;
                        }
                        else if ((DV == -1) && (DU == 0)) //180
                        {
                            ind = ((i * nCol) + j) * 9 + 5;
                            _Features[offset + ind] += 1.0;
                        }
                        else if ((DV == -1) && (DU == -1)) //-135
                        {
                            ind = ((i * nCol) + j) * 9 + 6;
                            _Features[offset + ind] += SQRT_2;
                        }
                        else if ((DV == 0) && (DU == -1)) //-90
                        {
                            ind = ((i * nCol) + j) * 9 + 7;
                            _Features[offset + ind] += 1.0;
                        }
                        else if ((DV == 1) && (DU == -1)) //-45
                        {
                            ind = ((i * nCol) + j) * 9 + 8;
                            _Features[offset + ind] += SQRT_2;
                        }
                    }
                }//for k
            }//for j
        }//for i

        return FeaLen;
    }

    int Zoning(byte inMx[][], int nCol, int nRow, int CellSize)
    {
        //H = inMx->H;	W = inMx->W; if there is no normalization
        int H = nRow*CellSize;
        int W = nCol*CellSize;
        int FeaLen = nRow*nCol;
        if (inMx == null)
            return FeaLen;

        int x1, x2, y1, y2;//, pxInSquare;
        double sum;
        float MeanBitmap[][] = MyMatrix.resize(inMx, H, W, MyMatrix.EResampleMethod.rmNearest, params.back);
        int offset = _feature_idx;
        Arrays.fill(_Features, offset, offset + FeaLen + 1, 0);

        //#pragma omp parallel for private(x1)
        for (y1 = 0; y1 < nRow; y1++){
            for (x1 = 0; x1 < nCol; x1++){
                sum = 0;
                for (y2 = 0; y2 < CellSize; y2++)
                    for (x2 = 0; x2 < CellSize; x2++)
                        sum += MeanBitmap[(y1*CellSize+y2)][x1*CellSize+x2];
                _Features[offset + y1*nCol + x1] = sum;
            }
        }
        return FeaLen;
    }

    int Loci(byte inMx[][], int MAX_H_CROSS, int MAX_V_CROSS)
    {
        int y, x, Dir0, Dir1, Dir2, Dir3, ix;
        int W, H;
        int FeaLen = (MAX_H_CROSS+1)*(MAX_H_CROSS+1)*(MAX_V_CROSS+1)*(MAX_V_CROSS+1);
        int offset = _feature_idx;
        //double SendRecordStatus[] = _Features + _feature_idx;

        //@941210 ویژگیها باید ابتدا به صفر مقداردهی اولیه شوند
        Arrays.fill(_Features, offset, offset + FeaLen + 1, 0);

        if (inMx == null)
            return FeaLen;

        byte[][] Mx = inMx;//ZeroPad(inMx);
    	/*if(_debug > 1){
    		inMx->SaveToImage(_T("D:\\1.bmp"));
    		Mx->SaveToImage(_T("D:\\2.bmp"));
    	}*/
        W = Mx[0].length + 1; //+ 1 for left padding
        H = Mx.length + 1; //+ 1 for top padding

        short hMx[][] = new short[H][W];//includes horizontal crosses
        short vMx[][] = new short[H][W];//includes vertical crosses

        short hCross[] = new short[H];
        short vCross[] = new short[W];

        //Mx->SaveToImage(_T("test.bmp"));
        for (y = 0; y < H; y++)
        {
            for (x = 0; x < W; x++)
            {
                if (x == 0 || y == 0 || Mx[y-1][x-1] == params.back)
                {// white pixel, BackGround
                    hMx[y][x] = hCross[y];
                    vMx[y][x] = vCross[x];
                    if((y > 0) && (x < W-1) && (Mx[y-1][x] != params.back))
                        hCross[y]++;
                    if((x > 0) && (y < H-1) && Mx[y][x-1] != params.back)
                        vCross[x]++;
                }
            }
        }
        //hCross.SaveToCSV(_T("hCross.txt"), _T("%d,"));
        //vCross.SaveToCSV(_T("vCross.txt"), _T("%d,"));

        //#pragma omp parallel for private(x)
        for (y = 0; y < H; y++)
        {
            for (x = 0; x < W; x++){
                if (x > 0 && y > 0 && Mx[y-1][x-1] != params.back)
                    continue;

                Dir0 = Math.min(hCross[y]-hMx[y][x], MAX_H_CROSS);//-->
                Dir2 = Math.min(hMx[y][x], MAX_H_CROSS);//<--
                Dir1 = Math.min(vCross[x]-vMx[y][x], MAX_V_CROSS);//.|.
                Dir3 = Math.min(vMx[y][x], MAX_V_CROSS);//'|'
                ix = Dir3*(MAX_H_CROSS+1)*(MAX_H_CROSS+1)*(MAX_V_CROSS+1) + Dir2*(MAX_H_CROSS+1)*(MAX_H_CROSS+1) + Dir1*(MAX_H_CROSS+1) + Dir0;

                //ix = Dir3*27 + Dir2*9 + Dir1*3 + Dir0;
                //SendRecordStatus[ix]++;
                _Features[offset + ix]++;
            }
        }
        return FeaLen;
    }

    void GetFeatures(byte mx[][], byte feature_codes[])
    {
        assert(mx != null);
        _feature_idx = 0;

        for (int f = 0; f < feature_codes.length; f++){
            int len = (ExtractFea[feature_codes[f]]).extract(mx);
            //double* f = _Features.getData();
            double norm = 0.0;
            if(params.norm_method == ENormalizationMethod.DivideByNorm)
            {
                for (int i = _feature_idx; i < _feature_idx+len; i++)
                    norm += _Features[i]*_Features[i];
                norm = Math.sqrt(norm);
            }
            else
            {
                //norm = _Features.maxVal(_feature_idx, _feature_idx+len);
                norm = -1000;
                for(int i = _feature_idx; i < _feature_idx+len; i++ )
                    if(_Features[i] > norm)
                        norm = _Features[i];
            }

            norm *= Math.sqrt((double)feature_codes.length);//old (norm *= feature_codes.len)
            if (norm != 0)
                for (int i = _feature_idx; i < _feature_idx+len; i++)
                    _Features[i] = _Features[i]*params.norm/norm;
            _feature_idx += len;
        }
    }
}
