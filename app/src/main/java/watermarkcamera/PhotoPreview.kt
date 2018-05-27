package watermarkcamera

import android.content.Context
import android.graphics.Bitmap
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import com.example.kangning.rxancoroutines.R
import kotlinx.android.synthetic.main.component_phone_pager.view.*
import android.widget.LinearLayout.LayoutParams as Params

class PhotoPreview : FrameLayout {

    private val customView: View by lazy {
        LayoutInflater.from(context).inflate(R.layout.component_phone_pager, null, false)
    }


    private fun makePagerItem() = ImageView(context).apply {
        setOnClickListener { this@PhotoPreview.visibility = View.GONE }
    }


    fun initPhotos(imgSource: ArrayList<Bitmap>, currentIndex: Int) {
        val photos = ArrayList<ImageView>()
        imgSource.forEach {
            val item = makePagerItem()
            item.setImageBitmap(it)
            photos.add(item)
        }
        pager.adapter = PhotoPagerAdapter(photos, pager)
        pager.currentItem = currentIndex
        pos_text.text = "${currentIndex + 1} / ${imgSource.size}"
        pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                pos_text.text = "${position + 1} / ${imgSource.size}"
            }
        })
    }

    constructor(context: Context) : super(context) {
        this.addView(customView)
    }

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        this.addView(customView)
    }


    class PhotoPagerAdapter : PagerAdapter {

        private var photos: ArrayList<ImageView>
        private var viewPager: ViewPager

        constructor(photos: ArrayList<ImageView>, viewPager: ViewPager) : super() {
            this.photos = photos
            this.viewPager = viewPager
        }

        override fun isViewFromObject(view: View?, `object`: Any?): Boolean {
            return view == `object`
        }

        override fun getCount(): Int {
            return photos.size
        }

        override fun destroyItem(container: ViewGroup?, position: Int, `object`: Any?) {
            viewPager.removeView(photos[position])
        }

        override fun instantiateItem(container: ViewGroup?, position: Int): Any {
            viewPager.addView(photos[position])
            return photos[position]
        }
    }


}