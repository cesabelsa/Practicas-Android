package es.layout.aplicacioncompleta;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        TabLayout tabLayout = findViewById(R.id.tab_layout_home);
        ViewPager2 viewPager = findViewById(R.id.view_pager_home);

        HomePagerAdapter adapter = new HomePagerAdapter(this);
        viewPager.setAdapter(adapter);

        viewPager.setCurrentItem(0, false);

        new TabLayoutMediator(tabLayout, viewPager,
                new TabLayoutMediator.TabConfigurationStrategy() {
                    @Override
                    public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {

                        View customView = LayoutInflater.from(HomeActivity.this)
                                .inflate(R.layout.tab_custom_icon, null);

                        FrameLayout container = customView.findViewById(R.id.tabContainer);
                        ImageView icon = customView.findViewById(R.id.tabIcon);

                        // Fondo blanco de cada tab
                        container.setBackgroundColor(0xFFFFFFFF);

                        // PNG exacto según pestaña
                        switch (position) {
                            case 0:
                                icon.setImageResource(R.drawable.camara_recorte);
                                break;
                            case 1:
                                icon.setImageResource(R.drawable.montana_recorte);
                                break;
                            case 2:
                                icon.setImageResource(R.drawable.coche_recorte);
                                break;
                            case 3:
                            default:
                                icon.setImageResource(R.drawable.cara_recorte);
                                break;
                        }

                        tab.setCustomView(customView);
                    }
                }).attach();
    }
}
