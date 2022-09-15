package pl.ms.ultrasound;

import android.media.AudioRecord;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.time.StopWatch;

import java.util.List;

import pl.ms.ultrasound.ui.DecoderCallback;
import pl.ms.ultrasound.ui.DecoderLog;
import ultrasound.AbstractDecoder;
import ultrasound.CircularBuffer;


public class Decoder extends AbstractDecoder implements Runnable {

    private static final String TAG = "DEC";

    private final CircularBuffer<List<Short>> buffer;
    private DecoderCallback callback;

    private RecorderThread recorder;
    private Thread recorderThread;


    Decoder(DecoderBuilder builder) throws Exception {
        super(builder);

        this.buffer = new CircularBuffer<>(64);

        this.recorder = new RecorderThread(builder.audioRecord,buffer,N,sampleRate);
        this.recorderThread = new Thread(recorder);
        this.recorderThread.setName("Recorder Thread");

        this.callback = builder.callback;

    }

    public static class DecoderBuilder extends AbstractDecoderBuilder {

        protected DecoderCallback callback;
        private AudioRecord audioRecord;

        public DecoderBuilder(int sampleRate, int noOfChannels, int firstFreq, int freqStep, int nfft,
                              double threshold) {
            super(sampleRate, noOfChannels, firstFreq, freqStep, nfft, threshold);


        }

        public DecoderBuilder callback(DecoderCallback callback) {
            this.callback = callback;
            return this;
        }

        public DecoderBuilder audioRecord(AudioRecord audioRecord) {
            this.audioRecord = audioRecord;
            return this;
        }

        @Override
        public Decoder build() {
            Decoder decoder;
            try {
                validate();
                decoder = new Decoder(this);
                return decoder;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        protected final void validate() {
            super.validate();
            if (this.audioRecord == null) {
                throw new NullPointerException("Audio Recorder can't be null");
            }
        }

    }

    protected void startRecording() {
        recorderThread.start();
    }

    @Override
    protected void closeRecorder() {
        recorder.stop();
        try {
            recorderThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        recorderThread = null;
    }

    @Override
    public short[] getAudioSamples() {

        short[] frag = new short[N];
        if(!buffer.isEmpty()) {
            List<Short> list = buffer.poll();
            Short[] arr = (Short[]) list.toArray();
            frag = ArrayUtils.toPrimitive(arr);
        }
        return frag;

    }

    @Override
    protected void logMessage(String s) {

        Log.i(TAG,s);
        DecoderLog.getInstance().setMessage(s);
    }

    @Override
    protected void onDataFrameSuccessfullyReceived(byte address, byte command, byte[] dataStr) {
        super.onDataFrameSuccessfullyReceived(address, command, dataStr);

        callback.onDataFrameCorrReceived(address, command, dataStr);
    }



}