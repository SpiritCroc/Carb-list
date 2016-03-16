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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class CalculateMissingDialog extends DialogFragment{
    private static final String ARG_WEIGHT_PER_BE = "weightPerBE";
    private static final String ARG_BE_PER_PIECES= "bePerPiece";
    private static final String ARG_UNIT = "unit";

    private String unit;
    private double weightPerBE, bePerPiece;

    private EditDialog parent;
    private EditText editWeight, editPieces;
    private TextView viewPieces;

    public CalculateMissingDialog setParent(EditDialog parent){
        this.parent = parent;
        return this;
    }

    public static CalculateMissingDialog weightToPiece(EditDialog parent, double weightPerBE, String unit){
        Bundle args = new Bundle();
        CalculateMissingDialog fragment = new CalculateMissingDialog();
        args.putDouble(ARG_WEIGHT_PER_BE, weightPerBE);
        args.putDouble(ARG_BE_PER_PIECES, -1);
        args.putString(ARG_UNIT, unit);
        fragment.setArguments(args);
        return fragment.setParent(parent);
    }
    public static CalculateMissingDialog pieceToWeight(EditDialog parent, double bePerPiece, String unit){
        Bundle args = new Bundle();
        CalculateMissingDialog fragment = new CalculateMissingDialog();
        args.putDouble(ARG_WEIGHT_PER_BE, -1);
        args.putDouble(ARG_BE_PER_PIECES, bePerPiece);
        args.putString(ARG_UNIT, unit);
        fragment.setArguments(args);
        return fragment.setParent(parent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        if (getArguments() == null){
            Log.e("CalculateMissingDialog", "Could not get arguments");
        }
        else{
            weightPerBE = getArguments().getDouble(ARG_WEIGHT_PER_BE);
            bePerPiece = getArguments().getDouble(ARG_BE_PER_PIECES);
            unit = getArguments().getString(ARG_UNIT);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        final Activity activity = getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        final View view = activity.getLayoutInflater().inflate(R.layout.dialog_calculate_missing, null);
        viewPieces = (TextView) view.findViewById(R.id.view_pieces);
        editPieces = (EditText) view.findViewById(R.id.edit_pieces);
        editWeight = (EditText) view.findViewById(R.id.edit_weight);
        ((TextView)view.findViewById(R.id.view_unit)).setText(unit);

        editPieces.addTextChangedListener(new PluralsWatcher(viewPieces, getActivity(), R.plurals.piece, "1"));

        builder.setView(view)
                .setTitle(bePerPiece == -1 ?
                        Util.getString(getActivity(), R.string.dialog_calculate_weight_to_piece_title) :
                        Util.getString(getActivity(), R.string.dialog_calculate_piece_to_weight_title))
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
                        double weight, pieces;
                        try {
                            weight = Double.parseDouble(editWeight.getText().toString());
                        }
                        catch (Exception e){
                            weight = -1;
                        }
                        try {
                            pieces = Double.parseDouble(editPieces.getText().toString());
                        }
                        catch (Exception e){
                            pieces = 1;
                        }

                        if (weight <= 0 || pieces == 0)
                            Toast.makeText(getActivity(), R.string.toast_illegal_value, Toast.LENGTH_SHORT).show();
                        else {
                            if (parent == null)
                                Log.e("CalculateMissingDialog", "parent == null");
                            else if (bePerPiece != -1)
                                parent.setWeightPerBE(weight/pieces/bePerPiece);
                            else if (weightPerBE != -1)
                                parent.setBePerPiece(weight/weightPerBE/pieces);
                            alertDialog.dismiss();
                        }
                    }
                });
            }
        });
        return alertDialog;
    }
}
