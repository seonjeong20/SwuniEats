package com.example.swunieats

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ChatroomAdapter(
    private val chatrooms: List<Chatroom>,
    private val onEnter: (Chatroom) -> Unit
) : RecyclerView.Adapter<ChatroomAdapter.VH>() {

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tvChatroomName)
        private val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        private val tvLoc: TextView = itemView.findViewById(R.id.tvLocation)
        private val btnEnter: Button = itemView.findViewById(R.id.btnEnter)

        fun bind(cr: Chatroom) {
            tvName.text = cr.name
            tvTime.text = cr.time
            tvLoc.text = cr.location
            btnEnter.setOnClickListener { onEnter(cr) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_item, parent, false)
        return VH(view)
    }

    override fun getItemCount(): Int = chatrooms.size
    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(chatrooms[position])
}