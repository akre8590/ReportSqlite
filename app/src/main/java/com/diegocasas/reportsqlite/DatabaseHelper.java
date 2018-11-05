package com.diegocasas.reportsqlite;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.widget.Toast;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private Font fTitle = new Font(Font.FontFamily.TIMES_ROMAN, 9, Font.BOLD);
    private Font fSubtitle = new Font(Font.FontFamily.TIMES_ROMAN, 18, Font.BOLD);
    private Font fText = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD);
    private Font fHighText = new Font(Font.FontFamily.TIMES_ROMAN, 9, Font.BOLD, BaseColor.RED);

    private static final String DATABASE_NAME = "datos.db3";
    private static final int DATABASE_VERSION = 1;
    private final Context context;
    SQLiteDatabase db;

    private static final String DATABASE_PATH = "/storage/emulated/0/db/";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
        createDb();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
    public void createDb(){
        boolean dbExist = checkDbExist();

        if(!dbExist){
            this.getReadableDatabase();
            copyDatabase();
        }
    }
    private void copyDatabase(){
        try {
            InputStream inputStream = context.getAssets().open(DATABASE_NAME);

            String outFileName = DATABASE_PATH + DATABASE_NAME;

            OutputStream outputStream = new FileOutputStream(outFileName);

            byte[] b = new byte[1024];
            int length;

            while ((length = inputStream.read(b)) > 0){
                outputStream.write(b, 0, length);
            }

            outputStream.flush();
            outputStream.close();
            inputStream.close();
        } catch (IOException e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    private boolean checkDbExist(){
        SQLiteDatabase sqLiteDatabase = null;

        try{
            String path = DATABASE_PATH + DATABASE_NAME;
            sqLiteDatabase = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY);
        } catch (Exception ex){
            Toast.makeText(context, ex.getMessage(), Toast.LENGTH_SHORT).show();
        }

        if(sqLiteDatabase != null){
            sqLiteDatabase.close();
            return true;
        }
        return false;
    }
    private SQLiteDatabase openDatabase(){
        String path = DATABASE_PATH + DATABASE_NAME;
        db = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READWRITE);
        return db;
    }
    public void close(){
        if(db != null){
            db.close();
        }
    }
    @TargetApi(Build.VERSION_CODES.KITKAT)
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void generateReportPDF() throws FileNotFoundException, DocumentException {
        String selectQuery = "SELECT CONS_INM, CASE WHEN TRIM(NUM_INT)<>\"\" THEN NUM_EXT||\"/\"||NUM_INT \n" +
                "ELSE NUM_EXT END NUMERO, ETIQUETA, MED_LUZ, DESC_UBIC_E, RASGO_PERD_E, \n" +
                "CASE WHEN RESVIS1=\"1\" THEN \"Entrevista completa\" \n" +
                "WHEN RESVIS1=\"2\" THEN \"Entrevista incompleta\" \n" +
                "WHEN RESVIS1=\"3\" THEN \"Ausencia de residentes\" \n" +
                "WHEN RESVIS1=\"4\" THEN \"Negativa\" \n" +
                "WHEN RESVIS1=\"5\" THEN \"Deshabitada\" \n" +
                "WHEN RESVIS1=\"6\" THEN \"Uso temporal\" \n" +
                "WHEN RESVIS1=\"7\" THEN \"Vivienda colectiva\" \n" +
                "WHEN RESVIS1=\"8\" THEN \"No es vivienda\" \n" +
                "WHEN RESVIS1=\"9\" THEN \"Invitación de internet\" \n" +
                "WHEN RESVIS1=\"0\" THEN \"Error de registro\" END RESVISF, \n" +
                "CASE WHEN TIPO_INM=\"1\" THEN \"Casa o departamento\" \n" +
                "WHEN TIPO_INM=\"2\" THEN \"Establecimiento de salud\" \n" +
                "WHEN TIPO_INM=\"3\" THEN \"Escuela\" \n" +
                "WHEN TIPO_INM=\"4\" THEN \"Edificación en ruinas o en construcción\" \n" +
                "WHEN TIPO_INM=\"5\" THEN \"Parque, jardín o plaza pública\" \n" +
                "WHEN TIPO_INM=\"6\" THEN \"Infraestructura\" \n" +
                "WHEN TIPO_INM=\"7\" THEN \"Lote baldío\" \n" +
                "WHEN TIPO_INM=\"8\" THEN \"Local\" \n" +
                "WHEN TIPO_INM=\"9\" THEN \"Otro tipo de inmueble\" \n" +
                "WHEN TIPO_INM=\"0\" THEN \"Frente sin inmuebles\" END TIPO_INM, \n" +
                "CASE WHEN SUP_RESULT=\"1\" THEN \"Correcto\" \n" +
                "WHEN SUP_RESULT=\"2\" THEN \"Incorrecto\" \n" +
                "WHEN SUP_RESULT=\"3\" THEN \"No encontrado\" \n" +
                "WHEN SUP_RESULT=\"4\" THEN \"Invasión\" END SUP_RESULT, ID_INM \n" +
                "FROM TR_INM ";
        db = openDatabase();
        String filename="Report.pdf";

        Cursor c1 = db.rawQuery(selectQuery, null);
        Document document = new Document();
        File root = new File(Environment.getExternalStorageDirectory().toString(), "PDF");
        if (!root.exists()) {
            root.mkdirs();
        }
        File gpxfile = new File(root,filename);
        PdfWriter.getInstance(document,new FileOutputStream(gpxfile));
        document.open();

        Paragraph p3 = new Paragraph();
        p3.add("REPORTE: ");
        p3.setSpacingAfter(30);
        p3.setFont(fTitle);
        p3.setAlignment(Element.ALIGN_CENTER);
        document.add(p3);

        PdfPTable table = new PdfPTable(10);
        table.setWidthPercentage(100);
        table.addCell(new Paragraph("CONS_INM", fHighText));
        table.addCell(new Paragraph("NÚMERO", fHighText));
        table.addCell(new Paragraph("ETIQUETA", fHighText));
        table.addCell(new Paragraph("MED_LUZ", fHighText));
        table.addCell(new Paragraph("DESC_UBIC_E", fHighText));
        table.addCell(new Paragraph("RASGO_PERD_E", fHighText));
        table.addCell(new Paragraph("REVISF", fHighText));
        table.addCell(new Paragraph("TIPO_INM", fHighText));
        table.addCell(new Paragraph("SUP_RESULT", fHighText));
        table.addCell(new Paragraph("ID_INM", fHighText));
        while (c1.moveToNext()) {
            String CONS_INM = c1.getString(0);
            String NÚMERO = c1.getString(1);
            String ETIQUETA = c1.getString(2);
            String MED_LUZ = c1.getString(3);
            String DESC_UBIC_E = c1.getString(4);
            String RASGO_PERD_E = c1.getString(5);
            String RESVISF = c1.getString(6);
            String TIPO_INM = c1.getString(7);
            String SUP_RESULT = c1.getString(8);
            String ID_INM = c1.getString(9);

            table.addCell(CONS_INM);
            table.addCell(NÚMERO);
            table.addCell(ETIQUETA);
            table.addCell(MED_LUZ);
            table.addCell(DESC_UBIC_E);
            table.addCell(RASGO_PERD_E);
            table.addCell(RESVISF);
            table.addCell(TIPO_INM);
            table.addCell(SUP_RESULT);
            table.addCell(ID_INM);
        }
        document.add(table);
        document.addCreationDate();
        document.close();
        Toast.makeText(context, "Reporte creado exitosamente", Toast.LENGTH_SHORT).show();
    }
}
