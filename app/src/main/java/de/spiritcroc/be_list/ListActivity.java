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
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AlphabetIndexer;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.LinkedHashSet;

import de.spiritcroc.be_list.settings.Keys;
import de.spiritcroc.be_list.settings.SettingsActivity;

public class ListActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {
    private static final String ARG_SHOW_CONFIRMED = "showConfirmed";
    private static final String ARG_SHOW_NOT_CONFIRMED = "showNotConfirmed";
    private static final String ARG_SORT_ORDER = "sortOrder";

    private enum SearchMode {
        ALL,
        BEGINNING,
        CONTAINING,
        BEGINNING_CONTAINING_NO_ORDER,
        CONTAINING_NO_ORDER,
        BEGINNING_ANY_CONTAINING_NO_ORDER,
        ANY_CONTAINING_NO_ORDER,
        ANY_CONTAINING_ANY
    }
    public enum SortOrder {NAME, ID, ID_REVERSE}
    private volatile ArrayList<BEEntryDisplay> display;
    private Cursor cursor;
    private SharedPreferences sharedPreferences;
    private ListView listView;
    private SearchView searchView;
    private TextView emptyView;
    private String[] projection = new String[] {
            BEContract.BEEntry._ID,
            BEContract.BEEntry.COLUMN_NAME_ENTRY_NAME,
            BEContract.BEEntry.COLUMN_NAME_ENTRY_UNIT,
            BEContract.BEEntry.COLUMN_NAME_ENTRY_GRAMS_PER_BE,
            BEContract.BEEntry.COLUMN_NAME_ENTRY_BE_PER_PIECE,
            BEContract.BEEntry.COLUMN_NAME_ENTRY_COMMENT,
            BEContract.BEEntry.COLUMN_NAME_ENTRY_CONFIRMED
    };
    private volatile String selection;
    String search = "";
    boolean fullSearch = false;
    private boolean rememberPosition = true;
    private BEEntryDisplay scrollToEntry;
    private String unit1, unit2, unit3;
    private int nameSize, weightPerBESize, bePerPieceSize, commentSize;
    private int textColor, textColorUnconfirmed, textColorWeightForBE, textColorBEPerPiece,
            textColorComment;
    private int precisionBePerPiece, precision1, precision2, precision3;
    private SortOrder sortOrder = SortOrder.NAME;

    // Only use following to check whether the adapter should be re-created
    private String beUnit, beUnitPlural;

    // Filter
    boolean showConfirmed = true, showNotConfirmed = true;

