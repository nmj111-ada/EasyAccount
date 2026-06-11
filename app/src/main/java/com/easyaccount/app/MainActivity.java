package com.easyaccount.app;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.easyaccount.app.ui.add.AddTransactionFragment;
import com.easyaccount.app.ui.home.HomeFragment;
import com.easyaccount.app.ui.profile.ProfileFragment;
import com.easyaccount.app.ui.search.SearchFragment;
import com.easyaccount.app.ui.stats.StatsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewPager = findViewById(R.id.view_pager);
        bottomNav = findViewById(R.id.bottom_navigation);

        // 5 个页面：流水明细 / 统计 / 记账(占位) / 搜索 / 我的
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager(), getLifecycle());
        viewPager.setAdapter(adapter);
        viewPager.setUserInputEnabled(false);

        // 底部导航切换
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                viewPager.setCurrentItem(0, false);
                return true;
            } else if (itemId == R.id.nav_stats) {
                viewPager.setCurrentItem(1, false);
                return true;
            } else if (itemId == R.id.nav_add) {
                viewPager.setCurrentItem(2, false);
                return true;
            } else if (itemId == R.id.nav_search) {
                viewPager.setCurrentItem(3, false);
                return true;
            } else if (itemId == R.id.nav_profile) {
                viewPager.setCurrentItem(4, false);
                return true;
            }
            return false;
        });

        // ViewPager 切换同步导航栏
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                switch (position) {
                    case 0: bottomNav.setSelectedItemId(R.id.nav_home); break;
                    case 1: bottomNav.setSelectedItemId(R.id.nav_stats); break;
                    case 2: bottomNav.setSelectedItemId(R.id.nav_add); break;
                    case 3: bottomNav.setSelectedItemId(R.id.nav_search); break;
                    case 4: bottomNav.setSelectedItemId(R.id.nav_profile); break;
                }
            }
        });
    }

    private static class ViewPagerAdapter extends FragmentStateAdapter {

        public ViewPagerAdapter(@NonNull FragmentManager fm, @NonNull Lifecycle lifecycle) {
            super(fm, lifecycle);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0: return new HomeFragment();
                case 1: return new StatsFragment();
                case 2: return new AddTransactionFragment();
                case 3: return new SearchFragment();
                case 4: return new ProfileFragment();
                default: return new HomeFragment();
            }
        }

        @Override
        public int getItemCount() {
            return 5;
        }
    }
}
