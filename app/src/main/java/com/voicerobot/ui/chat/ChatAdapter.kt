package com.voicerobot.ui.chat

import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.voicerobot.R

class ChatAdapter : ListAdapter<ChatMessage, ChatAdapter.VH>(DIFF) {

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val root: ConstraintLayout = itemView as ConstraintLayout
        val tvMessage: TextView = itemView.findViewById(R.id.tvMessage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_message, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        holder.tvMessage.text = item.text

        val set = ConstraintSet()
        set.clone(holder.root)

        // clear previous constraints
        set.clear(R.id.tvMessage, ConstraintSet.START)
        set.clear(R.id.tvMessage, ConstraintSet.END)

        when (item.speaker) {
            Speaker.BOT -> {
                set.connect(R.id.tvMessage, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
                holder.tvMessage.setBackgroundColor(Color.parseColor("#4D000000"))
                holder.tvMessage.gravity = Gravity.START
            }
            Speaker.USER -> {
                set.connect(R.id.tvMessage, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
                holder.tvMessage.setBackgroundColor(Color.parseColor("#4D3D8BFF"))
                holder.tvMessage.gravity = Gravity.START
            }
        }

        set.applyTo(holder.root)
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<ChatMessage>() {
            override fun areItemsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean =
                oldItem === newItem

            override fun areContentsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean =
                oldItem == newItem
        }
    }
}
