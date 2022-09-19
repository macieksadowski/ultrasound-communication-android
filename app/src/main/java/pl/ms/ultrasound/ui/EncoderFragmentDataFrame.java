package pl.ms.ultrasound.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import pl.ms.ultrasound.Encoder;
import pl.ms.ultrasound.Encoder.EncoderBuilder;
import pl.ms.ultrasound.R;
import pl.ms.ultrasound.databinding.FragmentEncoderDataframeBinding;
import ultrasound.AbstractCoder;
import ultrasound.dataframe.ControlCodes;
import ultrasound.dataframe.DataFrame;
import ultrasound.dataframe.IAsciiControlCodes;
import ultrasound.dataframe.IDataFrame;


public class EncoderFragmentDataFrame extends Fragment implements AdapterView.OnItemSelectedListener {

    private EncoderBuilder encoderBuilder;
    private Encoder encoder;
    private TextView logsField;
    private EditText messageField;
    private Button transmit;
    private DataFrame.DataFrameBuilder dataFrameBuilder;
    private HashMap<String, Byte> commands;



    public EncoderFragmentDataFrame() {
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

        encoderBuilder.mode(AbstractCoder.CoderMode.DATA_FRAME);

        dataFrameBuilder = new DataFrame.DataFrameBuilder(DataFrame.BROADCAST_ADDRESS, noOfChannels);

        commands = getAvailableCommands();

        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        FragmentEncoderDataframeBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_encoder_dataframe, container, false);

        View view = binding.getRoot();
        binding.setLog(EncoderLog.getInstance());

        transmit = view.findViewById(R.id.transmitBtn);
        transmit.setEnabled(false);
        messageField = view.findViewById(R.id.messageFieldAscii);
        //messageField.setEnabled(false);
        //messageField.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.cmd_field_disabled));


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

        Spinner spinner = view.findViewById(R.id.cmdSpinner);
        ArrayList<String> valList = new ArrayList<>(Arrays.asList(commands.keySet().toArray(new String[0])));
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, valList);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);

        spinner.setOnItemSelectedListener(this);

        messageField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(!s.toString().isEmpty()) {
                    transmit.setEnabled(true);
                    dataFrameBuilder.data(s.toString().getBytes(StandardCharsets.US_ASCII));
                }
            }
        });


        transmit.setOnClickListener(v -> {

            IDataFrame frame = dataFrameBuilder.build();
            if(frame != null) {
                EncoderLog.getInstance().clear();
                encoder.setDataFrame(frame);
                encoder.run();
            } else {
                EncoderLog.getInstance().setMessage("Error while creating Data Frame!");
            }

        });

        return view;
    }

     {

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        dataFrameBuilder.data(null);
        messageField.setText(null);
        messageField.setEnabled(false);
        transmit.setEnabled(false);
        messageField.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.cmd_field_disabled));

        String commandStr = parent.getItemAtPosition(position).toString();
        Byte command = commands.get(commandStr);
        if(command != null) {
            dataFrameBuilder.command(command);
            boolean textMessage = command == IAsciiControlCodes.STX;
            messageField.setEnabled(textMessage);
            transmit.setEnabled(!textMessage);
            if(textMessage) {
                messageField.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.cmd_field));
            }
        }


    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private HashMap<String, Byte> getAvailableCommands() {
        HashMap<String, Byte> cmds = new HashMap<>();
        cmds.put("Text message", IAsciiControlCodes.STX);
        cmds.put("Enquiry", IAsciiControlCodes.ENQ);
        cmds.put("Acknowledge", IAsciiControlCodes.ACK);
        cmds.put("Bell", IAsciiControlCodes.BEL);
        cmds.put("Device Control 1", IAsciiControlCodes.DC1);
        cmds.put("Device Control 2", IAsciiControlCodes.DC2);
        cmds.put("Device Control 3", IAsciiControlCodes.DC3);
        cmds.put("Device Control 4", IAsciiControlCodes.DC4);
        cmds.put("Negative Acknowledge", IAsciiControlCodes.NAK);
        cmds.put("Synchronize", IAsciiControlCodes.SYN);
        cmds.put("Cancel", IAsciiControlCodes.CAN);

        return cmds;
    }
}