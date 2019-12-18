package com.example.android.pets.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import com.example.android.pets.data.PetContract.PetEntry;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

public class PetProvider extends ContentProvider {
    public static final String LOG_TAG = PetProvider.class.getSimpleName();

    private static final int PETS = 100;
    private static final int PETS_ID = 101;
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static{
        //The call to addUri() go here, for all of the content uri patterns that the provider
        //should recognize. All paths added to the UriMatcher have a corresponding code to return
        //when a match is found.

        //The content URI of the form "content://com.example.android.pets/pets" will map to the
        //integer code{@link #PETS}. This URI is used to provide access  to MULTIPLE rows
        //of the pets table
        sUriMatcher.addURI(PetEntry.CONTENT_AUTHORITY,PetEntry.PATH_PETS, PETS);

        //The content URI of the form "content://com.example.android.pets/pets/#" will map to the
        //integer code{@link #PET_ID}. This URI is used to provide access  to a SINGLE row
        //of the pets table
        //Here, the # wildcard is used where "#" can be substituted for an integer.
        //For example, "content://com.example.android.pets/pets/3" matches but
        //"content://com.example.android.pets/pets" doesn't match.
        sUriMatcher.addURI(PetEntry.CONTENT_AUTHORITY, PetEntry.PATH_PETS + "/#", PETS_ID);
    }
    //Database helper object
    private PetDbHelper mDbHelper;
    @Override
    public boolean onCreate() {
        mDbHelper = new PetDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection,  String selection, String[] selectionArgs,  String sortOrder) {

        //Get readable database
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        //This cursor will hold the result of the query
        Cursor cursor;

        //Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch(match){
            case PETS:
                //For the pets  code, query the pets table directly with the given
                //projection, selection, selection arguments, sort order. The cursor
                //could contain multiple rows of the pets table
                cursor = db.query(PetEntry.TABLE_NAME,      // The table to query
                        projection,                         // The columns to return
                        selection,                          // The columns for the WHERE clause
                        selectionArgs,                      // The values for the WHERE clause
                        null,                       // Don't group the rows
                        null,                        // Don't filter by row groups
                        sortOrder);                         // The sort order
                break;
            case PETS_ID:
                //For the PET_ID code extract out the ID from the URI
                //For an example URI such as "content://com.example.android.pets/pets/3"
                //the selection will be "_id = ?" and the selection argument will be a String
                //Array containing the actual ID of 3  in this case.

                //For every "?" in the selection, we need to have an element in the selection
                //argument that will fill in the "?". Since we have 1 question mark in the
                //selection, we have 1 String in the selection argument's String array.
                selection = PetEntry._ID + "=?";

                //ContentUris.parseId() gets the integer value of the type while we rely on
                //the String.valueOf() method to convert the int to String
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                //This will perform  a query on the pets table where the _id equals 3 to return a
                //Cursor containing that row of the table.
                cursor = db.query(PetEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;

            default:
                throw new IllegalArgumentException("Cannot query unknown URI" + uri);
        }

        return cursor;
    }

    @Override
    public String getType( Uri uri) {
        return null;
    }

    @Override
    public Uri insert( Uri uri,  ContentValues values) {
        final int match = sUriMatcher.match(uri);
        switch(match){
            case PETS:
                return insertPet(uri, values);
                default:
                    throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }

    }

    private Uri insertPet(Uri uri, ContentValues values){
        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Insert the new pet with the given values
        long id = database.insert(PetEntry.TABLE_NAME, null, values);

        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }
        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete( Uri uri,  String selection,  String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update( Uri uri,  ContentValues values,  String selection,  String[] selectionArgs) {
        return 0;
    }
}
