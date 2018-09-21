package com.example.android.inventoryapp;

import android.Manifest;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.android.inventoryapp.data.InventoryContract.ProductEntry;


 //Create a new product or edit an existing one
public class EditorActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    // Identifier for the pet data loader
    private static final int EXISTING_PRODUCT_LOADER = 0;

    // Content URI for the existing product
    private Uri mCurrentProductUri;

    //EditText field to enter the book title
    private EditText mTitleEditText;

    // EditText field for the book price
    private EditText mPriceEditText;

     //EditText field for the book quantity
    private EditText mQuantityEditText;

     // EditText field for the book supplier name
     private EditText mSupplierNameText;

     // EditText field for the book supplier phone number
     private EditText mSupplierPhoneText;

    // Boolean flag to keep track of whether the product has been edited
    private boolean mProductChanged = false;

    // OnTouchListener that listens for any user touches on a View

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mProductChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new product or editing an existing one.
        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();

        // If the intent DOES NOT contain a product content URI, then we know that we are
        // creating a new product.
        if (mCurrentProductUri == null) {
            // This is a new product, set the app bar to say "Add a Product"
            setTitle(getString(R.string.editor_title_new_product));

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a product that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            // Otherwise this is an existing product, set app bar to say "Edit Product"
            setTitle(getString(R.string.editor_title_edit_product));

            // Initialize a loader to read the product data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);
        }

        // Find all relevant views that we will need to read user input from
        mTitleEditText = findViewById(R.id.edit_book_title);
        mPriceEditText = findViewById(R.id.edit_book_price);
        mQuantityEditText = findViewById(R.id.edit_quantity);
        mSupplierNameText = findViewById(R.id.edit_supplier_name);
        mSupplierPhoneText = findViewById(R.id.edit_supplier_phone);

        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        mTitleEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mSupplierNameText.setOnTouchListener(mTouchListener);
        mSupplierPhoneText.setOnTouchListener(mTouchListener);

        Button quantityPlusButton = findViewById(R.id.quantity_button_plus);
        quantityPlusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(mQuantityEditText.getText().toString())) {
                    int quantity = Integer.valueOf(mQuantityEditText.getText().toString());
                    mQuantityEditText.setText(String.valueOf(quantity + 1));
                    mProductChanged = true;
                }
            }
        });
        Button quantityMinusButton = findViewById(R.id.quantity_button_minus);
        quantityMinusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(mQuantityEditText.getText().toString())) {
                    int quantity = Integer.valueOf(mQuantityEditText.getText().toString());
                    if (quantity > 0) {
                        mQuantityEditText.setText(String.valueOf(quantity - 1));
                        mProductChanged = true;
                    }
                } else {
                    mQuantityEditText.setText("0");
                    mProductChanged = true;
                }
            }
        });
        Button saveButton = findViewById(R.id.save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Save product to database
                saveProduct();
                // Exit activity
                finish();
            }
        });
        Button deleteButton = findViewById(R.id.delete_button);
        if (mCurrentProductUri != null) {
            deleteButton.setVisibility(View.VISIBLE);
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDeleteConfirmationDialog();
                }
            });
        } else
            deleteButton.setVisibility(View.GONE);

        //Setting the order button
        Button callButton = findViewById(R.id.call_button);
        if (mCurrentProductUri != null) {
            callButton.setVisibility(View.VISIBLE);
            callButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_DIAL,
                                Uri.parse("tel:" + mSupplierPhoneText.getText()));
                        startActivity(intent);
                }
            });
        } else
            callButton.setVisibility(View.GONE);

    }

      //Get user input from editor and save product into database
     private void saveProduct() {
         // Read from input fields
         // Use trim to eliminate leading or trailing white space
         String title = mTitleEditText.getText().toString().trim();
         String price = mPriceEditText.getText().toString().trim();
         String quantity = mQuantityEditText.getText().toString().trim();
         String supplierName = mSupplierNameText.getText().toString().trim();
         String supplierPhone = mSupplierPhoneText.getText().toString().trim();

         // Check if this is supposed to be a new product
         // and check if all the fields in the editor are blank
         if (mCurrentProductUri == null &&
                 TextUtils.isEmpty(title) && TextUtils.isEmpty(price) &&
                 TextUtils.isEmpty(quantity) && TextUtils.isEmpty(supplierName)&&
                 TextUtils.isEmpty(supplierPhone) ) {
             // Return early without creating a new product.
             return;
         }

         if (TextUtils.isEmpty(title)) {
             Toast.makeText(this, R.string.missing_book_title, Toast.LENGTH_LONG).show();
             return;
         }
         if (TextUtils.isEmpty(price)) {
             Toast.makeText(this, R.string.missing_book_price, Toast.LENGTH_LONG).show();
             return;
         }
         if (TextUtils.isEmpty(supplierName)) {
             Toast.makeText(this, R.string.missing_supplier_name, Toast.LENGTH_LONG).show();
             return;
         }
         if (TextUtils.isEmpty(supplierPhone)) {
             Toast.makeText(this, R.string.missing_supplier_phone, Toast.LENGTH_LONG).show();
             return;
         }

         // Create a ContentValues object where column names are the keys,
         // and product attributes from the editor are the values.
         ContentValues values = new ContentValues();
         values.put(ProductEntry.COLUMN_PRODUCT_TITLE, title);
         values.put(ProductEntry.COLUMN_PRODUCT_PRICE, price);
         values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, quantity);
         values.put(ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME, supplierName);
         values.put(ProductEntry.COLUMN_PRODUCT_SUPPLIER_PHONE, supplierPhone);

         // If the product quantity is not provided, Use 0 by default.
         int quantityInt = 0;
         if (!TextUtils.isEmpty(quantity)) {
             quantityInt = Integer.parseInt(quantity);
         }
         values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, quantityInt);

         // Determine if this is a new or existing product by checking if mCurrentProductUri is null or not
         if (mCurrentProductUri == null) {
             // This is a new product, add a new product into the provider,
             // returning the content URI for the new product.
             Uri newUri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);

             // Show a toast message depending on whether or not the insertion was successful
             if (newUri == null) {
                 // If the new content URI is null, then there was an error with insertion
                 Toast.makeText(this, getString(R.string.editor_add_product_failed),
                         Toast.LENGTH_SHORT).show();
             } else {
                 // Otherwise, the insertion was successful and we can display a toast
                 Toast.makeText(this, getString(R.string.editor_add_product_successful),
                         Toast.LENGTH_SHORT).show();
             }
         } else {
             // This is an existing product, update the product with content URI: mCurrentProductUri
             // and pass in the new ContentValues. Pass in null for the selection and selection args
             // because mCurrentProductUri will already identify the correct row in the database that
             // we want to modify.
             int rowsAffected = getContentResolver().update(mCurrentProductUri, values, null, null);

             // Show a toast message depending on whether or not the update was successful.
             if (rowsAffected == 0) {
                 // If no rows were affected, then there was an error with the update.
                 Toast.makeText(this, getString(R.string.editor_update_product_failed),
                         Toast.LENGTH_SHORT).show();
             } else {
                 // Otherwise, the update was successful and we can display a toast.
                 Toast.makeText(this, getString(R.string.editor_update_product_successful),
                         Toast.LENGTH_SHORT).show();
             }
             finish();
         }
     }

     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu options from the res/menu/editor_menu.xml file.
         getMenuInflater().inflate(R.menu.editor_menu, menu);
         return true;
     }

      // This method is called after invalidateOptionsMenu(), so that the
      // menu can be updated (some menu items can be hidden or made visible)
     @Override
     public boolean onPrepareOptionsMenu(Menu menu) {
         super.onPrepareOptionsMenu(menu);
         // If this is a new product, hide the "Delete" menu item.
         if (mCurrentProductUri == null) {
             MenuItem menuItem = menu.findItem(R.id.action_delete);
             menuItem.setVisible(false);
         }
         return true;
     }

     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         // User clicked on a menu option in the app bar overflow menu
         switch (item.getItemId()) {
             // Respond to a click on the "Save" menu option
             case R.id.action_save:
                 // Save product to database
                 saveProduct();
                 // Exit activity
                 finish();
                 return true;
             // Respond to a click on the "Delete" menu option
             case R.id.action_delete:
                 // Pop up confirmation dialog for deletion
                 showDeleteConfirmationDialog();
                 return true;
             // Respond to a click on the "Up" arrow button in the app bar
             case android.R.id.home:
                 // If the product hasn't changed, continue with navigating up to parent activity
                 if (!mProductChanged) {
                     NavUtils.navigateUpFromSameTask(EditorActivity.this);
                     return true;
                 }
                 // If there are unsaved changes, setup a dialog to warn the user.
                 // Create a click listener to handle the user confirming that
                 // changes should be discarded.
                 DialogInterface.OnClickListener discardButtonClickListener =
                         new DialogInterface.OnClickListener() {
                             @Override
                             public void onClick(DialogInterface dialogInterface, int i) {
                                 // User clicked "Discard" button, navigate to parent activity.
                                 NavUtils.navigateUpFromSameTask(EditorActivity.this);
                             }
                         };
                 // Show a dialog that notifies the user they have unsaved changes
                 showUnsavedChangesDialog(discardButtonClickListener);
                 return true;
         }
         return super.onOptionsItemSelected(item);
     }

      //This method is called when the back button is pressed
     @Override
     public void onBackPressed() {
         // If the product hasn't changed, continue with handling back button press
         if (!mProductChanged) {
             super.onBackPressed();
             return;
         }

         // If there are unsaved changes, setup a dialog to warn the user.
         // Create a click listener to handle the user confirming that changes should be discarded.
         DialogInterface.OnClickListener discardButtonClickListener =
                 new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialogInterface, int i) {
                         // User clicked "Discard" button, close the current activity.
                         finish();
                     }
                 };

         // Show dialog that there are unsaved changes
         showUnsavedChangesDialog(discardButtonClickListener);
     }

     private void showUnsavedChangesDialog(
         DialogInterface.OnClickListener discardButtonClickListener) {
             // Create an AlertDialog.Builder and set the message, and click listeners
             // for the positive and negative buttons on the dialog.
             AlertDialog.Builder builder = new AlertDialog.Builder(this);
             builder.setMessage(R.string.unsaved_changes_msg);
             builder.setPositiveButton(R.string.discard, discardButtonClickListener);
             builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int id) {
                     // User clicked the "Keep editing" button, so dismiss the dialog
                     // and continue editing the product.
                     if (dialog != null) {
                         dialog.dismiss();
                     }
                 }
             });
             // Create and show the AlertDialog
             AlertDialog alertDialog = builder.create();
             alertDialog.show();
     }

     // Prompt the user to confirm that they want to delete this product
     private void showDeleteConfirmationDialog() {

         // Create an AlertDialog.Builder and set the message, and click listeners
         // for the positive and negative buttons on the dialog
         AlertDialog.Builder builder = new AlertDialog.Builder(this);
         builder.setMessage(R.string.delete_dialog_msg);
         builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int id) {
                 // User clicked the "Delete" button, delete the product
                 deleteProduct();
             }
         });
         builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int id) {
                 // User clicked the "Cancel" button, so dismiss the dialog
                 // and continue editing the product.
                 if (dialog != null) {
                     dialog.dismiss();
                 }
             }
         });
         // Create and show the AlertDialog
         AlertDialog alertDialog = builder.create();
         alertDialog.show();
     }

     // Perform the deletion of the product in the database
     private void deleteProduct() {
         // Only perform the delete if this is an existing product
         if (mCurrentProductUri != null) {
             // Call the ContentResolver to delete the product at the given content URI.
             // Pass in null for the selection and selection args because the mCurrentPetUri
             // content URI already identifies the product that we want.
             int rowsDeleted = getContentResolver().delete(mCurrentProductUri, null, null);

             // Show a toast message depending on whether or not the delete was successful.
             if (rowsDeleted == 0) {
                 // If no rows were deleted, then there was an error with the delete.
                 Toast.makeText(this, getString(R.string.editor_delete_product_failed),
                         Toast.LENGTH_SHORT).show();
             } else {
                 // Otherwise, the delete was successful and we can display a toast.
                 Toast.makeText(this, getString(R.string.editor_delete_product_successful),
                         Toast.LENGTH_SHORT).show();
             }
         }
         // Close the activity
         finish();
     }

     @Override
     public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
         // Since the editor shows all product attributes, define a projection that contains
         // all columns from the books table
         String[] projection = {
                 ProductEntry._ID,
                 ProductEntry.COLUMN_PRODUCT_TITLE,
                 ProductEntry.COLUMN_PRODUCT_QUANTITY,
                 ProductEntry.COLUMN_PRODUCT_PRICE,
                 ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME,
                 ProductEntry.COLUMN_PRODUCT_SUPPLIER_PHONE };

         // This loader will execute the ContentProvider's query method on a background thread
         return new CursorLoader(this,   // Parent activity context
                 mCurrentProductUri,         // Query the content URI for the current productt
                 projection,             // Columns to include in the resulting Cursor
                 null,                   // No selection clause
                 null,                   // No selection arguments
                 null);                  // Default sort order
     }

     @Override
     public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
         // Bail early if the cursor is null or there is less than 1 row in the cursor
         if (cursor == null || cursor.getCount() < 1) {
             return;
         }

         // Proceed with moving to the first row of the cursor and reading data from it
         // (This should be the only row in the cursor)
         if (cursor.moveToFirst()) {
             // Find the columns of pet attributes that we're interested in
             int titleColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_TITLE);
             int quantityColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY);
             int priceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE);
             int supplierNameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME);
             int supplierPhoneColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_SUPPLIER_PHONE);

             // Extract out the value from the Cursor for the given column index
             String title = cursor.getString(titleColumnIndex);
             int quantity = cursor.getInt(quantityColumnIndex);
             Double price = cursor.getDouble(priceColumnIndex);
             String supplierName = cursor.getString(supplierNameColumnIndex);
             String supplierPhone = cursor.getString(supplierPhoneColumnIndex);

             // Update the views on the screen with the values from the database
             mTitleEditText.setText(title);
             mQuantityEditText.setText(String.valueOf(quantity));
             mPriceEditText.setText(String.valueOf(price));
             mSupplierNameText.setText(supplierName);
             mSupplierPhoneText.setText(supplierPhone);
         }
     }

     @Override
     public void onLoaderReset(Loader<Cursor> loader) {
         // If the loader is invalidated, clear out all the data from the input fields.
         mTitleEditText.setText("");
         mQuantityEditText.setText("");
         mPriceEditText.setText("");
         mSupplierNameText.setText("");
         mSupplierPhoneText.setText("");
     }
 }
