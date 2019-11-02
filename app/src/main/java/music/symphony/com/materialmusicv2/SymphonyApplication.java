package music.symphony.com.materialmusicv2;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.res.ResourcesCompat;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;

import java.util.Objects;

import es.dmoral.toasty.Toasty;
import music.symphony.com.materialmusicv2.service.MusicService;
import music.symphony.com.materialmusicv2.service.PlayingQueueManager;
import music.symphony.com.materialmusicv2.utils.languageutils.LocaleHelper;
import music.symphony.com.materialmusicv2.utils.mediasessionutils.MediaSessionUtils;
import music.symphony.com.materialmusicv2.utils.misc.Statics;
import music.symphony.com.materialmusicv2.utils.preferenceutils.PreferenceUtils;
import music.symphony.com.materialmusicv2.widgets.WidgetUtils;

import static music.symphony.com.materialmusicv2.utils.misc.Etc.isMIUI;
import static music.symphony.com.materialmusicv2.utils.queryutils.QueryUtils.createPlaylist;

public class SymphonyApplication extends Application implements BillingProcessor.IBillingHandler {

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    private boolean isMiui = false;

    private BillingProcessor bp;

    private MediaSessionUtils mediaSessionUtils = null;
    private PreferenceUtils preferenceUtils = null;
    private PlayingQueueManager playingQueueManager = null;
    private WidgetUtils widgetUtils = null;

    public MediaSessionUtils getMediaSessionUtils() {
        if (mediaSessionUtils == null) {
            mediaSessionUtils = new MediaSessionUtils(getApplicationContext());
        }
        return mediaSessionUtils;
    }

    public PreferenceUtils getPreferenceUtils() {
        if (preferenceUtils == null) {
            preferenceUtils = new PreferenceUtils(getApplicationContext());
        }
        preferenceUtils.checkNull();
        return preferenceUtils;
    }

    public PlayingQueueManager getPlayingQueueManager() {
        if (playingQueueManager == null) {
            playingQueueManager = new PlayingQueueManager();
        }
        return playingQueueManager;
    }

    public WidgetUtils getWidgetUtils() {
        if (widgetUtils == null) {
            widgetUtils = new WidgetUtils(getApplicationContext());
        }
        return widgetUtils;
    }

    private static SymphonyApplication symphonyApplication;

    public static SymphonyApplication getInstance() {
        return symphonyApplication;
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        symphonyApplication = this;
        isMiui = isMIUI(getApplicationContext());
        Statics.loadTreeUri(this);
        createPlaylist("Favorites", getApplicationContext());
        AsyncTask.execute(() -> bp = new BillingProcessor(getApplicationContext(), getString(R.string.in_app_billing_api_key), SymphonyApplication.this));
        Intent intent = new Intent(this, MusicService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        try {
            Toasty.Config.getInstance().tintIcon(true)
                    .setToastTypeface(Objects.requireNonNull(ResourcesCompat.getFont(this, R.font.font_product_sans)))
                    .setTextSize(16)
                    .allowQueue(true)
                    .apply();
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onProductPurchased(@NonNull String productId, @Nullable TransactionDetails details) {
    }

    @Override
    public void onPurchaseHistoryRestored() {
    }

    @Override
    public void onBillingError(int errorCode, @Nullable Throwable error) {
    }

    @Override
    public void onBillingInitialized() {
        AsyncTask.execute(() -> {
            boolean donated = false;
            for (String donationId : getResources().getStringArray(R.array.DonationIds)) {
                TransactionDetails transactionDetails = bp.getPurchaseTransactionDetails(donationId);
                try {
                    if (transactionDetails != null) {
                        donated = true;
                        break;
                    }
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }
            if (donated) {
                getPreferenceUtils().setDonated(true);
            }
        });
    }

    public void nullify() {
        mediaSessionUtils = null;
        preferenceUtils = null;
        playingQueueManager = null;
        widgetUtils = null;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base, "en"));
    }

    public boolean isMiui() {
        return isMiui;
    }
}