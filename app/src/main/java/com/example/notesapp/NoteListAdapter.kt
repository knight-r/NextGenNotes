package com.example.notesapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView

class NoteListAdapter(): RecyclerView.Adapter<ViewHolder>() {
    private val mList = ArrayList<Note>()
    private var listener: NoteListAdapter.onItemClickListener? = null
    fun setList(notes: List<Note>) {
        mList.clear()
        mList.addAll(notes)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.note_item,
            parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.setOnClickListener {
            listener?.onItemCLicked(position)
        }
        holder.bind(mList[position])
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    interface onItemClickListener {
        fun onItemCLicked(position: Int)
    }
    fun setOnItemClickListener(listener: onItemClickListener) {
        this.listener = listener
    }
}
class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
    private val tvNoteTitle: TextView = itemView.findViewById(R.id.tv_note_title)
    private val tvNoteDate: TextView = itemView.findViewById(R.id.tv_note_date)
    private val tvNoteContent: TextView = itemView.findViewById(R.id.tv_note_content)
    private val tvNoteTime: TextView = itemView.findViewById(R.id.tv_note_time)
    private val clNoteListItem: ConstraintLayout = itemView.findViewById(R.id.cl_note_list_item)
    fun bind(note: Note) {
        tvNoteDate.text = note.date
        tvNoteTitle.text = note.title
        tvNoteContent.text = note.content
        tvNoteTime.text = note.time
//        clNoteListItem.setOnClickListener {
//            clickListener(note)
//        }
    }
}