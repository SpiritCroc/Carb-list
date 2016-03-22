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

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.widget.Toast;

import de.spiritcroc.be_list.R;
import de.spiritcroc.be_list.Util;

public class SettingsUnitsFragment extends CustomPreferenceFragment {

    private EditTextPreference precisionCarbPerPiecePref;
    private EditTextPreference unitPrimaryPref;
    private EditTextPreference unitSecondaryPref;
    private EditTextPreference unitTertiaryPref;
    private EditTextPreference precisionPrimaryPref;
    private EditTextPreference precisionSecondaryPref;
    private EditTextPreference precisionTertiaryPref;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences_units);

        precisionCarbPerPiecePref = (EditTextPreference) findPreference(Keys.CARB_PER_PIECE_PRECISION);
        unitPrimaryPref = (EditTextPreference) findPreference(Keys.MASS_PRIMARY_UNIT);
        unitSecondaryPref = (EditTextPreference) findPreference(Keys.MASS_SECONDARY_UNIT);
        unitTertiaryPref = (EditTextPreference) findPreference(Keys.MASS_TERTIARY_UNIT);
        precisionPrimaryPref = (EditTextPreference) findPreference(Keys.MASS_PRIMARY_PRECISION);
        precisionSecondaryPref = (EditTextPreference) findPreference(Keys.MASS_SECONDARY_PRECISION);
        precisionTertiaryPref = (EditTextPreference) findPreference(Keys.MASS_TERTIARY_PRECISION);
    }

    @Override
    public void onResume() {
        super.onResume();
        init();
    }

    private void init() {
        setEditTextPreferenceSummary(Keys.CARB_UNIT);
        setEditTextPreferenceSummary(Keys.CARB_UNIT_PL);
        setPrecisionSummary(precisionCarbPerPiecePref, correctInteger(Keys.CARB_PER_PIECE_PRECISION,
                precisionCarbPerPiecePref.getText(), 0));
        setPrecisionSummary(precisionPrimaryPref, correctInteger(Keys.MASS_PRIMARY_PRECISION,
                precisionPrimaryPref.getText(), 0));
        setPrecisionSummary(precisionSecondaryPref, correctInteger(Keys.MASS_SECONDARY_PRECISION,
                precisionSecondaryPref.getText(), 0));
        setPrecisionSummary(precisionTertiaryPref, correctInteger(Keys.MASS_TERTIARY_PRECISION,
                precisionTertiaryPref.getText(), 0));
        setUnitSummaries(getPreferenceScreen().getSharedPreferences());
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case Keys.CARB_UNIT:
            case Keys.CARB_UNIT_PL:
                Util.reloadSetUpBEUnitPl(getActivity());
                setEditTextPreferenceSummary(key);
                break;
            case Keys.MASS_PRIMARY_UNIT:
            case Keys.MASS_SECONDARY_UNIT:
            case Keys.MASS_TERTIARY_UNIT:
                setUnitSummaries(sharedPreferences);
                break;
            case Keys.CARB_PER_PIECE_PRECISION:
                setPrecisionSummary(precisionCarbPerPiecePref, correctInteger(
                        Keys.CARB_PER_PIECE_PRECISION, precisionCarbPerPiecePref.getText(), 0));
                break;
            case Keys.MASS_PRIMARY_PRECISION:
                setPrecisionSummary(precisionPrimaryPref, correctInteger(
                        Keys.MASS_PRIMARY_PRECISION, precisionPrimaryPref.getText(), 0));
                break;
            case Keys.MASS_SECONDARY_PRECISION:
                setPrecisionSummary(precisionSecondaryPref, correctInteger(
                        Keys.MASS_SECONDARY_PRECISION, precisionSecondaryPref.getText(), 0));
                break;
            case Keys.MASS_TERTIARY_PRECISION:
                setPrecisionSummary(precisionTertiaryPref, correctInteger(
                        Keys.MASS_TERTIARY_PRECISION, precisionTertiaryPref.getText(), 0));
                break;
        }
    }

    private void setPrecisionSummary(Preference preference, int precision) {
        preference.setSummary(getResources().getQuantityString(R.plurals.pref_precisions_summary,
                precision, precision));
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
