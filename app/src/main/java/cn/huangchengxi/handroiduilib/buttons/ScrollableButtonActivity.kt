package cn.huangchengxi.handroiduilib.buttons

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import cn.huangchengxi.handroiduilib.R
import cn.huangchengxi.handroiduilib.databinding.ActivityScrollableButtonBinding
import cn.huangchengxi.uilib.buttons.OptionTextView
import cn.huangchengxi.uilib.buttons.ScrollableButton

class ScrollableButtonActivity : AppCompatActivity() {
    private val mBinding by lazy { ActivityScrollableButtonBinding.inflate(layoutInflater) }
    private val mItems= ArrayList<ScrollItem>()
    private val mAdapter=ScrollableButtonsAdapter(mItems)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)
        for (i in 0 until 20){
            mItems.add(ScrollItem("Item:${i}",false))
        }
        mBinding.rv.adapter=mAdapter
    }
}