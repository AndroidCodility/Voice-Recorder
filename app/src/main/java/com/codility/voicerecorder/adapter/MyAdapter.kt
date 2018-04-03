package com.codility.recyclerview

import android.media.MediaPlayer
import android.os.Build
import android.os.Handler
import android.support.annotation.RequiresApi
import android.support.v7.widget.RecyclerView
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import com.codility.voicerecorder.R
import com.codility.voicerecorder.model.Recording
import kotlinx.android.synthetic.main.item_recording.view.*

/**
 * Created by Govind on 3/28/2018.
 */

@RequiresApi(Build.VERSION_CODES.KITKAT)
class MyAdapter(private val recordList: ArrayList<Recording>) : RecyclerView.Adapter<MyAdapter.ViewHolder>() {

    private var onClickListener: OnClickListener? = null

    fun setListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_recording, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems(recordList, onClickListener)
    }

    override fun getItemCount(): Int {
        return recordList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var mediaPlayer: MediaPlayer? = null
        private var lastProgress = 0
        private val mHandler = Handler()

        fun bindItems(recordingList: ArrayList<Recording>, onClickListener: OnClickListener?) {
            val recording: Recording = recordingList[adapterPosition]
            val tvRecordName = itemView.findViewById<TextView>(R.id.tvRecordName)
            val imgViewPlay = itemView.findViewById<ImageView>(R.id.imgViewPlay)
            val seekBar = itemView.findViewById<SeekBar>(R.id.seekBar)
            tvRecordName.text = recording.fileName

            if (recording.isPlaying) {
                imgViewPlay.setImageResource(R.drawable.ic_pause_circle)
                TransitionManager.beginDelayedTransition(itemView as ViewGroup)
                seekBar.visibility = View.VISIBLE
                seekUpdate(itemView)
            } else {
                imgViewPlay.setImageResource(R.drawable.ic_play_circle)
                TransitionManager.beginDelayedTransition(itemView as ViewGroup)
                seekBar.visibility = View.GONE
            }

            manageSeekBar(seekBar)

            imgViewPlay.setOnClickListener(View.OnClickListener {
                if (onClickListener != null) {
                    onClickListener.onClickPlay(itemView, recording, recordingList, adapterPosition)
                }
            })
        }

        private fun manageSeekBar(seekBar: SeekBar?) {
            seekBar!!.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    if (mediaPlayer != null && fromUser) {
                        mediaPlayer!!.seekTo(progress)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {

                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {

                }
            })
        }

        private var runnable: Runnable = Runnable { seekUpdate(itemView) }

        private fun seekUpdate(itemView: View) {
            if (mediaPlayer != null) {
                val mCurrentPosition = mediaPlayer!!.currentPosition
                itemView.seekBar.max = mediaPlayer!!.duration
                itemView.seekBar.progress = mCurrentPosition
                lastProgress = mCurrentPosition
            }
            mHandler.postDelayed(runnable, 100)
        }
    }

    interface OnClickListener {
        fun onClickPlay(view: View, record: Recording, recordingList: ArrayList<Recording>, position: Int)
    }
}