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

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

public class RandomEntryDialog extends DialogFragment {
    private ListActivity listActivity;

    public RandomEntryDialog setListActivity(ListActivity listActivity) {
        this.listActivity = listActivity;
        return this;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final BEEntryDisplay entry = listActivity.getRandomBEEntry();
        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.dialog_random_entry_title)
                .setMessage(entry.getName())
                .setPositiveButton(R.string.dialog_show, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        CalculatorDialog.newInstance(entry.getDatabaseID(), entry.getName(),
                                entry.getWeightUnit(),
                                listActivity.getUnit(entry.getWeightUnit()),
                                entry.getWeightPerBE(), entry.getBEPerPiece(),
                                entry.getComment(), entry.getConfirmed())
                                .show(getFragmentManager(), "CalculatorDialog");
                    }
                })
                .setNegativeButton(R.string.dialog_close, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Only close
                    }
                })
                .setNeutralButton(R.string.dialog_new_random, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new RandomEntryDialog().setListActivity(listActivity).show(getFragmentManager(), "RandomEntryDialog");
                    }
                })
                .create();
    }
}
