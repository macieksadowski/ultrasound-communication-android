package pl.ms.ultrasound.ui;

import android.content.SharedPreferences;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.PreferenceManager;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import ultrasound.AbstractCoder;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentStateAdapter {

    private AbstractCoder.CoderMode encoderType;

    public SectionsPagerAdapter(FragmentActivity fa) {
        super(fa);
    }

    @Override
    public Fragment createFragment(int position) {
        switch(position) {
            case 0:
                if(encoderType == AbstractCoder.CoderMode.DATA_FRAME) {
                    return new EncoderFragmentDataFrame();
                } else {
                    return new EncoderFragmentSimple();
                }
            case 1:
                return new DecoderFragment();
            default:
                return null;
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }

    public void setEncoderType(AbstractCoder.CoderMode mode) {
        this.encoderType = mode;
    }
}