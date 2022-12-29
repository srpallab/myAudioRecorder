package com.srpallab.myaudiorecorder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class AudioAdapter(private var records: ArrayList<AudioRecord>, var listener: OnItemClickListener) :
    RecyclerView.Adapter<AudioAdapter.ViewHolder>() {
    private var editMode = false
    fun isEditMode(): Boolean { return  editMode }
    fun setEditMode(mode: Boolean){
        if (editMode !=  mode){
            editMode = mode
            notifyDataSetChanged()
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener,
        View.OnLongClickListener {
        var tvFileName: TextView = itemView.findViewById(R.id.tvFileName)
        var tvMeta: TextView = itemView.findViewById(R.id.tvMeta)
        var checkBox: CheckBox = itemView.findViewById(R.id.btnSelectCheckBox)

        init {
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
        }

        override fun onClick(v: View?) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION){
                listener.onItemClickListener(position)
            }
        }

        override fun onLongClick(v: View?): Boolean {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION){
                listener.onItemLongClickListener(position)
            }
            return true
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.itemview_layout,
            parent,
            false
        )
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return records.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position != RecyclerView.NO_POSITION) {
            val record = records[position]

            var sdf = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
            var date = Date(record.timestamp)
            var strDate = sdf.format(date)

            holder.tvFileName.text = record.filename
            holder.tvMeta.text = "${record.duration} $date"

            if (editMode) {
                holder.checkBox.visibility = View.VISIBLE
                holder.checkBox.isChecked = record.isChecked
            } else{
                holder.checkBox.visibility = View.GONE
                holder.checkBox.isChecked = false
            }
        }
    }
}