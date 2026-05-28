package com.yijian.android

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.tencent.kuikly.core.KuiklyApplication

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 使用 Kuikly 原生渲染承载页面
        KuiklyApplication.startPager(this, "MainPage")
    }
}
