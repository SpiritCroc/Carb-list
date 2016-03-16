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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.widget.Toast;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    EditTextPreference sizeGramsPerBEPref, sizeBePerPiecePref;
    EditTextPreference precisionBEPerPiecePref, precisionPrimaryPref, precisionSecondaryPref, precisionTertiaryPref;
    EditTextPreference unitPrimaryPref, unitSecondaryPref, unitTertiaryPref;
    ListPreference textColorWeightForBEPref, textColorBEPerPiecePref;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        SharedPreferences sharedPreferences = getPreferenceManager().getSharedPreferences();

        sizeGramsPerBEPref = (EditTextPreference) findPreference("pref_grams_per_be_size");
        sizeBePerPiecePref = (EditTextPreference) findPreference("pref_be_per_piece_size");
        textColorWeightForBEPref = (ListPreference) findPreference("pref_text_color_weight_for_be");
        textColorBEPerPiecePref = (ListPreference) findPreference("pref_text_color_be_per_piece");

        precisionBEPerPiecePref = (EditTextPreference) findPreference("pref_be_per_piece_precisions");
        precisionPrimaryPref = (EditTextPreference) findPreference("pref_primary_precision");
        precisionSecondaryPref = (EditTextPreference) findPreference("pref_secondary_precision");
        precisionTertiaryPref = (EditTextPreference) findPreference("pref_tertiary_precision");
        unitPrimaryPref = (EditTextPreference) findPreference("pref_primary_unit");
        unitSecondaryPref = (EditTextPreference) findPreference("pref_secondary_unit");
        unitTertiaryPref = (EditTextPreference) findPreference("pref_tertiary_unit");

        setEditValueToSummary("pref_name_size");
        setEditValueToSummary("pref_grams_per_be_size");
        setEditValueToSummary("pref_be_per_piece_size");
        setEditValueToSummary("pref_comment_size");
        setListValueToSummary("pref_text_color");
        setListValueToSummary("pref_text_color_unconfirmed");
        setListValueToSummary("pref_text_color_weight_for_be");
        setListValueToSummary("pref_text_color_be_per_piece");
        setListValueToSummary("pref_text_color_comment");
        setEditValueToSummary("pref_be_unit");
        setEditValueToSummary("pref_be_unit_pl");
        setPrecisionSummary(precisionBEPerPiecePref,
                correctInteger(sharedPreferences, "pref_be_per_piece_precisions", precisionBEPerPiecePref.getText(), 0));
        setPrecisionSummary(precisionPrimaryPref,
                correctInteger(sharedPreferences, "pref_primary_precision", precisionPrimaryPref.getText(), 0));
        setPrecisionSummary(precisionSecondaryPref,
                correctInteger(sharedPreferences, "pref_secondary_precision", precisionSecondaryPref.getText(), 0));
        setPrecisionSummary(precisionTertiaryPref,
                correctInteger(sharedPreferences, "pref_tertiary_precision", precisionTertiaryPref.getText(), 0));

        setUnitSummaries(sharedPreferences);

        setCustomizedUnitSummaries();
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }
    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("pref_name_size") ||
                key.equals("pref_grams_per_be_size") ||
                key.equals("pref_be_per_piece_size") ||
                key.equals("pref_comment_size")) {
            EditTextPreference tmpEditTextPreference = (EditTextPreference) findPreference(key);
            tmpEditTextPreference.setSummary(String.valueOf(correctInteger(sharedPreferences, key, tmpEditTextPreference.getText(), 15)));
        } else if (key.equals("pref_primary_precision") ||
                key.equals("pref_secondary_precision") ||
                key.equals("pref_tertiary_precision")) {
            EditTextPreference tmpEditTextPreference = (EditTextPreference) findPreference(key);
            setPrecisionSummary(tmpEditTextPreference, correctInteger(sharedPreferences, key, tmpEditTextPreference.getText(), 0));
        } else if (key.equals("pref_be_per_piece_precisions")) {
            EditTextPreference tmpEditTextPreference = (EditTextPreference) findPreference(key);
            setPrecisionSummary(tmpEditTextPreference, correctInteger(sharedPreferences, key, tmpEditTextPreference.getText(), 1));
        } else if (key.equals("pref_primary_unit") ||
                key.equals("pref_secondary_unit") ||
                key.equals("pref_tertiary_unit")) {
            setUnitSummaries(sharedPreferences);
        } else if (key.equals("pref_text_color") ||
                key.equals("pref_text_color_unconfirmed") ||
                key.equals("pref_text_color_weight_for_be") ||
                key.equals("pref_text_color_be_per_piece") ||
                key.equals("pref_text_color_comment"))
            setListValueToSummary(key);
        else if (key.equals("pref_be_unit")) {
            Util.reloadSetUpBEUnit(getActivity());
            setEditValueToSummary(key);
            setCustomizedUnitSummaries();
        }
        else if (key.equals("pref_be_unit_pl")) {
            Util.reloadSetUpBEUnitPl(getActivity());
            setEditValueToSummary(key);
            setCustomizedUnitSummaries();
        }
    }

    private void setEditValueToSummary(String key) {
        EditTextPreference tmpEditTextPreference = (EditTextPreference) findPreference(key);
        tmpEditTextPreference.setSummary(tmpEditTextPreference.getText());
    }
    private void setListValueToSummary (String key) {
        ListPreference preference = (ListPreference) findPreference(key);
        preference.setSummary(preference.getEntry());
    }

    private int correctInteger(SharedPreferences sharedPreferences, String key, String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(key, String.valueOf(defaultValue));
            editor.apply();
            return defaultValue;
        }
    }
    private void setPrecisionSummary(Preference preference, int precision) {
        preference.setSummary(getResources().getQuantityString(R.plurals.pref_precisions_summary,
                precision, precision));
    }
    private void setCustomizedUnitSummaries() {
        Context context = getActivity();
        sizeGramsPerBEPref.setTitle(Util.getString(context, R.string.pref_grams_per_be_size));
        sizeBePerPiecePref.setTitle(Util.getString(context, R.string.pref_be_per_piece_size));
        precisionBEPerPiecePref.setTitle(Util.getString(context, R.string.pref_be_per_piece_precisions));
        textColorWeightForBEPref.setTitle(Util.getString(context, R.string.pref_text_color_weight_for_be));
        textColorBEPerPiecePref.setTitle(Util.getString(context, R.string.pref_text_color_be_per_piece));
    }
    private void setUnitSummaries(SharedPreferences sharedPreferences) {
        String primaryUnit = unitPrimaryPref.getText(),
                secondaryUnit = unitSecondaryPref.getText(),
                tertiaryUnit = unitTertiaryPref.getText();
        if (primaryUnit.equals("")) {
            // Should not be empty
            primaryUnit = getResources().getStringArray(R.array.auto_complete_unit)[0];
            sharedPreferences.edit().putString("pref_primary_unit", primaryUnit).apply();
            unitPrimaryPref.setText(primaryUnit);
            Toast.makeText(getActivity(), R.string.toast_primary_unit_required, Toast.LENGTH_SHORT).show();
        }
        if (secondaryUnit.equals("")) {
            tertiaryUnit = "";
            precisionSecondaryPref.setEnabled(false);
            unitTertiaryPref.setEnabled(false);
        } else {
            precisionSecondaryPref.setEnabled(true);
            unitTertiaryPref.setEnabled(true);
        }
        if (tertiaryUnit.equals("")) {
            precisionTertiaryPref.setEnabled(false);
        } else {
            precisionTertiaryPref.setEnabled(true);
        }
        unitPrimaryPref.setSummary(primaryUnit);
        unitSecondaryPref.setSummary(secondaryUnit);
        unitTertiaryPref.setSummary(tertiaryUnit);
    }
}
