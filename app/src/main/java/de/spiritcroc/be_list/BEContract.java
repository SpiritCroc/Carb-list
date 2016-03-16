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

import android.provider.BaseColumns;

public final class BEContract {
    public BEContract(){}

    public static abstract class BEEntry implements BaseColumns{
        public static final String TABLE_NAME = "beentry";
        public static final String COLUMN_NAME_ENTRY_NAME = "name";
        public static final String COLUMN_NAME_ENTRY_GRAMS_PER_BE = "gramsperbe";
        public static final String COLUMN_NAME_ENTRY_BE_PER_PIECE = "beperpiece";
        public static final String COLUMN_NAME_ENTRY_COMMENT = "comment";
        public static final String COLUMN_NAME_ENTRY_CONFIRMED = "confirmed";
        public static final String COLUMN_NAME_ENTRY_UNIT = "unit";
    }
}
