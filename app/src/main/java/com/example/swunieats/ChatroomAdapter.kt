package com.example.swunieats

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ChatroomAdapter(
    private val chatrooms: List<Chatroom>,
    private val onEnterClicked: (Chatroom) -> Unit
) : RecyclerView.Adapter<ChatroomAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName = view.findViewById<TextView>(R.id.tvChatroomName)
        val tvTime = view.findViewById<TextView>(R.id.tvTime)
        val tvLocation = view.findViewById<TextView>(R.id.tvLocation)
        val btnEnter = view.findViewById<Button>(R.id.btnEnter)

        fun bind(chatroom: Chatroom) {
            tvName.text = chatroom.name
            tvTime.text = "시간: ${chatroom.time}"
            tvLocation.text = "장소: ${chatroom.location}"
            btnEnter.setOnClickListener {
                onEnterClicked(chatroom)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = chatrooms.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(chatrooms[position])
    }
}
