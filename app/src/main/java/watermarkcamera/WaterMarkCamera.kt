package watermarkcamera

import android.content.Context
import android.graphics.Bitmap
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
import java.util.*

/**
 * Created by kangning on 2018/5/25.
 */
class WaterMarkCamera : FrameLayout {

    companion object {
        private const val SIZE = 3
    }

    private val dataStack: Stack<ImageSource> by lazy {
        Stack<ImageSource>()
    }


    fun getCapturedData() = kotlin.run {
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


    interface PhotoClickListener {
        fun onLocalPress()
        fun onCapturedPress(index: Int)
    }

    lateinit var onCapturePressListener: PhotoClickListener

    private val waterMarkAdapter: WaterMarkAdapter by lazy {
        WaterMarkAdapter(R.layout.marker_image_item).apply {
            dataStack.push(ImageSource.LocalImageSource)
            setNewData(dataStack.toList())
            setOnItemChildClickListener { adapter, view, position ->
                when (view.id) {
                    R.id.img ->
                        when (adapter.data[position]) {
                            is ImageSource.LocalImageSource -> {
                                onCapturePressListener.onLocalPress()
                            }
                            is ImageSource.CapturedImageSource -> {
                                onCapturePressListener.onCapturedPress(position)
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
            when (item) {
                is ImageSource.CapturedImageSource -> {
                    helper.setImageBitmap(R.id.img, item.bitmap)
                }
                is ImageSource.LocalImageSource -> {
                    helper.setImageResource(R.id.img, item.id)
                    helper.setImageResource(R.id.close, R.drawable.ic_launcher_background)
                }
            }
            helper.addOnClickListener(R.id.img)
            helper.addOnClickListener(R.id.close)
        }
    }

    private fun getDataSize() = dataStack.size

    fun addImage(bitmap: Bitmap) {
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


    sealed class ImageSource(imageType: ImageType) {
        data class CapturedImageSource(val bitmap: Bitmap) : ImageSource(ImageType.CAPTURE)
        object LocalImageSource : ImageSource(ImageType.LOCAL) {
            const val id: Int = R.drawable.ic_launcher_background
        }
    }

    enum class ImageType {
        LOCAL, CAPTURE
    }

    class ImageUtil {
        companion object {
            fun zipImage(source: Bitmap): Bitmap {

                return source
            }
        }
    }
}
