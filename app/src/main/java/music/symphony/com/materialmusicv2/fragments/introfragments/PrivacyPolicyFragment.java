package music.symphony.com.materialmusicv2.fragments.introfragments;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.github.paolorotolo.appintro.ISlidePolicy;

import butterknife.BindView;
import butterknife.ButterKnife;
import music.symphony.com.materialmusicv2.R;

import static music.symphony.com.materialmusicv2.utils.misc.Etc.TOAST_ERROR;
import static music.symphony.com.materialmusicv2.utils.misc.Etc.TOAST_NORMAL;
import static music.symphony.com.materialmusicv2.utils.misc.Etc.postToast;
import static music.symphony.com.materialmusicv2.utils.themeutils.ThemeUtils.getThemePrimaryColor;

public class PrivacyPolicyFragment extends Fragment implements ISlidePolicy {

    @BindView(R.id.main)
    LinearLayout main;
    @BindView(R.id.check)
    CheckBox check;
    @BindView(R.id.readPrivacyPolicy)
    Button readPrivacyPolicy;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_privacy_policy, container, false);
        ButterKnife.bind(this, rootView);

        if (getActivity() != null) {
            main.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.md_cyan_500));
        }

        readPrivacyPolicy.setOnClickListener(v -> {
            try {
                String url = "https://sites.google.com/site/symphonymusicplayerreborn/";
                CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                builder.setToolbarColor(getThemePrimaryColor(getActivity()));
                CustomTabsIntent customTabsIntent = builder.build();
                customTabsIntent.launchUrl(getActivity(), Uri.parse(url));
            } catch (Exception e) {
                postToast(R.string.error_label, getActivity(), TOAST_ERROR);
                e.printStackTrace();
            }
        });
        return rootView;
    }

    @Override
    public boolean isPolicyRespected() {
        return check.isChecked();
    }

    @Override
    public void onUserIllegallyRequestedNextPage() {
        postToast(R.string.accept_the_privacy_policy, getActivity(), TOAST_NORMAL);
    }
}
