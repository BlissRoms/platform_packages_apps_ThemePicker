/*
 * Copyright (C) 2019 The Android Open Source Project
 * Copyright (C) 2019-2020 The BlissRoms Project
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
package com.android.customization.picker;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.android.customization.model.volume.BaseVolumeDialogManager;
import com.android.customization.model.volume.VolumeDialogInfo;
import com.android.customization.model.volume.ContentProviderVolumeDialogProvider;
import com.android.customization.picker.volume.VolumeFragment;
import com.android.customization.picker.volume.VolumeFragment.VolumeFragmentHost;
import com.android.wallpaper.R;

/**
 * Activity allowing for the clock face picker to be linked to from other setup flows.
 *
 * This should be used with startActivityForResult. The resulting intent contains an extra
 * "clock_face_name" with the id of the picked clock face.
 */
public class VolumeDialogPickerActivity extends FragmentActivity implements VolumeFragmentHost {

    private static final String EXTRA_VOLUME_DIALOG_NAME = "volume_dialog_name";

    private BaseVolumeDialogManager mVolumeManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_volume_dialog_picker);

        // Creating a class that overrides {@link ClockManager#apply} to return the clock id to the
        // calling activity instead of putting the value into settings.
        //
        mVolumeManager = new BaseVolumeDialogManager(
                new ContentProviderVolumeDialogProvider(VolumeDialogPickerActivity.this)) {

            @Override
            protected void handleApply(VolumeDialogInfo option, Callback callback) {
                Intent result = new Intent();
                result.putExtra(EXTRA_VOLUME_DIALOG_NAME, option.getId());
                setResult(RESULT_OK, result);
                callback.onSuccess();
                finish();
            }

            @Override
            protected String lookUpCurrentVolumeDialog() {
                return getIntent().getStringExtra(EXTRA_VOLUME_DIALOG_NAME);
            }
        };
        if (!mVolumeManager.isAvailable()) {
            finish();
        } else {
            final FragmentManager fm = getSupportFragmentManager();
            final FragmentTransaction fragmentTransaction = fm.beginTransaction();
            final VolumeFragment volumeFragment = VolumeFragment.newInstance(
                    getString(R.string.volume_title));
            fragmentTransaction.replace(R.id.fragment_container, volumeFragment);
            fragmentTransaction.commitNow();
        }
    }

    @Override
    public BaseVolumeDialogManager getCurrentVolumeDialog() {
        return mVolumeManager;
    }
}
