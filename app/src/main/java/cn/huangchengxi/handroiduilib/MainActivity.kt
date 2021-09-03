package cn.huangchengxi.handroiduilib

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import cn.huangchengxi.handroiduilib.databinding.ActivityMainBinding
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {
    private val mBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val mPages= arrayListOf(
        BlankFragment(), BlankFragment(), BlankFragment(), BlankFragment()
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)
        mBinding.pager.apply {
            this.adapter=object : FragmentPagerAdapter(supportFragmentManager,BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT){
                override fun getCount(): Int {
                    return mPages.size
                }
                override fun getItem(position: Int): Fragment {
                    return mPages[position]
                }

                override fun getPageTitle(position: Int): CharSequence {
                    return "Hello World"
                }
            }
        }
        mBinding.tablayout.setupWithViewPager(mBinding.pager)
        mBinding.indicator.itemCount=mPages.size
        mBinding.pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener{
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {

            }
            override fun onPageSelected(position: Int) {
                mBinding.indicator.position=position
            }
            override fun onPageScrollStateChanged(state: Int) {

            }
        })
    }
}