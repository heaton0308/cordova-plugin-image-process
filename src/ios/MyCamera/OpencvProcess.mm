//
//  OpencvProcess.m
//  BYFCameraProcess
//
//  Created by xiaobai on 17/9/18.
//  Copyright © 2017年 byf. All rights reserved.
//

#import "OpencvProcess.h"
#import <opencv2/opencv.hpp>

@implementation OpencvProcess


#pragma mark - 二值化方法
#pragma mark - opencv method
//转化图片
+(cv::Mat)cvMatFromUIImage:(UIImage *)image
{
    CGColorSpaceRef colorSpace = CGImageGetColorSpace(image.CGImage);
    CGFloat cols = image.size.width;
    CGFloat rows = image.size.height;
    
    cv::Mat cvMat(rows, cols, CV_8UC4); // 8 bits per component, 4 channels
    
    CGContextRef contextRef = CGBitmapContextCreate(cvMat.data,                 // Pointer to  data
                                                    cols,                       // Width of bitmap
                                                    rows,                       // Height of bitmap
                                                    8,                          // Bits per component
                                                    cvMat.step[0],              // Bytes per row
                                                    colorSpace,                 // Colorspace
                                                    kCGImageAlphaNoneSkipLast |
                                                    kCGBitmapByteOrderDefault); // Bitmap info flags
    
    CGContextDrawImage(contextRef, CGRectMake(0, 0, cols, rows), image.CGImage);
    CGContextRelease(contextRef);
    CGColorSpaceRelease(colorSpace);
    
    return cvMat;
}


#pragma mark - custom method
// OSTU算法求出阈值
int  Otsu(unsigned char* pGrayImg , int iWidth , int iHeight)
{
    if((pGrayImg==0)||(iWidth<=0)||(iHeight<=0))return -1;
    int ihist[256];
    int thresholdValue=0; // „–÷µ
    int n, n1, n2 ;
    double m1, m2, sum, csum, fmax, sb;
    int i,j,k;
    memset(ihist, 0, sizeof(ihist));
    n=iHeight*iWidth;
    sum = csum = 0.0;
    fmax = -1.0;
    n1 = 0;
    for(i=0; i < iHeight; i++)
    {
        for(j=0; j < iWidth; j++)
        {
            ihist[*pGrayImg]++;
            pGrayImg++;
        }
    }
    pGrayImg -= n;
    for (k=0; k <= 255; k++)
    {
        sum += (double) k * (double) ihist[k];
    }
    for (k=0; k <=255; k++)
    {
        n1 += ihist[k];
        if(n1==0)continue;
        n2 = n - n1;
        if(n2==0)break;
        csum += (double)k *ihist[k];
        m1 = csum/n1;
        m2 = (sum-csum)/n2;
        sb = (double) n1 *(double) n2 *(m1 - m2) * (m1 - m2);
        if (sb > fmax)
        {
            fmax = sb;
            thresholdValue = k;
        }
    }
    return(thresholdValue);
}

+(UIImage *)opencvProcessImage:(UIImage *)image andConstValue:(double)value{
    
    //根据自定义算法进行阈值转换
    //    cv::Mat matImage = [self cvMatFromUIImage:imageTow];
    //    cv::Mat matGrey ;
    //    cv::cvtColor(matImage, matGrey, CV_BGR2GRAY);// grey
    //    cv::Mat matBinary;
    //    IplImage grey = matGrey;
    //    unsigned char* dataImage = (unsigned char*)grey.imageData;
    //    int threshold = Otsu(dataImage, grey.width, grey.height);
    //    printf("阈值：%d\n",threshold);
    //    cv::threshold(matGrey, matBinary, threshold, 255, cv::THRESH_BINARY);
    
    // 图像读取及判断
    cv::Mat srcImage = [OpencvProcess cvMatFromUIImage:image];
    // 灰度转换
    cv::Mat srcGray;
    cv::cvtColor(srcImage, srcGray, CV_RGB2GRAY);
    cv::Mat dstImage;
    cv::Mat dstBlurImage;
    // 初始化自适应阈值参数
    int blockSize = 27;
    double constValue = value;
    const int maxVal = 255;
    /* 自适应阈值算法
     0：ADAPTIVE_THRESH_MEAN_C
     1: ADAPTIVE_THRESH_GAUSSIAN_C
     阈值类型
     0: THRESH_BINARY
     1: THRESH_BINARY_INV */
    int adaptiveMethod = 1;
    int thresholdType = 0;
    // 图像自适应阈值操作
    cv::adaptiveThreshold(srcGray, dstImage,
                          maxVal, adaptiveMethod,
                          thresholdType, blockSize,
                          constValue);
    UIImage* returnImage = [[UIImage alloc]init];
    returnImage = [OpencvProcess UIImageFromCVMat:dstImage];
    return returnImage;
}

//生成图片
+(UIImage *)UIImageFromCVMat:(cv::Mat)cvMat
{
    NSData *data = [NSData dataWithBytes:cvMat.data length:cvMat.elemSize()*cvMat.total()];
    CGColorSpaceRef colorSpace;
    
    if (cvMat.elemSize() == 1) {
        colorSpace = CGColorSpaceCreateDeviceGray();
    } else {
        colorSpace = CGColorSpaceCreateDeviceRGB();
    }
    
    CGDataProviderRef provider = CGDataProviderCreateWithCFData((__bridge CFDataRef)data);
    
    // Creating CGImage from cv::Mat
    CGImageRef imageRef = CGImageCreate(cvMat.cols,                                 //width
                                        cvMat.rows,                                 //height
                                        8,                                          //bits per component
                                        8 * cvMat.elemSize(),                       //bits per pixel
                                        cvMat.step[0],                            //bytesPerRow
                                        colorSpace,                                 //colorspace
                                        kCGImageAlphaNone|kCGBitmapByteOrderDefault,// bitmap info
                                        provider,                                   //CGDataProviderRef
                                        NULL,                                       //decode
                                        false,                                      //should interpolate
                                        kCGRenderingIntentDefault                   //intent
                                        );
    
    
    // Getting UIImage from CGImage
    UIImage *finalImage = [UIImage imageWithCGImage:imageRef];
    CGImageRelease(imageRef);
    CGDataProviderRelease(provider);
    CGColorSpaceRelease(colorSpace);
    
    return finalImage;
}


@end
