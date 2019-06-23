package com.eos.parkban.anpr.farsi_ocr_anpr;



public class MyMatrix {
    public enum EResampleMethod{rmNearest, rmBiLinear};//Resampling method

    //Same as normalizecenter2 in C++
    public static float [][] resize(byte [][] mx, int H, int W, EResampleMethod Method, byte BackGround)
    {
        float WR, HR, scale;
        int ifX, ifY, ifX1, ifY1, xMax, yMax;
        float dy, dx, fx, fy;
        byte v1, v2, v3, v4;
        float vx1, vx2;
        float Result[][] = new float[H][W];
        int wSrc, hSrc;
        //inMx->RemoveWhiteSpaces(BackGround);
        hSrc = mx.length;
        wSrc = mx[0].length;
        WR = (float)W/wSrc;
        HR = (float)H/hSrc;
        scale = WR-0.000000001f;//to prevent some problems due to inexact accuracy
        if(HR < WR)
            scale = HR-0.000000001f;

        int ox = (int)((W - scale*wSrc)/2.0);
        int oy = (int)((H - scale*hSrc)/2.0);

        switch (Method)
        {
            case rmNearest:
                for (int y = 0; y < (int)(hSrc*scale); y++)
                    for (int x = 0; x < (int)(wSrc*scale); x++) {
                        Result[y + oy][x + ox] = mx[(int) (y / scale)][(int) (x / scale)];
                    }
                break;
            case rmBiLinear:
                xMax = wSrc-1;
                yMax = hSrc-1;
                for (int y = 0; y < (int)(hSrc*scale); y++)
                {
                    fy = y / scale;
                    ifY = (int) fy;
                    ifY1 = Math.min(yMax, ifY+1);
                    dy = fy - ifY;
                    for (int x = 0; x < (int)(wSrc*scale); x++)
                    {
                        fx = x / scale;
                        ifX = (int)fx;
                        ifX1 = Math.min(xMax, ifX+1);
                        dx = fx - ifX;
                        // Interpolate using the four nearest pixels in the source
                        v1 = mx[ifY][ifX];
                        v2 = mx[ifY][ifX1];
                        v3 = mx[ifY1][ifX];
                        v4 = mx[ifY1][ifX1];
                        // Interplate in x direction
                        vx1 = v1 * (1 - dy) + v3 * dy;
                        vx2 = v2 * (1 - dy) + v4 * dy;
                        //set output values
                        //SendRecordStatus[trunc(y+oy), trunc(x+ox)] = round( ol * (vx1*(1-dx)+vx2*dx) )
                        if(BackGround == 0)
                            Result[(y+oy)][x+ox] = vx1*(1-dx)+vx2*dx;
                        else
                            Result[(y+oy)][x+ox] = 1 - (vx1*(1-dx)+vx2*dx);
                    }//for x
                }//for y
        }//switch

        return Result;
    }//resize

    //Same as Resample in C++
    public static byte [][] resample(byte [][] mx, int newX, int newY, byte back, EResampleMethod Method, Boolean ReserveXYRatio, byte grayFactor)
    {
        byte Result [][];
        Result = new byte[newY][newX];
        if((newX < 3) || (newY < 3))
            return Result;

        int H = mx.length;
        int W = mx[0].length;
        if(back != 0)
        {
            for (int y = 0; y < newY; y++)
                for (int x = 0; x < newX; x++)
                    Result[y][x] = back;
        }

        if(grayFactor < 1)
            grayFactor = 1;
        float xScale = (float)newX/(float)W;
        float yScale = (float)newY/(float)H;

        int ifX, ifY, ifX1, ifY1, xMax, yMax;
        float dy, dx, fx, fy;
        int x, y, ox = 0, oy = 0;
        byte v1, v2, v3, v4;
        float vx1, vx2;

        float scale = xScale;
        if(yScale < xScale)
            scale = yScale;
        if(ReserveXYRatio){
            xScale = yScale = scale;
            ox = (int)((newX - scale*W)/2);
            oy = (int)((newY - scale*H)/2);
        }

        switch (Method)
        {
            case rmNearest:
                //omp_set_num_threads(1);
                //#pragma omp parallel for private(x)
                for (y = 0; y < (int)(H*yScale); y++)
                    for (x = 0; x < (int)(W*xScale); x++)
                        Result[y+oy][x+ox]= mx[(int) (y/yScale)][(int) (x/xScale)];
                break;
            case rmBiLinear:
                xMax = W-1;
                yMax = H-1;
                //omp_set_num_threads(1);
                //#pragma omp parallel for private(x)
                for (y = 0; y < (int)(H*yScale+0.5); y++)
                {
                    fy = y / yScale;
                    ifY = (int)fy;
                    ifY1 = Math.min(yMax, ifY+1);
                    dy = fy - ifY;
                    for (x = 0; x < (int)(W*xScale+0.5); x++)
                    {
                        fx = x / xScale;
                        ifX = (int)fx;
                        ifX1 = Math.min(xMax, ifX+1);
                        dx = fx - ifX;
                        // Interpolate using the four nearest pixels in the source
                        v1 = mx[ifY][ifX];
                        v2 = mx[ifY][ifX1];
                        v3 = mx[ifY1][ifX];
                        v4 = mx[ifY1][ifX1];
                        // Interpolate in x direction
                        vx1 = v1 * (1 - dy) + v3 * dy;
                        vx2 = v2 * (1 - dy) + v4 * dy;
                        //set output values
                        Result[y+oy][x+ox] = (byte) (grayFactor*(vx1*(1-dx)+vx2*dx) + 0.5);
                    };//for x
                };
                break;
        };
        return Result;
    }//resample
}