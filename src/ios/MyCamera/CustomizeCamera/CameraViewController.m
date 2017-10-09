//
//  CameraView.m
//  Camera
//
//  Created by wzh on 2017/6/2.
//  Copyright © 2017年 wzh. All rights reserved.
//

#import "CameraViewController.h"
#import <AVFoundation/AVFoundation.h>
#import <AssetsLibrary/AssetsLibrary.h>
#import "BYFCameraProcessHeader.h"
#import "ClipViewController.h"
#import "UIImage+Rotate.h"
#define KWIDTH [UIScreen mainScreen].bounds.size.width
#define KHEIGHT [UIScreen mainScreen].bounds.size.height
@interface CameraViewController ()<UIGestureRecognizerDelegate,UINavigationControllerDelegate,UIImagePickerControllerDelegate,ClipPhotoDelegate>
{
    UIView *_balckView;
    UIButton *_lampButton;
    UIButton *_closeButton;
    UIButton *_takeButton;
    
    UIView *_line;
    UIView *_line2;
    UIView *_line3;
    UIView *_line4;
    UILabel *_prompt;
}

/**
 *  AVCaptureSession对象来执行输入设备和输出设备之间的数据传递
 */
@property (nonatomic, strong) AVCaptureSession* session;
/**
 *  输入设备
 */
@property (nonatomic, strong) AVCaptureDeviceInput* videoInput;
/**
 *  照片输出流
 */
@property (nonatomic, strong) AVCaptureStillImageOutput* stillImageOutput;
/**
 *  预览图层
 */
@property (nonatomic, strong) AVCaptureVideoPreviewLayer* previewLayer;

/**
 *  记录开始的缩放比例
 */
@property(nonatomic,assign)CGFloat beginGestureScale;
/**
 * 最后的缩放比例
 */
@property(nonatomic,assign)CGFloat effectiveScale;

@property (nonatomic, strong) AVCaptureConnection *stillImageConnection;

@property (nonatomic, strong) NSData  *jpegData;

@property (nonatomic, assign) CFDictionaryRef attachments;

@property (nonatomic, strong) UIView *toolView;

@property (nonatomic, strong) UIView *editorView;

@property (nonatomic, strong) UIImagePickerController *imgPicker;


@end

@implementation CameraViewController
- (void)viewDidLoad {
    [super viewDidLoad];
    
//    [self CreatedCamera];
//    [self selectImageFrromCamera];
    self.navigationController.navigationBarHidden = YES;

    [self initAVCaptureSession];
    [self setUpGesture];
    [self createdTool];
}

