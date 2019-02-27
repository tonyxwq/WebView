package com.rx.webview;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;

import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity
{
    private WebView mWebView;
    private Button mButton;

    // android js 相互调用  webview 全面介绍
    //https://www.jianshu.com/p/345f4d8a5cfa
    //https://www.jianshu.com/p/3c94ae673e2a

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mButton = findViewById(R.id.btn_confirm);
        LinearLayout mLinearLayout = findViewById(R.id.root);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        //避免WebView内存泄露 不在xml中定义 Webview ，而是在需要的时候在Activity中创建，并且Context使用 getApplicationgContext()
        mWebView = new WebView(getApplicationContext());
        mWebView.setLayoutParams(layoutParams);
        mLinearLayout.addView(mWebView);
        //初始化设置
        initSeting(mWebView);
        //通过 WebView的addJavascriptInterface（）进行对象映射
        //AndroidtoJS类对象映射到js的test对象
        //缺点：存在严重的漏洞问题，存在严重的漏洞问题，具体请看文章 https://www.jianshu.com/p/3a345d27cd42
        mWebView.addJavascriptInterface(new AndroidtoJs(), "test");
        mWebView.loadUrl("file:///android_asset/demo.html");
        WebViewClient();
        WebChromeClient();

        mButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                //方法一
                // mWebView.loadUrl("javascript:callJS()");
                //该方法比第一种方法效率更高、使用更简洁。
                //因为该方法的执行不会使页面刷新，而第一种方法（loadUrl ）的执行则会。
                //Android 4.4 后才可使用
                mWebView.evaluateJavascript("javascript:callJS()", new ValueCallback<String>()
                {
                    @Override
                    public void onReceiveValue(String value)
                    {
                        //此处为 js 返回的结果
                        Toast.makeText(MainActivity.this, "value" + value, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void initSeting(WebView mWebView)
    {
        // 特别注意：5.1以上默认禁止了https和http混用，以下方式是开启
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            mWebView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        //声明WebSettings子类
        WebSettings webSettings = mWebView.getSettings();
        //如果访问的页面中要与Javascript交互，则webview必须设置支持Javascript
        webSettings.setJavaScriptEnabled(true);
        // 若加载的 html 里有JS 在执行动画等操作，会造成资源浪费（CPU、电量）
        // 在 onStop 和 onResume 里分别把 setJavaScriptEnabled() 给设置成 false 和 true 即可
        //设置自适应屏幕，两者合用
        webSettings.setUseWideViewPort(true); //将图片调整到适合webview的大小
        webSettings.setLoadWithOverviewMode(true); // 缩放至屏幕的大小
        //缩放操作
        webSettings.setSupportZoom(true); //支持缩放，默认为true。是下面那个的前提。
        webSettings.setBuiltInZoomControls(true); //设置内置的缩放控件。若为false，则该WebView不可缩放
        webSettings.setDisplayZoomControls(false); //隐藏原生的缩放控件
        //其他细节操作
        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK); //关闭webview中缓存
        webSettings.setAllowFileAccess(true); //设置可以访问文件
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true); //支持通过JS打开新窗口
        webSettings.setLoadsImagesAutomatically(true); //支持自动加载图片
        webSettings.setDefaultTextEncodingName("utf-8");//设置编码格式

        mWebView.setInitialScale(200);//为25%，最小缩放等级

        /**
         * 设置WebView缓存
         *
         * 当加载 html 页面时，WebView会在/data/data/包名目录下生成 database 与 cache 两个文件夹
         * 请求的 URL记录保存在 WebViewCache.db，而 URL的内容是保存在 WebViewCache 文件夹下
         * 是否启用缓存：
         *  //优先使用缓存:
         *  WebView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
         *         //缓存模式如下：
         *         //LOAD_CACHE_ONLY: 不使用网络，只读取本地缓存数据
         *         //LOAD_DEFAULT: （默认）根据cache-control决定是否从网络上取数据。
         *         //LOAD_NO_CACHE: 不使用缓存，只从网络获取数据.
         *         //LOAD_CACHE_ELSE_NETWORK，只要本地有，无论是否过期，或者no-cache，都使用缓存中的数据。
         *
         *     //不使用缓存:
         *     WebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
         *
         *
         */

        /*if (NetStatusUtil.isConnected(getApplicationContext()))
        {
            webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);//根据cache-control决定是否从网络上取数据。
        } else
        {
            webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);//没网，则从本地获取，即离线加载
        }

        webSettings.setDomStorageEnabled(true); // 开启 DOM storage API 功能
        webSettings.setDatabaseEnabled(true);   //开启 database storage API 功能
        webSettings.setAppCacheEnabled(true);//开启 Application Caches 功能

        String cacheDirPath = getFilesDir().getAbsolutePath() + APP_CACAHE_DIRNAME;
        webSettings.setAppCachePath(cacheDirPath); //设置  Application Caches 缓存目录*/
    }

    /**
     * WebViewClient类
     * 处理各种通知 & 请求事件
     */
    private void WebViewClient()
    {
        /**
         * 常见方法1：shouldOverrideUrlLoading()
         *
         * 作用：打开网页时不调用系统浏览器， 而是在本WebView中显示；在网页上的所有加载都经过这个方法,这个函数我们可以做很多操作。
         */
        mWebView.setWebViewClient(new WebViewClient()
        {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url)
            {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon)
            {
                //设定加载开始的操作
                //Toast.makeText(MainActivity.this, "onPageStarted", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPageFinished(WebView view, String url)
            {
                //设定加载结束的操作
                //Toast.makeText(MainActivity.this, "onPageFinished", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onLoadResource(WebView view, String url)
            {
                super.onLoadResource(view, url);
                //在加载页面资源时会调用，每一个资源（比如图片）的加载都会调用一次。
            }

            //步骤1：写一个html文件（error_handle.html），用于出错时展示给用户看的提示页面
            //步骤2：将该html文件放置到代码根目录的assets文件夹下
            //步骤3：复写WebViewClient的onRecievedError方法
            //该方法传回了错误码，根据错误类型可以进行不同的错误分类处理
            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error)
            {
                super.onReceivedError(view, request, error);
                view.loadUrl("file:///android_assets/error_handle.html");
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error)
            {
                handler.proceed();    //表示等待证书响应
                // handler.cancel();      //表示挂起连接，为默认方式
                // handler.handleMessage(null);    //可做其他处理
            }


        });
    }

    /**
     * 用：辅助 WebView 处理 Javascript 的对话框,网站图标,网站标题等等。
     */
    private void WebChromeClient()
    {
        mWebView.setWebChromeClient(new WebChromeClient()

                                    {
                                        public void onProgressChanged(WebView view, int progress)
                                        {
                                            setProgress(progress * 100);
                                            Log.d("mmp", "===============" + progress);
                                            if (progress == 100)
                                            {
                                            }
                                        }

                                        //获取Web页中的标题
                                        public void onReceivedTitle(WebView view, String title)
                                        {
                                            Log.d("mmp", "========title=======" + title);
                                        }

                                        //支持javascript的警告框
                                        @Override
                                        public boolean onJsAlert(WebView view, String url, String message, final JsResult result)
                                        {
                                            new AlertDialog.Builder(MainActivity.this)
                                                    .setTitle("JsAlert")
                                                    .setMessage(message)
                                                    .setPositiveButton("OK", new DialogInterface.OnClickListener()
                                                    {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which)
                                                        {
                                                            result.confirm();
                                                        }
                                                    })
                                                    .setCancelable(false)
                                                    .show();
                                            return true;
                                        }

                                        //支持javascript的确认框
                                        @Override
                                        public boolean onJsConfirm(WebView view, String url, String message, final JsResult result)
                                        {
                                            new AlertDialog.Builder(MainActivity.this)
                                                    .setTitle("JsConfirm")
                                                    .setMessage(message)
                                                    .setPositiveButton("OK", new DialogInterface.OnClickListener()
                                                    {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which)
                                                        {
                                                            result.confirm();
                                                        }
                                                    })
                                                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener()
                                                    {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which)
                                                        {
                                                            result.cancel();
                                                        }
                                                    })
                                                    .setCancelable(false)
                                                    .show();
                                            // 返回布尔值：判断点击时确认还是取消
                                            // true表示点击了确认；false表示点击了取消；
                                            return true;
                                        }

                                        //支持javascript输入框
                                        //点击确认返回输入框中的值，点击取消返回 null。
                                        @Override
                                        public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, final JsPromptResult result)
                                        {
                                            final EditText et = new EditText(MainActivity.this);
                                            et.setText(defaultValue);
                                            new AlertDialog.Builder(MainActivity.this)
                                                    .setTitle(message)
                                                    .setView(et)
                                                    .setPositiveButton("OK", new DialogInterface.OnClickListener()
                                                    {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which)
                                                        {
                                                            result.confirm(et.getText().toString());
                                                        }
                                                    })
                                                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener()
                                                    {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which)
                                                        {
                                                            result.cancel();
                                                        }
                                                    })
                                                    .setCancelable(false)
                                                    .show();

                                            return true;
                                        }


                                    }

        );


    }

}