    private ActionMode actionMode;
    private int actionModePosition = -1;
    private View lastSelectedView;
    private ActionMode.Callback actionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.context_menu, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            BEEntryDisplay c = display.get(actionModePosition);
            switch (item.getItemId()){
                case R.id.action_edit:
                    EditDialog.newInstance(false, c.getDatabaseID(), c.getName(), c.getWeightUnit(),
                            c.getWeightPerBE(), c.getBEPerPiece(), c.getComment(), c.getConfirmed())
                            .show(getFragmentManager(), "EditDialog");
                    mode.finish();
                    return true;
                case R.id.action_copy:
                    EditDialog.newInstance(true, c.getDatabaseID(), c.getName(), c.getWeightUnit(),
                            c.getWeightPerBE(), c.getBEPerPiece(), c.getComment(), c.getConfirmed())
                            .show(getFragmentManager(), "CopyDialog");
                    mode.finish();
                    return true;
                case R.id.action_delete:
                    DeleteDialog.newInstance(c.getDatabaseID(), c.getName())
                            .show(getFragmentManager(), "DeleteDialog");
                    mode.finish();
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            lastSelectedView.setBackgroundColor(Color.TRANSPARENT);
            actionMode = null;
            actionModePosition = -1;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        if (Util.localize(this)){
            // First start
            new BasicSetupDialog().show(getFragmentManager(), "BasicSetupDialog");
        }

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        listView = (ListView) findViewById(R.id.list_view);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (actionMode != null && actionModePosition != position)
                    actionMode.finish();
                else if (actionMode == null) {
                    BEEntryDisplay c = display.get(position);
                    CalculatorDialog.newInstance(c.getDatabaseID(), c.getName(), c.getWeightUnit(),
                            getUnit(c.getWeightUnit()), c.getWeightPerBE(), c.getBEPerPiece(),
                            c.getComment(), c.getConfirmed())
                            .show(getFragmentManager(), "CalculatorDialog");
                }
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (actionMode != null) {
                    if (actionModePosition == position) {
                        actionMode.finish();
                        return true;
                    }
                    actionMode.finish();
                }
                lastSelectedView = view;
                view.setBackgroundColor(getResources().getColor(R.color.list_item_long_click));
                actionModePosition = position;
                actionMode = startActionMode(actionModeCallback);
                return true;
            }
        });

        emptyView = (TextView) findViewById(R.id.empty_view);
    }

    @Override
    protected void onResume(){
        super.onResume();

        setTitle(Util.getString(this, R.string.app_name));

        checkPreferences();
    }

    public void checkPreferences() {
        // Lots of prefs to keep up-to-date.
        String unit1 = sharedPreferences.getString(Keys.MASS_PRIMARY_UNIT, "g");
        String unit2 = sharedPreferences.getString(Keys.MASS_SECONDARY_UNIT, "ml");
        String unit3 = sharedPreferences.getString(Keys.MASS_TERTIARY_UNIT, "");
        String beUnit = sharedPreferences.getString(Keys.CARB_UNIT, "");
        String beUnitPlural = sharedPreferences.getString(Keys.CARB_UNIT_PL, "");
        int precisionBePerPiece = Integer.parseInt(sharedPreferences.getString(Keys.CARB_PER_PIECE_PRECISION, "1"));
        int precision1 = Integer.parseInt(sharedPreferences.getString(Keys.MASS_PRIMARY_PRECISION, "0"));
        int precision2 = Integer.parseInt(sharedPreferences.getString(Keys.MASS_SECONDARY_PRECISION, "0"));
        int precision3 = Integer.parseInt(sharedPreferences.getString(Keys.MASS_TERTIARY_PRECISION, "0"));
        int nameSize = Integer.parseInt(sharedPreferences.getString(Keys.TEXT_SIZE_NAME, "25"));
        int weightPerBESize = Integer.parseInt(sharedPreferences.getString(Keys.TEXT_SIZE_MASS_PER_CARB, "15"));
        int bePerPieceSize = Integer.parseInt(sharedPreferences.getString(Keys.TEXT_SIZE_CARBS_PER_PIECE, "15"));
        int commentSize = Integer.parseInt(sharedPreferences.getString(Keys.TEXT_SIZE_COMMENT, "15"));
        int textColor = Integer.parseInt(sharedPreferences.getString(Keys.TEXT_COLOR, getString(R.string.pref_color_dkgray_value)));
        int textColorUnconfirmed = Integer.parseInt(sharedPreferences.getString(Keys.TEXT_COLOR_UNCONFIRMED, getString(R.string.pref_color_blue_value)));
        int textColorWeightForBE = Integer.parseInt(sharedPreferences.getString(Keys.TEXT_COLOR_MASS_PER_CARB,getString(R.string.pref_color_dkgray_value)));
        int textColorBEPerPiece = Integer.parseInt(sharedPreferences.getString(Keys.TEXT_COLOR_CARBS_PER_PIECE, getString(R.string.pref_color_dkgray_value)));
        int textColorComment = Integer.parseInt(sharedPreferences.getString(Keys.TEXT_COLOR_COMMENT, getString(R.string.pref_color_dkgray_value)));
        if (!unit1.equals(this.unit1) ||
                !unit2.equals(this.unit2) ||
                !unit3.equals(this.unit3) ||
                !beUnit.equals(this.beUnit) ||
                !beUnitPlural.equals(this.beUnitPlural) ||
                precisionBePerPiece != this.precisionBePerPiece ||
                precision1 != this.precision1 ||
                precision2 != this.precision2 ||
                precision3 != this.precision3 ||
                nameSize != this.nameSize ||
                weightPerBESize != this.weightPerBESize ||
                bePerPieceSize != this.bePerPieceSize ||
                commentSize != this.commentSize ||
                textColor != this.textColor ||
                textColorUnconfirmed != this.textColorUnconfirmed ||
                textColorWeightForBE != this.textColorWeightForBE ||
                textColorBEPerPiece != this.textColorBEPerPiece ||
                textColorComment != this.textColorComment) {
            this.unit1 = unit1;
            this.unit2 = unit2;
            this.unit3 = unit3;
            this.beUnit = beUnit;
            this.beUnitPlural = beUnitPlural;
            this.precisionBePerPiece = precisionBePerPiece;
            this.precision1 = precision1;
            this.precision2 = precision2;
            this.precision3 = precision3;
            this.nameSize = nameSize;
            this.weightPerBESize = weightPerBESize;
            this.bePerPieceSize = bePerPieceSize;
            this.commentSize = commentSize;
            this.textColor = textColor;
            this.textColorUnconfirmed = textColorUnconfirmed;
            this.textColorWeightForBE = textColorWeightForBE;
            this.textColorBEPerPiece = textColorBEPerPiece;
            this.textColorComment = textColorComment;
            loadContent(true);
            emptyView.setTextSize(TypedValue.COMPLEX_UNIT_SP, nameSize);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putBoolean(ARG_SHOW_CONFIRMED, showConfirmed);
        savedInstanceState.putBoolean(ARG_SHOW_NOT_CONFIRMED, showNotConfirmed);
        savedInstanceState.putString(ARG_SORT_ORDER, sortOrder.toString());
    }
    @Override
    public void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        showConfirmed = savedInstanceState.getBoolean(ARG_SHOW_CONFIRMED);
        showNotConfirmed = savedInstanceState.getBoolean(ARG_SHOW_NOT_CONFIRMED);
        sortOrder = SortOrder.valueOf(savedInstanceState.getString(ARG_SORT_ORDER));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_list, menu);
        searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setOnQueryTextListener(this);
        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus && (search == null || search.equals(""))) {
                    closeSearchView();

                }
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                EditDialog.newAddDialogInstance().show(getFragmentManager(), "AddDialog");
                return true;
            case R.id.action_filter:
                FilterDialog.newInstance(this, showConfirmed, showNotConfirmed, sortOrder.toString())
                        .show(getFragmentManager(), "FilterDialog");
                return true;
            case R.id.action_random_entry:
                new RandomEntryDialog().setListActivity(this)
                        .show(getFragmentManager(), "RandomEntryDialog");
                return true;
            case R.id.action_about:
                new AboutDialog().show(getFragmentManager(), "AboutDialog");
                return true;
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if (searchView.isIconified()) {
            if (Util.DEBUG) Log.d("onBackPressed", "super");
            super.onBackPressed();
        } else {
            if (Util.DEBUG) Log.d("onBackPressed", "close");
            closeSearchView();
        }
    }

    @Override
    public boolean onQueryTextChange(String text) {
        fullSearch = false;
        search = text;
        loadContent(false);
        return false;
    }
    @Override
    public boolean onQueryTextSubmit(String text) {
        fullSearch = true;
        search = text;
        loadContent(false);
        // Clear focus so searchView does not steal next back button press
        searchView.clearFocus();
        return false;
    }

    private void closeSearchView() {
        searchView.setQuery("", false);
        searchView.setIconified(true);
    }

    private void createSelection(SearchMode searchMode) {
        selection = "((" + BEContract.BEEntry.COLUMN_NAME_ENTRY_NAME + " NOTNULL) AND (" + BEContract.BEEntry.COLUMN_NAME_ENTRY_NAME + " != '' )";
        if (!showConfirmed)
            selection += " AND (" + BEContract.BEEntry.COLUMN_NAME_ENTRY_CONFIRMED + " =0)";
        if (!showNotConfirmed)
            selection += " AND (" + BEContract.BEEntry.COLUMN_NAME_ENTRY_CONFIRMED + " !=0)";
        switch (searchMode){
            case BEGINNING:
                selection += " AND ("+ BEContract.BEEntry.COLUMN_NAME_ENTRY_NAME+" LIKE '" + search +"%')";
                break;
            case CONTAINING:
                selection += " AND ("+ BEContract.BEEntry.COLUMN_NAME_ENTRY_NAME+" LIKE '%" + search +"%')";
                break;
            case BEGINNING_CONTAINING_NO_ORDER:{
                String[] searches = search.split(" ");
                selection += " AND ("+ BEContract.BEEntry.COLUMN_NAME_ENTRY_NAME+" LIKE '" + searches[0] +"%')";
                String[] newSearches = new String[searches.length-1];
                System.arraycopy(searches, 1, newSearches, 0, newSearches.length);
                for (String subSearch: newSearches)
                    selection += " AND (" + BEContract.BEEntry.COLUMN_NAME_ENTRY_NAME + " LIKE '%" + subSearch + "%')";
                break;
            }
            case CONTAINING_NO_ORDER:{
                String[] searches = search.split(" ");
                for (String subSearch: searches)
                    selection += " AND (" + BEContract.BEEntry.COLUMN_NAME_ENTRY_NAME + " LIKE '%" + subSearch + "%')";
                break;
            }
            case BEGINNING_ANY_CONTAINING_NO_ORDER:{
                String[] searches = search.split(" ");
                selection += " AND ("+ BEContract.BEEntry.COLUMN_NAME_ENTRY_NAME+" LIKE '" + searches[0] +"%')";
                String[] newSearches = new String[searches.length-1];
                System.arraycopy(searches, 1, newSearches, 0, newSearches.length);
                for (String subSearch: newSearches)
                    selection += " AND ((" + BEContract.BEEntry.COLUMN_NAME_ENTRY_NAME + " LIKE '%" + subSearch + "%')" +
                            " OR (" + BEContract.BEEntry.COLUMN_NAME_ENTRY_COMMENT + " LIKE '%" + subSearch + "%'))";
                break;
            }
            case ANY_CONTAINING_NO_ORDER:{
                String[] searches = search.split(" ");
                for (String subSearch: searches)
                    selection += " AND ((" + BEContract.BEEntry.COLUMN_NAME_ENTRY_NAME + " LIKE '%" + subSearch + "%')" +
                            " OR (" + BEContract.BEEntry.COLUMN_NAME_ENTRY_COMMENT + " LIKE '%" + subSearch + "%'))";
                break;
            }
            case ANY_CONTAINING_ANY:{
                String[] searches = search.split(" ");
                selection += " AND ((" + BEContract.BEEntry.COLUMN_NAME_ENTRY_NAME + " LIKE '%" + searches[0] + "%')" +
                        " OR (" + BEContract.BEEntry.COLUMN_NAME_ENTRY_COMMENT + " LIKE '%" + searches[0] + "%')";
                String[] newSearches = new String[searches.length-1];
                System.arraycopy(searches, 1, newSearches, 0, newSearches.length);
                for (String subSearch: newSearches)
                    selection += " OR (" + BEContract.BEEntry.COLUMN_NAME_ENTRY_NAME + " LIKE '%" + subSearch + "%')" +
                            " OR (" + BEContract.BEEntry.COLUMN_NAME_ENTRY_COMMENT + " LIKE '%" + subSearch + "%')";
                selection += ")";
                break;
            }
            case ALL:
            default:
                break;
        }
        selection += ")";
    }
    private String getSortOrder() {
        switch (sortOrder) {
            case ID:
                return BEContract.BEEntry._ID + " ASC";
            case ID_REVERSE:
                return BEContract.BEEntry._ID + " DESC";
            case NAME:
            default:
                return BEContract.BEEntry.COLUMN_NAME_ENTRY_NAME + " COLLATE NOCASE ASC";
        }
    }

    public void setFilter(boolean showConfirmed, boolean showNotConfirmed, SortOrder sortOrder) {
        if (showConfirmed != this.showConfirmed || showNotConfirmed != this.showNotConfirmed ||
                sortOrder != this.sortOrder) {
            this.showConfirmed = showConfirmed;
            this.showNotConfirmed = showNotConfirmed;
            this.sortOrder = sortOrder;
            loadContent(false);
        }
    }

    private void setEmptyViewText() {
        if (display.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            if (search != null && !search.equals(""))
                emptyView.setText(fullSearch ?
                        R.string.empty_list_full_search : R.string.empty_list_search);
            else if (!showConfirmed || !showNotConfirmed)
                emptyView.setText(R.string.empty_list_filtered);
            else
                emptyView.setText(R.string.empty_list);
        } else
            emptyView.setVisibility(View.GONE);
    }

    public String getUnit(int unit) {
        switch (unit){
            case 0:
                return unit1;
            case 1:
                return unit2;
            case 2:
                return unit3;
            default:
                return "";
        }
    }
    private int getPrecision(int unit) {
        switch (unit){
            case 0:
                return precision1;
            case 1:
                return precision2;
            case 2:
                return precision3;
            default:
                return 1;
        }
    }

    public BEEntryDisplay getRandomBEEntry() {
        int position = (int) Math.round(Math.random() * (display.size()-1));
        return display.get(position);
    }


    public void loadContent(boolean rememberPosition) {
        setRememberPosition(rememberPosition);
        new LoadCursorTask().execute();
    }
    public void loadContent(BEEntryDisplay scrollToEntry) {
        setScrollToEntry(scrollToEntry);
        new LoadCursorTask().execute();
    }
    private ArrayList<BEEntryDisplay> getBEEntriesFromCursor(Cursor cursor) {
        ArrayList<BEEntryDisplay> list = new ArrayList<>();
        if (!cursor.moveToFirst())
            return list;
        boolean moveSucceeded;
        int nameIndex = cursor.getColumnIndex(BEContract.BEEntry.COLUMN_NAME_ENTRY_NAME);
        int unitIndex = cursor.getColumnIndex(BEContract.BEEntry.COLUMN_NAME_ENTRY_UNIT);
        int gramsIndex = cursor.getColumnIndex(BEContract.BEEntry.COLUMN_NAME_ENTRY_GRAMS_PER_BE);
        int beIndex  = cursor.getColumnIndex(BEContract.BEEntry.COLUMN_NAME_ENTRY_BE_PER_PIECE);
        int commentIndex = cursor.getColumnIndex(BEContract.BEEntry.COLUMN_NAME_ENTRY_COMMENT);
        int confirmedIndex = cursor.getColumnIndex(BEContract.BEEntry.COLUMN_NAME_ENTRY_CONFIRMED);
        int idIndex = cursor.getColumnIndex(BEContract.BEEntry._ID);
        do {
            double addGrams, addBEs;
            if (cursor.isNull(gramsIndex))
                addGrams = -1;
            else
                addGrams = cursor.getDouble(gramsIndex);
            if (cursor.isNull(beIndex))
                addBEs = -1;
            else
                addBEs = cursor.getDouble(beIndex);
            list.add(new BEEntryDisplay(cursor.getLong(idIndex), cursor.getString(nameIndex), cursor.getInt(unitIndex), addGrams, addBEs, cursor.getString(commentIndex), cursor.getInt(confirmedIndex) != 0));
            moveSucceeded = cursor.move(1);
        } while (moveSucceeded);
        return list;
    }

    private void setRememberPosition(boolean rememberPosition) {
        this.rememberPosition = rememberPosition;
    }
    private void setScrollToEntry(BEEntryDisplay scrollToEntry) {
        rememberPosition = true;
        this.scrollToEntry = scrollToEntry;
    }

    private class LoadCursorTask extends AsyncTask<Void, Void, ArrayList<BEEntryDisplay>> {
        @Override
        protected ArrayList<BEEntryDisplay> doInBackground(Void... params) {
            if (Util.SCREENSHOT)
                return display = Util.getScreenshotList();
            if (search == null || search.equals("")) {
                createSelection(SearchMode.ALL);
                display = loadCursor();
            } else {
                createSelection(SearchMode.BEGINNING);
                display = loadCursor();
                createSelection(SearchMode.CONTAINING);
                if (Util.DEBUG) display.add(new BEEntryDisplay(0, "CONTAINING", 0, -1, -1, "", false));
                display.addAll(loadCursor());
                createSelection(SearchMode.BEGINNING_CONTAINING_NO_ORDER);
                if (Util.DEBUG) display.add(new BEEntryDisplay(0, "BEGINNING_CONTAINING_NO_ORDER", 0, -1, -1, "", false));
                display.addAll(loadCursor());
                createSelection(SearchMode.CONTAINING_NO_ORDER);
                if (Util.DEBUG) display.add(new BEEntryDisplay(0, "CONTAINING_NO_ORDER", 0, -1, -1, "", false));
                display.addAll(loadCursor());
                createSelection(SearchMode.BEGINNING_ANY_CONTAINING_NO_ORDER);
                if (Util.DEBUG) display.add(new BEEntryDisplay(0, "BEGINNING_ANY_CONTAINING_NO_ORDER", 0, -1, -1, "", false));
                display.addAll(loadCursor());
                createSelection(SearchMode.ANY_CONTAINING_NO_ORDER);
                if (Util.DEBUG) display.add(new BEEntryDisplay(0, "ANY_CONTAINING_NO_ORDER", 0, -1, -1, "", false));
                display.addAll(loadCursor());

                // Show more results after using the submit button
                if (fullSearch){
                    createSelection(SearchMode.ANY_CONTAINING_ANY);
                    if (Util.DEBUG) display.add(new BEEntryDisplay(0, "ANY_CONTAINING_ANY", 0, -1, -1, "", false));
                    display.addAll(loadCursor());
                }

                // Remove duplicates
                LinkedHashSet<BEEntryDisplay> linkedHashSet = new LinkedHashSet<>(display);
                display = new ArrayList<>(linkedHashSet);
            }
            return display;
        }
        @Override
        protected void onPostExecute(ArrayList<BEEntryDisplay> display) {
            CustomArrayAdapter adapter = (sortOrder == SortOrder.NAME && !Util.SCREENSHOT) ?
                    new SectionIndexedArrayAdapter(getApplicationContext(), R.layout.be_list_item, display.toArray(new BEEntryDisplay[display.size()])) :
                    new CustomArrayAdapter(getApplicationContext(), R.layout.be_list_item, display.toArray(new BEEntryDisplay[display.size()]));
            if (rememberPosition) {
                final View view = listView.getChildAt(0);
                //int top = (view == null ? 0 : (view.getTop() - listView.getPaddingTop()));    //http://stackoverflow.com/questions/3014089/maintain-save-restore-scroll-position-when-returning-to-a-listview says that
                int top = (view == null ? 0 : view.getTop());
                int index = listView.getFirstVisiblePosition();
                listView.setAdapter(adapter);
                listView.setSelectionFromTop(index, top);
                if (scrollToEntry != null){
                    final int position = display.indexOf(scrollToEntry);
                    if (position >= 0 && position < display.size()) {
                        listView.post(new Runnable() {
                            @Override
                            public void run() {
                                listView.smoothScrollToPositionFromTop(position,
                                        getResources().getDimensionPixelOffset(
                                                R.dimen.list_item_vertical_padding),
                                        200);
                            }
                        });

                    }
                    scrollToEntry = null;
                }
            } else
                listView.setAdapter(adapter);
            // Update section indexer
            adapter.notifyDataSetChanged();
            setEmptyViewText();
        }
    }
    // Call from AsyncTask
    private ArrayList<BEEntryDisplay> loadCursor() {
        BEEntryDbHelper dbHelper = new BEEntryDbHelper(ListActivity.this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        cursor = db.query(BEContract.BEEntry.TABLE_NAME, projection, selection, null, null, null, getSortOrder());
        ArrayList<BEEntryDisplay> display = getBEEntriesFromCursor(cursor);
        dbHelper.close();
        return display;
    }

    private class CustomArrayAdapter extends ArrayAdapter<BEEntryDisplay> {
        BEEntryDisplay[] objects;
        public CustomArrayAdapter(Context context, int resource, BEEntryDisplay[] objects) {
            super(context, resource, objects);
            this.objects = objects;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            ItemHolder holder;

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.be_list_item, parent, false);

                holder = new ItemHolder();
                holder.nameView = (TextView) view.findViewById(R.id.name_view);
                holder.valueView = (ViewGroup) view.findViewById(R.id.value_view);
                holder.mainView = (ViewGroup) view.findViewById(R.id.main_view);

                view.setTag(holder);
            }
            else
                holder = (ItemHolder) convertView.getTag();

            Resources resources = getResources();
            view.setPadding(
                    resources.getDimensionPixelSize(R.dimen.list_item_horizontal_padding),
                    resources.getDimensionPixelSize(position == 0 ? R.dimen.list_item_vertical_padding_end : R.dimen.list_item_vertical_padding),
                    resources.getDimensionPixelSize(R.dimen.list_item_horizontal_padding),
                    resources.getDimensionPixelSize(position == objects.length - 1 ? R.dimen.list_item_vertical_padding_end : R.dimen.list_item_vertical_padding)
            );

            holder.valueView.removeAllViews();
            holder.mainView.removeAllViews();
            holder.mainView.addView(holder.nameView);

            String name = getItem(position).getName();
            double gramsPerBE = getItem(position).getWeightPerBE();
            double bePerPiece = getItem(position).getBEPerPiece();
            String comment = getItem(position).getComment();
            boolean confirmed = getItem(position).getConfirmed();
            int unit = getItem(position).getWeightUnit();

            if (confirmed)
                holder.nameView.setTextColor(textColor);
            else
                holder.nameView.setTextColor(textColorUnconfirmed);
            holder.nameView.setText(name);
            holder.nameView.setTextSize(TypedValue.COMPLEX_UNIT_SP, nameSize);
            if (gramsPerBE > 0) {
                TextView valueView1 = new TextView(getApplicationContext());
                valueView1.setGravity(Gravity.END);
                valueView1.setText(Util.roundToString(gramsPerBE, getPrecision(unit)) + " " +
                        Util.getString(ListActivity.this, R.string.grams_per_be_short)
                                .replaceAll("\\?", getUnit(unit)));
                valueView1.setTextSize(TypedValue.COMPLEX_UNIT_SP, weightPerBESize);
                valueView1.setTextColor(textColorWeightForBE);
                holder.valueView.addView(valueView1);
            }
            if (bePerPiece >= 0) {
                TextView valueView2 = new TextView(getApplicationContext());
                valueView2.setGravity(Gravity.END);
                String roundedString = Util.roundToString(bePerPiece, precisionBePerPiece);
                valueView2.setText(roundedString + " " +
                        Util.getQuantityString(ListActivity.this, R.plurals.be_per_piece_short,
                                Util.getDoubleQuantity(roundedString)));
                valueView2.setTextSize(TypedValue.COMPLEX_UNIT_SP, bePerPieceSize);
                valueView2.setTextColor(textColorBEPerPiece);
                holder.valueView.addView(valueView2);
            }
            if (comment != null && !comment.equals("")) {
                TextView commentView = new TextView(getApplicationContext());
                commentView.setText(comment);
                commentView.setTextSize(TypedValue.COMPLEX_UNIT_SP, commentSize);
                commentView.setGravity(Gravity.START);
                commentView.setTextColor(textColorComment);
                holder.mainView.addView(commentView);
            }

            if (actionMode != null && position == actionModePosition) {
                if (lastSelectedView != null)
                    lastSelectedView.setBackgroundColor(Color.TRANSPARENT);
                lastSelectedView = view;
                view.setBackgroundColor(getResources().getColor(R.color.list_item_long_click));
            } else {
                view.setBackgroundColor(Color.TRANSPARENT);
            }

            return view;
        }
    }
    private class SectionIndexedArrayAdapter extends CustomArrayAdapter implements SectionIndexer {
        AlphabetIndexer alphaIndexer;
        private AlphabetIndexer getAlphaIndexer() {
            if (alphaIndexer == null) {
                alphaIndexer = new AlphabetIndexer(cursor, cursor.getColumnIndex(BEContract.BEEntry.COLUMN_NAME_ENTRY_NAME), "ABCDEFGHIJKLMNOPQRSTUVWXYZ");
            }
            return alphaIndexer;
        }
        public SectionIndexedArrayAdapter(Context context, int resource, BEEntryDisplay[] objects) {
            super(context, resource, objects);
            getAlphaIndexer();
        }
        @Override
        public int getPositionForSection(int section) {
            return getAlphaIndexer().getPositionForSection(section);
        }
        @Override
        public int getSectionForPosition(int position) {
            return getAlphaIndexer().getSectionForPosition(position);
        }
        @Override
        public Object[] getSections() {
            return getAlphaIndexer().getSections();
        }
    }

    static class ItemHolder {
        TextView nameView;
        ViewGroup valueView, mainView;
    }
}
