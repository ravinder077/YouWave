package com.tuespotsolutions.blacktube.util;

import android.os.Bundle;

import com.tuespotsolutions.blacktube.R;

public class FilePickerActivityHelper extends com.nononsenseapps.filepicker.FilePickerActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if(ThemeHelper.isLightThemeSelected(this)) {
            this.setTheme(R.style.FilePickerThemeLight);
        } else {
            this.setTheme(R.style.FilePickerThemeDark);
        }
        super.onCreate(savedInstanceState);
    }
}
