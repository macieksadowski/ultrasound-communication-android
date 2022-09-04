package pl.ms.ultrasound.ui;

import android.os.Bundle;
import android.text.InputType;
import android.text.method.DigitsKeyListener;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceFragmentCompat;

import pl.ms.ultrasound.R;

public class ParametersActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }



    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            EditTextPreference sampleRatePreference = findPreference("sampleRate");
            if(sampleRatePreference != null) {
                sampleRatePreference.setOnBindEditTextListener(editText -> editText.setInputType(InputType.TYPE_CLASS_NUMBER));
            }

            EditTextPreference firstFreqPreference = findPreference("firstFreq");
            if(firstFreqPreference != null) {
                firstFreqPreference.setOnBindEditTextListener(editText -> editText.setInputType(InputType.TYPE_CLASS_NUMBER));
            }

            EditTextPreference freqStepPreference = findPreference("freqStep");
            if(freqStepPreference != null) {
                freqStepPreference.setOnBindEditTextListener(editText -> editText.setInputType(InputType.TYPE_CLASS_NUMBER));
            }

            EditTextPreference tOnePulsePreference = findPreference("tOnePulse");
            if(tOnePulsePreference != null) {
                tOnePulsePreference.setOnBindEditTextListener(editText -> editText.setInputType(InputType.TYPE_CLASS_NUMBER));
                tOnePulsePreference.setOnBindEditTextListener(editText -> editText.setKeyListener(DigitsKeyListener.getInstance("0123456789.")));
            }

            EditTextPreference tBreakPreference = findPreference("tBreak");
            if(tBreakPreference != null) {
                tBreakPreference.setOnBindEditTextListener(editText -> editText.setInputType(InputType.TYPE_CLASS_NUMBER));
                tBreakPreference.setOnBindEditTextListener(editText -> editText.setKeyListener(DigitsKeyListener.getInstance("0123456789.")));
            }

            EditTextPreference thresholdPreference = findPreference("threshold");
            if(thresholdPreference != null) {
                thresholdPreference.setOnBindEditTextListener(editText -> editText.setInputType(InputType.TYPE_CLASS_NUMBER));
                thresholdPreference.setOnBindEditTextListener(editText -> editText.setKeyListener(DigitsKeyListener.getInstance("0123456789.")));
            }

            EditTextPreference nfftPreference = findPreference("nfft");
            if(nfftPreference != null) {
                nfftPreference.setOnBindEditTextListener(editText -> editText.setInputType(InputType.TYPE_CLASS_NUMBER));
            }

            EditTextPreference deltaFPreference = findPreference("deltaF");
            if(deltaFPreference != null) {
                deltaFPreference.setOnBindEditTextListener(editText -> editText.setInputType(InputType.TYPE_CLASS_NUMBER));
            }
        }
    }
}