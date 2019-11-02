package music.symphony.com.materialmusicv2.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.github.paolorotolo.appintro.AppIntro2;
import com.github.paolorotolo.appintro.AppIntro2Fragment;
import com.github.paolorotolo.appintro.model.SliderPage;

import music.symphony.com.materialmusicv2.R;
import music.symphony.com.materialmusicv2.SymphonyApplication;
import music.symphony.com.materialmusicv2.fragments.introfragments.PrivacyPolicyFragment;

import static music.symphony.com.materialmusicv2.utils.misc.Etc.TOAST_NORMAL;
import static music.symphony.com.materialmusicv2.utils.misc.Etc.postToast;

public class SymphonyIntroActivity extends AppIntro2 {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SliderPage frontPage = new SliderPage();
        frontPage.setTitle(getString(R.string.app_name));
        frontPage.setDescription(getString(R.string.about_symphony_text));
        frontPage.setImageDrawable(R.drawable.symphony_logo);
        frontPage.setBgColor(ContextCompat.getColor(getApplicationContext(), R.color.md_red_500));
        addSlide(AppIntro2Fragment.newInstance(frontPage));

        addSlide(new PrivacyPolicyFragment());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            SliderPage permissionsPage = new SliderPage();
            permissionsPage.setTitle(getString(R.string.permission));
            permissionsPage.setTitleColor(Color.BLACK);
            permissionsPage.setDescription(getString(R.string.permissions_text));
            permissionsPage.setDescColor(Color.BLACK);
            permissionsPage.setImageDrawable(R.drawable.permission);
            permissionsPage.setBgColor(ContextCompat.getColor(getApplicationContext(), R.color.md_amber_500));
            addSlide(AppIntro2Fragment.newInstance(permissionsPage));
            askForPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO}, 3);
        }

        showSkipButton(false);
    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        if (!(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)) {
            postToast(R.string.kindly_grant_permissions, getApplicationContext(), TOAST_NORMAL);
            new Handler().postDelayed(() -> ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO}, 0), 500);
        } else {
            SymphonyApplication.getInstance().getPreferenceUtils().setIsIntroShown(true);
            finish();
        }
    }

    @Override
    public void onSlideChanged(@Nullable Fragment oldFragment, @Nullable Fragment newFragment) {
        super.onSlideChanged(oldFragment, newFragment);
    }
}