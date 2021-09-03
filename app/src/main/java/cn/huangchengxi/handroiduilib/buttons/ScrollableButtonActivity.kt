package cn.huangchengxi.handroiduilib.buttons

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import cn.huangchengxi.handroiduilib.R
import cn.huangchengxi.handroiduilib.databinding.ActivityScrollableButtonBinding

class ScrollableButtonActivity : AppCompatActivity() {
    private val mBinding by lazy { ActivityScrollableButtonBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scrollable_button)
        mBinding.contentText.setOnClickListener {
            Toast.makeText(this, "Click", Toast.LENGTH_SHORT).show()
            mBinding.btn.expanded=!mBinding.btn.expanded
        }
        mBinding.contentText.setOnLongClickListener {
            Toast.makeText(this, "Long Click", Toast.LENGTH_SHORT).show()
            true
        }
    }
}