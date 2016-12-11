package com.dd.incremental;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

public class MainActivity extends Activity {

	/**
	 * 1. 将 bzip2 文件夹拷入, 将bspatch.c拷入 
	 * 2. 添加 native 支持 
	 * 3. javah 生成头文件 
	 * 4. bspatch.c 中添加头文件引用, 修改bzip2 的引入, 修改main()为bs_main()
	 */

	public static final String PATCH_FILE = "patch1to2.patch";

	public static final String URL_PATCH_DOWNLOAD = "http://192.168.1.108:8080/updateapp/"
			+ PATCH_FILE;

	public static final String PACKAGE_NAME = "com.dd.incremental";

	public static final String SD_CARD = Environment
			.getExternalStorageDirectory() + File.separator;

	// 新版本apk的目录
	public static final String NEW_APK_PATH = SD_CARD + "update2.apk";

	public static final String PATCH_FILE_PATH = SD_CARD + PATCH_FILE;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		new ApkUpdateTask().execute();
	}

	private class ApkUpdateTask extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Void... params) {
			try {

				// 下载差分包
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						Toast.makeText(MainActivity.this, "正在下载..", Toast.LENGTH_SHORT).show();
					}
				});
				File patchFile = download(URL_PATCH_DOWNLOAD);
				
				// 合并patch

				// 当前应用apk文件 data/app
				String oldfile = getSourceApkPath(MainActivity.this,
						PACKAGE_NAME);
				String newfile = NEW_APK_PATH;
				String patchfile = patchFile.getAbsolutePath();
				
				Log.e("tag", oldfile);
				Log.e("tag", newfile);
				Log.e("tag", patchfile);

				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						Toast.makeText(MainActivity.this, "正在合并..", Toast.LENGTH_SHORT).show();
					}
				});
				BsPatch.patch(oldfile, newfile, patchfile);
				return true;
			} catch (Exception e) {
				return false;
			}
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			// 安装 apk
			if (result) {
				Toast.makeText(MainActivity.this, "正在安装..", Toast.LENGTH_SHORT).show();
				installApk(MainActivity.this, NEW_APK_PATH);
			}
		}

	}

	/**
	 * 下载差分包
	 * 
	 * @param url
	 * @return
	 * @throws Exception
	 */
	private File download(String url) {
		File file = null;
		InputStream is = null;
		FileOutputStream os = null;
		try {
			file = new File(Environment.getExternalStorageDirectory(),
					PATCH_FILE);
			if (file.exists()) {
				file.delete();
			}
			HttpURLConnection conn = (HttpURLConnection) new URL(url)
					.openConnection();
			conn.setDoInput(true);
			is = conn.getInputStream();
			os = new FileOutputStream(file);
			byte[] buffer = new byte[1024];
			int len = 0;
			while ((len = is.read(buffer)) != -1) {
				os.write(buffer, 0, len);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				os.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return file;
	}

	private boolean isInstalled(Context context, String packageName) {
		PackageManager pm = context.getPackageManager();
		boolean installed = false;
		try {
			pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
			installed = true;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return installed;
	}

	/**
	 * 获取已安装Apk文件的源Apk文件 如：/data/app/my.apk
	 * 
	 * @param context
	 * @param packageName
	 * @return
	 */
	private String getSourceApkPath(Context context, String packageName) {
		if (TextUtils.isEmpty(packageName))
			return null;

		try {
			ApplicationInfo appInfo = context.getPackageManager()
					.getApplicationInfo(packageName, 0);
			return appInfo.sourceDir;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * 安装Apk
	 * 
	 * @param context
	 * @param apkPath
	 */
	public static void installApk(Context context, String apkPath) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.parse("file://" + apkPath),
				"application/vnd.android.package-archive");
		context.startActivity(intent);
	}

}
