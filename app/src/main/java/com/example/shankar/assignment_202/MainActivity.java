package com.example.shankar.assignment_202;

import android.Manifest;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.OperationApplicationException;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;

//Creating MainActivity by extending AppCompatActivity.
public class MainActivity extends AppCompatActivity
{
    //Creating References of the classes whose elements are used in the layout.
    Button updateBtn;
    EditText nameET,oldPhnET,newPhnET;
    private static final int PERMISSIONS_REQUEST_UPDATE_CONTACTS = 100;

    //Creating reference of ContentResolver.
    ContentResolver contentResolver;

    @Override
    //oncreate method.
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);   //Setting content View.

        //getting ContentResolver.
        contentResolver=getContentResolver();

        //Setting references with their IDs.
        updateBtn=(Button)findViewById(R.id.update_btn);
        nameET=(EditText)findViewById(R.id.name_et);
        oldPhnET=(EditText)findViewById(R.id.old_phone_et);
        newPhnET=(EditText)findViewById(R.id.new_phone_et);

        //Setting onClick Listener to Button.
        updateBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //Calling the method to update Contact.
                updateItem();
            }
        });
    }


    //Method to upDate method.
    private void updateItem()
    {
        //Checking that if version is greater than or equal to Marshmallow, if then requesting for permission.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED)
        {
            requestPermissions(new String[]{Manifest.permission.WRITE_CONTACTS}, PERMISSIONS_REQUEST_UPDATE_CONTACTS);
        }

        //Checking that all Details are fiiled.
        if(!nameET.getText().toString().isEmpty() &&
           !oldPhnET.getText().toString().isEmpty() &&
           !newPhnET.getText().toString().isEmpty())
        {
            //calling update() method.
            update(contentResolver,oldPhnET.getText().toString(),newPhnET.getText().toString());
            Toast.makeText(getApplicationContext(),"Contact Updated",Toast.LENGTH_SHORT).show();
        }
        else
        {
            //Displaying Toast.
            Toast.makeText(getApplicationContext(),"Please Fill All Details",Toast.LENGTH_SHORT).show();
        }
    }

    //update method.
    public static void update(ContentResolver contentResolver,String oldNumber,String newNumber)
    {
        //ArrayList of ContentProviderOperation class.
        ArrayList<ContentProviderOperation> operationArrayList = new ArrayList<>();
        //Fetching contactID of first contact.
        String contactID = String.valueOf(getContactID(contentResolver,oldNumber));

        //Creating Select query.
        String selectPhone = ContactsContract.Data.CONTACT_ID + "=? AND " + ContactsContract.Contacts.Data.MIMETYPE + "='" +
                  ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE + "'" + " AND "
                + ContactsContract.CommonDataKinds.Phone.TYPE + "=?";

        //Creating Arguements array.
        String[] phoneArgs = new String[]{contactID, String.valueOf(ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)};

        //Updating Contact.
        operationArrayList.add(ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                          .withSelection(selectPhone,phoneArgs)
                          .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER,newNumber)
                          .build());

        try
        {
            //Applaying batch to ContentResolver.
            contentResolver.applyBatch(ContactsContract.AUTHORITY,operationArrayList);
        }
        catch (RemoteException e)
        {
            e.printStackTrace();
        }
        catch (OperationApplicationException e)
        {
            e.printStackTrace();
        }

    }

    //Method to get ID of Contact.
    private static long getContactID(ContentResolver contentResolver,String number)
    {
        //Creating Uri.
        Uri contactUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
        String[] projection = { ContactsContract.PhoneLookup._ID };   //Creating projection.
        Cursor cursor=null;   //creating reference of Cursor.

        try
        {
            //creating cursor by query.
            cursor = contentResolver.query(contactUri, projection, null, null,null);

            if (cursor != null && cursor.moveToFirst())
            {
                //Fetching ID and returning ID.
                int personID = cursor.getColumnIndex(ContactsContract.PhoneLookup._ID);
                return cursor.getLong(personID);
            }

            return -1;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (cursor != null) {
                cursor.close();
            }
        }
        return -1;
    }
}
