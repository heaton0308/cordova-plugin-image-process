//
//  ClipViewController.m
//  Camera
//
//  Created by wzh on 2017/6/6.
//  Copyright © 2017年 wzh. All rights reserved.
//

#import "ClipViewController.h"
#import "TKImageView.h"
#import "BYFCameraProcessHeader.h"
#import "UIImage+Rotate.h"
#import "OpencvProcess.h"
#import "KVNProgress.h"


#define SelfWidth [UIScreen mainScreen].bounds.size.width
#define SelfHeight  [UIScreen mainScreen].bounds.size.height
@interface ClipViewController ()
{
    UIButton *_closeButton;
    UIButton *_takeButton;
    UIButton *_orientationButton;
    UIView *_balckView;
    
    UILabel *_prompt;
}
@property (nonatomic, assign) BOOL isClip;

@property (nonatomic, strong) TKImageView *tkImageView;

@end

@implementation ClipViewController

- (void)viewDidLoad {
    [super viewDidLoad];
//    self.navigationController.navigationBar.hidden = YES;
    self.navigationController.navigationBarHidden = YES;
    
    self.view.backgroundColor = [UIColor blackColor];
    
    [self createdTkImageView];
    
    [self createdTool];
    
}

- (void)createdTkImageView
{
    _tkImageView = [[TKImageView alloc] initWithFrame:CGRectMake(0, 20, SelfWidth, SelfHeight - 120)];
    _tkImageView.backgroundColor = [UIColor clearColor];
    [self.view addSubview:_tkImageView];
    //需要进行裁剪的图片对象
    _tkImageView.toCropImage = [OpencvProcess opencvProcessImage:self.image andConstValue:7.5];
    //是否显示中间线
    _tkImageView.showMidLines = YES;
    //是否需要支持缩放裁剪
    _tkImageView.needScaleCrop = YES;
    //是否显示九宫格交叉线
    _tkImageView.showCrossLines = YES;
    _tkImageView.cornerBorderInImage = NO;
    _tkImageView.cropAreaCornerWidth = 25;
    _tkImageView.cropAreaCornerHeight = 35;
    _tkImageView.minSpace = 30;
    _tkImageView.cropAreaCornerLineColor = [UIColor whiteColor];
    _tkImageView.cropAreaBorderLineColor = [UIColor whiteColor];
    _tkImageView.cropAreaCornerLineWidth = 6;
    _tkImageView.cropAreaBorderLineWidth = 1;
    _tkImageView.cropAreaMidLineWidth = 20;
    _tkImageView.cropAreaMidLineHeight = 6;
    _tkImageView.cropAreaMidLineColor = [UIColor whiteColor];
    _tkImageView.cropAreaCrossLineColor = [UIColor whiteColor];
    _tkImageView.cropAreaCrossLineWidth = 0.5;
    _tkImageView.initialScaleFactor = .8f;
    _tkImageView.cropAspectRatio = 0;
    _tkImageView.maskColor = [UIColor colorWithRed:0/255.0 green:0/255.0 blue:0/255.0 alpha:0.5];
    
    self.isClip = NO;
}

