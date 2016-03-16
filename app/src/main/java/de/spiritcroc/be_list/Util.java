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
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.Locale;

public abstract class Util {
    public static final boolean DEBUG = false;//to do false for release
    public static final boolean SCREENSHOT = false;//to do false for release

    // Resource helper methods that replace defaultBEUnit with setUpBEUnit
    private static String defaultBEUnit;
    private static String defaultBEUnitPl;
    private static String setUpBEUnit;
    private static String setUpBEUnitPl;
    private static String getDefaultBEUnit(Context context) {
        if (defaultBEUnit == null)
            defaultBEUnit = context.getString(R.string.default_be_unit);
        return defaultBEUnit;
    }
    public static String getSetUpBEUnit(Context context) {
        if (setUpBEUnit == null)
            setUpBEUnit = PreferenceManager.getDefaultSharedPreferences(context)
                    .getString("pref_be_unit", getDefaultBEUnit(context));
        return setUpBEUnit;
    }
    private static String getDefaultBEUnitPl(Context context) {
        if (defaultBEUnitPl == null)
            defaultBEUnitPl = context.getString(R.string.default_be_unit_pl);
        return defaultBEUnitPl;
    }
    public static String getSetUpBEUnitPl(Context context) {
        if (setUpBEUnitPl == null)
            setUpBEUnitPl = PreferenceManager.getDefaultSharedPreferences(context)
                    .getString("pref_be_unit_pl", getDefaultBEUnitPl(context));
        return setUpBEUnitPl;
    }
    public static void reload(Context context) {
        reloadSetUpBEUnit(context);
        reloadSetUpBEUnitPl(context);
    }
    public static void reloadSetUpBEUnit(Context context) {
        setUpBEUnit = PreferenceManager.getDefaultSharedPreferences(context)
                .getString("pref_be_unit", getDefaultBEUnit(context));
    }
    public static void reloadSetUpBEUnitPl(Context context) {
        setUpBEUnitPl = PreferenceManager.getDefaultSharedPreferences(context)
                .getString("pref_be_unit_pl", getDefaultBEUnitPl(context));
    }
    public static String firstLetterUppercase(String text) {
        String firstLetter = "" + text.charAt(0);
        return text.replaceFirst(firstLetter, firstLetter.toUpperCase());
    }

    public static String getSetUpString(Context context, String text) {
        if (getDefaultBEUnit(context).equals(getSetUpBEUnit(context)))
            return text;
        else
            return getSetUpString(context, getSetUpString(context, text, true), false);
    }
    public static String getSetUpString(Context context, String text, boolean plural) {
        if (getDefaultBEUnit(context).equals(getSetUpBEUnit(context)))
            return text;
        else if (plural)
            return text.replaceAll(getDefaultBEUnitPl(context), getSetUpBEUnitPl(context))
                    .replaceAll(firstLetterUppercase(getDefaultBEUnitPl(context)), firstLetterUppercase(getSetUpBEUnitPl(context)));
        else
            return text.replaceAll(getDefaultBEUnit(context), getSetUpBEUnit(context))
                    .replaceAll(firstLetterUppercase(getDefaultBEUnit(context)), firstLetterUppercase(getSetUpBEUnit(context)));
    }
    public static String getString(Context context, int resId) {
        String result = context.getString(resId);
        return getSetUpString(context, result);
    }
    public static String getQuantityString(Context context, int id, int quantity) {
        String result = context.getResources().getQuantityString(id, quantity);
        return getSetUpString(context, result, quantity != 1);
    }
    public static String getQuantityString(Context context, int id, int quantity, Object... formatArgs) {
        String result = context.getResources().getQuantityString(id, quantity, formatArgs);
        return getSetUpString(context, result, quantity != 1);
    }


