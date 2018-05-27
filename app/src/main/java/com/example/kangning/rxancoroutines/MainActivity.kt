package com.example.kangning.rxancoroutines

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import io.reactivex.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.android.UI
import java.util.concurrent.ScheduledThreadPoolExecutor

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        launch { runBlockFunc() }
    }


}