- (void)createdTool
{
    _balckView = [[UIView alloc]initWithFrame:CGRectMake(0, MAKE.size.height - 80, MAKE.size.width, 80)];
    _balckView.userInteractionEnabled = YES;
    _balckView.backgroundColor = [UIColor blackColor];
    _balckView.alpha = 0.5;
    [self.view addSubview:_balckView];
    
    //闪光灯
    _lampButton = [UIButton buttonWithType:UIButtonTypeCustom];
    _lampButton.frame = CGRectMake(15, 15, 50, 50);
    [_lampButton setImage:[UIImage imageNamed:@"takepic_btn_lightoff@3x.png"] forState:UIControlStateNormal];
    [_lampButton setImage:[UIImage imageNamed:@"takepic_btn_lighton@3x.png"] forState:UIControlStateSelected];
    _lampButton.transform = CGAffineTransformMakeRotation(M_PI/2);
    [_lampButton addTarget:self action:@selector(flashButtonClick:) forControlEvents:UIControlEventTouchUpInside];
    [_balckView addSubview:_lampButton];
    
    //取消
    _closeButton = [UIButton buttonWithType:UIButtonTypeCustom];
    _closeButton.frame = CGRectMake(MAKE.size.width - 65, 15, 50, 50);
    [_closeButton setImage:[UIImage imageNamed:@"takepic_btn_normal@3x.png"] forState:UIControlStateNormal];
    _closeButton.transform = CGAffineTransformMakeRotation(M_PI/2);
    [_closeButton addTarget:self action:@selector(cancleCamera) forControlEvents:UIControlEventTouchUpInside];
    [_balckView addSubview:_closeButton];
    
    //拍照
    _takeButton = [UIButton buttonWithType:UIButtonTypeCustom];
    _takeButton.frame = CGRectMake((MAKE.size.width - 60)/2, 15, 60, 60);
    _takeButton.transform = CGAffineTransformMakeRotation(M_PI/2);
    [_takeButton setImage:[UIImage imageNamed:@"takepic_btn_takepic_normal@3x.png"] forState:UIControlStateNormal];
    [_takeButton addTarget:self action:@selector(takePhotoButtonClick) forControlEvents:UIControlEventTouchUpInside];
    [_balckView addSubview:_takeButton];
    
    //分界线
    _line = [[UIView alloc]initWithFrame:CGRectMake(5,70, MAKE.size.width - 10, 1)];
    _line.backgroundColor = [UIColor whiteColor];
    _line.alpha = 0.6;
    [self.view addSubview:_line];
    
    _line2 = [[UIView alloc]initWithFrame:CGRectMake(MAKE.size.width - MAKE.size.width/4.5, 20, 1, MAKE.size.height - 100)];
    _line2.backgroundColor = [UIColor whiteColor];
    _line2.alpha = 0.6;
    [self.view addSubview:_line2];
    
    _line3 = [[UIView alloc]initWithFrame:CGRectMake(5, MAKE.size.height - _takeButton.frame.size.height - 80, MAKE.size.width - 10, 1)];
    _line3.backgroundColor = [UIColor whiteColor];
    _line3.alpha = 0.6;
    [self.view addSubview:_line3];
    
    _line4 = [[UIView alloc]initWithFrame:CGRectMake(MAKE.size.width/4.5, 20, 1, MAKE.size.height - 100)];
    _line4.backgroundColor = [UIColor whiteColor];
    _line4.alpha = 0.6;
    [self.view addSubview:_line4];
    
    _prompt = [[UILabel alloc]initWithFrame:CGRectMake(-85 + (MAKE.size.width/4.5 - 15)/2, (MAKE.size.height - MAKE.size.width/4.5 - 15)/2 + 15, 200,MAKE.size.width/4.5 - 15 )];
    _prompt.text = @"题目文字尽可能置于画面中间与参考线平行";
    _prompt.numberOfLines = 2;
    _prompt.textAlignment = NSTextAlignmentCenter;
    _prompt.transform = CGAffineTransformMakeRotation(M_PI/2);
    _prompt.textColor = [UIColor whiteColor];
    [self.view addSubview:_prompt];
}

- (void)initAVCaptureSession{
    
    self.session = [[AVCaptureSession alloc] init];
    
    NSError *error;
    
    self.effectiveScale = 1.0;
    
    AVCaptureDevice *device = [AVCaptureDevice defaultDeviceWithMediaType:AVMediaTypeVideo];
    
    //更改这个设置的时候必须先锁定设备，修改完后再解锁，否则崩溃
    [device lockForConfiguration:nil];
    
    [device unlockForConfiguration];
    
    self.videoInput = [[AVCaptureDeviceInput alloc] initWithDevice:device error:&error];
    if (error) {
        NSLog(@"%@",error);
    }
    self.stillImageOutput = [[AVCaptureStillImageOutput alloc] init];
    //输出设置。AVVideoCodecJPEG   输出jpeg格式图片
    NSDictionary * outputSettings = [[NSDictionary alloc] initWithObjectsAndKeys:AVVideoCodecJPEG,AVVideoCodecKey, nil];
    [self.stillImageOutput setOutputSettings:outputSettings];
    
    if ([self.session canAddInput:self.videoInput]) {
        [self.session addInput:self.videoInput];
    }
    if ([self.session canAddOutput:self.stillImageOutput]) {
        [self.session addOutput:self.stillImageOutput];
    }
    
    //初始化预览图层
    self.previewLayer = [[AVCaptureVideoPreviewLayer alloc] initWithSession:self.session];
    [self.previewLayer setVideoGravity:AVLayerVideoGravityResizeAspectFill];

    self.previewLayer.frame = CGRectMake(0, 0,KWIDTH, KHEIGHT);
    self.view.layer.masksToBounds = YES;
    [self.view.layer addSublayer:self.previewLayer];
    
    [self resetFocusAndExposureModes];
}

- (void)viewWillAppear:(BOOL)animated{
    
    [super viewWillAppear:YES];
    if (self.session) {
        
        [self.session startRunning];
    }
    NSLog(@"%d",_lampButton.isSelected);
    if (_lampButton.isSelected == YES) {
        _lampButton.selected = !_lampButton.selected;
    }
    if (_takeButton) {
        _takeButton.userInteractionEnabled = YES;
    }
}


