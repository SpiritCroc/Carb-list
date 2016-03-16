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
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class BEEntryDbHelper extends SQLiteOpenHelper {
    private static final String TEXT_TYPE = " TEXT";
    private static final String DECIMAL_TYPE = " DECIMAL";
    private static final String BOOLEAN_TYPE = " BOOLEAN";
    private static final String INTEGER_TYPE = " INTEGER";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_ENTRIES = "CREATE TABLE " + BEContract.BEEntry.TABLE_NAME + " (" +
            BEContract.BEEntry._ID  + " INTEGER PRIMARY KEY," +
            BEContract.BEEntry.COLUMN_NAME_ENTRY_NAME + TEXT_TYPE + COMMA_SEP +
            BEContract.BEEntry.COLUMN_NAME_ENTRY_BE_PER_PIECE + DECIMAL_TYPE + COMMA_SEP +
            BEContract.BEEntry.COLUMN_NAME_ENTRY_GRAMS_PER_BE + DECIMAL_TYPE + COMMA_SEP +
            BEContract.BEEntry.COLUMN_NAME_ENTRY_COMMENT + TEXT_TYPE + COMMA_SEP +
            BEContract.BEEntry.COLUMN_NAME_ENTRY_CONFIRMED + BOOLEAN_TYPE + COMMA_SEP +
            BEContract.BEEntry.COLUMN_NAME_ENTRY_UNIT + INTEGER_TYPE + " )";
    private static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + BEContract.BEEntry.TABLE_NAME;

    public static final int DATABASE_VERSION = 3;   //increment if database schema is changed
    public static final String DATABASE_NAME = "BEEntries.db";

    public BEEntryDbHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db){
        db.execSQL(SQL_CREATE_ENTRIES);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        if (oldVersion > newVersion) {
            db.execSQL(SQL_DELETE_ENTRIES);
            onCreate(db);
            return;
        }
        switch (oldVersion){
            case 2:
                db.execSQL("alter table " + BEContract.BEEntry.TABLE_NAME + " add " +
                        BEContract.BEEntry.COLUMN_NAME_ENTRY_UNIT + INTEGER_TYPE + " default 0");
                break;
            default:
                db.execSQL(SQL_DELETE_ENTRIES);
                onCreate(db);
                break;
        }
    }
    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion){
        onUpgrade(db, oldVersion, newVersion);
    }
}
