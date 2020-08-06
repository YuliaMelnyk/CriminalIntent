package com.hfad.criminalintent.database;

/**
 * @author yuliiamelnyk on 06/08/2020
 * @project CriminalIntent
 */
public class CrimeDbShema {
    public static final class CrimeTable {
        public static final String NAME = "crimes";

        public static final class Cols{
            public static final String UUID = "uuid";
            public static final String TITLE = "title";
            public static final String DATE = "date";
            public static final String SOLVED = "solved";
        }
    }
}
