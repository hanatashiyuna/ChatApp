package com.example.yunaproject.UX.Adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.yunaproject.UI.Fragment.ChatsFragment;
import com.example.yunaproject.UI.Fragment.ProfileFragment;
import com.example.yunaproject.UI.Fragment.UsersFragment;

public class ViewPager2Adapter extends FragmentStateAdapter {

    public static final int TAB_LAYOUT_COUNT = 3;

    public ViewPager2Adapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position){
            case 1:
                return new UsersFragment();
            case 2:
                return new ProfileFragment();
            default:
                return new ChatsFragment();
        }
    }

    @Override
    public int getItemCount() {
        return TAB_LAYOUT_COUNT;
    }
}
