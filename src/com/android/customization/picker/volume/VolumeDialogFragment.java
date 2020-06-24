/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.customization.picker.volume;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.recyclerview.widget.RecyclerView;

import com.android.customization.model.CustomizationManager.Callback;
import com.android.customization.model.CustomizationManager.OptionsFetchedListener;
import com.android.customization.model.volume.BaseVolumeDialogManager;
import com.android.customization.model.volume.VolumeDialogInfo;
import com.android.customization.module.ThemesUserEventLogger;
import com.android.customization.picker.BasePreviewAdapter;
import com.android.customization.picker.BasePreviewAdapter.PreviewPage;
import com.android.customization.widget.OptionSelectorController;
import com.android.customization.widget.PreviewPager;
import com.android.wallpaper.R;
import com.android.wallpaper.asset.Asset;
import com.android.wallpaper.module.InjectorProvider;
import com.android.wallpaper.picker.ToolbarFragment;

import java.util.List;

/**
 * Fragment that contains the main UI for selecting and applying a Clockface.
 */
public class VolumeDialogFragment extends ToolbarFragment {

    private static final String TAG = "VolumeDialogFragment";

    /**
     * Interface to be implemented by an Activity hosting a {@link ClockFragment}
     */
    public interface VolumeDialogFragmentHost {
        BaseVolumeDialogManager getCurrentVolumeDialog();
    }

    public static VolumeDialogFragment newInstance(CharSequence title) {
        VolumeDialogFragment fragment = new VolumeDialogFragment();
        fragment.setArguments(ToolbarFragment.createArguments(title));
        return fragment;
    }

    private RecyclerView mOptionsContainer;
    private OptionSelectorController<VolumeDialogInfo> mOptionsController;
    private VolumeDialogInfo mSelectedOption;
    private BaseVolumeDialogManager mVolumeManager;
    private ContentLoadingProgressBar mLoading;
    private View mContent;
    private View mError;
    private ThemesUserEventLogger mEventLogger;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mVolumeManager = ((VolumeDialogFragmentHost) context).getCurrentVolumeDialog();
        mEventLogger = (ThemesUserEventLogger)
                InjectorProvider.getInjector().getUserEventLogger(context);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(
                R.layout.fragment_volume_dialog_picker, container, /* attachToRoot */ false);
        setUpToolbar(view);
        mContent = view.findViewById(R.id.content_section);
        mOptionsContainer = view.findViewById(R.id.options_container);
        mLoading = view.findViewById(R.id.loading_indicator);
        mError = view.findViewById(R.id.error_section);
        setUpOptions();
        view.findViewById(R.id.apply_button).setOnClickListener(v -> {
            mVolumeManager.apply(mSelectedOption, new Callback() {
                @Override
                public void onSuccess() {
                    mOptionsController.setAppliedOption(mSelectedOption);
                    Toast.makeText(getContext(), R.string.applied_clock_msg,
                            Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(@Nullable Throwable throwable) {
                    if (throwable != null) {
                        Log.e(TAG, "Error loading volume dialogs", throwable);
                    }
                    //TODO(santie): handle
                }
            });

        });
        return view;
    }

    private void setUpOptions() {
        hideError();
        mLoading.show();
        mVolumeManager.fetchOptions(new OptionsFetchedListener<VolumeDialogInfo>() {
           @Override
           public void onOptionsLoaded(List<VolumeDialogInfo> options) {
               mLoading.hide();
               mOptionsController = new OptionSelectorController<>(mOptionsContainer, options);

               mOptionsController.addListener(selected -> {
                   mSelectedOption = (VolumeDialogInfo) selected;
                   mEventLogger.logVolumeDialogSelected(mSelectedOption);
               });
               mOptionsController.initOptions(mVolumeManager);
               for (VolumeDialogInfo option : options) {
                   if (option.isActive(mVolumeManager)) {
                       mSelectedOption = option;
                   }
               }
               // For development only, as there should always be a grid set.
               if (mSelectedOption == null) {
                   mSelectedOption = options.get(0);
               }
           }
           @Override
            public void onError(@Nullable Throwable throwable) {
                if (throwable != null) {
                   Log.e(TAG, "Error loading VolumeDialogInfos", throwable);
                }
                showError();
            }
       }, false);
    }

    private void hideError() {
        mContent.setVisibility(View.VISIBLE);
        mError.setVisibility(View.GONE);
    }

    private void showError() {
        mLoading.hide();
        mContent.setVisibility(View.GONE);
        mError.setVisibility(View.VISIBLE);
    }
}
