package cn.huangchengxi.handroiduilib.buttons

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import cn.huangchengxi.handroiduilib.R
import cn.huangchengxi.handroiduilib.databinding.ActivityScrollableButtonBinding
import cn.huangchengxi.uilib.buttons.OptionTextView
import cn.huangchengxi.uilib.buttons.ScrollableButton

class ScrollableButtonActivity : AppCompatActivity() {
    private val mBinding by lazy { ActivityScrollableButtonBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scrollable_button)
        mBinding.btn.apply {
            addOption(
                OptionTextView(context).apply {
                    text="Hello"
                },
                object : ScrollableButton.OnOptionClickListener{
                    override fun onClick(view: OptionTextView) {
                        Toast.makeText(context, "Hello", Toast.LENGTH_SHORT).show()
                    }
                }
            )
            addOption(
                OptionTextView(context).apply {
                    text="World"
                },
                object : ScrollableButton.OnOptionClickListener{
                    override fun onClick(view: OptionTextView) {
                        Toast.makeText(context, "World", Toast.LENGTH_SHORT).show()
                    }
                }
            )
            addOption(
                OptionTextView(context).apply {
                    text="Fuck"
                },
                object : ScrollableButton.OnOptionClickListener{
                    override fun onClick(view: OptionTextView) {
                        Toast.makeText(context, "Fuck", Toast.LENGTH_SHORT).show()
                    }
                }
            )
            val onContentClickListener=object : ScrollableButton.OnContentClickListener{
                override fun onClick(view: View) {
                    Toast.makeText(context, "Content Click", Toast.LENGTH_SHORT).show()
                }
            }
            setOnContentClickListener(onContentClickListener)
            setOnContentLongClickListener(object : ScrollableButton.OnContentLongClickListener{
                override fun onLongClick(view: View) {
                    Toast.makeText(context, "Content Long Click", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}