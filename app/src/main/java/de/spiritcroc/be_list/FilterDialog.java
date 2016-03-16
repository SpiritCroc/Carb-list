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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;

import java.util.Arrays;

public class FilterDialog extends DialogFragment {
    private static final String ARG_SHOW_CONFIRMED = "showConfirmed";
    private static final String ARG_SHOW_NOT_CONFIRMED = "showNotConfirmed";
    private static final String ARG_SORT_ORDER = "sortOrder";

    private ListActivity callback;
    private boolean showConfirmed, showNotConfirmed;
    String sortOrder;

    private CheckBox checkShowConfirmed, checkShowNotConfirmed;

    public FilterDialog setCallback(ListActivity callback) {
        this.callback = callback;
        return this;
    }
    public static FilterDialog newInstance(ListActivity callback, boolean showConfirmed, boolean showNotConfirmed, String sortOrder) {
        Bundle args = new Bundle();
        FilterDialog fragment = new FilterDialog();
        args.putBoolean(ARG_SHOW_CONFIRMED, showConfirmed);
        args.putBoolean(ARG_SHOW_NOT_CONFIRMED, showNotConfirmed);
        args.putString(ARG_SORT_ORDER, sortOrder);
        fragment.setArguments(args);
        return fragment.setCallback(callback);
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() == null){
            Log.e("FilterDialog", "Could not get arguments");
        } else {
            showConfirmed = getArguments().getBoolean(ARG_SHOW_CONFIRMED);
            showNotConfirmed = getArguments().getBoolean(ARG_SHOW_NOT_CONFIRMED);
            sortOrder = getArguments().getString(ARG_SORT_ORDER);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Activity activity = getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        final View view = activity.getLayoutInflater().inflate(R.layout.dialog_filter, null);
        checkShowConfirmed = (CheckBox) view.findViewById(R.id.show_confirmed);
        checkShowConfirmed.setChecked(showConfirmed);
        checkShowNotConfirmed = (CheckBox) view.findViewById(R.id.show_not_confirmed);
        checkShowNotConfirmed.setChecked(showNotConfirmed);
        view.findViewById(R.id.show_confirmed_text).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkShowConfirmed.setChecked(!checkShowConfirmed.isChecked());
            }
        });
        view.findViewById(R.id.show_not_confirmed_text).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkShowNotConfirmed.setChecked(!checkShowNotConfirmed.isChecked());
            }
        });
        final Spinner sortOrderSpinner = (Spinner) view.findViewById(R.id.sort_order_spinner);
        sortOrderSpinner.setAdapter(new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_dropdown_item,
                getResources().getStringArray(R.array.sort_order_array)));
        final String[] sortOrderValues = getResources().getStringArray(R.array.sort_order_value_array);
        sortOrderSpinner.setSelection(Arrays.asList(sortOrderValues).indexOf(sortOrder));

        return builder.setView(view)
                .setTitle(R.string.dialog_filter_title)
                .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        callback.setFilter(checkShowConfirmed.isChecked(),
                                checkShowNotConfirmed.isChecked(),
                                ListActivity.SortOrder.valueOf(sortOrderValues[sortOrderSpinner.getSelectedItemPosition()]));
                    }
                })
                .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Only close dialog
                    }
                }).create();
    }
}
