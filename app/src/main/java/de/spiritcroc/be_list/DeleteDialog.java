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
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;

public class DeleteDialog extends DialogFragment{
    private static final String ARG_DATABASE_ID  = "databaseID";
    private static final String ARG_NAME  = "name";

    private long databaseID;
    private String name;

    public static DeleteDialog newInstance (long databaseID, String name){
        Bundle args = new Bundle();
        DeleteDialog fragment = new DeleteDialog();
        args.putLong(ARG_DATABASE_ID, databaseID);
        args.putString(ARG_NAME, name);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        if (getArguments() == null){
            Log.e("DeleteDialog", "Could not get arguments");
            name = "";
        }
        else{
            databaseID = getArguments().getLong(ARG_DATABASE_ID);
            name = getArguments().getString(ARG_NAME);
        }
    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.dialog_delete_title)
                .setMessage(getString(R.string.dialog_delete_entry_pre) + name  + getString(R.string.dialog_delete_entry_post))
                .setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        BEEntryDbHelper dbHelper = new BEEntryDbHelper(getActivity());
                        SQLiteDatabase db = dbHelper.getWritableDatabase();
                        String selection = BEContract.BEEntry._ID + " LIKE ?";
                        String[] selectionArgs = {String.valueOf(databaseID)};
                        db.delete(BEContract.BEEntry.TABLE_NAME, selection, selectionArgs);
                        Activity activity = getActivity();
                        if (activity instanceof ListActivity)
                            ((ListActivity) activity).loadContent(true);
                    }
                })
                .setNegativeButton(R.string.dialog_no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //don't change anything
                    }
                });
        return builder.create();
    }
}
