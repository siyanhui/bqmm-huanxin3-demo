/**
 * Copyright (C) 2016 Hyphenate Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hyphenate.chatuidemo;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.easemob.redpacketsdk.RedPacket;
import com.melink.bqmmsdk.sdk.BQMM;
import com.tencent.bugly.crashreport.CrashReport;
// ============== fabric start
//import com.crashlytics.android.Crashlytics;
//import io.fabric.sdk.android.Fabric;
// ============== fabric end

public class DemoApplication extends Application {

	public static Context applicationContext;
	private static DemoApplication instance;
	// login user name
	public final String PREF_USERNAME = "username";
	
	/**
	 * nickname for current user, the nickname instead of ID be shown when user receive notification from APNs
	 */
	public static String currentUserNick = "";

	@Override
	public void onCreate() {
		super.onCreate();
// ============== fabric start
//		Fabric.with(this, new Crashlytics());
// ============== fabric end
        applicationContext = this;
        instance = this;
        
        //init demo helper
        DemoHelper.getInstance().init(applicationContext);
		//red packet code : 初始化红包上下文，开启日志输出开关
		RedPacket.getInstance().initContext(applicationContext);
		RedPacket.getInstance().setDebugMode(true);
		//end of red packet code
		CrashReport.initCrashReport(getApplicationContext());
		/**
		 * BQMM集成
		 * 首先从AndroidManifest.xml中取得appId和appSecret，然后对BQMM SDK进行初始化
		 */
		try {
			Bundle bundle = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA).metaData;
			BQMM.getInstance().initConfig(applicationContext, bundle.getString("bqmm_app_id"), bundle.getString("bqmm_app_secret"));
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
	}

	public static DemoApplication getInstance() {
		return instance;
	}

	@Override
	protected void attachBaseContext(Context base) {
		super.attachBaseContext(base);
	}
}
