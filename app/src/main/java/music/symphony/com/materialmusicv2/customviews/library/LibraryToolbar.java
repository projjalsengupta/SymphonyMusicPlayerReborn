package music.symphony.com.materialmusicv2.customviews.library;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.card.MaterialCardView;

import music.symphony.com.materialmusicv2.R;

import static music.symphony.com.materialmusicv2.utils.colorutils.ColorUtils.ContrastColor;
import static music.symphony.com.materialmusicv2.utils.colorutils.ColorUtils.adjustAlpha;
import static music.symphony.com.materialmusicv2.utils.colorutils.ColorUtils.setColorFilter;
import static music.symphony.com.materialmusicv2.utils.colorutils.ColorUtils.setTextColor;

public class LibraryToolbar extends RelativeLayout {

    private TextView title;
    private ImageButton hamburgerMenu;
    private ImageButton menu;
    private MaterialCardView cardView;

    public LibraryToolbar(Context context) {
        super(context);
        init();
    }

    public LibraryToolbar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LibraryToolbar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.layout_toolbar, this);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        title = findViewById(R.id.title);
        hamburgerMenu = findViewById(R.id.hamburgerMenu);
        menu = findViewById(R.id.menu);
        cardView = findViewById(R.id.toolbar_card);
    }

    public void setTitle(String title) {
        if (this.title != null) {
            this.title.setText(title);
        }
    }

    public void setHamburgerMenuClick(@NonNull Runnable click) {
        if (hamburgerMenu != null) {
            hamburgerMenu.setOnClickListener(view -> click.run());
        }
    }

    public void setMenuClick(@NonNull Runnable click) {
        if (menu != null) {
            menu.setOnClickListener(view -> click.run());
        }
    }

    public void setTitleClick(@NonNull Runnable click) {
        if (title != null) {
            title.setOnClickListener(view -> click.run());
        }
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        title = null;
        menu = null;
        hamburgerMenu = null;
    }

    @Override
    public void setBackgroundColor(int color) {
        if (cardView != null) {
            cardView.setCardBackgroundColor(color);
            cardView.setStrokeColor(adjustAlpha(ContrastColor(color), 0.2f));
        }

        setTextColor(ContrastColor(color), title);
        setColorFilter(ContrastColor(color), hamburgerMenu, menu);
    }

    public View getMenu() {
        return menu;
    }
}
