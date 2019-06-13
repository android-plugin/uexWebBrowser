package org.zywx.wbpalmstar.plugin.uexwebbrowser;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.RelativeLayout;

import com.tencent.smtt.sdk.*;

import org.zywx.wbpalmstar.base.BDebug;
import org.zywx.wbpalmstar.engine.DataHelper;
import org.zywx.wbpalmstar.engine.EBrowserView;
import org.zywx.wbpalmstar.engine.universalex.EUExBase;
import org.zywx.wbpalmstar.plugin.uexwebbrowser.vo.InitVO;
import org.zywx.wbpalmstar.plugin.uexwebbrowser.vo.OpenVO;

public class EUExWebBrowser extends EUExBase {

    private static final String TAG = "EUExWebBrowser";
    private static final String BUNDLE_DATA = "data";

    WebView mX5WebView;
    X5WebViewClient mX5WebViewClient;
    X5WebChromeClient mX5WebChromeClient;
    WebSettings mX5WebSettings;
    public EUExWebBrowser(Context context, EBrowserView eBrowserView) {
        super(context, eBrowserView);
    }

    @Override
    protected boolean clean() {
        return false;
    }
    

    @Override
    public void onHandleMessage(Message message) {
        if(message == null){
            return;
        }
        Bundle bundle=message.getData();
        switch (message.what) {

        default:
                super.onHandleMessage(message);
        }
    }

    public void init(String[] params){
        InitVO initVO=DataHelper.gson.fromJson(params[0],InitVO.class);
        mX5WebView=new WebView(mContext);
        mX5WebViewClient=new X5WebViewClient();
        mX5WebChromeClient=new X5WebChromeClient();
        mX5WebView.setWebViewClient(mX5WebViewClient);
        mX5WebView.setWebChromeClient(mX5WebChromeClient);
        mX5WebSettings =mX5WebView.getSettings();
        printX5KernelDebugLog();
        initWebSetttings();
        configWebView(initVO);
    }

    private void initWebSetttings(){
        mX5WebSettings.setLoadWithOverviewMode(true);
        mX5WebSettings.setAllowContentAccess(true);
        mX5WebSettings.setJavaScriptEnabled(true);
        mX5WebSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        mX5WebSettings.setAllowContentAccess(true);
        mX5WebSettings.setAllowFileAccess(true);
        mX5WebSettings.setGeolocationEnabled(true);
        mX5WebSettings.setAllowFileAccessFromFileURLs(true);
        mX5WebSettings.setDomStorageEnabled(true);
        mX5WebSettings.setAppCacheMaxSize(Long.MAX_VALUE);
        mX5WebSettings.setUseWideViewPort(true);
        mX5WebSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        if (!isX5Enabled()){
            //如果X5内核未加载成功时，则需要手动设置允许混合模式，解决个别页面或者视频无法正常显示和播放的问题（http与https）
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mX5WebSettings.setMixedContentMode(android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
            }
        }
    }

    private void configWebView(InitVO initVO){
        if (!TextUtils.isEmpty(initVO.userAgent)){
            mX5WebSettings.setUserAgent(mX5WebSettings.getUserAgentString()+initVO.userAgent);
        }
        WebView.setWebContentsDebuggingEnabled(initVO.debug);

    }

    /**
     * 输出调试版本信息
     */
    private void printX5KernelDebugLog(){
        int x5SdkVersion = WebView.getTbsSDKVersion(mContext);
        int x5CoreVersion = WebView.getTbsCoreVersion(mContext);
        BDebug.d(TAG ,"printX5KernelDebugLog: x5SdkVersion=" + x5SdkVersion);
        if (mX5WebView == null){
            return;
        }
        if (isX5Enabled()){
            BDebug.d(TAG, "printX5KernelDebugLog: using X5 Core");
            BDebug.d(TAG, "printX5KernelDebugLog: x5CoreVersion=" + x5CoreVersion);
        }else{
            BDebug.d(TAG, "printX5KernelDebugLog: not using X5 Core, using System Core");
        }
    }

    /**
     * 是否已经加载成功X5内核
     *
     */
    private boolean isX5Enabled(){
        return mX5WebView.getX5WebViewExtension() != null;
    }

    public boolean open(String[] params) {
        if (mX5WebView==null){
            return false;
        }
        String json = params[0];
        OpenVO openVO= DataHelper.gson.fromJson(json,OpenVO.class);
        RelativeLayout.LayoutParams lp=new RelativeLayout.LayoutParams(openVO.width,openVO.height);
        lp.leftMargin=openVO.x;
        lp.topMargin=openVO.y;
        addViewToCurrentWindow(mX5WebView,lp);
        if (!TextUtils.isEmpty(openVO.url)){
            mX5WebView.loadUrl(openVO.url);
        }
        return true;
    }

    public void close(String[] params) {
        if (mX5WebView!=null) {
            mX5WebView.destroy();
            removeViewFromCurrentWindow(mX5WebView);
            mX5WebView=null;
        }
    }

    public void goBack(String[] params) {
        mX5WebView.goBack();
    }

    public void goForward(String[] params) {
        mX5WebView.goForward();
    }

    public boolean canGoBack(String[] params) {
        return mX5WebView.canGoBack();
    }

    public boolean canGoForward(String[] params) {
        return mX5WebView.canGoForward();
    }

    public void reload(String[] params) {
        mX5WebView.reload();
    }

    public void loadUrl(String[] params) {
        mX5WebView.loadUrl(params[0]);
    }

    public void evaluateJavascript(String[] params) {
        mX5WebView.evaluateJavascript(params[0], new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String s) {

            }
        });
    }

    public String getTitle(String[] params){
        return mX5WebView==null?"":mX5WebView.getTitle();
    }

    private void callBackPluginJs(String methodName, String jsonData){
        String js = SCRIPT_HEADER + "if(" + methodName + "){"
                + methodName + "('" + jsonData + "');}";
        onCallback(js);
    }


    public static void onApplicationCreate(Context context){
        QbSdk.PreInitCallback cb = new QbSdk.PreInitCallback() {

            @Override
            public void onViewInitFinished(boolean arg0) {
                BDebug.e(" onViewInitFinished is " + arg0);
            }

            @Override
            public void onCoreInitFinished() {

            }
        };
        QbSdk.setTbsListener(new TbsListener() {
            @Override
            public void onDownloadFinish(int i) {
                BDebug.d("onDownloadFinish is " + i);
            }

            @Override
            public void onInstallFinish(int i) {
                BDebug.d("onInstallFinish is " + i);
            }

            @Override
            public void onDownloadProgress(int i) {
                BDebug.d("onDownloadProgress:"+i);
            }
        });

        QbSdk.initX5Environment(context,  cb);

    }
}
