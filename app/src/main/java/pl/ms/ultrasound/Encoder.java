package pl.ms.ultrasound;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.os.HandlerThread;
import android.util.Log;

import pl.ms.ultrasound.ui.EncoderCallback;
import pl.ms.ultrasound.ui.EncoderLog;
import ultrasound.AbstractEncoder;

public class Encoder extends AbstractEncoder implements Runnable {

	private static final String TAG = "ENC";

    private AudioTrack track;
    private EncoderCallback callback;

	private Encoder(EncoderBuilder builder) {

		super(builder);
		this.callback = builder.callback;
	}

    public static class EncoderBuilder extends AbstractEncoderBuilder {

	    private EncoderCallback callback;

        public EncoderBuilder(int sampleRate, int noOfChannels, int firstFreq, int freqStep) {
            super(sampleRate, noOfChannels, firstFreq, freqStep);

        }

        public EncoderBuilder callback(EncoderCallback callback) {
            this.callback = callback;
            return this;
        }

        @Override
        public Encoder build() {
            validate();
            return new Encoder(this);
        }

        protected final void validate() {
            super.validate();
        }

    }

    @Override
    public void run() {

        callback.onTransmissionStarted();

        super.run();

        callback.onTransmissionCompleted();
    }

    @Override
    protected void playSound(short[] soundData) {

        track.play();
        track.write(soundData,0,soundData.length);
        track.play();
    }

    @Override
    protected void constructAudioStream() {
        // construct audio stream in 16bit format with given sample rate
        int minBuffSize = AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        track = new AudioTrack.Builder()
                .setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_UNKNOWN)
                        .build())
                .setAudioFormat(new AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build())
                .setBufferSizeInBytes(minBuffSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build();

        track.setVolume(AudioTrack.getMaxVolume());
    }

    @Override
    protected void closeAudioStream() {
        track.stop();
        track.release();
        track = null;
    }

    @Override
    protected void logMessage(String s) {
        Log.i(TAG,s);
        EncoderLog.getInstance().setMessage(s);
    }

}
