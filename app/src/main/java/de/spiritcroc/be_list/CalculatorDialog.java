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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class CalculatorDialog extends DialogFragment {
    private static final String ARG_DATABASE_ID  = "databaseID";
    private static final String ARG_NAME  = "name";
    private static final String ARG_WEIGHT_PER_BE  = "weightPerBE";
    private static final String ARG_BE_PER_PIECE  = "bePerPiece";
    private static final String ARG_COMMENT  = "comment";
    private static final String ARG_CONFIRMED = "confirmed";
    private static final String ARG_UNIT = "unit";
    private static final String ARG_UNIT_NAME = "unitName";

    private long databaseID;
    private String name, comment, unitName;
    private int unit;
    private double weightPerBE, bePerPiece, currentBE;
    private boolean confirmed;
    // For a uniform layout
    private int maxViewWidth = 0;

    private EditText editBE, editWeight, editPiece;
    private TextView viewBE, viewPiece, viewUnit;
    private boolean hiddenWeight = false, hiddenPiece = false, skipTextWatchers = false;

    public static CalculatorDialog newInstance (long databaseID, String name, int unit, String unitName, double weightPerBE, double bePerPiece, String comment, boolean confirmed){
        Bundle args = new Bundle();
        CalculatorDialog fragment = new CalculatorDialog();
        args.putLong(ARG_DATABASE_ID, databaseID);
        args.putString(ARG_NAME, name);
        args.putInt(ARG_UNIT, unit);
        args.putString(ARG_UNIT_NAME, unitName);
        args.putDouble(ARG_WEIGHT_PER_BE, weightPerBE);
        args.putDouble(ARG_BE_PER_PIECE, bePerPiece);
        args.putString(ARG_COMMENT, comment);
        args.putBoolean(ARG_CONFIRMED, confirmed);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        if (getArguments() == null){
            Log.e("CalculatorDialog", "Could not get arguments");
        }
        else{
            databaseID = getArguments().getLong(ARG_DATABASE_ID);
            name = getArguments().getString(ARG_NAME);
            unit = getArguments().getInt(ARG_UNIT);
            unitName = getArguments().getString(ARG_UNIT_NAME);
            weightPerBE = getArguments().getDouble(ARG_WEIGHT_PER_BE);
            bePerPiece = getArguments().getDouble(ARG_BE_PER_PIECE);
            comment = getArguments().getString(ARG_COMMENT);
            confirmed = getArguments().getBoolean(ARG_CONFIRMED);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        final Activity activity = getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        final View view = activity.getLayoutInflater().inflate(R.layout.calculator, null);

        editBE = (EditText) view.findViewById(R.id.edit_be);
        editWeight = (EditText) view.findViewById(R.id.edit_weight);
        editPiece = (EditText) view.findViewById(R.id.edit_pieces);
        viewBE = (TextView) view.findViewById(R.id.view_be);
        viewPiece = (TextView) view.findViewById(R.id.view_pieces);
        viewUnit = (TextView) view.findViewById(R.id.view_weight);
        if (!confirmed)
            ((TextView)view.findViewById(R.id.name_view)).setText("(" + getString(R.string.not_confirmed) + ")");
        if (comment != null && !comment.equals("")) {
            ((TextView) view.findViewById(R.id.comment_view)).setText(comment);
        } else {
            view.findViewById(R.id.comment_title).setVisibility(View.GONE);
        }

        editBE.addTextChangedListener(new PluralsWatcher(viewBE, getActivity(), R.plurals.be, "1"));
        editPiece.addTextChangedListener(new PluralsWatcher(viewPiece, getActivity(), R.plurals.piece, null));
        viewUnit.setText(unitName);

        if (weightPerBE <= 0) {
            view.findViewById(R.id.weight_layout).setVisibility(View.GONE);
            hiddenWeight = true;
        }
        if (bePerPiece < 0) {
            view.findViewById(R.id.piece_layout).setVisibility(View.GONE);
            hiddenPiece = true;
        }
        if (hiddenWeight && hiddenPiece){
            // No values given, so no use letting users enter BEs
            view.findViewById(R.id.be_layount).setVisibility(View.GONE);
        }
        setBE(1);

        editBE.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (skipTextWatchers)
                    return;
                try {
                    setBE(Double.parseDouble(editBE.getText().toString()));
                } catch (Exception e) {
                    setBE(1);
                }
            }
        });
        editWeight.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (skipTextWatchers)
                    return;
                try {
                    setWeight(Double.parseDouble(editWeight.getText().toString()));
                } catch (Exception e) {
                }
            }
        });
        editPiece.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (skipTextWatchers)
                    return;
                try {
                    setPiece(Double.parseDouble(editPiece.getText().toString()));
                } catch (Exception e) {
                }
            }
        });
        viewBE.addOnLayoutChangeListener(layoutChangeListener);
        viewUnit.addOnLayoutChangeListener(layoutChangeListener);
        viewPiece.addOnLayoutChangeListener(layoutChangeListener);

        return builder.setView(view)
                .setTitle(name + " - " + getString(R.string.dialog_calculator_title))
                .setNegativeButton(R.string.dialog_close, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //only close dialog
                    }
                })
                .setNeutralButton(R.string.action_edit, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditDialog.newInstance(false, databaseID, name, unit,
                                weightPerBE, bePerPiece, comment, confirmed)
                                .show(getFragmentManager(), "EditDialog");
                    }
                })
                .create();
    }

    private void setBE(double be){
        skipTextWatchers = true;
        currentBE = be;
        if (!hiddenWeight){
            editWeight.setText("" + weightPerBE * be);
        }
        if (!hiddenPiece){
            editPiece.setText("" + be / bePerPiece);
        }
        skipTextWatchers = false;
    }
    private void setWeight(double weight){
        skipTextWatchers = true;
        currentBE = weight / weightPerBE;
        editBE.setText("" + currentBE);
        if (!hiddenPiece){
            editPiece.setText("" + currentBE / bePerPiece);
        }
        skipTextWatchers = false;
    }
    private void setPiece(double piece){
        skipTextWatchers = true;
        currentBE = bePerPiece * piece;
        editBE.setText("" + currentBE);
        if (!hiddenWeight){
            editWeight.setText("" + weightPerBE * currentBE);
        }
        skipTextWatchers = false;
    }

    private void setMinViewWidth(int viewWidth){
        if (viewWidth > maxViewWidth){
            maxViewWidth = viewWidth;
            viewBE.setMinWidth(viewWidth);
            viewUnit.setMinWidth(viewWidth);
            viewPiece.setMinWidth(viewWidth);
        }
    }
    View.OnLayoutChangeListener layoutChangeListener = new View.OnLayoutChangeListener() {
        @Override
        public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
            setMinViewWidth(right-left);
        }
    };
}
