package music.symphony.com.materialmusicv2.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.SkuDetails;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.klinker.android.sliding.SlidingActivity;

import java.util.ArrayList;

import butterknife.BindArray;
import butterknife.BindView;
import butterknife.ButterKnife;
import music.symphony.com.materialmusicv2.R;
import music.symphony.com.materialmusicv2.SymphonyApplication;
import music.symphony.com.materialmusicv2.utils.languageutils.LocaleHelper;
import music.symphony.com.materialmusicv2.utils.themeutils.ThemeUtils;

import static music.symphony.com.materialmusicv2.utils.colorutils.ColorUtils.ContrastColor;
import static music.symphony.com.materialmusicv2.utils.misc.Etc.TOAST_ERROR;
import static music.symphony.com.materialmusicv2.utils.misc.Etc.TOAST_SUCCESS;
import static music.symphony.com.materialmusicv2.utils.misc.Etc.postToast;
import static music.symphony.com.materialmusicv2.utils.themeutils.ThemeUtils.getThemeAccentColor;
import static music.symphony.com.materialmusicv2.utils.themeutils.ThemeUtils.isThemeDarkOrBlack;

public class BuyProActivity extends SlidingActivity {

    @BindView(R.id.buyProContainer)
    LinearLayout buyProContainer;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    @BindView(R.id.donateText)
    TextView donateText;

    @BindArray(R.array.DonationIds)
    String[] donationIds;

    private BillingProcessor bp = null;

    BillingProcessor.IBillingHandler donationHandler = new BillingProcessor.IBillingHandler() {
        @Override
        public void onProductPurchased(@NonNull String productId, @Nullable TransactionDetails details) {
            SymphonyApplication.getInstance().getPreferenceUtils().setDonated(true);
            postToast(R.string.successful_purchase, getApplicationContext(), TOAST_SUCCESS);
            finish();
        }

        @Override
        public void onPurchaseHistoryRestored() {
        }

        @Override
        public void onBillingError(int errorCode, @Nullable Throwable error) {
            postToast(R.string.error_label, getApplicationContext(), TOAST_ERROR);
        }

        @Override
        public void onBillingInitialized() {
            AsyncTask.execute(() -> {
                try {
                    if (bp != null && donationIds != null) {
                        final ArrayList<String[]> donations = new ArrayList<>();
                        for (String donationId : donationIds) {
                            SkuDetails currentSkuDetails = bp.getPurchaseListingDetails(donationId);
                            donations.add(new String[]{currentSkuDetails.priceText, currentSkuDetails.title, currentSkuDetails.description, currentSkuDetails.productId});
                        }
                        new Handler(Looper.getMainLooper()).post(() -> {
                            if (progressBar != null) {
                                progressBar.setVisibility(View.GONE);
                            }
                            if (donateText != null) {
                                donateText.setVisibility(View.VISIBLE);
                            }
                            boolean generalTheme = isThemeDarkOrBlack();
                            int accentContrastColor = ContrastColor(getThemeAccentColor(getApplicationContext()));
                            for (int i = 0; i < donations.size(); i++) {
                                LayoutInflater layoutInflater = getLayoutInflater();
                                @SuppressLint("InflateParams") final View donationItem = layoutInflater.inflate(R.layout.donation_item, null);
                                Button price = donationItem.findViewById(R.id.price);
                                TextView title = donationItem.findViewById(R.id.title);
                                TextView description = donationItem.findViewById(R.id.description);
                                CardView cardView = donationItem.findViewById(R.id.cardView);
                                String[] params = donations.get(i);
                                price.setText(params[0]);
                                price.setTextColor(accentContrastColor);
                                price.setOnClickListener(v -> purchase(params[3]));
                                title.setText(params[1].substring(0, (params[1].indexOf("(")) - 1));
                                description.setText(params[2]);
                                if (generalTheme) {
                                    cardView.setCardBackgroundColor(getResources().getColor(R.color.md_grey_900));
                                } else {
                                    cardView.setCardBackgroundColor(getResources().getColor(R.color.md_grey_100));
                                }
                                if (buyProContainer != null) {
                                    buyProContainer.addView(donationItem);
                                }
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    };

    @Override
    public void init(Bundle savedInstanceState) {
        setTheme(ThemeUtils.getTheme(getApplicationContext()));
        setImage(R.drawable.symphony_banner);
        setContent(R.layout.activity_buy_pro);
        ButterKnife.bind(this);
        AsyncTask.execute(() -> bp = new BillingProcessor(getApplicationContext(), getString(R.string.in_app_billing_api_key), donationHandler));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (bp != null && !bp.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void purchase(String itemId) {
        bp.purchase(BuyProActivity.this, itemId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (bp != null) {
            bp.release();
            bp = null;
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }
}
