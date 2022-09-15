package pl.ms.ultrasound;

import android.media.AudioRecord;

import org.apache.commons.lang3.time.StopWatch;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import pl.ms.ultrasound.ui.DecoderCallback;
import pl.ms.ultrasound.ui.DecoderLog;
import ultrasound.AbstractDecoder;

//@Ignore("This test will be ignored")
@RunWith(MockitoJUnitRunner.class)
public class DecoderTest {


    @Mock
    MockDecoder decoder;

    Thread decoderThread;
    double audioSigMockLen = 0.0;

    @Before
    public void init() throws Exception {

        InputStream in = this.getClass().getClassLoader().getResourceAsStream("6C.txt");
        Scanner s = new Scanner(in);

        List<Short> audioSigList = new ArrayList<>();
        while (s.hasNext()) {
            audioSigList.add((short) s.nextInt());
        }
        short[] audioSig = new short[audioSigList.size()];
        for (int i = 0; i < audioSig.length; i++) {
            audioSig[i] = audioSigList.get(i);
        }

        int sampleRate = 48000;
        int noOfChannels = 16;
        int firstFreq = 20000;
        int freqStep = 40;
        int nfft = 12;
        double threshold = 0.3;

        MockDecoder.MockDecoderBuilder builder = new MockDecoder.MockDecoderBuilder(sampleRate, noOfChannels, firstFreq, freqStep, (int) Math.pow(2, nfft), threshold);
        builder.callback(new DecoderCallback() {
            @Override
            public void onStarted() {
                DecoderLog.getInstance().setMessage("Decoder started!");

            }

            @Override
            public void onStopped() {
                DecoderLog.getInstance().setMessage("Decoder stopped!");

            }

            @Override
            public void updateFigures() {
            }

            @Override
            public void onDataFrameCorrReceived(byte address, byte command, byte[] dataStr) {

            }
        });

        builder.audioDataForMock(audioSig);

        decoder = builder.build();

        audioSigMockLen = (double) audioSig.length / (double) sampleRate;

    }

    @Test
    public void decode() {

        decoderThread = new Thread(decoder);
        decoderThread.start();
        StopWatch watch = new StopWatch();
        watch.start();
        while(true) {
            if(watch.getTime()/1000.0 > audioSigMockLen) {
                watch.stop();
                decoder.stopRecording();

                decoderThread = null;
                break;
            }
        }
        Assert.assertEquals("6c",decoder.getResHex());

    }

    @After
    public void finish() {

    }

}