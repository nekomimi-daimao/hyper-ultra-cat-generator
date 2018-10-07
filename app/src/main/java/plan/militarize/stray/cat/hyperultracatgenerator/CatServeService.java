package plan.militarize.stray.cat.hyperultracatgenerator;

import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

public class CatServeService extends IntentService {
    private static final String ACTION_SAVE_CATS = "plan.militarize.stray.cat.hyperultracatgenerator.action.save.cats";

    private static final String EXTRA_NUM_FROM = "plan.militarize.stray.cat.hyperultracatgenerator.extra.num.from";
    private static final String EXTRA_NUM_TO = "plan.militarize.stray.cat.hyperultracatgenerator.extra.num.to";

    private static final String CHANNEL_ID_SAVE_CAT = "saveCat";


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
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID_SAVE_CAT, getString(R.string.save_the_cats), NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            manager.createNotificationChannel(channel);
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID_SAVE_CAT);
        builder.setContentTitle(getString(R.string.app_name))
                .setContentText("now generating cats...")
                .setSmallIcon(R.mipmap.ic_launcher);
        startForeground(100, builder.build());

    }

}
