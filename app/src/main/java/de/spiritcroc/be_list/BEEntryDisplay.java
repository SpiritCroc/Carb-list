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

public class BEEntryDisplay {
    private long databaseID;
    private double weightPerBE = -1, bePerPiece = -1;
    private String name, comment;
    int unit;
    private boolean confirmed;
    public BEEntryDisplay(long databaseID, String name, int unit, double weightPerBE, double bePerPiece, String comment, boolean confirmed){
        setName(name);
        try {
            setWeightPerBE(weightPerBE);
        }
        catch (IllegalArgumentException e){
            //just don't set value jet
        }
        try {
            setBEPerPiece(bePerPiece);
        }
        catch (IllegalArgumentException e){
            //just don't set value jet
        }
        this.unit = unit;
        this.comment = comment;
        this.confirmed = confirmed;
        this.databaseID = databaseID;
    }
    public String getName(){
        return name;
    }
    public double getBEPerPiece(){
        return bePerPiece;
    }
    public double getWeightPerBE(){
        return weightPerBE;
    }
    public String getComment(){
        return comment;
    }
    public boolean getConfirmed(){
        return confirmed;
    }
    public long getDatabaseID(){
        return databaseID;
    }
    public int getWeightUnit(){
        return unit;
    }
    public void setName(String name){
        if (name!=null && !name.equals(""))
            this.name = name;
        else
            throw new IllegalArgumentException("de.spiritcroc.be_list.setName: Illegal name");
    }
    public void setWeightPerBE(double weightPerBE){
        if (weightPerBE > 0)
            this.weightPerBE = weightPerBE;
        else
            throw new IllegalArgumentException("de.spiritcroc.be_list.setGramsPerBE: Should be higher than 0");
    }
    public void setBEPerPiece(double bePerPiece){
        if (bePerPiece >= 0)
            this.bePerPiece = bePerPiece;
        else
            throw new IllegalArgumentException("de.spiritcroc.be_list.setBEPerPiece: Should not be negative");
    }
    public void setWeightUnit(int unit){
        this.unit = unit;
    }

    // Generated methods
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BEEntryDisplay display = (BEEntryDisplay) o;

        if (databaseID != display.databaseID) return false;
        if (Double.compare(display.weightPerBE, weightPerBE) != 0) return false;
        if (Double.compare(display.bePerPiece, bePerPiece) != 0) return false;
        if (unit != display.unit) return false;
        if (confirmed != display.confirmed) return false;
        if (name != null ? !name.equals(display.name) : display.name != null) return false;
        return !(comment != null ? !comment.equals(display.comment) : display.comment != null);

    }
    @Override
    public int hashCode() {
        int result;
        long temp;
        result = (int) (databaseID ^ (databaseID >>> 32));
        temp = Double.doubleToLongBits(weightPerBE);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(bePerPiece);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (comment != null ? comment.hashCode() : 0);
        result = 31 * result + unit;
        result = 31 * result + (confirmed ? 1 : 0);
        return result;
    }
}
