package cn.huangchengxi.handroiduilib.buttons

import android.text.SpannableString
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import cn.huangchengxi.handroiduilib.R

class ScrollableButtonsAdapter(private val mItems:List<String>):RecyclerView.Adapter<SBHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SBHolder {
        return SBHolder(LayoutInflater.from(parent.context).inflate(R.layout.layout_scrollable_button,parent,false))
    }

    override fun onBindViewHolder(holder: SBHolder, position: Int) {
        val item=mItems[position]
        holder.contentText.text=SpannableString(item)
    }

    override fun getItemCount(): Int {
        return mItems.size
    }

}
class SBHolder(view:View):RecyclerView.ViewHolder(view){
    val contentText=view.findViewById<TextView>(R.id.contentText)
}