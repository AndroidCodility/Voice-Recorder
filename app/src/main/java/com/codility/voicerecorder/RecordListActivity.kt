package com.codility.voicerecorder

import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.annotation.RequiresApi
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.SeekBar
import com.codility.recyclerview.MyAdapter
import com.codility.voicerecorder.model.Recording
import kotlinx.android.synthetic.main.item_recording.view.*
import kotlinx.android.synthetic.main.record_list.*
import java.io.File
import java.io.IOException

/**
 * Created by Govind on 3/29/2018.
 */
@RequiresApi(Build.VERSION_CODES.KITKAT)
class RecordListActivity : AppCompatActivity(), MyAdapter.OnClickListener {
    private var mediaPlayer: MediaPlayer? = null
    private var lastProgress = 0
    private val mHandler = Handler()
    private var isPlaying = false
    private var last_index = -1
    private var myAdapter: MyAdapter? = null

    override fun onClickPlay(view: View, record: Recording, recordingList: ArrayList<Recording>, position: Int) {
        playRecordItem(view, record, recordingList, position)
    }

    private fun playRecordItem(view: View, recordItem: Recording, recordingList: ArrayList<Recording>, position: Int) {
        val recording = recordingList[position]

        if (isPlaying) {
            stopPlaying()
            if (position == last_index) {
                recording.isPlaying = false
                stopPlaying()
                myAdapter!!.notifyItemChanged(position)
            } else {
                markAllPaused(recordingList)
                recording.isPlaying = true
                myAdapter!!.notifyItemChanged(position)
                startPlaying(recording.uri, recordItem, view.seekBar, position)
                last_index = position
            }
            seekUpdate(view)
        } else {
            if (recording.isPlaying) {
                recording.isPlaying = false
                stopPlaying()
            } else {
                startPlaying(recording.uri, recordItem, view.seekBar, position)
                recording.isPlaying = true
                view.seekBar.max = mediaPlayer!!.duration
            }
            myAdapter!!.notifyItemChanged(position)
            last_index = position
        }

        manageSeekBar(view.seekBar)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.record_list)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        getAllRecordings()
    }

    private fun getAllRecordings() {
        val recordArrayList = ArrayList<Recording>()
        val root = android.os.Environment.getExternalStorageDirectory()
        val path = root.absolutePath + "/AndroidCodility/Audios"
        val directory = File(path)
        val files = directory.listFiles()
        if (files != null) {

            for (i in files.indices) {
                val fileName = files[i].name
                val recordingUri = root.absolutePath + "/AndroidCodility/Audios/" + fileName
                recordArrayList.add(Recording(recordingUri, fileName, false))
            }
            tvNoData.visibility = View.GONE
            recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
            myAdapter = MyAdapter(recordArrayList)
            myAdapter!!.setListener(this)
            recyclerView.adapter = myAdapter
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item!!.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun seekUpdate(itemView: View) {
        if (mediaPlayer != null) {
            val mCurrentPosition = mediaPlayer!!.currentPosition
            itemView.seekBar.max = mediaPlayer!!.duration
            itemView.seekBar.progress = mCurrentPosition
            lastProgress = mCurrentPosition
        }
        mHandler.postDelayed(Runnable { seekUpdate(itemView) }, 100)
    }

    private fun manageSeekBar(seekBar: SeekBar?) {
        seekBar!!.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (mediaPlayer != null && fromUser) {
                    mediaPlayer!!.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
    }

    private fun stopPlaying() {
        try {
            mediaPlayer!!.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        mediaPlayer = null
        isPlaying = false
    }

    private fun startPlaying(uri: String?, audio: Recording?, seekBar: SeekBar?, position: Int) {
        mediaPlayer = MediaPlayer()
        try {
            mediaPlayer!!.setDataSource(uri)
            mediaPlayer!!.prepare()
            mediaPlayer!!.start()
        } catch (e: IOException) {
            Log.e("LOG_TAG", "prepare() failed")
        }
        //showing the pause button
        seekBar!!.max = mediaPlayer!!.duration
        isPlaying = true

        mediaPlayer!!.setOnCompletionListener(MediaPlayer.OnCompletionListener {
            audio!!.isPlaying = false
            myAdapter!!.notifyItemChanged(position)
        })
    }

    private fun markAllPaused(recordingList: ArrayList<Recording>) {
        for (i in recordingList.indices) {
            recordingList[i].isPlaying = false
            recordingList[i] = recordingList[i]
        }
        myAdapter!!.notifyDataSetChanged()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        stopPlaying()
    }
}