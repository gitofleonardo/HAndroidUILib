package cn.huangchengxi.handroiduilib

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import cn.huangchengxi.handroiduilib.buttons.ScrollableButtonActivity
import cn.huangchengxi.handroiduilib.container.NestedScrollActivity
import cn.huangchengxi.handroiduilib.container.OverScrollActivity
import cn.huangchengxi.handroiduilib.databinding.ActivityMainBinding
import cn.huangchengxi.handroiduilib.indicators.PageIndicatorActivity
import cn.huangchengxi.handroiduilib.system.SystemBottomBarActivity

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
            bottomPullBar.setOnClickListener {
                startActivity(Intent(this@MainActivity,SystemBottomBarActivity::class.java))
            }
            overScrollView.setOnClickListener {
                startActivity(Intent(this@MainActivity,OverScrollActivity::class.java))
            }
            nestedView.setOnClickListener {
                startActivity(Intent(this@MainActivity,NestedScrollActivity::class.java))
            }
        }
    }
}