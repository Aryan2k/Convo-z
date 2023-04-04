package com.example.convo_z.ui.home.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.convo_z.R;
import com.example.convo_z.adapters.StatusAdapters.MutedStatusAdapter;
import com.example.convo_z.adapters.StatusAdapters.RecentStatusAdapter;
import com.example.convo_z.adapters.StatusAdapters.ViewedStatusAdapter;
import com.example.convo_z.databinding.FragmentStatusBinding;
import com.example.convo_z.model.User;
import com.example.convo_z.ui.status.AddStatusPage;
import com.example.convo_z.ui.status.OwnStatusPage;
import com.example.convo_z.utils.Constants;
import com.example.convo_z.utils.Data;
import com.example.convo_z.utils.FunctionUtils;
import com.example.convo_z.utils.Resource;
import com.example.convo_z.viewmodel.home.fragments.StatusFragmentViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class StatusFragment extends Fragment {

    public StatusFragment() {
        // Required empty public constructor
    }

    FragmentStatusBinding binding;
    ArrayList<User> recentList = new ArrayList<>();
    ArrayList<User> viewedList = new ArrayList<>();
    ArrayList<User> mutedList = new ArrayList<>();
    private StatusFragmentViewModel viewModel;
    User loggedInUser;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentStatusBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(StatusFragmentViewModel.class);
        binding.mutedStatusRecyclerView.setVisibility(View.GONE);

        binding.recentStatusRecyclerView.setAdapter(new RecentStatusAdapter(recentList, getContext()));
        binding.viewedStatusRecyclerView.setAdapter(new ViewedStatusAdapter(viewedList, getContext()));
        binding.mutedStatusRecyclerView.setAdapter(new MutedStatusAdapter(mutedList, getContext()));

        setUpClickListeners();
        handleLoadUserLiveData();
        handleLoadAllStatusLiveData();

        viewModel.getUser(FirebaseAuth.getInstance().getUid(), this.getContext());

        return binding.getRoot();
    }

    private void setUpClickListeners() {
        binding.muted.setOnClickListener(view -> handleMuted(0));
        binding.ll1.setOnClickListener(view -> {
            Intent intent;
            if (loggedInUser.getStatus().size() > 1)
                intent = new Intent(StatusFragment.this.getContext(), OwnStatusPage.class);
            else
                intent = new Intent(StatusFragment.this.getContext(), AddStatusPage.class)
                        .putExtra(getString(R.string.previous_activity), Constants.CASE_HOME_ACTIVITY);
            intent.putExtra(getString(R.string.user), loggedInUser);
            startActivity(intent);
        });
    }

    private void handleLoadUserLiveData() {
        androidx.lifecycle.Observer<Resource<Data<User>>> observer = resource -> {
            switch (resource.getStatus()) {
                case LOADING:
                    break;
                case SUCCESS:
                    loggedInUser = resource.getData().get();
                    Picasso.get().load(loggedInUser.getProfilePic()).placeholder(R.drawable.ic_user).into(binding.profileImage);
                    if (loggedInUser.getStatus().size() > 1)  // checked with 1 because 0th entry is dummy
                    {
                        ArrayList<HashMap<String, Object>> status = loggedInUser.getStatus();
                        HashMap<String, Object> hMap = status.get(status.size() - 1);

                        binding.userNameList.setText(R.string.my_status);
                        assert hMap != null;
                        String time = (String) hMap.get(getString(R.string.time));

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            binding.time.setText(FunctionUtils.timeSetter(time));
                        }

                        Picasso.get().load((String) hMap.get(getString(R.string.link))).placeholder(R.drawable.ic_user).into(binding.profileImage);
                        binding.profileImage.setBorderColor(Color.WHITE);
                        binding.plus.setVisibility(View.GONE);
                        binding.profileImage.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.my_status_border));

                        setUpClickListeners();
                    }
                    viewModel.loadAllStatus(recentList, viewedList, mutedList, getContext());
                    break;
                case EXCEPTION:
                    // break;
            }
        };
        viewModel.LoadUserLiveData.observe(getViewLifecycleOwner(), observer);
    }

    private void handleLoadAllStatusLiveData() {
        @SuppressLint("NotifyDataSetChanged") androidx.lifecycle.Observer<Resource<Data<Boolean>>> observer = resource -> {
            switch (resource.getStatus()) {
                case LOADING:
                    break;
                case SUCCESS:
                    if (resource.getData().get()) {
                        if (recentList.isEmpty())
                            binding.recent.setText(R.string.no_recent_updates);
                        else
                            binding.recent.setText(R.string.new_updates);

                        if (viewedList.isEmpty())
                            binding.seen.setText(R.string.no_viewed_updates);
                        else
                            binding.seen.setText(R.string.viewed_updates);

                        if (mutedList.isEmpty()) {
                            binding.muted.setText(R.string.no_muted_updates);
                            binding.muted.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                        } else {
                            binding.muted.setText(R.string.muted_updates);
                            binding.muted.setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(requireContext(), R.drawable.ic_baseline_keyboard_arrow_down_24), null);
                        }
                    } else {
                        handleMuted(1);
                    }
                    break;
                case EXCEPTION:
                    // break;
            }
            Objects.requireNonNull(binding.recentStatusRecyclerView.getAdapter()).notifyDataSetChanged();  // notifies the adapter that a change has been made in the list (onBindViewHolder)
            Objects.requireNonNull(binding.viewedStatusRecyclerView.getAdapter()).notifyDataSetChanged();
            Objects.requireNonNull(binding.mutedStatusRecyclerView.getAdapter()).notifyDataSetChanged();
        };
        viewModel.LoadAllStatusLiveData.observe(getViewLifecycleOwner(), observer);
    }

    void handleMuted(int calledFrom) {
        if (!mutedList.isEmpty()) {   // don't view up/down arrow on tapping muted if mutedList is empty
            if (binding.mutedStatusRecyclerView.getVisibility() != View.VISIBLE && calledFrom != 1) {
                binding.mutedStatusRecyclerView.setVisibility(View.VISIBLE);
                binding.scrollview.post(() -> binding.scrollview.fullScroll(ScrollView.FOCUS_DOWN));
                binding.muted.setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(requireContext(), R.drawable.ic_baseline_keyboard_arrow_up_24), null);
            } else {
                binding.mutedStatusRecyclerView.setVisibility(View.GONE);
                binding.muted.setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(requireContext(), R.drawable.ic_baseline_keyboard_arrow_down_24), null);
            }
        }
    }
}