- (void)createdTool
{
    _balckView = [[UIView alloc]initWithFrame:CGRectMake(0, MAKE.size.height - 80, MAKE.size.width, 80)];
    _balckView.userInteractionEnabled = YES;
    _balckView.backgroundColor = [UIColor blackColor];
    [self.view addSubview:_balckView];
    
    //旋转
    _orientationButton = [UIButton buttonWithType:UIButtonTypeCustom];
    _orientationButton.frame = CGRectMake(MAKE.size.width - 80, 15, 60, 50);
    _orientationButton.transform = CGAffineTransformMakeRotation(M_PI/2);
    [_orientationButton addTarget:self action:@selector(orientation:) forControlEvents:UIControlEventTouchUpInside];
    _orientationButton.tag = 101;
    
    UIImageView *backImage = [[UIImageView alloc]initWithFrame:CGRectMake(2, (_orientationButton.frame.size.width - 18)/2, 18, 18)];
    backImage.image = [UIImage imageNamed:@"marquee_btn_back_prebtn_rotate_normal@3x.png"];
    [_orientationButton addSubview:backImage];
    
    UILabel *backLabel = [[UILabel alloc]initWithFrame:CGRectMake(backImage.frame.origin.x + backImage.frame.size.width, 0, 40, 50)];
    backLabel.text = @"旋转";
    backLabel.textColor = [UIColor whiteColor];
    backLabel.font = [UIFont systemFontOfSize:16];
    [_orientationButton addSubview:backLabel];
    
    [_balckView addSubview:_orientationButton];
    
    //拍照
    _takeButton = [UIButton buttonWithType:UIButtonTypeCustom];
    _takeButton.frame = CGRectMake((MAKE.size.width - 60)/2, 10, 60, 60);
    _takeButton.transform = CGAffineTransformMakeRotation(M_PI/2);
    [_takeButton setImage:[UIImage imageNamed:@"marquee_btn_ok_normal@3x.png"] forState:UIControlStateNormal];
    [_takeButton addTarget:self action:@selector(takePhotoAction:) forControlEvents:UIControlEventTouchUpInside];
    _takeButton.tag = 101;
    [_balckView addSubview:_takeButton];
    
    //返回
    _closeButton = [UIButton buttonWithType:UIButtonTypeCustom];
    _closeButton.frame = CGRectMake(20, 15, 60, 50);
    _closeButton.transform = CGAffineTransformMakeRotation(M_PI/2);
    [_closeButton addTarget:self action:@selector(dissMissButton) forControlEvents:UIControlEventTouchUpInside];
    
    UIImageView *backImage2 = [[UIImageView alloc]initWithFrame:CGRectMake(2, (_closeButton.frame.size.width - 18)/2, 18, 18)];
    backImage2.image = [UIImage imageNamed:@"marquee_btn_retake_normal.png"];
    [_closeButton addSubview:backImage2];
    
    UILabel *backLabel2 = [[UILabel alloc]initWithFrame:CGRectMake(backImage2.frame.origin.x + backImage2.frame.size.width, 0, 40, 50)];
    backLabel2.text = @"重拍";
    backLabel2.textColor = [UIColor whiteColor];
    backLabel2.font = [UIFont systemFontOfSize:16];
    [_closeButton addSubview:backLabel2];
    
    [_balckView addSubview:_closeButton];
}

-(void)orientation:(UIButton *)button{
    if (button.tag == 101) {
        NSLog(@"%ld",(long)self.image.imageOrientation);
        if (self.image.imageOrientation == 3) {
            self.image = [self.image rotate:UIImageOrientationRight];
        }
        self.image = [self.image rotate:UIImageOrientationRight];
        _tkImageView.toCropImage = [OpencvProcess opencvProcessImage:self.image andConstValue:7.5];
    }
}

-(void)dissMissButton{
    if (self.flog == 10086) {
        [self dismissViewControllerAnimated:YES completion:nil];
        [self.delegate savePhotoPath:@""];
        return;
    }
    if (self.flog == 999) {
        [self dismissViewControllerAnimated:YES completion:nil];
    }else{
        [self.navigationController popViewControllerAnimated:YES];
    }
}

-(void)takePhotoAction:(UIButton *)button{
    if (button.tag == 101) {
        UIImage *SaveImage = [_tkImageView currentCroppedImage];
        
        // 本地沙盒目录
        NSString *path = [NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES) lastObject];
        // 得到本地沙盒中名为"MyImage"的路径，"MyImage"是保存的图片名
        
        NSDateFormatter *formater = [[NSDateFormatter alloc] init];
        formater.dateFormat = @"yyyyMMddHHmmss";
        NSString *currentTimeStr = [[formater stringFromDate:[NSDate date]] stringByAppendingFormat:@"_%d" ,arc4random_uniform(10000)];
        
        NSString *imageFilePath = [path stringByAppendingPathComponent:[NSString stringWithFormat:@"%@.jpg",currentTimeStr]];
        
        if (![imageFilePath hasSuffix:@".jpg"]) {
            imageFilePath = [NSString stringWithFormat:@"%@.jpg",imageFilePath];
        }
        
        NSFileManager *manager = [NSFileManager defaultManager];
        [manager createDirectoryAtPath:imageFilePath withIntermediateDirectories:YES attributes:nil error:nil];
        NSString *path2 = imageFilePath;
        [manager removeItemAtPath:path2 error:nil];
        
        NSLog(@"%@",imageFilePath);
        
//        BOOL success;
//        NSData *data = [self compressOriginalImage:SaveImage toMaxDataSizeKBytes:300];

        [self compressedImageFiles:SaveImage imageKB:295 imageBlock:^(NSData *image) {
            NSData *data = image;
            BOOL success;
            NSLog(@"%lu",(unsigned long)data.length/1024);
            success = [data writeToFile:imageFilePath  atomically:YES];
            if (success){
                NSString *fileName = [imageFilePath pathExtension];
                NSLog(@"fileName:%@",fileName);
                NSLog(@"imageFilePath:%@",imageFilePath);
                [self OpenDraw:imageFilePath];
            }
        }];

        NSLog(@"储存到本地");
    }
}
    
    /**
     *  压缩图片
     *
     *  @param image       需要压缩的图片
     *  @param fImageBytes 希望压缩后的大小(以KB为单位)
     *
     *  @return 压缩后的图片
     */