- (void)viewDidDisappear:(BOOL)animated{
    
    [super viewDidDisappear:YES];
    if (self.session) {
        
        [self.session stopRunning];
    }
}
//自动聚焦、曝光
- (BOOL)resetFocusAndExposureModes{


    AVCaptureDevice *device = self.videoInput.device;
    AVCaptureExposureMode exposureMode = AVCaptureExposureModeContinuousAutoExposure;
    AVCaptureFocusMode focusMode = AVCaptureFocusModeContinuousAutoFocus;
    BOOL canResetFocus = [device isFocusPointOfInterestSupported] && [device isFocusModeSupported:focusMode];
    BOOL canResetExposure = [device isExposurePointOfInterestSupported] && [device isExposureModeSupported:exposureMode];
    //CGPoint centerPoint = CGPointMake(0.5f, 0.5f);
    NSError *error;
    if ([device lockForConfiguration:&error]) {
        if (canResetFocus) {
            device.focusMode = focusMode;
            //device.focusPointOfInterest = centerPoint;
        }
        if (canResetExposure) {
            device.exposureMode = exposureMode;
            //device.exposurePointOfInterest = centerPoint;
        }
        [device unlockForConfiguration];
        return YES;
    }
    else{
        NSLog(@"%@", error);
        return NO;
    }
}

- (void)touchesBegan:(NSSet<UITouch *> *)touches withEvent:(UIEvent *)event
{
    UITouch *touch = touches.anyObject;
    CGPoint point = [touch locationInView:self.view];
    [self focusAtPoint:point];
}
//聚焦
- (id)focusAtPoint:(CGPoint)point {
    AVCaptureDevice *device = self.videoInput.device;
    if ([self cameraSupportsTapToFocus] && [device isFocusModeSupported:AVCaptureFocusModeAutoFocus])
    {
        NSError *error;
        if ([device lockForConfiguration:&error]) {
            //device.focusPointOfInterest = point;
            device.focusMode = AVCaptureFocusModeAutoFocus;
            [device unlockForConfiguration];

            double delayInSeconds = 2.0;
            dispatch_queue_t mainQueue = dispatch_get_main_queue();
            dispatch_time_t popTime = dispatch_time(DISPATCH_TIME_NOW,delayInSeconds * NSEC_PER_SEC);
            dispatch_after(popTime, mainQueue, ^{
                NSLog(@"延时执行的2秒");
                [self resetFocusAndExposureModes];
            });
        }
        return error;
    }
    return nil;
//    AVCaptureDevice *device = [AVCaptureDevice defaultDeviceWithMediaType:AVMediaTypeVideo];
//    if ([self cameraSupportsTapToFocus] && [device isFocusModeSupported:AVCaptureFocusModeAutoFocus]) {
//        NSError *error;
//        if ([device lockForConfiguration:&error]) {
//            device.focusPointOfInterest = point;
//            device.focusMode = AVCaptureFocusModeAutoFocus;
//            [device unlockForConfiguration];
//        }
//        else{
//            NSLog(@"%@", error);
//
//        }
//    }
}

- (BOOL)cameraSupportsTapToFocus {
    return [[AVCaptureDevice defaultDeviceWithMediaType:AVMediaTypeVideo] isFocusPointOfInterestSupported];
}

//获取设备方向
-(AVCaptureVideoOrientation)avOrientationForDeviceOrientation:(UIDeviceOrientation)deviceOrientation
{
    AVCaptureVideoOrientation result = (AVCaptureVideoOrientation)deviceOrientation;
    if ( deviceOrientation == UIDeviceOrientationLandscapeLeft )
        result = AVCaptureVideoOrientationLandscapeRight;
    else if ( deviceOrientation == UIDeviceOrientationLandscapeRight )
        result = AVCaptureVideoOrientationLandscapeLeft;
    return result;
}
//照相
- (void)takePhotoButtonClick {
    _takeButton.userInteractionEnabled = NO;
    _stillImageConnection = [self.stillImageOutput        connectionWithMediaType:AVMediaTypeVideo];
    UIDeviceOrientation curDeviceOrientation = [[UIDevice currentDevice] orientation];
    AVCaptureVideoOrientation avcaptureOrientation = [self avOrientationForDeviceOrientation:curDeviceOrientation];
    [_stillImageConnection setVideoOrientation:avcaptureOrientation];
    [_stillImageConnection setVideoScaleAndCropFactor:self.effectiveScale];
    
    [self.stillImageOutput captureStillImageAsynchronouslyFromConnection:_stillImageConnection completionHandler:^(CMSampleBufferRef imageDataSampleBuffer, NSError *error) {
        
        NSData *jpegData = [AVCaptureStillImageOutput jpegStillImageNSDataRepresentation:imageDataSampleBuffer];
        NSLog(@"jpegData:%lu",jpegData.length/1024);
        UIImage *image = [UIImage imageWithData:jpegData];
        image = [image rotate:UIImageOrientationRight];
        [self openClipView:image];
    }];
}

