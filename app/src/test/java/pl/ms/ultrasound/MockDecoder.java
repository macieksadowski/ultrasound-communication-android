package pl.ms.ultrasound;

import android.media.AudioRecord;

import java.util.Arrays;

import pl.ms.ultrasound.ui.DecoderCallback;
import ultrasound.AbstractDecoder;

class MockDecoder extends Decoder {

    private short[] audioData;
    private int i;

    private MockDecoder(MockDecoderBuilder builder) throws Exception {
        super(builder);
        this.audioData = builder.audioData;
        i = N;
        this.recordFrag = new short[N];
    }

    public static class MockDecoderBuilder extends Decoder.DecoderBuilder {

        private short[] audioData;

        public MockDecoderBuilder(int sampleRate, int noOfChannels, int firstFreq, int freqStep, int nfft,
                                  double threshold) {
            super(sampleRate, noOfChannels, firstFreq, freqStep, nfft, threshold);

        }

        public MockDecoderBuilder callback(DecoderCallback callback) {
            this.callback = callback;
            return this;
        }

        public MockDecoderBuilder audioDataForMock(short[] audioData) {
            this.audioData = audioData;
            return this;
        }

        @Override
        public MockDecoder build() {
            MockDecoder decoder;
            try {
                decoder = new MockDecoder(this);
                return decoder;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }


    }

    @Override
    public short[] getAudioSamples() {

        short[] frag = new short[N];
        if (i < audioData.length)
            frag = Arrays.copyOfRange(this.audioData, i - N, i);
        i += N;
        return frag;

    }

    protected void startRecording() {

    }

    public void stopRecording() {

    }

}
