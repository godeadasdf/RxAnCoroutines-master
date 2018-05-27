package watermarkcamera

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat.startActivityForResult
import android.support.v4.content.FileProvider
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.example.kangning.rxancoroutines.R
import kotlinx.android.synthetic.main.marker.view.*
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.util.*

/**
 * 水印相机+图片预览封装
 *
 * use case:
 *class MainActivity : AppCompatActivity() {
 *
 *override fun onCreate(savedInstanceState: Bundle?) {
 *    super.onCreate(savedInstanceState)
 *    setContentView(R.layout.activity_main)
 *    water_mark_camera.attachedActivity = this
 *    water_mark_camera.photoPreview = photo_preview
 * }
 *
 *override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
 *     water_mark_camera.onActivityResult(requestCode, resultCode, data)
 *}
 *}
 */


class WaterMarkCamera : FrameLayout {


    //以下为使用该套件的需要依赖的全部两个参数
    lateinit var attachedActivity: Activity
    lateinit var photoPreview: PhotoPreview

    companion object {
        private const val SIZE = 3
        const val REQUEST_CAMERA = 0x0000001
    }

    private val dataStack: Stack<ImageSource> by lazy {
        Stack<ImageSource>()
    }


    private fun getCapturedData() = kotlin.run {
        val bitmaps = ArrayList<Bitmap>()
        dataStack.forEach {
            if (it is ImageSource.CapturedImageSource) {
                bitmaps.add(it.bitmap)
            }
        }
        bitmaps
    }


    private val customView: View by lazy {
        LayoutInflater.from(context).inflate(R.layout.marker, null, false)
    }

    private val photos: RecyclerView by lazy {
        photoRecycler.apply {
            layoutManager = GridLayoutManager(context, 3)
        }
    }

    private val waterMarkAdapter: WaterMarkAdapter by lazy {
        WaterMarkAdapter(R.layout.marker_image_item).apply {
            dataStack.push(ImageSource.LocalImageSource)
            setNewData(dataStack.toList())
            setOnItemChildClickListener { adapter, view, position ->
                when (view.id) {
                    R.id.img ->
                        when (adapter.data[position]) {
                            is ImageSource.LocalImageSource -> {
                                openCamera()
                            }
                            is ImageSource.CapturedImageSource -> {
                                if (photoPreview != null) {
                                    photoPreview.visibility = View.VISIBLE
                                    photoPreview.initPhotos(getCapturedData(), position)
                                }
                            }
                        }
                    R.id.close ->
                        removeImage(position)
                }

            }
        }
    }


    constructor(context: Context) : super(context) {
        this.addView(customView)
        photos.adapter = waterMarkAdapter
    }

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        this.addView(customView)
        photos.adapter = waterMarkAdapter
    }


    class WaterMarkAdapter(resourceId: Int) : BaseQuickAdapter<ImageSource, BaseViewHolder>(resourceId) {
        override fun convert(helper: BaseViewHolder, item: ImageSource) {
            helper.setImageResource(R.id.close, R.mipmap.close)
            when (item) {
                is ImageSource.CapturedImageSource -> {
                    helper.setImageBitmap(R.id.img, item.bitmap)
                    helper.setVisible(R.id.close, true)
                }
                is ImageSource.LocalImageSource -> {
                    helper.setImageResource(R.id.img, item.id)
                    helper.setVisible(R.id.close, false)
                }
            }
            helper.addOnClickListener(R.id.img)
            helper.addOnClickListener(R.id.close)
        }
    }

    private fun getDataSize() = dataStack.size

    private fun addImage(bitmap: Bitmap) {
        addCapturedImage(ImageSource.CapturedImageSource(bitmap))
    }

    private fun addCapturedImage(imageSource: ImageSource.CapturedImageSource) {
        val dataSize = getDataSize()
        /*   waterMarkAdapter.remove(dataSize - 1)
           waterMarkAdapter.addData(imageSource)
           if (dataSize < 3) {
               waterMarkAdapter.addData(ImageSource.LocalImageSource(R.drawable.ic_launcher_background))
           }*/
        dataStack.pop()
        dataStack.add(imageSource)
        if (dataSize < SIZE) {
            dataStack.push(ImageSource.LocalImageSource)
        }
        waterMarkAdapter.setNewData(dataStack)
    }

    private fun removeImage(index: Int) {
        val dataSize = getDataSize()
        dataStack.remove(dataStack[index])
        if (index == dataSize - 1 || dataStack.indexOf(ImageSource.LocalImageSource) == -1) {
            dataStack.push(ImageSource.LocalImageSource)
        }
        waterMarkAdapter.setNewData(dataStack)
    }


    sealed class ImageSource {
        data class CapturedImageSource(val bitmap: Bitmap) : ImageSource()
        object LocalImageSource : ImageSource() {
            const val id: Int = R.mipmap.plus
        }
    }


    private fun makePhotoFile() =
            Environment.getExternalStorageDirectory().path + "/${System.currentTimeMillis()}.png"// 获取SD卡路径


    private var currentPath = ""
    // 拍照后存储并显示图片
    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)// 启动系统相机
        currentPath = makePhotoFile()
        val photoFile = File(currentPath) // 传递路径
        val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                photoFile
        )
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)// 更改系统默认存储路径
        startActivityForResult(attachedActivity, intent, REQUEST_CAMERA, null)
    }

    //用于置于activity中onActivityResult的方法
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (data == null) return
        if (resultCode == Activity.RESULT_OK) { // 如果返回数据
            if (requestCode == REQUEST_CAMERA) {
                var fis: FileInputStream? = null
                try {
                    fis = FileInputStream(currentPath)
                    val bitmap = BitmapFactory.decodeStream(fis)
                    addImage(bitmap)
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
