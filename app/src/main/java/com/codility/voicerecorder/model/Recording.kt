package com.codility.voicerecorder.model

import java.io.Serializable

/**
 * Created by Govind on 3/28/2018.
 */
class Recording(var uri: String, var fileName: String, var isPlaying: Boolean) :Serializable {}