- (void)compressedImageFiles:(UIImage *)image
                     imageKB:(CGFloat)fImageKBytes
                  imageBlock:(void(^)(NSData *image))block {
    
    __block UIImage *imageCope = image;
    CGFloat fImageBytes = fImageKBytes * 1024;//需要压缩的字节Byte
    
    __block NSData *uploadImageData = nil;
    
    uploadImageData = UIImagePNGRepresentation(imageCope);
    NSLog(@"图片压前缩成 %fKB",uploadImageData.length/1024.0);
    CGSize size = imageCope.size;
    CGFloat imageWidth = size.width;
    CGFloat imageHeight = size.height;
    
    if (uploadImageData.length > fImageBytes && fImageBytes >0) {
        
        dispatch_async(dispatch_queue_create("CompressedImage", DISPATCH_QUEUE_SERIAL), ^{
            
            /* 宽高的比例 **/
            CGFloat ratioOfWH = imageWidth/imageHeight;
            /* 压缩率 **/
            CGFloat compressionRatio = fImageBytes/uploadImageData.length;
            /* 宽度或者高度的压缩率 **/
            CGFloat widthOrHeightCompressionRatio = sqrt(compressionRatio);
            
            CGFloat dWidth   = imageWidth *widthOrHeightCompressionRatio;
            CGFloat dHeight  = imageHeight*widthOrHeightCompressionRatio;
            if (ratioOfWH >0) { /* 宽 > 高,说明宽度的压缩相对来说更大些 **/
                dHeight = dWidth/ratioOfWH;
            }else {
                dWidth  = dHeight*ratioOfWH;
            }
            
            imageCope = [self drawWithWithImage:imageCope width:dWidth height:dHeight];
            uploadImageData = UIImagePNGRepresentation(imageCope);
            
            NSLog(@"当前的图片已经压缩成 %fKB",uploadImageData.length/1024.0);
            /* 控制在 1M 以内**/
            while (fabs(uploadImageData.length - fImageBytes) > 1024) {
                /* 再次压缩的比例**/
                CGFloat nextCompressionRatio = 0.9;
                
                if (uploadImageData.length > fImageBytes) {
                    dWidth = dWidth*nextCompressionRatio;
                    dHeight= dHeight*nextCompressionRatio;
                }else {
                    dWidth = dWidth/nextCompressionRatio;
                    dHeight= dHeight/nextCompressionRatio;
                }
                
                imageCope = [self drawWithWithImage:imageCope width:dWidth height:dHeight];
                uploadImageData = UIImagePNGRepresentation(imageCope);
                
            }
            
            NSLog(@"图片已经压缩成 %fKB",uploadImageData.length/1024.0);
            
            dispatch_sync(dispatch_get_main_queue(), ^{
                block(uploadImageData);
            });
        });
    }
    else
    {
        block(uploadImageData);
    }
}
    
    /* 根据 dWidth dHeight 返回一个新的image**/
- (UIImage *)drawWithWithImage:(UIImage *)imageCope width:(CGFloat)dWidth height:(CGFloat)dHeight{
    
    UIGraphicsBeginImageContext(CGSizeMake(dWidth, dHeight));
    [imageCope drawInRect:CGRectMake(0, 0, dWidth, dHeight)];
    imageCope = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();
    
    return imageCope;
    
}

-(void)OpenDraw:(NSString *)imagePath{
    if (self.flog != 10086) {
        [self.navigationController popViewControllerAnimated:YES];
    }else{
        [self dismissViewControllerAnimated:YES completion:nil];
    }
    [self.delegate savePhotoPath:imagePath];
}


- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

/*
#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
}
*/

@end
