package cn.huangchengxi.handroiduilib

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import cn.huangchengxi.handroiduilib.buttons.ScrollableButtonActivity
import cn.huangchengxi.handroiduilib.databinding.ActivityMainBinding
import cn.huangchengxi.handroiduilib.indicators.PageIndicatorActivity
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {
    private val mBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)
        mBinding.apply {
            scrollButtonBtn.setOnClickListener {
                startActivity(Intent(this@MainActivity,ScrollableButtonActivity::class.java))
            }
            indicatorBtn.setOnClickListener {
                startActivity(Intent(this@MainActivity,PageIndicatorActivity::class.java))
            }
        }
    }
}