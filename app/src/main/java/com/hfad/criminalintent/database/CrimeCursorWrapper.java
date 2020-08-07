package com.hfad.criminalintent.database;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.hfad.criminalintent.database.CrimeDbShema.CrimeTable;
import com.hfad.criminalintent.model.Crime;

import java.util.Date;
import java.util.UUID;

/**
 * @author yuliiamelnyk on 06/08/2020
 * @project CriminalIntent
 */
public class CrimeCursorWrapper extends CursorWrapper {
    public CrimeCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public Crime getCrime() {
        String uuidString = getString(getColumnIndex(CrimeTable.Cols.UUID));
        String title = getString(getColumnIndex(CrimeTable.Cols.TITLE));
        long date = getLong(getColumnIndex(CrimeTable.Cols.DATE));
        int isSolved = getInt(getColumnIndex(CrimeTable.Cols.SOLVED));
        String suspect = getString(getColumnIndex(CrimeTable.Cols.SUSPECT));

        Crime crime = new Crime(UUID.fromString(uuidString));
        crime.setTitle(title);
        crime.setDate(new Date(date));
        crime.setSolved(isSolved != 0);
        crime.setSuspect(suspect);

        return crime;
    }
}
