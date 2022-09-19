package pl.ms.ultrasound.ui;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import java.util.Arrays;

import pl.ms.ultrasound.Decoder;
import pl.ms.ultrasound.R;
import pl.ms.ultrasound.databinding.FragmentDecoderBinding;
import ultrasound.AbstractCoder;
import ultrasound.dataframe.ControlCodes;


public class DecoderFragment extends Fragment {

    private Decoder decoder;
    private Thread decoderThread;
    private SharedPreferences sharedPreferences;

    private  Button startStop;
    ImageView figureFft;
    Bitmap fftBitmap;
    Canvas fftCanvas;
    Paint fftPaint;
    TextView cmdText;
    TextView dataText;
    byte receivedAddress;
    byte receivedCommand;
    byte[] receivedDataStr;

    Boolean updateFigures = false;

    public DecoderFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireActivity());
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        FragmentDecoderBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_decoder, container, false);
        View view =  binding.getRoot();
        binding.setLog(DecoderLog.getInstance());

        //figureFft = view.findViewById(R.id.figureFFT);
        //bitmap is 10px taller than max signal height to show zero line
        //show usable FFT output



        /* GUI Elements */
        TextView logsField = view.findViewById(R.id.logsFieldDec);
        logsField.setMovementMethod(new ScrollingMovementMethod());
        cmdText = view.findViewById(R.id.cmdText);
        dataText = view.findViewById(R.id.dataText);

        startStop = view.findViewById(R.id.transmitBtn);
        startStop.setOnClickListener(v -> {
            if (decoderThread == null || !decoder.isRunning()) {
                onStartClick();
                startStop.setText(R.string.stop_listening);

            } else {
                decoder.stopRecording();
                decoderThread = null;
                startStop.setText(R.string.start_listening);
                cmdText.setText("CMD");
                dataText.setText(null);
                cmdText.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.cmd_field));
                dataText.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.cmd_field));

            }
        });

        return view;
    }

    public void printFFT() {

        double[] ampl = decoder.getAmpl();

        @SuppressWarnings("OptionalGetWithoutIsPresent") double max = Arrays.stream(ampl).max().getAsDouble();
        //normalization for proper vertical presentation of FFT spectrum
        for (int i = 0; i < ampl.length; i++) ampl[i] /= max;

        //drawing, (0,0) - upper left corner
        for (int i = 0; i < ampl.length; i++) {
            //for (int i = 371; i < 397; i++) {
            int upy = (int) (400 - (ampl[i] * 400.0));
            int downy = 400;
            fftCanvas.drawLine(i, upy, i +1, downy, fftPaint);
        }
    }

    public void onStartClick() {

        int sampleRate = Integer.parseInt(sharedPreferences.getString("sampleRate", String.valueOf(R.integer.sampleRate)));
        int noOfChannels = Integer.parseInt(sharedPreferences.getString("noOfChannels", String.valueOf(R.integer.noOfChannels)));
        int firstFreq = Integer.parseInt(sharedPreferences.getString("firstFreq", String.valueOf(R.integer.firstFreq)));
        int freqStep = Integer.parseInt(sharedPreferences.getString("freqStep", String.valueOf(R.integer.freqStep)));
        int nfft = Integer.parseInt(sharedPreferences.getString("nfft", String.valueOf(R.integer.nfft)));
        double threshold = Double.parseDouble(sharedPreferences.getString("threshold", String.valueOf(R.integer.threshold)));
        double tOnePulse = Double.parseDouble(sharedPreferences.getString("tOnePulse", String.valueOf(0.5)));

        double tBreak = Double.parseDouble(sharedPreferences.getString("tBreak", String.valueOf(0.5)));
        boolean secdedEnabled = sharedPreferences.getBoolean("secded",false);

        AbstractCoder.CoderMode coderMode = AbstractCoder.CoderMode.valueOf(sharedPreferences.getString("coderMode","DATA_FRAME"));

        int channelConfiguration = AudioFormat.CHANNEL_IN_MONO;
        int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
        int bufferSize = AudioRecord.getMinBufferSize(sampleRate,
                channelConfiguration, audioEncoding);

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{
                    Manifest.permission.RECORD_AUDIO
            },223345);

        }

        AudioRecord audioRecord = new AudioRecord(
                MediaRecorder.AudioSource.MIC, sampleRate,
                channelConfiguration, audioEncoding, bufferSize);

        try {
            Decoder.DecoderBuilder builder = new Decoder.DecoderBuilder(sampleRate, noOfChannels, firstFreq, freqStep, (int) Math.pow(2,nfft), threshold);

            builder.secdedEnabled(secdedEnabled);
            builder.tOnePulse(tOnePulse);

            builder.mode(coderMode);

            builder.audioRecord(audioRecord);
            builder.callback(new DecoderCallback() {
                @Override
                public void onStarted() {
                    DecoderLog.getInstance().setMessage("Decoder started!");

                }

                @Override
                public void onStopped() {
                    decoder.stopRecording();
                    DecoderLog.getInstance().setMessage("Decoder stopped!");

                }

                @Override
                public void updateFigures() {
                    updateFigures = true;
                    printFFT();
                }

                @Override
                public void onDataFrameCorrReceived(byte address, byte command, byte[] dataStr) {

                    receivedAddress = address;
                    receivedCommand = command;
                    receivedDataStr = dataStr;
                    updateReceivedDataInfo();
                }
            });

            decoder = builder.build();
            if (decoder != null) {

                decoderThread = new Thread(decoder);
                decoderThread.setName("Decoder Thread");
                decoderThread.start();
                DecoderLog.getInstance().clear();

                /*
                fftBitmap = Bitmap.createBitmap(decoder.getNfft() /2, 410, Bitmap.Config.ARGB_8888);
                fftCanvas = new Canvas(fftBitmap);
                fftPaint = new Paint();
                fftPaint.setColor(Color.GREEN);
                figureFft.setImageBitmap(fftBitmap);
                */

            } else
                throw new Exception("Failed to build Decoder object!");

        } catch (Exception e) {
            DecoderLog.getInstance().setMessage(e.toString());
            e.printStackTrace();
        }

    }

    private void updateReceivedDataInfo() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                cmdText.setText(ControlCodes.getCodeNameByValue(receivedCommand));
                dataText.setText(new String(receivedDataStr));
                cmdText.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.cmd_field_green));
                dataText.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.cmd_field_green));
            }
        });

    }


}

