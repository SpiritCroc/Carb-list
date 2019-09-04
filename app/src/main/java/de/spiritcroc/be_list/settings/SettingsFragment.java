/*
 * Copyright (C) 2015-2016 SpiritCroc
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

package de.spiritcroc.be_list.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;

import de.spiritcroc.be_list.R;
import de.spiritcroc.be_list.Util;

public class SettingsFragment extends CustomPreferenceFragment {

    private EditTextPreference sizeMassPerCarbPref;
    private EditTextPreference sizeCarbsPerPiecePref;
    private ListPreference textColorMassPerCarbPref;
    private ListPreference textColorCarbPerPiecePref;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        sizeMassPerCarbPref = (EditTextPreference) findPreference(Keys.TEXT_SIZE_MASS_PER_CARB);
        sizeCarbsPerPiecePref = (EditTextPreference) findPreference(Keys.TEXT_SIZE_CARBS_PER_PIECE);
        textColorMassPerCarbPref = (ListPreference) findPreference(Keys.TEXT_COLOR_MASS_PER_CARB);
        textColorCarbPerPiecePref = (ListPreference) findPreference(Keys.TEXT_COLOR_CARBS_PER_PIECE);
    }

    @Override
    public void onResume() {
        super.onResume();
        init();
    }

    private void init() {
        setEditTextPreferenceSummary(Keys.TEXT_SIZE_NAME);
        setEditTextPreferenceSummary(sizeMassPerCarbPref);
        setEditTextPreferenceSummary(sizeCarbsPerPiecePref);
        setEditTextPreferenceSummary(Keys.TEXT_SIZE_COMMENT);
        setListPreferenceSummary(Keys.TEXT_COLOR);
        setListPreferenceSummary(Keys.TEXT_COLOR_UNCONFIRMED);
        setListPreferenceSummary(Keys.TEXT_COLOR_MASS_PER_CARB);
        setListPreferenceSummary(Keys.TEXT_COLOR_CARBS_PER_PIECE);
        setListPreferenceSummary(Keys.TEXT_COLOR_COMMENT);
        setListPreferenceSummary(Keys.TEXT_COLOR_DARK);
        setListPreferenceSummary(Keys.TEXT_COLOR_UNCONFIRMED_DARK);
        setListPreferenceSummary(Keys.TEXT_COLOR_MASS_PER_CARB_DARK);
        setListPreferenceSummary(Keys.TEXT_COLOR_CARBS_PER_PIECE_DARK);
        setListPreferenceSummary(Keys.TEXT_COLOR_COMMENT_DARK);

        setCustomizedUnitTitles();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case Keys.TEXT_SIZE_NAME:
            case Keys.TEXT_SIZE_MASS_PER_CARB:
            case Keys.TEXT_SIZE_CARBS_PER_PIECE:
            case Keys.TEXT_SIZE_COMMENT:
                setEditTextPreferenceSummary(key);
                break;
            case Keys.TEXT_COLOR:
            case Keys.TEXT_COLOR_UNCONFIRMED:
            case Keys.TEXT_COLOR_MASS_PER_CARB:
            case Keys.TEXT_COLOR_CARBS_PER_PIECE:
            case Keys.TEXT_COLOR_COMMENT:
            case Keys.TEXT_COLOR_DARK:
            case Keys.TEXT_COLOR_UNCONFIRMED_DARK:
            case Keys.TEXT_COLOR_MASS_PER_CARB_DARK:
            case Keys.TEXT_COLOR_CARBS_PER_PIECE_DARK:
            case Keys.TEXT_COLOR_COMMENT_DARK:
                setListPreferenceSummary(key);
                break;
        }
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

    private void setCustomizedUnitTitles() {
        Context context = getActivity();
        sizeMassPerCarbPref.setTitle(Util.getString(context, R.string.pref_grams_per_be_size));
        sizeCarbsPerPiecePref.setTitle(Util.getString(context, R.string.pref_be_per_piece_size));
        textColorMassPerCarbPref.setTitle(Util.getString(context, R.string.pref_text_color_weight_for_be));
        textColorCarbPerPiecePref.setTitle(Util.getString(context, R.string.pref_text_color_be_per_piece));
    }
}
