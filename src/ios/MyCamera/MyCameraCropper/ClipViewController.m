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

#define SelfWidth [UIScreen mainScreen].bounds.size.width
#define SelfHeight  [UIScreen mainScreen].bounds.size.height
@interface ClipViewController ()
{
    UIButton *_closeButton;
    UIButton *_takeButton;
    UIButton *_orientationButton;
    UIView *_balckView;

    UILabel *_prompt;


    UIView *_loadingView;
    UILabel *_loadingText;
    UILabel *_loadingImage;

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

    [self createLoading];

}


-(void)createLoading{
    _loadingView = [[UIView alloc]initWithFrame:CGRectMake(0, 0, 100, 100)];
    _loadingView.center = CGPointMake(MAKE.size.width/2, MAKE.size.height/2);
    [_loadingView setBackgroundColor:[UIColor colorWithWhite:0.2 alpha:0.8]];
    _loadingView.layer.cornerRadius = 10;
   _loadingView.transform = CGAffineTransformMakeRotation(M_PI/2);
    [self.view addSubview:_loadingView];

    UIImageView * imageView = [[UIImageView alloc]initWithFrame:CGRectMake(10, 20, 55, 55)];
    imageView.center = CGPointMake(_loadingView.frame.size.width/2, 40);
    imageView.image = [UIImage imageNamed:@"quan.png"];

    UILabel * label = [[UILabel alloc]initWithFrame:CGRectMake(0, CGRectGetMaxY(imageView.frame), _loadingView.frame.size.width, 20)];
    label.text = @"图片处理请稍后";
    label.font = [UIFont systemFontOfSize:13];
    label.textAlignment = NSTextAlignmentCenter;
    [label setTextColor:[UIColor whiteColor]];
    [_loadingView addSubview:label];


    //------- 旋转动画 -------//
    CABasicAnimation *animation = [ CABasicAnimation
                                   animationWithKeyPath: @"transform" ];
    animation.fromValue = [NSValue valueWithCATransform3D:CATransform3DIdentity];
    // 围绕Z轴旋转，垂直与屏幕
    animation.toValue = [ NSValue valueWithCATransform3D:
                         CATransform3DMakeRotation(M_PI, 0.0, 0.0, 1.0) ];
    animation.duration = 0.5;
    // 旋转效果累计，先转180度，接着再旋转180度，从而实现360旋转
    animation.cumulative = YES;
    animation.repeatCount = 1000;

    //在图片边缘添加一个像素的透明区域，去图片锯齿
    CGRect imageRrect = CGRectMake(0, 0,imageView.frame.size.width, imageView.frame.size.height);
    UIGraphicsBeginImageContext(imageRrect.size);
    [imageView.image drawInRect:CGRectMake(1,1,imageView.frame.size.width-2,imageView.frame.size.height-2)];
    imageView.image = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();
    // 添加动画
    [imageView.layer addAnimation:animation forKey:nil];
    [_loadingView addSubview:imageView];

    _loadingView.hidden = YES;

}

-(void)loadingShowAndDismiss{
    _loadingView.hidden = !_loadingView.hidden;
    if (_loadingView.hidden) {
        self.view.userInteractionEnabled = YES;
    }else{
        self.view.userInteractionEnabled = NO;
    }
}

- (void)createdTkImageView
{
    _tkImageView = [[TKImageView alloc] initWithFrame:CGRectMake(0, 20, SelfWidth, SelfHeight - 120)];
    _tkImageView.backgroundColor = [UIColor clearColor];
    [self.view addSubview:_tkImageView];
    //需要进行裁剪的图片对象[OpencvProcess opencvProcessImage:self.image andConstValue:7.5];
    _tkImageView.toCropImage = self.image;
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
        _tkImageView.toCropImage = self.image;
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

        [self loadingShowAndDismiss];

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

        SaveImage  = [SaveImage rotate:UIImageOrientationLeft];
//        BOOL success;
//        NSData *data = [self compressOriginalImage:SaveImage toMaxDataSizeKBytes:300];
        NSData *data = [self compressOriginalImage:[self imageCompressWithSimple:SaveImage] toMaxDataSizeKBytes:300.0];
        BOOL success;
        NSLog(@"%lu",(unsigned long)data.length/1024);
        success = [data writeToFile:imageFilePath  atomically:YES];
        if (success){
            NSString *fileName = [imageFilePath pathExtension];
            NSLog(@"fileName:%@",fileName);
            NSLog(@"imageFilePath:%@",imageFilePath);
            [self loadingShowAndDismiss];
            [self OpenDraw:imageFilePath];
        }else{
            [self loadingShowAndDismiss];
        }

        NSLog(@"储存到本地");
    }
}

- (UIImage*)imageCompressWithSimple:(UIImage*)image{

    CGSize size = image.size;
    CGFloat scale = 1.0;
    //TODO:KScreenWidth屏幕宽
    if (size.width > MAKE.size.width || size.height > MAKE.size.height) {
        if (size.width > size.height) {
            scale = MAKE.size.width / size.width;
        }else {
            scale = MAKE.size.height / size.height;
        }
    }
    CGFloat width = size.width;
    CGFloat height = size.height;
    CGFloat scaledWidth = width * scale;
    CGFloat scaledHeight = height * scale;
    CGSize secSize =CGSizeMake(scaledWidth, scaledHeight);
    //TODO:设置新图片的宽高
    UIGraphicsBeginImageContext(secSize); // this will crop
    [image drawInRect:CGRectMake(0,0,scaledWidth,scaledHeight)];
    UIImage* newImage= UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();
    return newImage;
}

/**
 *  压缩图片到指定文件大小
 *
 *  @param image 目标图片
 *  @param size  目标大小（最大值）
 *
 *  @return 返回的图片文件
 */
- (NSData *)compressOriginalImage:(UIImage *)image toMaxDataSizeKBytes:(CGFloat)size{
    NSData * data = UIImageJPEGRepresentation(image, 1.0);
    CGFloat dataKBytes = data.length/1000.0;
    CGFloat maxQuality = 0.9f;
    CGFloat lastData = dataKBytes;
    while (dataKBytes > size && maxQuality > 0.01f) {
        maxQuality = maxQuality - 0.01f;
        data = UIImageJPEGRepresentation(image, maxQuality);
        dataKBytes = data.length / 1000.0;
        if (lastData == dataKBytes) {
            break;
        }else{
            lastData = dataKBytes;
        }
    }
    return data;
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
