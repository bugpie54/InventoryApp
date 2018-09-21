package com.example.android.inventoryapp.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public final class InventoryContract {

    private InventoryContract() {}

    //Content authority
    public static final String CONTENT_AUTHORITY = "com.example.android.inventoryapp";
    
     // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     // the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

     // Possible path
    public static final String PATH_BOOKS = "books";

     // Inner class that defines constant values for the books database table.
     // Each entry in the table represents a single book.

    public static final class ProductEntry implements BaseColumns {

        //The content URI to access the pet data in the provider
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_BOOKS);

        // The MIME type of the list of books
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_BOOKS;

        //The MIME type for a single book
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_BOOKS;

        // Name of the products database table
        public final static String TABLE_NAME = "books";

        // The columns of the books table
        public final static String _ID = BaseColumns._ID;
        public final static String COLUMN_PRODUCT_TITLE = "title";
        public final static String COLUMN_PRODUCT_QUANTITY = "quantity";
        public final static String COLUMN_PRODUCT_PRICE = "price";
        public final static String COLUMN_PRODUCT_SUPPLIER_NAME = "supplier_name";
        public final static String COLUMN_PRODUCT_SUPPLIER_PHONE = "supplier_phone";
    }

}
