/*
 * Copyright (C) 2015 SpiritCroc
 * Email: spiritcroc@gmail.com
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

package de.spiritcroc.be_list;

import android.content.Context;
import androidx.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.TextView;

public class PluralsWatcher implements TextWatcher {
    private TextView updateView;
    private Context context;
    private int pluralsResource;

    public PluralsWatcher(TextView updateView, Context context, int pluralsResource, @Nullable String initText) {
        this.updateView = updateView;
        this.context = context;
        this.pluralsResource = pluralsResource;
        if (initText != null)
            update(initText);
    }
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {}

    @Override
    public void afterTextChanged(Editable s) {
        update(s.toString());
    }

    private void update(String text){
        updateView.setText(Util.getQuantityString(context, pluralsResource, Util.getDoubleQuantity(text)));
    }
}
