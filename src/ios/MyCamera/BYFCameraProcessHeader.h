//
//  BYFCameraProcessHeader.h
//  BYFCameraProcess
//
//  Created by xiaobai on 17/9/18.
//  Copyright © 2017年 byf. All rights reserved.
//

#ifndef BYFCameraProcessHeader_h
#define BYFCameraProcessHeader_h

#define MAKE [UIScreen mainScreen].bounds

#ifdef DEBUG
#define NSLog(format, ...) do {                                                             \
fprintf(stderr, "<%s : %d> %s\n",                                           \
[[[NSString stringWithUTF8String:__FILE__] lastPathComponent] UTF8String],  \
__LINE__, __func__);                                                        \
(NSLog)((format), ##__VA_ARGS__);                                           \
fprintf(stderr, "---------------------------------------------------------------------------\n");                                               \
} while (0)
#else
#define NSLog(...)
#endif


#endif /* BYFCameraProcessHeader_h */
