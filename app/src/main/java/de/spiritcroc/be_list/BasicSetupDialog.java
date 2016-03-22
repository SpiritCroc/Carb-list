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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.util.Arrays;
import java.util.List;

import de.spiritcroc.be_list.settings.Keys;

public class BasicSetupDialog extends DialogFragment {
    private LinearLayout secondaryLayout, tertiaryLayout;
    private AutoCompleteTextView editUnit, editPrimary, editSecondary, editTertiary;
    private EditText editUnitPlural;
    private Button unitButton, primaryButton, secondaryButton, tertiaryButton;
    private String[] beUnits, beUnitPlurals;
    private boolean beUnitClicked = false;
    private SharedPreferences sharedPreferences;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        final Activity activity = getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        View view = activity.getLayoutInflater().inflate(R.layout.dialog_basic_setup, null);

        editUnit = (AutoCompleteTextView) view.findViewById(R.id.edit_be_unit);
        editUnitPlural = (EditText) view.findViewById(R.id.edit_be_unit_plural);
        editPrimary = (AutoCompleteTextView) view.findViewById(R.id.edit_primary_unit);
        editSecondary = (AutoCompleteTextView) view.findViewById(R.id.edit_secondary_unit);
        editTertiary = (AutoCompleteTextView) view.findViewById(R.id.edit_tertiary_unit);
        secondaryLayout = (LinearLayout) view.findViewById(R.id.secondary_layout);
        tertiaryLayout = (LinearLayout) view.findViewById(R.id.tertiary_layout);
        unitButton = (Button) view.findViewById(R.id.be_unit_button);
        primaryButton = (Button) view.findViewById(R.id.primary_unit_button);
        secondaryButton = (Button) view.findViewById(R.id.secondary_unit_button);
        tertiaryButton = (Button) view.findViewById(R.id.tertiary_unit_button);
        secondaryLayout.setVisibility(View.GONE);
        tertiaryLayout.setVisibility(View.GONE);
        unitButton.setVisibility(View.GONE);

        beUnits = getResources().getStringArray(R.array.auto_complete_be_unit);
        beUnitPlurals = getResources().getStringArray(R.array.auto_complete_be_unit_plural);
        editUnit.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_dropdown_item_1line, beUnits));
        final String[] weightUnits = getResources().getStringArray(R.array.auto_complete_unit);
        editPrimary.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_dropdown_item_1line, weightUnits));
        editSecondary.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_dropdown_item_1line, weightUnits));
        editTertiary.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_dropdown_item_1line, weightUnits));

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        editUnit.setText(sharedPreferences.getString(Keys.CARB_UNIT, getString(R.string.default_localized_be_unit)));
        editUnitPlural.setText(sharedPreferences.getString(Keys.CARB_UNIT_PL, getString(R.string.default_localized_be_unit_pl)));

        // Select correct plural when selecting a unit from dropdown
        editUnit.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                beUnitClicked = true;
                int pos = Arrays.asList(beUnits).indexOf(editUnit.getText().toString());
                if (pos < beUnitPlurals.length)
                    editUnitPlural.setText(beUnitPlurals[pos]);
                else
                    Log.e("BasicSetupDialog", "editUnit.onItemClick: no beUnitPlurals entry for position " + position);
            }
        });

        // TextWatchers
        editUnit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (beUnitClicked) {
                    beUnitClicked = false;
                } else {
                    editUnitPlural.setText(s);
                }
                unitButton.setVisibility(s.toString().equals("") ? View.VISIBLE : View.GONE);
            }
        });
        editPrimary.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                boolean empty = s.toString().equals("");
                secondaryLayout.setVisibility(empty ? View.GONE : View.VISIBLE);
                primaryButton.setVisibility(empty ? View.VISIBLE : View.GONE);
            }
        });
        editSecondary.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                boolean empty = s.toString().equals("");
                tertiaryLayout.setVisibility(empty ? View.GONE : View.VISIBLE);
                secondaryButton.setVisibility(empty ? View.VISIBLE : View.GONE);
            }
        });
        editTertiary.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                tertiaryButton.setVisibility(s.toString().equals("") ? View.VISIBLE : View.GONE);
            }
        });

        // Dropdown buttons:
        unitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editUnit.showDropDown();
            }
        });
        primaryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editPrimary.showDropDown();
            }
        });
        secondaryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editSecondary.showDropDown();
            }
        });
        tertiaryButton.findViewById(R.id.tertiary_unit_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editTertiary.showDropDown();
            }
        });

        final AlertDialog alertDialog = builder.setView(view)
                .setTitle(R.string.dialog_basic_setup)
                .setCancelable(false)
                .setPositiveButton(R.string.dialog_ok, null)
                .setNeutralButton(R.string.dialog_use_default, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Close dialog
                    }
                }).create();

        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                editUnit.dismissDropDown();
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String unit = editUnit.getText().toString(),
                                        unitPl = editUnitPlural.getText().toString(),
                                        primary = editPrimary.getText().toString(),
                                        secondary = editSecondary.getText().toString(),
                                        tertiary = editTertiary.getText().toString();
                                boolean success = true;
                                if (unit.equals("")) {
                                    editUnit.setError(getString(R.string.required));
                                    success = false;
                                }
                                if (unitPl.equals("")) {
                                    editUnitPlural.setError(getString(R.string.required));
                                    success = false;
                                }
                                if (primary.equals("")) {
                                    editPrimary.setError(getString(R.string.required));
                                    success = false;
                                }
                                if (secondary.equals("")) {
                                    tertiary = "";
                                }
                                if (success) {
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putString(Keys.CARB_UNIT, unit)
                                            .putString(Keys.CARB_UNIT_PL, unitPl)
                                            .putString(Keys.MASS_PRIMARY_UNIT, primary)
                                            .putString(Keys.MASS_SECONDARY_UNIT, secondary)
                                            .putString(Keys.MASS_TERTIARY_UNIT, tertiary);
                                    List weightUnitList = Arrays.asList(weightUnits);
                                    String[] weightUnitPrecisions = getResources().getStringArray(R.array.auto_complete_unit_precisions);
                                    //Try to set precisions:
                                    try {
                                        editor.putString(Keys.MASS_PRIMARY_PRECISION, weightUnitPrecisions[weightUnitList.indexOf(primary)]);
                                    } catch (Exception e){}
                                    try {
                                        editor.putString(Keys.MASS_SECONDARY_PRECISION, weightUnitPrecisions[weightUnitList.indexOf(secondary)]);
                                    } catch (Exception e){}
                                    try {
                                        editor.putString(Keys.MASS_TERTIARY_PRECISION, weightUnitPrecisions[weightUnitList.indexOf(tertiary)]);
                                    } catch (Exception e){}
                                    editor.apply();
                                    dismiss();
                                    Util.reload(activity);
                                    if (activity instanceof ListActivity)
                                        ((ListActivity)activity).checkPreferences();
                                }
                            }
                        }
                );
            }
        });
        return alertDialog;
    }

    @Override
    public void onCancel(DialogInterface dialogInterface){
        // This dialog should not be canceled
        new BasicSetupDialog().show(getFragmentManager(), "BasicSetupDialog");
    }
}
