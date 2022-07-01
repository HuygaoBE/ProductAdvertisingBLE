package com.example.bluetoothgatt;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.ParcelUuid;
import android.util.Log;

import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class DBAdapter extends SQLiteOpenHelper {
    private static final String TAG = "DBAdapter";



    public DBAdapter(Context context) {
        super(context, "Catalogue.db", null, 1);
    }



    @Override
    public void onCreate(SQLiteDatabase DB) {
        DB.execSQL("create Table Userdetails(Profile TEXT primary key, ID TEXT, AdvCount TEXT, Off_set TEXT, MF TEXT, DataAdv TEXT, Rssi TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase DB, int i, int i1) {
        DB.execSQL("drop Table if exists Userdetails");
    }

    //--------------------------------





    //--------------------------------

    public Boolean insertdata(String Profile, String ID, String AdvCount, String Off_set, String MF, String DataAdv, String Rssi)
    {
        SQLiteDatabase DB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("Profile", Profile);
        contentValues.put("ID", ID);
        contentValues.put("AdvCount", AdvCount);
        contentValues.put("Off_set", Off_set);
        contentValues.put("MF", MF);
        contentValues.put("DataAdv", DataAdv);
        contentValues.put("Rssi", Rssi);
        long result=DB.insert("Userdetails", null, contentValues);
        if(result==-1){
            return false;
        }else{

            return true;
        }
    }

    //--------------------------------------

    public void updatedata(){
//        Log.i(TAG,"Xin chao1 Day la UPDATEDATA");

//---------------------------------------
        ArrayList<String> listProfile = new ArrayList<String>();
        ArrayList<String> listID = new ArrayList<String>();
        ArrayList<String> listAdvCount = new ArrayList<String>();
        ArrayList<String> listOff_set = new ArrayList<String>();
        ArrayList<String> listMF = new ArrayList<String>();
        ArrayList<String> listDataAdv = new ArrayList<String>();
        ArrayList<String> listRssi = new ArrayList<String>();

        SQLiteDatabase DB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        Cursor cursor = DB.rawQuery("SELECT * FROM Userdetails WHERE ID = (SELECT ID FROM Userdetails GROUP BY ID ORDER BY COUNT(*) DESC LIMIT 1) ORDER BY CAST(Off_set AS INTEGER) ASC;",null);


        if(cursor.moveToFirst()){
            do{
                listProfile.add(cursor.getString(0));
                listID.add(cursor.getString(1));
                listAdvCount.add(cursor.getString(2));
                listOff_set.add(cursor.getString(3));
                listMF.add(cursor.getString(4));
                listDataAdv.add(cursor.getString(5));
                listRssi.add(cursor.getString(6));

            }while(cursor.moveToNext());
        }
//        DB.close();
        Log.i(TAG, "KQ ------ Profile:" + listProfile);
        Log.i(TAG, "KQ ------ ID:" + listID);
        Log.i(TAG, "KQ ------ AdvCount:" + listAdvCount);
        Log.i(TAG, "KQ ------ Off_set:" + listOff_set);
        Log.i(TAG, "KQ ------ MF:" + listMF);
        Log.i(TAG, "KQ ------ DataAdv:" + listDataAdv);
        Log.i(TAG, "KQ ------ Rssi:" + listRssi);


            int SumlistProfile = listProfile.size();
            if(SumlistProfile < 2){
                return;
            }
            else if ((Integer.parseInt(listOff_set.get(0)) == 0) &
                    ((Integer.parseInt(listAdvCount.get(0))) / 15) == (Integer.parseInt(listMF.get(0)))) {
                int FirstMF = Integer.parseInt(listMF.get(0));

                int SecondMF = Integer.parseInt(listMF.get(1));


                String SecondProfile = listProfile.get(1);

                if(FirstMF <= SecondMF){
                    Cursor cursordeldup = DB.rawQuery("Select * from Userdetails WHERE Profile = ? ", new String[] {SecondProfile});

                    if (cursordeldup.getCount() > 0){
                        long resultdel = DB.delete("Userdetails", "Profile=?", new String[]{SecondProfile});

                    }

                }
                else if((FirstMF - 1) == SecondMF){



                    String OutAdvCount = String.valueOf( (Integer.parseInt(listAdvCount.get(0)) - (listDataAdv.get(1)).length()) );

                    String OutOff_set = "0";
                    String OutMF = String.valueOf(Integer.parseInt(listMF.get(0) ) -1);
                    String OutDataAdv = listDataAdv.get(0) + listDataAdv.get(1);
                    String OutRssi = listRssi.get(1);



                    Log.i(TAG, "KQ ------ OutAdvCount---:" + OutAdvCount);
                    Log.i(TAG, "KQ ------ OutMF---:" + OutMF);
                    Log.i(TAG, "KQ ------ OutDataAdv---:" + OutDataAdv);
                    Log.i(TAG, "KQ ------ OutRssi---:" + String.valueOf(OutRssi));





                    String FirstID = listID.get(0);


                    String FirstProfile = listProfile.get(0);


                    Cursor cursordel = DB.rawQuery("Select * from Userdetails WHERE Profile = ? ", new String[] {SecondProfile});

                    if (cursordel.getCount() > 0){
                        long resultdel = DB.delete("Userdetails", "Profile=?", new String[]{SecondProfile});
//-                        Log.i(TAG, "KQ ------ FirstProfile:" + SecondProfile);
                    }
                    else{
                        Log.i(TAG,"ERROR cursordel....");
                    }



                    Cursor cursorinsert = DB.rawQuery("Select * from Userdetails WHERE Profile = ? ", new String[] {FirstProfile});
                    contentValues.put("AdvCount", OutAdvCount);
                    contentValues.put("Off_set", OutOff_set);
                    contentValues.put("MF", OutMF);
                    contentValues.put("DataAdv", OutDataAdv);
                    contentValues.put("Rssi", OutRssi);

//-                    Log.i(TAG,"KQ Count cursordel ++++++++:" + String.valueOf(cursorinsert.getCount()));

                    if (cursorinsert.getCount() > 0) {
                        long result = DB.update("Userdetails", contentValues, "Profile=?", new String[] {FirstProfile});
//-                        Log.i(TAG, "KQ ------ FirstProfile:" + FirstProfile);
                    }
                    else{
                        Log.i(TAG,"ERROR cursorinsert...");
                    }


                }

            }



    }




    public void deletedata () {
        SQLiteDatabase DB = this.getWritableDatabase();
        long result = DB.delete("Userdetails",null, null);
        return;
    }


    public Cursor getdata ()
    {
        SQLiteDatabase DB = this.getWritableDatabase();
        Cursor cursor = DB.rawQuery("Select * from Userdetails", null);
        return cursor;

    }





}
