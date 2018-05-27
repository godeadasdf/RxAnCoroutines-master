package com.example.kangning.rxancoroutines

import android.app.Activity
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import watermarkcamera.WaterMarkCamera
import android.graphics.BitmapFactory
import android.content.Intent
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import android.support.v4.content.FileProvider
import android.view.View


class MainActivity : AppCompatActivity(), WaterMarkCamera.PhotoClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        water_mark_camera.onCapturePressListener = this
//        launch { runBlockFunc() }
    }

    override fun onLocalPress() {
        //打开相机
        openCamera()
    }

    override fun onCapturedPress(index: Int) {
        photo_preview.visibility = View.VISIBLE
        photo_preview.initPhotos(water_mark_camera.getCapturedData(),index)
    }

    private fun makePhotoFile() =
            Environment.getExternalStorageDirectory().path + "/${System.currentTimeMillis()}.png"// 获取SD卡路径


    companion object {
        private const val REQUEST_CAMERA = 0x0000001
    }

    private var currentPath = ""
    // 拍照后存储并显示图片
    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)// 启动系统相机
        currentPath = makePhotoFile()
        val photoFile = File(currentPath) // 传递路径
        val uri = FileProvider.getUriForFile(
                this,
                "$packageName.provider",
                photoFile
        )
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)// 更改系统默认存储路径
        startActivityForResult(intent, REQUEST_CAMERA)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (data == null) return
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) { // 如果返回数据
            if (requestCode == REQUEST_CAMERA) {
                var fis: FileInputStream? = null
                try {
                    fis = FileInputStream(currentPath)
                    val bitmap = BitmapFactory.decodeStream(fis)
                    water_mark_camera.addImage(bitmap)
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                } finally {
                    try {
                        fis?.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                }
            }
        }
    }
}
