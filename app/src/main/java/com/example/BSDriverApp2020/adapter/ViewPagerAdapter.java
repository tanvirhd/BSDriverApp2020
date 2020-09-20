package com.example.BSDriverApp2020.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.BSDriverApp2020.fragment.FragmentHome;
import com.example.BSDriverApp2020.fragment.FragmentProfile;

public class ViewPagerAdapter extends FragmentStateAdapter {

    public ViewPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }


    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position){
            case 1:
                return new FragmentProfile();
            default:
                return new FragmentHome();
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
