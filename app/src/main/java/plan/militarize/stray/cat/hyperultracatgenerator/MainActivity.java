package plan.militarize.stray.cat.hyperultracatgenerator;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatSeekBar;
import android.support.v7.widget.AppCompatTextView;
import android.widget.SeekBar;

import plan.militarize.stray.cat.hyperultracatgenerator.com.android.egg.neko.Cat;

public class MainActivity extends Activity {


    private AppCompatTextView mTextFrom;
    private AppCompatTextView mTextTo;
    private AppCompatSeekBar mSeekFrom;
    private AppCompatSeekBar mSeekTo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextFrom = findViewById(R.id.text_from);
        mTextTo = findViewById(R.id.text_to);

        mSeekFrom = findViewById(R.id.seek_from);
        mSeekTo = findViewById(R.id.seek_to);
        SeekBar.OnSeekBarChangeListener change = new OnSeekChanged();
        mSeekFrom.setOnSeekBarChangeListener(change);
        mSeekTo.setOnSeekBarChangeListener(change);
    }


    private class OnSeekChanged implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            Cat cat = new Cat(getApplicationContext(), progress);
            seekBar.setThumb(cat.getCurrent());
            if (seekBar.getId() == R.id.seek_from) {
                mTextFrom.setText("from : " + progress);
            } else {
                mTextTo.setText("to : " + progress);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            //NOP
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            //NOP
        }
    }

}
