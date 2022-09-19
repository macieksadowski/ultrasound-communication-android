package pl.ms.ultrasound;

import android.media.AudioRecord;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.time.StopWatch;

import java.util.Arrays;
import java.util.List;

import pl.ms.ultrasound.ui.DecoderLog;
import ultrasound.utils.CircularBuffer;

public class RecorderThread implements Runnable {

    private final CircularBuffer<List<Short>> buffer;
    private AudioRecord audioRecord;
    private StopWatch watch;

    private int N;
    private int sampleRate;
    private boolean isRunning;

    public RecorderThread(AudioRecord audioRecord, CircularBuffer<List<Short>> buffer, int N, int fs) {
        this.audioRecord = audioRecord;
        this.buffer = buffer;
        this.N = N;
        this.sampleRate = fs;
        this.watch = new StopWatch();

    }

    @Override
    public void run() {

        isRunning = true;

        final short[] data = new short[N];
        audioRecord.startRecording();

        while (isRunning) {

            watch.start();

            if ((audioRecord.read(data, 0, N)) == -1) {
                break;
            }

            List<Short> list = Arrays.asList(ArrayUtils.toObject(data));
            buffer.offer(list);
            watch.stop();
            double duration = watch.getTime() / 1000.0;
            //DecoderLog.getInstance().setMessage("REC","Recording time: " + duration);
            watch.reset();

            boolean communicate = false;
            while (buffer.isFull() && isRunning) {
                if (!communicate) {
                    DecoderLog.getInstance().setMessage("REC", "Buffer is full!");
                    communicate = true;
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
        audioRecord.stop();
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void stop() {
        isRunning = false;
    }
}
