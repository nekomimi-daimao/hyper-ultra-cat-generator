package plan.militarize.stray.cat.hyperultracatgenerator;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatSeekBar;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.SeekBar;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import plan.militarize.stray.cat.hyperultracatgenerator.com.android.egg.neko.Cat;

public class MainActivity extends AppCompatActivity {

    public static final String[] PERMISSIONS = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    public static final int EXPORT_BITMAP_SIZE = 600;

    private AppCompatImageView mCurrentCat;
    private AppCompatTextView mCurrentCatName;
    private NumberPicker mPicker;
    private AppCompatEditText mJumpEdit;

    private CardView mCardSave;
    private AppCompatTextView mTextFrom;
    private AppCompatTextView mTextTo;
    private AppCompatImageView mCatFrom;
    private AppCompatImageView mCatTo;
    private AppCompatSeekBar mSeekFrom;
    private AppCompatSeekBar mSeekTo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (getSupportActionBar() != null) {
            getSupportActionBar().show();
        }

        // --- generate cat ---
        mCurrentCat = findViewById(R.id.current_cat);
        mCurrentCat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String[] denied = Arrays.stream(PERMISSIONS)
                        .filter(permission -> ContextCompat.checkSelfPermission(getApplicationContext(), permission) != PackageManager.PERMISSION_GRANTED)
                        .toArray((String[]::new));

                if (denied.length > 0) {
                    requestPermissions(denied, v.getId());
                    return;
                }
                shareCat(new Cat(getApplicationContext(), mPicker.getValue()));
            }
        });
        mCurrentCatName = findViewById(R.id.current_cat_name);
        mJumpEdit = findViewById(R.id.input_jump);
        AppCompatButton jumpButton = findViewById(R.id.button_jump);
        jumpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input = mJumpEdit.getText().toString();
                int jump;
                try {
                    jump = Integer.parseInt(input);
                } catch (NumberFormatException error) {
                    Snackbar.make(mCardSave, R.string.limit_over, Snackbar.LENGTH_SHORT).show();
                    return;
                }
                mPicker.setValue(jump);
                Cat cat = new Cat(getApplicationContext(), jump);
                mCurrentCat.setImageDrawable(cat);
                mCurrentCatName.setText(cat.getName());
            }
        });

        mPicker = findViewById(R.id.picker);
        mPicker.setMinValue(0);
        mPicker.setMaxValue(Integer.MAX_VALUE);
        mPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                Cat cat = new Cat(getApplicationContext(), newVal);
                mCurrentCat.setImageDrawable(cat);
                mCurrentCatName.setText(cat.getName());
            }
        });

        // initialize
        mJumpEdit.setText(String.valueOf(Math.abs(ThreadLocalRandom.current().nextInt())));
        jumpButton.callOnClick();


        // --- save 1000 cats ---
        mCardSave = findViewById(R.id.card_save);

        mTextFrom = findViewById(R.id.text_from);
        mTextTo = findViewById(R.id.text_to);

        mCatFrom = findViewById(R.id.cat_from);
        mCatTo = findViewById(R.id.cat_to);

        mSeekFrom = findViewById(R.id.seek_from);
        mSeekTo = findViewById(R.id.seek_to);
        SeekBar.OnSeekBarChangeListener change = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Cat cat = Cat.create(getApplicationContext());
                if (seekBar.getId() == R.id.seek_from) {
                    mTextFrom.setText(getString(R.string.from, String.valueOf(progress)));
                    mCatFrom.setImageDrawable(cat);
                } else {
                    mTextTo.setText(getString(R.string.to, String.valueOf(progress)));
                    mCatTo.setImageDrawable(cat);
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

        AppCompatButton saveButton = findViewById(R.id.save_cats);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] denied = Arrays.stream(PERMISSIONS)
                        .filter(permission -> ContextCompat.checkSelfPermission(getApplicationContext(), permission) != PackageManager.PERMISSION_GRANTED)
                        .toArray((String[]::new));

                if (denied.length > 0) {
                    requestPermissions(denied, v.getId());
                    return;
                }
                saveCat(mSeekFrom.getProgress(), mSeekTo.getProgress());
            }
        });

        // initialize
        Random random = new Random();
        int a = random.nextInt(999);
        int b = random.nextInt(999);
        mSeekFrom.setProgress(a > b ? b : a);
        mSeekTo.setProgress(a > b ? a : b);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int grantResult : grantResults) {
            if (grantResult != PackageManager.PERMISSION_GRANTED) {
                Snackbar.make(mCardSave, R.string.request_permission, Snackbar.LENGTH_LONG).show();
                return;
            }
        }
        switch (requestCode) {
            case R.id.current_cat:
                shareCat(new Cat(getApplicationContext(), mPicker.getValue()));
                break;
            case R.id.save_cats:
                saveCat(mSeekFrom.getProgress(), mSeekTo.getProgress());
                break;
            default:
                return;
        }

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
//                Uri uri = Uri.fromFile(png);
                Uri uri = FileProvider.getUriForFile(
                        getApplicationContext(),
                        getApplicationContext().getPackageName() + ".fileprovider",
                        png
                );
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_STREAM, uri);
                intent.putExtra(Intent.EXTRA_SUBJECT, cat.getName());
                intent.setType("image/png");
                startActivity(Intent.createChooser(intent, null));
                cat.logShare(this);
            } catch (IOException e) {
                Snackbar.make(mCardSave, R.string.fail_save, Snackbar.LENGTH_LONG).show();
            }
        }
    }

    private void saveCat(final int from, final int to) {
        if (from > to) {
            Snackbar.make(mCardSave, R.string.from_and_to, Snackbar.LENGTH_LONG).show();
            return;
        }

        CatServeService.startActionSaveCats(getApplicationContext(), from, to);

        Snackbar.make(mCardSave, R.string.snack_store, Snackbar.LENGTH_LONG)
                .setAction(R.string.go, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_PICK);
                        final File dir = new File(
                                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                                getString(R.string.directory_name));
                        Uri uri = Uri.parse(dir.getAbsolutePath());
                        intent.setDataAndType(uri, "image/png");
                        startActivity(intent);
                    }
                })
                .show();
    }


}
