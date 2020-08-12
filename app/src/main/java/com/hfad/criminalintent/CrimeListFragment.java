package com.hfad.criminalintent;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.hfad.criminalintent.model.Crime;
import com.hfad.criminalintent.model.CrimeLab;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static android.content.ContentValues.TAG;
import static androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_SWIPE;

/**
 * @author yuliiamelnyk on 23/07/2020
 * @project CriminalIntent
 */
public class CrimeListFragment extends Fragment {

    private static final String SAVED_SUBTITLE_VISIBLE = "subtitle";

    private RecyclerView mCrimeRecyclerView;
    private CrimeAdapter mAdapter;
    private boolean mSubtitleVisible;
    private Callbacks mCallbacks;
    private OnDeleteCrimeListener mDeleteCallBack;

    private static int TYPE_NORMAL = 1;
    private static int TYPE_REQUIRES_POLICE = 2;
    private int mLastUpdatedPosition = -1;

    public interface Callbacks {
        void onCrimeSelected(Crime crime);
    }

    public interface OnDeleteCrimeListener {
        void onCrimeIdSelected(UUID crimeId);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mCallbacks = (Callbacks) context;
        mDeleteCallBack = (OnDeleteCrimeListener) context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_crime_list, container, false);
        mCrimeRecyclerView = view.findViewById(R.id.recycler_view);
        mCrimeRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        if (savedInstanceState != null) {
            mSubtitleVisible = savedInstanceState.getBoolean(SAVED_SUBTITLE_VISIBLE);
        }
        mCrimeRecyclerView.setHasFixedSize(true);
        setCrimeRecyclerViewItemTouchListener();
        updateUI();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SAVED_SUBTITLE_VISIBLE, mSubtitleVisible);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
        mDeleteCallBack = null;
    }

    public void deleteCrime(UUID crimeId) {
        Crime crime = CrimeLab.get(getActivity()).getCrime(crimeId);
        CrimeLab.get(getActivity()).deleteCrime(crime);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_crime_list, menu);

        MenuItem subtitleItem = menu.findItem(R.id.show_subtitle);
        if (mSubtitleVisible) {
            subtitleItem.setTitle(R.string.hide_subtitle);
        } else {
            subtitleItem.setTitle(R.string.show_subtitle);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.new_crime:
                Crime crime = new Crime();
                CrimeLab.get(getActivity()).addCrime(crime);
                updateUI();
                mCallbacks.onCrimeSelected(crime);
                return true;
            case R.id.show_subtitle:
                mSubtitleVisible = !mSubtitleVisible;
                getActivity().invalidateOptionsMenu();
                updateSubtitle();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateSubtitle() {
        CrimeLab crimeLab = CrimeLab.get(getActivity());
        int crimeCount = crimeLab.getCrimes().size();
        String subtitle = getResources().getQuantityString(R.plurals.subtitle_plural, crimeCount, crimeCount);

        if (!mSubtitleVisible) {
            subtitle = null;
        }

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.getSupportActionBar().setSubtitle(subtitle);
    }

    public void updateUI() {
        CrimeLab crimeLab = CrimeLab.get(getActivity());
        List<Crime> crimes = crimeLab.getCrimes();
        if (mAdapter == null) {
            mAdapter = new CrimeAdapter(crimes);
            mCrimeRecyclerView.setAdapter(mAdapter);
        } else {
            if (mLastUpdatedPosition > -1) {
                mAdapter.notifyItemChanged(mLastUpdatedPosition);
                mLastUpdatedPosition = -1;
            } else {
                mAdapter.setCrimes(crimes);
                mAdapter.notifyDataSetChanged();
            }
        }
        updateSubtitle();
    }

    public void setCrimeRecyclerViewItemTouchListener() {
        ItemTouchHelper.SimpleCallback itemTouchCallback =
                new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
                    @Override
                    public boolean onMove(RecyclerView recyclerView,
                                          RecyclerView.ViewHolder viewHolder,
                                          RecyclerView.ViewHolder target) {
                        return false;
                    }

                    @Override
                    public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                        int position = viewHolder.getAdapterPosition();
                        Crime crime = mAdapter.mCrimes.get(position);
                        mDeleteCallBack.onCrimeIdSelected(crime.getId());
                    }

                    @Override
                    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                        // get item
                        View itemview = viewHolder.itemView;
                        // get an icon from drawable folder
                        Drawable deleteIcon = ContextCompat.getDrawable(getContext(), R.drawable.ic_menu_delete);
                        // get height and width sizes from layout
                        float IcontHeight = deleteIcon.getIntrinsicHeight();
                        float IconWidth = deleteIcon.getIntrinsicWidth();
                        // get item's bottom and Top size
                        float itemHeight = itemview.getBottom() - itemview.getTop();

                        if (actionState == ACTION_STATE_SWIPE) {   // user is swipe
                            Log.d(TAG, "////////////////////////////////////////////////");
                            Log.d(TAG, "ACTION STATE SWAP is true: ");

                            Resources r = getResources();   // as you read
                            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);    // create paint object
                            // get layout all values from inflate crime_fragment_list.xml
                            RectF layout = new RectF(itemview.getLeft(), itemview.getTop(), itemview.getRight(), itemview.getBottom());
                            // set color
                            paint.setColor(r.getColor(R.color.colorAccent));
                            // drawing canvas
                            c.drawRect(layout, paint);

                            // to calculate deleteIcon object that necessary values
                            int deleteIconTop = (int) (itemview.getTop() + (itemHeight - IcontHeight) / 2);
                            int deleteIconBottom = (int) (deleteIconTop + IcontHeight);
                            int deleteIconMargin = (int) ((itemHeight - IcontHeight) / 2);
                            int deleteIconLeft = (int) (itemview.getRight() - deleteIconMargin - IconWidth);
                            int deleteIconRight = (int) itemview.getRight() - deleteIconMargin;
                            // then set boundry that get values above
                            deleteIcon.setBounds(deleteIconLeft, deleteIconTop, deleteIconRight, deleteIconBottom);
                            // to add canvas
                            deleteIcon.draw(c);

                            getDefaultUIUtil().onDraw(c, recyclerView, viewHolder.itemView, dX, dY, actionState, isCurrentlyActive);

                        } else {
                            Log.d(TAG, "////////////////////////////////////////////////");
                            Log.d(TAG, "ACTION STATE SWAP is false: ");
                        }
                    }
                };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchCallback);
        itemTouchHelper.attachToRecyclerView(mCrimeRecyclerView);
    }

    private class CrimeHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView mTitleTextView, mDateTextView;
        private ImageView mSolvedImageView;
        private String dateString;
        private Crime mCrime;


        public CrimeHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_crime, parent, false));

            mTitleTextView = itemView.findViewById(R.id.crime_title);
            mDateTextView = itemView.findViewById(R.id.crime_date);
            mSolvedImageView = itemView.findViewById(R.id.crime_solved);
            itemView.setOnClickListener(this);
        }

        private void bind(Crime crime) {
            mCrime = crime;
            mTitleTextView.setText(mCrime.getTitle());
            SimpleDateFormat format = new SimpleDateFormat("E, MMM dd, yyyy");
            dateString = format.format(new Date());
            mDateTextView.setText(dateString);
            mSolvedImageView.setVisibility(crime.isSolved() ? View.VISIBLE : View.GONE);
        }

        @Override
        public void onClick(View view) {
            mCallbacks.onCrimeSelected(mCrime);
            mLastUpdatedPosition = this.getAdapterPosition();

        }
    }

    private class CrimePoliceHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView mTitleTextView, mDateTextView;
        private ImageView mSolvedImageView;
        private Crime mCrime;
        private String dateString;


        public CrimePoliceHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_crime_police, parent, false));

            mTitleTextView = itemView.findViewById(R.id.crime_title_police);
            mDateTextView = itemView.findViewById(R.id.crime_date_police);
            mSolvedImageView = itemView.findViewById(R.id.crime_solved_police);
            itemView.setOnClickListener(this);
        }

        private void bind(Crime crime) {
            mCrime = crime;
            mTitleTextView.setText(mCrime.getTitle());
            mTitleTextView.setText(mCrime.getTitle());
            SimpleDateFormat format = new SimpleDateFormat("E, MMM dd, yyyy");
            dateString = format.format(new Date());
            mDateTextView.setText(dateString);
            mSolvedImageView.setVisibility(crime.isSolved() ? View.VISIBLE : View.GONE);
        }

        @Override
        public void onClick(View view) {
            Intent intent = CrimePagerActivity.newIntent(getActivity(), mCrime.getId());
            mLastUpdatedPosition = this.getAdapterPosition();
            startActivity(intent);
        }
    }

    private class CrimeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private List<Crime> mCrimes;

        public CrimeAdapter(List<Crime> crimes) {
            mCrimes = crimes;
        }

        @Override
        public int getItemViewType(int position) {
            if (mCrimes.get(position).isRequiresPolice()) {
                return TYPE_REQUIRES_POLICE;
            } else {
                return TYPE_NORMAL;
            }
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == TYPE_NORMAL) {
                LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
                return new CrimeHolder(layoutInflater, parent);
            } else {
                LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
                return new CrimePoliceHolder(layoutInflater, parent);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (getItemViewType(position) == TYPE_NORMAL) {
                ((CrimeHolder) holder).bind(mCrimes.get(position));
            } else {
                ((CrimePoliceHolder) holder).bind(mCrimes.get(position));
            }
        }

        @Override
        public int getItemCount() {
            return mCrimes.size();
        }

        public void setCrimes(List<Crime> crimes) {
            mCrimes = crimes;
        }
    }


}
