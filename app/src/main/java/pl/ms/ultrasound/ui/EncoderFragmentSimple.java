package pl.ms.ultrasound.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import pl.ms.ultrasound.Encoder;
import pl.ms.ultrasound.Encoder.EncoderBuilder;
import pl.ms.ultrasound.R;
import pl.ms.ultrasound.databinding.FragmentEncoderSimpleBinding;


public class EncoderFragmentSimple extends Fragment {

    private EncoderBuilder encoderBuilder;
    private Encoder encoder;
    private TextView logsField;


    public EncoderFragmentSimple() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(getActivity());
        int sampleRate = Integer.parseInt(sharedPreferences.getString("sampleRate", String.valueOf(R.integer.sampleRate)));
        int noOfChannels = Integer.parseInt(sharedPreferences.getString("noOfChannels", String.valueOf(R.integer.noOfChannels)));
        int firstFreq = Integer.parseInt(sharedPreferences.getString("firstFreq", String.valueOf(R.integer.firstFreq)));
        int freqStep = Integer.parseInt(sharedPreferences.getString("freqStep", String.valueOf(R.integer.freqStep)));
        double tOnePulse = Double.parseDouble(sharedPreferences.getString("tOnePulse", String.valueOf(0.5)));
        double tBreak = Double.parseDouble(sharedPreferences.getString("tBreak", String.valueOf(0.5)));
        boolean secdedEnabled = sharedPreferences.getBoolean("secded",false);

        encoderBuilder =  new EncoderBuilder(sampleRate, noOfChannels, firstFreq, freqStep);

        encoderBuilder.secdedEnabled(secdedEnabled);
        encoderBuilder.tOnePulse(tOnePulse);
        encoderBuilder.tBreak(tBreak);

        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        FragmentEncoderSimpleBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_encoder_simple, container, false);

        View view = binding.getRoot();
        binding.setLog(EncoderLog.getInstance());

        Button transmit = view.findViewById(R.id.transmitBtn);
        EditText messageField = view.findViewById(R.id.messageField);
        messageField.setText(R.string.hexMsgDefault);

        logsField = view.findViewById(R.id.logsField);
        logsField.setMovementMethod(new ScrollingMovementMethod());

        encoderBuilder.callback(new EncoderCallback() {
            @Override
            public void onTransmissionStarted() {
                transmit.post(() -> {
                    transmit.setEnabled(false);
                });
            }

            @Override
            public void onTransmissionCompleted() {
                transmit.post(() -> {
                    transmit.setEnabled(true);
                    EncoderLog.getInstance().setMessage("Transmission ended.");
                });
            }

        });

        this.encoder = encoderBuilder.build();

        transmit.setOnClickListener(v -> {

            String hexStr = messageField.getText().toString();
            if (!hexStr.isEmpty()) {
                EncoderLog.getInstance().clear();
                encoder.setHexData(hexStr.toLowerCase());
                encoder.run();
            }
        });

        return view;
    }


}