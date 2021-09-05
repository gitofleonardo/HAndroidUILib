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
import cn.huangchengxi.uilib.buttons.ScrollableButton

class ScrollableButtonsAdapter(private val mItems:MutableList<ScrollItem>):RecyclerView.Adapter<SBHolder>() {
    private var mExpandedPosition=-1
    private var mAttachedRecyclerView:RecyclerView?=null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SBHolder {
        return SBHolder(LayoutInflater.from(parent.context).inflate(R.layout.layout_scrollable_button,parent,false))
    }

    override fun onBindViewHolder(holder: SBHolder, position: Int) {
        val item=mItems[position]
        holder.contentText.text=SpannableString(item.message)
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
            if (mExpandedPosition==position) mExpandedPosition=-1
        }
        holder.scrollableBtn.setOnExpandedStateChangeListener {
            if (mExpandedPosition!=-1 && it){
                closeButton(mExpandedPosition)
            }
            mExpandedPosition=if (it) position else -1
        }
        holder.scrollableBtn.expanded=item.expanded
    }
    private fun closeButton(position: Int){
        val item=mItems[position]
        item.expanded=false
        mAttachedRecyclerView?.let {
            val holder=it.findViewHolderForAdapterPosition(position) as SBHolder?
            holder?.let { sb->
                sb.scrollableBtn.expanded=false
            }
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        mAttachedRecyclerView=recyclerView
        recyclerView.addOnScrollListener(object:RecyclerView.OnScrollListener(){
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState==RecyclerView.SCROLL_STATE_DRAGGING && mExpandedPosition!=-1){
                    closeButton(mExpandedPosition)
                }
            }
        })
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        mAttachedRecyclerView=null
    }

    override fun getItemCount(): Int {
        return mItems.size
    }
}
class SBHolder(view:View):RecyclerView.ViewHolder(view){
    val contentText=view.findViewById<TextView>(R.id.contentText)
    val image=view.findViewById<ImageView>(R.id.image)
    val delete=view.findViewById<TextView>(R.id.delete)
    val scrollableBtn=view.findViewById<ScrollableButton>(R.id.btn)
}