    // Other util methods
    // Localize the settings if not set already
    public static boolean localize(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (!sharedPreferences.contains("pref_be_unit")){
            sharedPreferences.edit().putString("pref_be_unit",
                    context.getString(R.string.default_localized_be_unit)).apply();
            sharedPreferences.edit().putString("pref_be_unit_pl",
                    context.getString(R.string.default_localized_be_unit_pl)).apply();
            return true;
        }
        else {
            // Already localized
            return false;
        }
    }
    public static String roundToString(double d, int maxAfterPoint) {
        if (maxAfterPoint <= 0 || d % 1 == 0)
            return String.valueOf(Math.round(d));
        else {
            int afterPoint = 10;
            for (int i = 1; i < maxAfterPoint; i++)
                afterPoint *= 10;
            String result = String.valueOf((double) Math.round(d*afterPoint)/afterPoint);
            // Remove possible .0 to make output as short as possible
            String backup = result;
            boolean isAfterPoint = true;
            while (isAfterPoint && (result.charAt(result.length()-1) == '0' || result.charAt(result.length()-1) == '.')) {
                if (result.charAt(result.length()-1) == '.')
                    isAfterPoint = false;
                result = result.substring(0, result.length() - 1);
            }
            // No point found?
            if (isAfterPoint)
                result = backup;
            return result;
        }
    }
    public static int getDoubleQuantity(String text) {
        try {
            return Integer.parseInt(text);
        } catch (Exception e) {
            try {
                if (Double.parseDouble(text) == 1.0)
                    return 1;
                else
                    return 42;// Random plural
            } catch (Exception e1) {
                // Probably no number in text → use singular
                return 1;
            }
        }
    }

    public static ArrayList<BEEntryDisplay> getScreenshotList() {
        ArrayList<BEEntryDisplay> list = new ArrayList<>();
        long id = 0;
        switch (Locale.getDefault().toString()) {
            case "de_DE":
                list.add(new BEEntryDisplay(++id,
                        "Apfelkuchen", 0, 50, -1, "von Oma", true));
                list.add(new BEEntryDisplay(++id,
                        "Apfelsaft", 1, 100, -1, "", true));
                list.add(new BEEntryDisplay(++id,
                        "Backerbsen", 0, 20, -1, "", true));
                list.add(new BEEntryDisplay(++id,
                        "Baggers", 0, 50, -1, "", true));
                list.add(new BEEntryDisplay(++id,
                        "Bienenstich", 0, 20, -1, "", true));
                list.add(new BEEntryDisplay(++id,
                        "Breze", 0, 25, 3.5, "", true));
                list.add(new BEEntryDisplay(++id,
                        "Brot", 0, 50, -1, "", true));
                list.add(new BEEntryDisplay(++id,
                        "Erdbeerkuchen", 0, 20, -1, "", true));
                list.add(new BEEntryDisplay(++id,
                        "Gummibärchen", 0, 13, -1, "", true));
                list.add(new BEEntryDisplay(++id,
                        "Hirse", 0, 40, -1, "gekocht", true));
                list.add(new BEEntryDisplay(++id,
                        "Honigreiswaffeln", 0, -1, 0.9, "", true));
                list.add(new BEEntryDisplay(++id,
                        "Joghurt", 0, 70, -1, "60-80g = 1BE", true));
                break;
            default:
                list.add(new BEEntryDisplay(++id,
                        "Apple juice", 1, 10, -1, "", true));
                list.add(new BEEntryDisplay(++id,
                        "Apple pie", 0, 5, -1, "Made by grandma", true));
                list.add(new BEEntryDisplay(++id,
                        "Bee sting cake", 0, 2, -1, "", true));
                list.add(new BEEntryDisplay(++id,
                        "Bread", 0, 2.5, -1, "", true));
                list.add(new BEEntryDisplay(++id,
                        "Cheese", 0, -1, 0, "", true));
                list.add(new BEEntryDisplay(++id,
                        "Fried batter pearls", 0, 2, -1, "", true));
                list.add(new BEEntryDisplay(++id,
                        "Gummy bear", 0, 1.3, -1, "", true));
                list.add(new BEEntryDisplay(++id,
                        "Hot chocolate", 1, 12.5, 20, "", true));
                list.add(new BEEntryDisplay(++id,
                        "Pretzel", 0, 2.5, 35, "", true));
                list.add(new BEEntryDisplay(++id,
                        "Strawberry pie", 0, 2, -1, "", true));
                list.add(new BEEntryDisplay(++id,
                        "Yogurt", 0, 7, -1, "6-8g = 1 carb", true));
                break;
        }
        return list;
    }
}
