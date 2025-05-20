package com.example.scanimin.Fragment.ViewPager2;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.scanimin.data.Object.Customer;

import java.util.ArrayList;
import java.util.List;

public class ViewPagerAdapter extends FragmentStateAdapter {
    private List<Customer> customerList;
    private final List<Fragment> fragmentList;

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity, List<Customer> customerList) {
        super(fragmentActivity);
        this.customerList = customerList;
        fragmentList = new ArrayList<>();
        fragmentList.add(new AllCustomer());
        fragmentList.add(new CustomerChecked());
        fragmentList.add(new CustomerNotChecked());
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return fragmentList.get(position);
    }

    public Fragment getFragment(int position) {
        return fragmentList.get(position);
    }

    @Override
    public int getItemCount() {
        return fragmentList.size();
    }
}