-(void)openClipView:(UIImage *)image
{
    ClipViewController *viewController = [[ClipViewController alloc] init];
    viewController.image = image;
    viewController.delegate = self;
    viewController.isTakePhoto = YES;
    viewController.nameStr = [NSString stringWithFormat:@"%@",self.nameStr];
    [self.navigationController pushViewController:viewController animated:YES];
}

-(void)savePhotoPath:(NSString *)imagePath
{
    [self.delegate dismissViewComtroller:imagePath];
    [self dismissViewControllerAnimated:YES completion:nil];
}
    
- (void)cancleCamera
{
    [self.delegate dismissViewComtroller:@""];
    [self dismissViewControllerAnimated:YES completion:nil];
}


//打开闪光灯
- (void)flashButtonClick:(UIButton *)sender {
    
    sender.selected = !sender.selected;
    if (sender.isSelected == YES) { //打开闪光灯
        AVCaptureDevice *captureDevice = [AVCaptureDevice defaultDeviceWithMediaType:AVMediaTypeVideo];
        NSError *error = nil;
        
        if ([captureDevice hasTorch]) {
            BOOL locked = [captureDevice lockForConfiguration:&error];
            if (locked) {
                captureDevice.torchMode = AVCaptureTorchModeOn;
                [captureDevice unlockForConfiguration];
            }
        }
    }else{//关闭闪光灯
        AVCaptureDevice *device = [AVCaptureDevice defaultDeviceWithMediaType:AVMediaTypeVideo];
        if ([device hasTorch]) {
            [device lockForConfiguration:nil];
            [device setTorchMode: AVCaptureTorchModeOff];
            [device unlockForConfiguration];
        }
    }
}
//添加手势代理
- (void)setUpGesture
{
    UIPinchGestureRecognizer *pinch = [[UIPinchGestureRecognizer alloc] initWithTarget:self action:@selector(handlePinchGesture:)];
    pinch.delegate = self;
    [self.view addGestureRecognizer:pinch];
}

//缩放手势 用于调整焦距
- (void)handlePinchGesture:(UIPinchGestureRecognizer *)recognizer{
    
    BOOL allTouchesAreOnThePreviewLayer = YES;
    NSUInteger numTouches = [recognizer numberOfTouches], i;
    for ( i = 0; i < numTouches; ++i ) {
        CGPoint location = [recognizer locationOfTouch:i inView:self.view];
        CGPoint convertedLocation = [self.previewLayer convertPoint:location fromLayer:self.previewLayer.superlayer];
        if ( ! [self.previewLayer containsPoint:convertedLocation] ) {
            allTouchesAreOnThePreviewLayer = NO;
            break;
        }
    }
    
    if ( allTouchesAreOnThePreviewLayer ) {
        
        
        self.effectiveScale = self.beginGestureScale * recognizer.scale;
        if (self.effectiveScale < 1.0){
            self.effectiveScale = 1.0;
        }
        
        NSLog(@"%f-------------->%f------------recognizerScale%f",self.effectiveScale,self.beginGestureScale,recognizer.scale);
        
        CGFloat maxScaleAndCropFactor = [[self.stillImageOutput connectionWithMediaType:AVMediaTypeVideo] videoMaxScaleAndCropFactor];
        
        NSLog(@"%f",maxScaleAndCropFactor);
        if (self.effectiveScale > maxScaleAndCropFactor)
            self.effectiveScale = maxScaleAndCropFactor;
        
        [CATransaction begin];
        [CATransaction setAnimationDuration:.025];
        [self.previewLayer setAffineTransform:CGAffineTransformMakeScale(self.effectiveScale, self.effectiveScale)];
        [CATransaction commit];
        
    }
    
}

- (BOOL)gestureRecognizerShouldBegin:(UIGestureRecognizer *)gestureRecognizer
{
    if ( [gestureRecognizer isKindOfClass:[UIPinchGestureRecognizer class]] ) {
        self.beginGestureScale = self.effectiveScale;
    }
    return YES;
}

//////返回上一界面
//- (void)openCamera
//{
//    [self.delegate dismissViewComtroller];
//}




@end
