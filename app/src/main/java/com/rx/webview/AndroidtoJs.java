package com.rx.webview;

import android.util.Log;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

/**
 * Author:XWQ
 * Time   2019/2/27
 * Descrition: this is AndroidtoJs
 */
// 继承自Object类
public class AndroidtoJs extends Object
{
    // 定义JS需要调用的方法
    // 被JS调用的方法必须加入@JavascriptInterface注解
    @JavascriptInterface
    public void hello(String msg)
    {
        Log.d("mmp","JS调用了Android的hello方法");
    }
}
