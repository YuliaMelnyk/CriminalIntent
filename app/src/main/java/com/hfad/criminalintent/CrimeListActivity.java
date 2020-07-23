package com.hfad.criminalintent;

import androidx.fragment.app.Fragment;

/**
 * @author yuliiamelnyk on 23/07/2020
 * @project CriminalIntent
 */
public class CrimeListActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment() {
        return new CrimeListFragment();
    }
}
