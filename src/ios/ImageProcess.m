//
//  TestPlugin.m
//  HelloWorld
//
//  Created by xiaobai on 17/9/14.
//
//

#import "ImageProcess.h"

@implementation ImageProcess

-(void)openCamera:(CDVInvokedUrlCommand *)command
{
    self.callbackId = [NSString stringWithFormat:@"%@",command.callbackId];

    if ([self showCameraVc]) {
        //进入相机
        UINavigationController *nav=[[UINavigationController alloc]init];
        
        CameraViewController *viewC = [[CameraViewController alloc]init];
        
        viewC.delegate = self;
        
        [nav addChildViewController:viewC];
        
        [self.viewController presentViewController:nav animated:YES completion:nil];
    }else{
        //CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"暂无权限"];
        //[self.commandDelegate sendPluginResult:pluginResult callbackId:self.callbackId];

        UIAlertView *alertView = [[UIAlertView alloc]initWithTitle:@"没有相机权限" message:[NSString stringWithFormat:@"请检查相机权限"] delegate:self cancelButtonTitle:@"知道了" otherButtonTitles:nil];
        [alertView show];

        //无相机权限
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"-1"];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:self.callbackId];
    }
    
    
}

-(void)openCrop:(CDVInvokedUrlCommand *)command
{
    self.callbackId = [NSString stringWithFormat:@"%@",command.callbackId];

    NSString *a = [command.arguments[1] substringFromIndex:6];
    
    UIImage *image = [UIImage imageWithContentsOfFile:a];
    //直接进入裁剪
    UINavigationController *nav=[[UINavigationController alloc]init];
    
    ClipViewController *viewC = [[ClipViewController alloc]init];
    
    viewC.image = image;
    
    viewC.flog = 10086;
    
    viewC.delegate = self;
    
    [nav addChildViewController:viewC];
    
    [self.viewController presentViewController:nav animated:YES completion:nil];
}
    
-(void)savePhotoPath:(NSString *)imagePath{
    [self dismissView:imagePath];
}

-(void)dismissView:(NSString *)path
{
    if (path == nil || path.length == 0 || [path isKindOfClass:[NSNull class]]) {
        path = [NSString stringWithFormat:@"0"];
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:path];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:self.callbackId];
    }else{
        NSURL *imageUrl = [NSURL fileURLWithPath:path];
        NSLog(@"url:%@",imageUrl);
        NSString *stringImageUrl = imageUrl.absoluteString;
        NSLog(@"stringImageUrl:%@",stringImageUrl);
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:stringImageUrl];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:self.callbackId];
    }
}

-(void)dismissViewComtroller:(NSString *)path
{
    [self dismissView:path];
}

-(void)openAlbum:(CDVInvokedUrlCommand *)command
{
    self.callbackId = [NSString stringWithFormat:@"%@",command.callbackId];
    
    if ([self showPickerVc]) {
        NSLog(@"进入相册");
        UIImagePickerController *imagePickerController = [[UIImagePickerController alloc] init];
        imagePickerController.delegate = self;
        imagePickerController.modalTransitionStyle = UIModalTransitionStyleFlipHorizontal;
        imagePickerController.sourceType = UIImagePickerControllerSourceTypePhotoLibrary;
        [self.viewController presentViewController:imagePickerController animated:YES completion:nil];
    }else{
//        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"暂无相册权限"];
//        [self.commandDelegate sendPluginResult:pluginResult callbackId:self.callbackId];

        UIAlertView *alertView = [[UIAlertView alloc]initWithTitle:@"没有相册权限" message:[NSString stringWithFormat:@"请检查相册权限"] delegate:self cancelButtonTitle:@"知道了" otherButtonTitles:nil];
        [alertView show];

        //无相册权限
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"-2"];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:self.callbackId];
    }
}

//该代理方法仅适用于只选取图片时
- (void)imagePickerController:(UIImagePickerController *)picker didFinishPickingImage:(UIImage *)image editingInfo:(nullable NSDictionary<NSString *,id> *)editingInfo {
    NSLog(@"选择完毕----image:%@-----info:%@",image,editingInfo);
    [self.viewController dismissViewControllerAnimated:YES completion:nil];
    
    //直接进入裁剪
    UINavigationController *nav=[[UINavigationController alloc]init];
    
    ClipViewController *viewC = [[ClipViewController alloc]init];
    
    viewC.image = image;
    
    viewC.flog = 10086;
    
    viewC.delegate = self;
    
    [nav addChildViewController:viewC];
    
    [self.viewController presentViewController:nav animated:YES completion:nil];
    
}

-(int)showCameraVc{
    AVAuthorizationStatus authStatus =  [AVCaptureDevice authorizationStatusForMediaType:AVMediaTypeVideo];
    if(authStatus == AVAuthorizationStatusRestricted || authStatus ==AVAuthorizationStatusDenied){
        return 0;
    }else{
        return 1;
    }

}

- (int)showPickerVc{
    ALAuthorizationStatus author = [ALAssetsLibrary authorizationStatus];
    if (author == kCLAuthorizationStatusRestricted || author == kCLAuthorizationStatusDenied){
        return 0;
    }else{
        return 1;
    }
}

@end
