/*
 * Copyright 2013 Gerhard Klostermeier
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


package com.example.admin.myapplication.nfc;

import android.app.Activity;
import android.content.Intent;

import com.main.BaseApplication;

/**
 * An Activity implementing the NFC foreground dispatch system overwriting
 * onResume() and onPause(). New Intents will be treated as new Tags.
 * @see BaseApplication#enableNfcForegroundDispatch(Activity)
 * @see BaseApplication#disableNfcForegroundDispatch(Activity)
 * @see BaseApplication#treatAsNewTag(Intent, android.content.Context)
 * @author Gerhard Klostermeier
 *
 */
public abstract class BasicActivity extends Activity {

    /**
     * Enable NFC foreground dispatch system.
     * @see BaseApplication#disableNfcForegroundDispatch(Activity)
     */
    @Override
    public void onResume() {
        super.onResume();
        BaseApplication.enableNfcForegroundDispatch(this);
    }

    /**
     * Disable NFC foreground dispatch system.
     * @see BaseApplication#disableNfcForegroundDispatch(Activity)
     */
    @Override
    public void onPause() {
        super.onPause();
        BaseApplication.disableNfcForegroundDispatch(this);
    }

    /**
     * Handle new Intent as a new tag Intent and if the tag/device does not
     * support Mifare Classic, then run {@link TagInfoTool}.
     * @see BaseApplication#treatAsNewTag(Intent, android.content.Context)
     * @see TagInfoTool
     */
    @Override
    public void onNewIntent(Intent intent) {
        int typeCheck = BaseApplication.treatAsNewTag(intent, this);
        if (typeCheck == -1 || typeCheck == -2) {
            // Device or tag does not support Mifare Classic.
            // Run the only thing that is possible: The tag info tool.
            Intent i = new Intent(this, TagInfoTool.class);
            startActivity(i);
        }
    }
}
