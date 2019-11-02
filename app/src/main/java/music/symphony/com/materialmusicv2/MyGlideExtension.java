package music.symphony.com.materialmusicv2;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;

import com.bumptech.glide.annotation.GlideExtension;
import com.bumptech.glide.annotation.GlideOption;
import com.bumptech.glide.request.RequestOptions;

@GlideExtension
public class MyGlideExtension {
    private MyGlideExtension() {
    }

    @NonNull
    @SuppressLint("CheckResult")
    @GlideOption
    public static RequestOptions circleCrop(RequestOptions options, boolean crop) {
        if (crop) {
            options.circleCrop();
        }
        return options;
    }
}