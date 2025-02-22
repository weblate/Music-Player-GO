package com.iven.musicplayergo.player

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import com.iven.musicplayergo.GoConstants
import com.iven.musicplayergo.goPreferences

// The headset connection states (0,1)
private const val HEADSET_DISCONNECTED = 0
private const val HEADSET_CONNECTED = 1

class NotificationReceiver(
    private val playerService: PlayerService,
    private val mediaPlayerHolder: MediaPlayerHolder
) : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        try {
            intent?.action?.let { act ->
                with(mediaPlayerHolder) {
                    when (act) {
                        GoConstants.PREV_ACTION -> instantReset()
                        GoConstants.PLAY_PAUSE_ACTION -> resumeOrPause()
                        GoConstants.NEXT_ACTION -> skip(isNext = true)
                        GoConstants.REPEAT_ACTION -> {
                            repeat(updatePlaybackStatus = true)
                            mediaPlayerInterface.onUpdateRepeatStatus()
                        }
                        GoConstants.CLOSE_ACTION -> if (playerService.isRunning && isMediaPlayer) {
                            stopPlaybackService(
                                stopPlayback = true
                            )
                        }

                        BluetoothDevice.ACTION_ACL_DISCONNECTED -> if (isCurrentSong && goPreferences.isHeadsetPlugEnabled) {
                            pauseMediaPlayer()
                        }
                        BluetoothDevice.ACTION_ACL_CONNECTED -> if (isCurrentSong && goPreferences.isHeadsetPlugEnabled) {
                            resumeMediaPlayer()
                        }

                        AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED ->
                            when (intent.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, -1)) {
                                AudioManager.SCO_AUDIO_STATE_CONNECTED -> if (isCurrentSong && goPreferences.isHeadsetPlugEnabled) {
                                    resumeMediaPlayer()
                                }
                                AudioManager.SCO_AUDIO_STATE_DISCONNECTED -> if (isCurrentSong && goPreferences.isHeadsetPlugEnabled) {
                                    pauseMediaPlayer()
                                }
                            }

                        Intent.ACTION_HEADSET_PLUG -> if (isCurrentSong && goPreferences.isHeadsetPlugEnabled) {
                            when (intent.getIntExtra("state", -1)) {
                                // 0 means disconnected
                                HEADSET_DISCONNECTED -> if (isCurrentSong && goPreferences.isHeadsetPlugEnabled) {
                                    pauseMediaPlayer()
                                }
                                // 1 means connected
                                HEADSET_CONNECTED -> if (isCurrentSong && goPreferences.isHeadsetPlugEnabled) {
                                    resumeMediaPlayer()
                                }
                            }
                        }
                        AudioManager.ACTION_AUDIO_BECOMING_NOISY -> if (isPlaying && goPreferences.isHeadsetPlugEnabled) {
                            pauseMediaPlayer()
                        }
                    }
                }
            }

            if (isOrderedBroadcast) {
                abortBroadcast()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
