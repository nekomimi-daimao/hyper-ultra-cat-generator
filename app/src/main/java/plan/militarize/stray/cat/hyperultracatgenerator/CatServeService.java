package plan.militarize.stray.cat.hyperultracatgenerator;

import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;

import plan.militarize.stray.cat.hyperultracatgenerator.com.android.egg.neko.Cat;

import static plan.militarize.stray.cat.hyperultracatgenerator.MainActivity.EXPORT_BITMAP_SIZE;

public class CatServeService extends IntentService {
    private static final String ACTION_SAVE_CATS = "plan.militarize.stray.cat.hyperultracatgenerator.action.save.cats";

    private static final String EXTRA_NUM_FROM = "plan.militarize.stray.cat.hyperultracatgenerator.extra.num.from";
    private static final String EXTRA_NUM_TO = "plan.militarize.stray.cat.hyperultracatgenerator.extra.num.to";

    private static final String CHANNEL_ID_SAVE_CAT = "saveCat";
    private static final int NOTIFICATION_ID_SAVE_CAT_PROGRESS = 100;
    private static final int NOTIFICATION_ID_SAVE_CAT_FINISH = 101;

    private ExecutorService executorService;
    private NotificationManager manager;


    public CatServeService() {
        super("CatServeService");
    }

    public CatServeService(String name) {
        super(name);
    }


    public static void startActionSaveCats(Context context, int from, int to) {
        Intent intent = new Intent(context, CatServeService.class);
        intent.setAction(ACTION_SAVE_CATS);
        intent.putExtra(EXTRA_NUM_FROM, from);
        intent.putExtra(EXTRA_NUM_TO, to);
        ContextCompat.startForegroundService(context, intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null || !ACTION_SAVE_CATS.equals(intent.getAction())
                || !intent.hasExtra(EXTRA_NUM_FROM) || !intent.hasExtra(EXTRA_NUM_TO)) {
            return;
        }

        final int from = intent.getIntExtra(EXTRA_NUM_FROM, 0);
        final int to = intent.getIntExtra(EXTRA_NUM_TO, 999);
        handleActionSaveCats(from, to);
    }

    private void handleActionSaveCats(final int from, final int to) {

        final int MAX = to - from + 1;

        manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID_SAVE_CAT, getString(R.string.save_the_cats), NotificationManager.IMPORTANCE_DEFAULT);
            manager.createNotificationChannel(channel);
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID_SAVE_CAT);
        builder.setContentTitle(getString(R.string.app_name))
                .setContentText("now generating cats...")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(false)
                .setWhen(System.currentTimeMillis())
                .setProgress(MAX, 0, false);
        startForeground(NOTIFICATION_ID_SAVE_CAT_PROGRESS, builder.build());

        Set<Integer> set = new HashSet<>(MAX);
        List<Future<?>> list = new ArrayList<Future<?>>(MAX);
        executorService = Executors.newWorkStealingPool();

        while (set.size() < MAX) {
            int seed = Math.abs(ThreadLocalRandom.current().nextInt());
            int name = seed % 1000;
            if (name < from || to < name) {
                continue;
            }
            if (set.contains(name)) {
                continue;
            }
            set.add(name);
            list.add(executorService.submit(new CatSaver(seed)));
        }
        executorService.shutdown();

        for (int count = 0; count < list.size(); count++) {
            try {
                list.get(count).get();
                builder.setProgress(MAX, count, false);
                manager.notify(NOTIFICATION_ID_SAVE_CAT_PROGRESS, builder.build());
            } catch (ExecutionException | InterruptedException e) {
            }
        }

        builder.setProgress(0, 0, false);
        builder.setContentText("complete! you have " + MAX + " cats!");
        manager.notify(NOTIFICATION_ID_SAVE_CAT_FINISH, builder.build());
    }

    private class CatSaver implements Runnable {

        private final long seed;

        CatSaver(final long seed) {
            this.seed = seed;
        }

        @Override
        public void run() {
            final File dir = new File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    getString(R.string.directory_name));
            if (!dir.exists() && !dir.mkdirs()) {
                return;
            }
            Cat cat = new Cat(getApplicationContext(), seed);
            final File png = new File(dir, cat.getName().replaceAll("[/ #:]+", "_") + ".png");
            Bitmap bitmap = cat.createBitmap(EXPORT_BITMAP_SIZE, EXPORT_BITMAP_SIZE);
            if (bitmap == null) {
                return;
            }
            try {
                OutputStream os = new FileOutputStream(png);
                bitmap.compress(Bitmap.CompressFormat.PNG, 0, os);
                bitmap.recycle();
                bitmap = null;
                MediaScannerConnection.scanFile(
                        getApplicationContext(),
                        new String[]{png.toString()},
                        new String[]{"image/png"},
                        null);
            } catch (Exception e) {
            }
        }
    }
}
