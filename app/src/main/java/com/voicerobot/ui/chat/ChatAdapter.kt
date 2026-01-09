package com.voicerobot.ui.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.voicerobot.R

class ChatAdapter : ListAdapter<ChatMessage, ChatAdapter.VH>(DIFF) {

    companion object {
        private const val VIEW_TYPE_USER = 1
        private const val VIEW_TYPE_BOT = 2

        private val DIFF = object : DiffUtil.ItemCallback<ChatMessage>() {
            override fun areItemsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
                // In a real app, you'd use a unique ID. For now, object reference is fine.
                return oldItem === newItem
            }

            override fun areContentsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
                return oldItem == newItem
            }
        }
    }

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvMessage: TextView = itemView.findViewById(R.id.tvMessage)
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position).speaker) {
            Speaker.USER -> VIEW_TYPE_USER
            Speaker.BOT -> VIEW_TYPE_BOT
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val layoutRes = when (viewType) {
            VIEW_TYPE_USER -> R.layout.item_chat_user
            else -> R.layout.item_chat_bot
        }
        val view = LayoutInflater.from(parent.context).inflate(layoutRes, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        holder.tvMessage.text = item.text
    }
}
