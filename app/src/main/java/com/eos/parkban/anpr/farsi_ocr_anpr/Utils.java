package com.eos.parkban.anpr.farsi_ocr_anpr;


/**
 * Created by kordbacheh.s on 24/10/2016.
 */
public class Utils {
    //public static < VarType extends Comparable<VarType> > void QuickSortWithIndex(VarType A[], int iA[], int iLo, int iHi)
    public static void QuickSortWithIndex(int A[], int iA[], int iLo, int iHi)
    {
        if(A.length < 2)
            return;
        int Lo = iLo, Hi = iHi;
        int Mid = A[(Lo+Hi)/2];
        int T;
        int T2 = 0;
        do{
            while (A[Lo] < Mid)
                Lo++;
            while (A[Hi] > Mid)
                Hi--;
            if (Lo <= Hi){
                T = A[Lo];
                A[Lo] = A[Hi];
                A[Hi] = T;
                // update indexes
                T2 = iA[Lo];
                iA[Lo] = iA[Hi];
                iA[Hi] = T2;
                ////
                Lo++;
                Hi--;
            };
        } while(Lo <= Hi);

        if (Hi > iLo) QuickSortWithIndex(A, iA, iLo, Hi);
        if (Lo < iHi) QuickSortWithIndex(A, iA, Lo, iHi);
    };
}
