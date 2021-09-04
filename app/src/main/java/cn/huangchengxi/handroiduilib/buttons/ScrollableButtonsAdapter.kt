package cn.huangchengxi.handroiduilib.buttons

import android.text.SpannableString
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import cn.huangchengxi.handroiduilib.R

class ScrollableButtonsAdapter(private val mItems:MutableList<String>):RecyclerView.Adapter<SBHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SBHolder {
        return SBHolder(LayoutInflater.from(parent.context).inflate(R.layout.layout_scrollable_button,parent,false))
    }

    override fun onBindViewHolder(holder: SBHolder, position: Int) {
        val item=mItems[position]
        holder.contentText.text=SpannableString(item)
        holder.contentText.setOnClickListener {
            Toast.makeText(holder.contentText.context, "Click:${item}", Toast.LENGTH_SHORT).show()
        }
        holder.image.setOnClickListener {
            Toast.makeText(holder.image.context, "Click image:${item}", Toast.LENGTH_SHORT).show()
        }
        holder.delete.setOnClickListener {
            Toast.makeText(holder.delete.context, "Delete:${item}", Toast.LENGTH_SHORT).show()
            mItems.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position,itemCount-position+1)
        }
    }

    override fun getItemCount(): Int {
        return mItems.size
    }
}
class SBHolder(view:View):RecyclerView.ViewHolder(view){
    val contentText=view.findViewById<TextView>(R.id.contentText)
    val image=view.findViewById<ImageView>(R.id.image)
    val delete=view.findViewById<TextView>(R.id.delete)
}