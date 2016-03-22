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
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;

import de.spiritcroc.be_list.settings.Keys;

public class EditDialog extends DialogFragment {
    private static final String ARG_DATABASE_ID  = "databaseID";
    private static final String ARG_NAME  = "name";
    private static final String ARG_WEIGHT  = "weight";
    private static final String ARG_BE_PER_PIECE  = "bePerPiece";
    private static final String ARG_COMMENT  = "comment";
    private static final String ARG_CONFIRMED = "confirmed";
    private static final String ARG_COPY_ENTRY = "copyEntry";
    private static final String ARG_NEW_ENTRY = "newEntry";
    private static final String ARG_UNIT = "unit";

    private long databaseID;
    private String name, comment;
    private double weight, bePerPiece;
    private boolean confirmed, copyEntry, newEntry;
    private int unit;

    private double enteredWeight, enteredBeForWeight, enteredBeForPieces, enteredPiecesForBE;
    private boolean hideCalculateUp = false, hideCalculateDown = false;

    private CheckBox confirmedToggle;
    private EditText editName, editComment, editWeight, editBeForWeight, editBeForPieces, editPiecesForBe;
    private TextView beForWeightView, beForPiecesView, piecesForBeView;
    private Spinner unitSpinner;
    private ArrayList<String> units;
    private LinearLayout calculateLayout;
    private Button calculateUpButton, calculateDownButton;

    public static EditDialog newAddDialogInstance() {
        Bundle args = new Bundle();
        EditDialog fragment = new EditDialog();
        args.putBoolean(ARG_NEW_ENTRY, true);
        fragment.setArguments(args);
        return fragment;
    }

