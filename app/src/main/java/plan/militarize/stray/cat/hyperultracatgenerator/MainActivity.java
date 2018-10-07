package plan.militarize.stray.cat.hyperultracatgenerator;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatSeekBar;
import android.support.v7.widget.AppCompatTextView;
import android.text.TextUtils;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.Toast;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import plan.militarize.stray.cat.hyperultracatgenerator.com.android.egg.neko.Cat;

public class MainActivity extends Activity {
    private static final int EXPORT_BITMAP_SIZE = 600;


    private AppCompatImageView mCurrentCat;
    private NumberPicker mPicker;
    private AppCompatEditText jumpEdit;

    private AppCompatTextView mTextFrom;
    private AppCompatTextView mTextTo;
    private AppCompatSeekBar mSeekFrom;
    private AppCompatSeekBar mSeekTo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCurrentCat = findViewById(R.id.current_cat);
        jumpEdit = findViewById(R.id.input_jump);
        AppCompatButton jumpButton = findViewById(R.id.button_jump);
        jumpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input = jumpEdit.getText().toString();
                int jump;
                try {
                    jump = Integer.parseInt(input);
                } catch (NumberFormatException error) {
                    Toast.makeText(getApplicationContext(), "limit over!", Toast.LENGTH_SHORT).show();
                    return;
                }
                mPicker.setValue(jump);
                mCurrentCat.setImageDrawable(new Cat(getApplicationContext(), jump));
            }
        });


        mPicker = findViewById(R.id.picker);
        mPicker.setMinValue(0);
        mPicker.setMaxValue(Integer.MAX_VALUE);
        mPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                mCurrentCat.setImageDrawable(new Cat(getApplicationContext(), newVal));
            }
        });


        mTextFrom = findViewById(R.id.text_from);
        mTextTo = findViewById(R.id.text_to);

        mSeekFrom = findViewById(R.id.seek_from);
        mSeekTo = findViewById(R.id.seek_to);
        SeekBar.OnSeekBarChangeListener change = new SeekBar.OnSeekBarChangeListener() {
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
                // NOP
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // NOP
            }
        };
        mSeekFrom.setOnSeekBarChangeListener(change);
        mSeekTo.setOnSeekBarChangeListener(change);


    }

    private void shareCat(Cat cat) {
        final File dir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                getString(R.string.directory_name));
        if (!dir.exists() && !dir.mkdirs()) {
            return;
        }
        final File png = new File(dir, cat.getName().replaceAll("[/ #:]+", "_") + ".png");
        Bitmap bitmap = cat.createBitmap(EXPORT_BITMAP_SIZE, EXPORT_BITMAP_SIZE);
        if (bitmap != null) {
            try {
                OutputStream os = new FileOutputStream(png);
                bitmap.compress(Bitmap.CompressFormat.PNG, 0, os);
                os.close();
                MediaScannerConnection.scanFile(
                        this,
                        new String[]{png.toString()},
                        new String[]{"image/png"},
                        null);
                Uri uri = Uri.fromFile(png);
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_STREAM, uri);
                intent.putExtra(Intent.EXTRA_SUBJECT, cat.getName());
                intent.setType("image/png");
                startActivity(Intent.createChooser(intent, null));
                cat.logShare(this);
            } catch (IOException e) {
            }
        }
    }


}
