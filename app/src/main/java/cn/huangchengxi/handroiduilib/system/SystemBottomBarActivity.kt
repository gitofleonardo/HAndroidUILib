package cn.huangchengxi.handroiduilib.system

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import cn.huangchengxi.handroiduilib.R
import cn.huangchengxi.uilib.system.BottomPullBar

class SystemBottomBarActivity : AppCompatActivity() {
    private val TAG="SystemBottomBarActivity";
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_system_bottom_bar)
        findViewById<BottomPullBar>(R.id.pull_bar).setOnBottomBarListener(object : BottomPullBar.AbstractBottomBarListener(){
            override fun onBarPulling() {
                super.onBarPulling()
                Log.v(TAG,"Bar pulling")
            }

            override fun onBarPullTop() {
                super.onBarPullTop()
                Log.v(TAG,"Bar pull top")
            }

            override fun onBarLongPress() {
                super.onBarLongPress()
                Log.v(TAG,"Bar long press")
            }

            override fun onBarClick() {
                super.onBarClick()
                Log.v(TAG,"Bar click")
            }

            override fun onBarPullTopLong() {
                super.onBarPullTopLong()
                Log.v(TAG,"Bar pull top long")
            }

            override fun onBarExpanded() {
                super.onBarExpanded()
                Log.v(TAG,"Bar expanded")
            }

            override fun onBarHidden() {
                super.onBarHidden()
                Log.v(TAG,"Bar hidden")
            }

            override fun onBarExpanding() {
                super.onBarExpanding()
                Log.v(TAG,"Bar expanding")
            }

            override fun onBarHiding() {
                super.onBarHiding()
                Log.v(TAG,"Bar hiding")
            }

            override fun onBarReleased() {
                super.onBarReleased()
                Log.v(TAG,"Bar release")
            }
        })
    }
}