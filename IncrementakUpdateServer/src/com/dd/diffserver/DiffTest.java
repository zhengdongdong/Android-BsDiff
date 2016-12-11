package com.dd.diffserver;

public class DiffTest {

	/**
	 *	增量更新依赖于 BsDiff(依赖Bzip2) 项目 
	 *
	 *	1. 下载 BsDiff 和 Bzip2(Windows 下找到 Windows port 下载源码)
	 *	2. 根据下载的 源码生成动态库(so/dll, 下面介绍在windows下使用vs)
	 *	2.1. 只拷贝里面的c/h/cpp(不需要bspatch.cpp)
	 *	2.2. 解决错误:
	 *				1.不安全的函数 (#define _CRT_SECURE_NO_WARNINGS)
	 *				2.过时的函数(#define _CRT_NONSTDC_NO_DEPRECATE)
	 *				3.(vs专属)严格的安全检查(属性 -> c/c++ -> 常规 -> SDL检查 -> 否)
	 *		  或者:属性 -> c/c++ -> 命令行 -> (-D _CRT_SECURE_NO_WARNINGS -D _CRT_NONSTDC_NO_DEPRECATE)
	 *	2.3. 根据源码, 修改  bsdiff.cpp 文件, main() -> bsdiff_main()
	 *	2.4. 根据c/c++代码, 编写 java层代码,生成头文件
	 *	2.5. 编写JNI函数, 供java层调用(bsdiff.cpp引入生成的头文件)(若引入不到, 则是编码问题)
	 *	2.6. 生成dll(属性 -> 常规 -> 配置类型 -> dll) (配置可选x64)
	 */
	
	private static final String OLD_FILE = "D:\\Develop\\Projects\\workspace\\IncrementakUpdateServer\\update1.0.apk";
	private static final String NEW_FILE = "D:\\Develop\\Projects\\workspace\\IncrementakUpdateServer\\update2.0.apk";
	private static final String PATCH_FILE = "D:\\Develop\\Projects\\workspace\\IncrementakUpdateServer\\patch1to2.patch";
	
	public static void main(String[] args) {
		
		Diff.diff(OLD_FILE, NEW_FILE, PATCH_FILE);
		
	}

}
