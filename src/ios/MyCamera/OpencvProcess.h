//
//  OpencvProcess.h
//  BYFCameraProcess
//
//  Created by xiaobai on 17/9/18.
//  Copyright © 2017年 byf. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>

@interface OpencvProcess : NSObject

+(UIImage *)opencvProcessImage:(UIImage *)image andConstValue:(double)value;

@end
