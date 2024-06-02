package com.mariana.androidhifam;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;

public class ViewPagerAdapter extends FragmentStateAdapter {
    private ArrayList<Fragment> pantallas;

    public ViewPagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle, ArrayList<Fragment> pantallas) {
        super(fragmentManager, lifecycle);
        this.pantallas = pantallas;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return pantallas.get(position);
    }

    @Override
    public int getItemCount() {
        return pantallas.size();
    }
}
