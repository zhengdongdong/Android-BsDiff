package com.dd.diffserver;

public class Diff {

	/**
	 * 差分
	 * 
	 * @param oldFile
	 *            旧文件
	 * @param newFile
	 *            新文件
	 * @param patchFile
	 *            差分文件
	 */
	public native static void diff(String oldFile, String newFile,
			String patchFile);
	
	static{
		System.loadLibrary("bsdiff");
	}
}
