//
//  TestPlugin.h
//  HelloWorld
//
//  Created by xiaobai on 17/9/14.
//
//
#import <Cordova/CDVPlugin.h>
#import "CameraViewController.h"
#import "ClipViewController.h"
#import <UIKit/UIKit.h>
#import <AssetsLibrary/AssetsLibrary.h>
#import <CoreLocation/CoreLocation.h>
#import <AVFoundation/AVFoundation.h>
#import <ImageIO/ImageIO.h>
#import <objc/message.h>

@interface ImageProcess : CDVPlugin<UIActionSheetDelegate,CameraViewDelegate,ClipPhotoDelegate,UIImagePickerControllerDelegate,UINavigationControllerDelegate,UIAlertViewDelegate>

//进入自定义相机
-(void)openCamera:(CDVInvokedUrlCommand *)command;

//进入裁剪
-(void)openCrop:(CDVInvokedUrlCommand *)command;

//进入相册
-(void)openAlbum:(CDVInvokedUrlCommand *)command;

@property (nonatomic) NSString *callbackId;

@end