    public static EditDialog newInstance (boolean copyEntry, long databaseID, String name,
                                          int weightUnit, double weightPerBE, double bePerPiece,
                                          String comment, boolean confirmed) {
        Bundle args = new Bundle();
        EditDialog fragment = new EditDialog();
        args.putBoolean(ARG_NEW_ENTRY, false);
        args.putBoolean(ARG_COPY_ENTRY, copyEntry);
        args.putLong(ARG_DATABASE_ID, databaseID);
        args.putString(ARG_NAME, name);
        args.putInt(ARG_UNIT, weightUnit);
        args.putDouble(ARG_WEIGHT, weightPerBE);
        args.putDouble(ARG_BE_PER_PIECE, bePerPiece);
        args.putString(ARG_COMMENT, comment);
        args.putBoolean(ARG_CONFIRMED, confirmed);
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() == null) {
            Log.e("EditDialog", "Could not get arguments");
        } else {
            newEntry = getArguments().getBoolean(ARG_NEW_ENTRY);
            if (!newEntry) {
                copyEntry = getArguments().getBoolean(ARG_COPY_ENTRY);
                databaseID = getArguments().getLong(ARG_DATABASE_ID);
                name = getArguments().getString(ARG_NAME);
                unit = getArguments().getInt(ARG_UNIT);
                weight = getArguments().getDouble(ARG_WEIGHT);
                bePerPiece = getArguments().getDouble(ARG_BE_PER_PIECE);
                comment = getArguments().getString(ARG_COMMENT);
                confirmed = getArguments().getBoolean(ARG_CONFIRMED);
            }
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Activity activity = getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        final View view = activity.getLayoutInflater().inflate(R.layout.dialog_add, null);
        beForWeightView = (TextView) view.findViewById(R.id.view_be_for_weight);
        beForPiecesView = (TextView) view.findViewById(R.id.view_be_for_pieces);
        piecesForBeView = (TextView) view.findViewById(R.id.view_pieces_for_be);
        confirmedToggle = ((CheckBox) view.findViewById(R.id.edit_confirmed));
        editName = (EditText) view.findViewById(R.id.edit_name);
        editComment = (EditText) view.findViewById(R.id.edit_comment);
        editWeight = (EditText) view.findViewById(R.id.edit_weight);
        editBeForWeight = (EditText) view.findViewById(R.id.edit_be_for_weight);
        editBeForPieces = (EditText) view.findViewById(R.id.edit_be_for_pieces);
        editPiecesForBe = (EditText) view.findViewById(R.id.edit_pieces_for_be);
        unitSpinner = (Spinner) view.findViewById(R.id.unit_spinner);
        calculateLayout = (LinearLayout) view.findViewById(R.id.calculate_missing_layout);
        calculateUpButton = (Button) view.findViewById(R.id.calculate_up_button);
        calculateDownButton = (Button) view.findViewById(R.id.calculate_down_button);
        view.findViewById(R.id.text_confirmed).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmedToggle.setChecked(!confirmedToggle.isChecked());
            }
        });
        calculateUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String unit = units.get(unitSpinner.getSelectedItemPosition());
                double beForPieces = enteredBeForPieces;
                if (beForPieces > 0)
                    beForPieces /= enteredPiecesForBE;
                else
                    beForPieces = 1 / enteredPiecesForBE;
                CalculateMissingDialog.pieceToWeight(EditDialog.this, beForPieces, unit).show(getFragmentManager(), "PieceToWeightDialog");
            }
        });
        calculateDownButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String unit = units.get(unitSpinner.getSelectedItemPosition());
                CalculateMissingDialog.weightToPiece(EditDialog.this, enteredWeight / enteredBeForWeight, unit).show(getFragmentManager(), "PieceToWeightDialog");
            }
        });

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        units = new ArrayList<>();
        units.add(sharedPreferences.getString(Keys.MASS_PRIMARY_UNIT, "g"));
        String tmpString = sharedPreferences.getString(Keys.MASS_SECONDARY_UNIT, "");
        if (!tmpString.equals("")){
            units.add(tmpString);
            tmpString = sharedPreferences.getString(Keys.MASS_TERTIARY_UNIT, "");
            if (!tmpString.equals(""))
                units.add(tmpString);
        }
        unitSpinner.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, units));

        if (!newEntry) {
            editName.setText(name);
            if (comment != null && !comment.equals(""))
                editComment.setText(comment);
            if (weight > 0) {
                if (weight % 1 == 0)
                    editWeight.setText(String.valueOf((int) weight));
                else
                    editWeight.setText(String.valueOf(weight));
            }
            if (bePerPiece >= 0) {
                if (bePerPiece % 1 == 0)
                    editBeForPieces.setText(String.valueOf((int) bePerPiece));
                else
                    editBeForPieces.setText(String.valueOf(bePerPiece));
            }
            confirmedToggle.setChecked(confirmed);
            if (unit > 0 && unit < units.size())
                unitSpinner.setSelection(unit);
        } else {
            // Set default values
            confirmedToggle.setChecked(true);
        }

        // Keep units up to date
        editBeForWeight.addTextChangedListener(new PluralsWatcher(beForWeightView, getActivity(), R.plurals.be, editBeForWeight.getText().toString()));
        editBeForPieces.addTextChangedListener(new PluralsWatcher(beForPiecesView, getActivity(), R.plurals.be, editBeForPieces.getText().toString()));
        editPiecesForBe.addTextChangedListener(new PluralsWatcher(piecesForBeView, getActivity(), R.plurals.piece, editPiecesForBe.getText().toString()));

        // Keep entered values up to date
        editWeight.addTextChangedListener(weightWatcher);
        editBeForWeight.addTextChangedListener(beForWeightWatcher);
        editBeForPieces.addTextChangedListener(beForPieceWatcher);
        editPiecesForBe.addTextChangedListener(pieceForBEWatcher);
        weightWatcher.update();
        beForWeightWatcher.update();
        beForPieceWatcher.update();
        pieceForBEWatcher.update();
        updateHiddenCalculator();

        builder.setView(view)
                .setTitle(newEntry ? R.string.dialog_add_title : copyEntry ? R.string.dialog_edit_copy_title : R.string.dialog_edit_title)
                .setPositiveButton(R.string.dialog_ok, null)
                .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //only close dialog
                    }
                });

        final AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                positiveButton.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View button){
                        String name = editName.getText().toString();
                        String comment = editComment.getText().toString();
                        boolean confirmed = confirmedToggle.isChecked();
                        unit = unitSpinner.getSelectedItemPosition();

                        boolean errorFound = false;
                        if ("".equals(name)) {
                            editName.setError(getString(R.string.toast_name_required));
                            errorFound = true;
                        }
                        if (enteredWeight == 0) {
                            editWeight.setError(getString(R.string.toast_illegal_value));
                            errorFound = true;
                        }
                        if (enteredPiecesForBE == 0 ) {
                            editPiecesForBe.setError(getString(R.string.toast_illegal_value));
                            errorFound = true;
                        }
                        if (enteredBeForWeight == 0) {
                            editBeForWeight.setError(getString(R.string.toast_illegal_value));
                            errorFound = true;
                        }
                        if (!errorFound) {
                            ContentValues values = new ContentValues();
                            values.put(BEContract.BEEntry.COLUMN_NAME_ENTRY_NAME, name);

                            double weightPerBE = enteredWeight > 0 ?
                                    enteredWeight/enteredBeForWeight :
                                    -1;
                            values.put(BEContract.BEEntry.COLUMN_NAME_ENTRY_GRAMS_PER_BE, weightPerBE);

                            double bePerPiece = enteredBeForPieces >= 0 ?
                                    enteredBeForPieces/enteredPiecesForBE :
                                    (enteredPiecesForBE != 1 ? 1/enteredPiecesForBE : -1);
                            values.put(BEContract.BEEntry.COLUMN_NAME_ENTRY_BE_PER_PIECE, bePerPiece);

                            values.put(BEContract.BEEntry.COLUMN_NAME_ENTRY_COMMENT, comment);
                            values.put(BEContract.BEEntry.COLUMN_NAME_ENTRY_CONFIRMED, confirmed);
                            values.put(BEContract.BEEntry.COLUMN_NAME_ENTRY_UNIT, unit);

                            BEEntryDbHelper dbHelper = new BEEntryDbHelper(getActivity());
                            SQLiteDatabase db = dbHelper.getWritableDatabase();

                            if (newEntry || copyEntry){
                                long id = db.insert(BEContract.BEEntry.TABLE_NAME, "null", values);
                                if (activity instanceof ListActivity)
                                    ((ListActivity) activity).loadContent(new BEEntryDisplay(id,
                                            name, unit, weightPerBE, bePerPiece, comment, confirmed));
                            } else {
                                String selection = BEContract.BEEntry._ID + " LIKE ?";
                                String[] selectionArgs = {String.valueOf(databaseID)};
                                db.update(BEContract.BEEntry.TABLE_NAME, values, selection, selectionArgs);
                                if (activity instanceof ListActivity) {
                                    if (name.equals(EditDialog.this.name)) {
                                        ((ListActivity) activity).loadContent(true);
                                    } else {
                                        ((ListActivity) activity).loadContent(new BEEntryDisplay(databaseID,
                                                name, unit, weightPerBE, bePerPiece, comment, confirmed));
                                    }
                                }
                            }
                            alertDialog.dismiss();
                        }
                    }
                });
            }
        });
        return alertDialog;
    }

    private void updateHiddenCalculator(){
        boolean hideUp = enteredBeForPieces <= 0 &&
                (enteredPiecesForBE == 0 || editPiecesForBe.getText().toString().equals(""));
        boolean hideDown = enteredWeight <= 0;
        boolean hideAll = hideUp && hideDown;
        if (hideAll != (hideCalculateUp && hideCalculateDown))
            calculateLayout.setVisibility(hideAll ? View.GONE : View.VISIBLE);
        if (hideUp != hideCalculateUp){
            hideCalculateUp = hideUp;
            calculateUpButton.setVisibility(hideUp ? View.GONE : View.VISIBLE);
        }
        if (hideDown != hideCalculateDown){
            hideCalculateDown = hideDown;
            calculateDownButton.setVisibility(hideDown ? View.GONE : View.VISIBLE);
        }
    }

    UpdateTextWatcher weightWatcher = new UpdateTextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}
        @Override
        public void afterTextChanged(Editable s) {
            update();
        }
        public void update(){
            try {
                enteredWeight = Double.parseDouble(editWeight.getText().toString());
            }
            catch (Exception e){
                enteredWeight = -1;
            }
            updateHiddenCalculator();
        }
    };
    UpdateTextWatcher beForWeightWatcher = new UpdateTextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}
        @Override
        public void afterTextChanged(Editable s) {
            update();
        }
        public void update(){
            try {
                enteredBeForWeight = Double.parseDouble(editBeForWeight.getText().toString());
            }
            catch (Exception e){
                enteredBeForWeight = 1;
            }
            updateHiddenCalculator();
        }
    };
    UpdateTextWatcher beForPieceWatcher = new UpdateTextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}
        @Override
        public void afterTextChanged(Editable s) {
            update();
        }
        public void update(){
            try {
                enteredBeForPieces = Double.parseDouble(editBeForPieces.getText().toString());
            }
            catch (Exception e){
                enteredBeForPieces = -1;
            }
            updateHiddenCalculator();
        }
    };
    UpdateTextWatcher pieceForBEWatcher = new UpdateTextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}
        @Override
        public void afterTextChanged(Editable s) {
            update();
        }
        public void update(){
            try {
                enteredPiecesForBE = Double.parseDouble(editPiecesForBe.getText().toString());
            }
            catch (Exception e){
                enteredPiecesForBE = 1;
            }
            updateHiddenCalculator();
        }
    };

    // Methods to use from CalculateMissingDialog
    public void setBePerPiece(double bePerPiece){
        editBeForPieces.setText(""+bePerPiece);
        editPiecesForBe.setText("");
    }
    public void setWeightPerBE(double weightPerBE){
        editWeight.setText(""+weightPerBE);
        editBeForWeight.setText("");
    }
    public interface UpdateTextWatcher extends TextWatcher{
        void update();
    }
}
