package music.symphony.com.materialmusicv2.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.view.KeyEvent;

import java.util.Objects;

import music.symphony.com.materialmusicv2.utils.misc.Statics;

public class MediaButtonBroadcastReceiver extends BroadcastReceiver {

    private static int clickCounter = 0;
    private static long lastClickTime = 0;
    private Intent myIntent;

    @Override
    public void onReceive(final Context context, Intent intent) {
        try {
            if (Objects.equals(intent.getAction(), Intent.ACTION_MEDIA_BUTTON)) {
                KeyEvent keyEvent = (KeyEvent) Objects.requireNonNull(intent.getExtras()).get(Intent.EXTRA_KEY_EVENT);
                assert keyEvent != null;
                if (keyEvent.getAction() != KeyEvent.ACTION_DOWN) {
                    if (clickCounter == 0) {
                        lastClickTime = android.os.SystemClock.uptimeMillis();
                    }
                    return;
                }
                switch (keyEvent.getKeyCode()) {
                    case KeyEvent.KEYCODE_HEADSETHOOK: {
                        clickCounter++;
                        long action_up = android.os.SystemClock.uptimeMillis();
                        long mTimeOut = 750;
                        if (action_up - lastClickTime > mTimeOut) {
                            clickCounter = 0;
                        }
                        if (clickCounter == 1 || clickCounter == 2) {
                            final Handler handler = new Handler();
                            handler.postDelayed(() -> {
                                if (clickCounter == 1) {
                                    myIntent = new Intent(Statics.NOTIFY_PAUSE_OR_RESUME);
                                    context.sendBroadcast(myIntent);
                                } else if (clickCounter == 2) {
                                    Intent playNextIntent = new Intent(Statics.NOTIFY_NEXT);
                                    context.sendBroadcast(playNextIntent);
                                }
                                clickCounter = 0;
                            }, 750);
                        }
                        if (clickCounter == 3) {
                            myIntent = new Intent(Statics.NOTIFY_PREVIOUS);
                            context.sendBroadcast(myIntent);
                            clickCounter = 0;
                        }
                        break;
                    }
                    case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE: {
                        myIntent = new Intent(Statics.NOTIFY_PAUSE_OR_RESUME);
                        context.sendBroadcast(myIntent);
                        break;
                    }
                    case KeyEvent.KEYCODE_MEDIA_PLAY:
                    case KeyEvent.KEYCODE_MEDIA_PAUSE: {
                        myIntent = new Intent(Statics.NOTIFY_PAUSE_OR_RESUME);
                        context.sendBroadcast(myIntent);
                        break;
                    }
                    case KeyEvent.KEYCODE_MEDIA_STOP: {
                        myIntent = new Intent(Statics.NOTIFY_STOP);
                        context.sendBroadcast(myIntent);
                        break;
                    }
                    case KeyEvent.KEYCODE_MEDIA_NEXT: {
                        myIntent = new Intent(Statics.NOTIFY_NEXT);
                        context.sendBroadcast(myIntent);
                        break;
                    }
                    case KeyEvent.KEYCODE_MEDIA_PREVIOUS: {
                        myIntent = new Intent(Statics.NOTIFY_PREVIOUS);
                        context.sendBroadcast(myIntent);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}