package com.app.persomy.v4;

import android.Manifest;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowMetrics;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Currency;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {

    /* VARIABILI DATA BASE */
    private static final String DATABASE_NAME = "PersoMyDB.db";
    private static final int REQUEST_WRITE_STORAGE = 112;
    private static Boolean uscita = true;
    /* CALENDARI E OROLOGI */
    private final Calendar c = Calendar.getInstance();
    private final DecimalFormat df = new DecimalFormat("###,##0.00");
    private final Verify verify = new Verify();
    private int mYear, mMonth, mDay, mHourOfDay, mMinute, menu_choise = R.menu.main, CURRENT_LAYOUT, id, j, da;
    private String giorni = String.valueOf(mDay), mesi = String.valueOf(mMonth), ora = String.valueOf(mHourOfDay), minuti = String.valueOf(mMinute);
    private String strPassword1;
    private String strPassword2;
    private String message = null;
    private LayoutInflater li;
    private Activity activity;
    private AlertDialog progressDialog;
    /* COMPONENTI FISICI DI LAYOUT */
    private TableLayout contentPane;
    private ListView myListView;
    private Spinner descrizioneSpesa, voceAutomatica, mese, anno, frequenza;
    private TextView mDateDisplayUscite, mDateDisplay, mTimeDisplay, mDateDisplayDa, mDateDisplayA;
    private RadioButton entrateRB, usciteRB;
    private EditText importoAutomatica, password1, password2, input;
    private View layout;
    private GridView myGridView;
    private SwitchCompat password, update;
    private CheckBox all;
    /* ARRAY */
    private ArrayList<Frequenza> myFrequenza;
    private ArrayList<Lista> myLista;
    private ArrayList<ListaReportAnno> myListaReportAnno;
    private ArrayList<Movimento> myMovimento;
    private ArrayList<Spesa> mySpesa;
    private ArrayAdapter<ListaReportAnno> myAdapterListaReporAnno;
    private ArrayAdapter<Lista> myAdapterLista;
    private ArrayAdapter<Movimento> myAdapterAutomatica;
    private ArrayAdapter<Spesa> myAdapter;
    private SQLiteDatabase database;
    private Cursor cur;
    /* CLASSI DI SUPPORTO */
    private ScaricaDati scaricaDati;
    private DatePickerDialog dialogDate, dialogDateA = null;
    private TimePickerDialog dialogTime = null;
    private SimpleDateFormat dateFormat;
    private Date dateObj;


    public MainActivity() {
    }

    private static void copyFile(File src, File dst) throws IOException {
        try (FileInputStream inStream = new FileInputStream(src); FileOutputStream outStream = new FileOutputStream(dst); FileChannel inChannel = inStream.getChannel(); FileChannel outChannel = outStream.getChannel()) {

            inChannel.transferTo(0, inChannel.size(), outChannel);
        } catch (IOException e) {
            Log.e("CopyFile", "IO Exception occurred", e);
        }
    }

    private float dpToPxMax(Context context, int dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    @Override
    protected void onDestroy() {
        if (cur != null && !cur.isClosed()) {
            cur.close();
        }
        if (database != null && database.isOpen()) {
            database.close();
        }
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            }
        } else {
            /* For devices running Android 10 or lower */
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

        myFrequenza = new ArrayList<>();
        myFrequenza.add(new Frequenza("Giornaliero"));
        myFrequenza.add(new Frequenza("Settimanale"));
        myFrequenza.add(new Frequenza("Decadale"));
        myFrequenza.add(new Frequenza("Mensile"));
        myFrequenza.add(new Frequenza("Semestrale"));
        myFrequenza.add(new Frequenza("Annuale"));
        myFrequenza.add(new Frequenza("Primo giorno del mese"));
        myFrequenza.add(new Frequenza("Ultimo giorno del mese"));

        contentPane = findViewById(R.id.contentPane);

        PersoMyDataSource datasource = new PersoMyDataSource(getApplicationContext());
        datasource.open();
        database = openOrCreateDatabase(DATABASE_NAME, Context.MODE_PRIVATE, null);

        try {
            if (PersoMyDB.latch != null) PersoMyDB.latch.await();

        } catch (InterruptedException e) {
            Log.e("onCreate", "Thread was interrupted", e);
        }

        if (verify.isTherePassword(database)) {
            id = 1;
            msgBox();
        }

        if (verify.isThereJobAutomatico(database)) {
            startJobAutomatici();
        }

        scaricaDati = new ScaricaDati(database);

        activity = this;

        registraContextMenu();

        ActionBar bar = getActionBar();
        if (bar != null) {
            bar.setDisplayHomeAsUpEnabled(true);
            bar.setDisplayShowTitleEnabled(true);
        } else {
            Log.e("MainActivity", "ActionBar non disponibile. Verifica il tema dell'attività.");
        }

        chiamaBottoni();

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (menu_choise == R.menu.main) {
                id = 7;
                msgBox();
                return true;
            }
            menu_choise = R.menu.main;
            contentPane.removeAllViews();
            setContentView(R.layout.activity_main);
            setTitle(R.string.title_activity_main);
            contentPane = findViewById(R.id.contentPane);
            registraContextMenu();
            invalidateOptionsMenu();
            chiamaBottoni();
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_MENU) {
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    /* *******************************************************************/
    /* MENU'*/
    /* *******************************************************************/
    private void registraContextMenu() {
        registerForContextMenu(findViewById(R.id.reportBtn));
        registerForContextMenu(findViewById(R.id.backupBtn));
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        MenuInflater inflater = getMenuInflater();

        if (menu_choise == R.menu.menu_report) {
            inflater.inflate(R.menu.menu_report, menu);
        } else if (menu_choise == R.menu.menu_backup) {
            inflater.inflate(R.menu.menu_backup, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_mensile) {
            setTitle(getString(R.string.title_activity_main) + " - " + getString(R.string.menu_report) + ": " + getString(R.string.menu_mensile));
            menu_choise = 0;
            startMensile();
        } else if (item.getItemId() == R.id.menu_periodo) {
            setTitle(getString(R.string.title_activity_main) + " - " + getString(R.string.menu_report) + ": " + getString(R.string.menu_periodo));
            /* menu_choise = R.menu.menu_solo_exit; */
            menu_choise = 0;
            startPeriodo();
        } else if (item.getItemId() == R.id.menu_anno) {
            setTitle(getString(R.string.title_activity_main) + " - " + getString(R.string.menu_report) + ": " + getString(R.string.menu_anno));
            /* menu_choise = R.menu.menu_solo_exit; */
            menu_choise = 0;
            startAnno();
        } else if (item.getItemId() == R.id.menu_totale) {
            setTitle(getString(R.string.title_activity_main) + " - " + getString(R.string.menu_report) + ": " + getString(R.string.menu_totale));
            /* menu_choise = R.menu.menu_solo_exit; */
            menu_choise = 0;
            startTotale();
        } else if (item.getItemId() == R.id.menu_voce) {
            /* menu_choise = R.menu.menu_solo_exit; */
            menu_choise = 0;
            setTitle(getString(R.string.title_activity_main) + " - " + getString(R.string.menu_report) + ": " + getString(R.string.menu_voce));
            startVoce();
        } else if (item.getItemId() == R.id.menu_backup) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (!Environment.isExternalStorageManager()) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                } else {
                    startBackup();
                }
            } else {
                startBackup();
            }
        } else if (item.getItemId() == R.id.menu_ripristino_backup) {
            startRestoreBackup();
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_WRITE_STORAGE) {
            /* Check if the permission request was granted */
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startBackup();
            } else {
                Toast.makeText(this, "Storage permission is required to perform the backup.", Toast.LENGTH_LONG).show();

                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    new AlertDialog.Builder(this).setTitle("Permission Required").setMessage("Storage permission is needed to perform the backup. Please enable it in the app settings.").setPositiveButton("Open Settings", (dialog, which) -> {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                    }).setNegativeButton("Cancel", null).show();
                }
            }
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();

        MenuInflater inflater = getMenuInflater();

        if (menu_choise == R.menu.menu_uscite) {
            inflater.inflate(R.menu.menu_uscite, menu);
        } else if (menu_choise == R.menu.main) {
            inflater.inflate(R.menu.main, menu);
        } else if (menu_choise == R.menu.menu_elimina) {
            inflater.inflate(R.menu.menu_elimina, menu);
        } else if (menu_choise == R.menu.menu_report) {
            inflater.inflate(R.menu.menu_report, menu);
        } else if (menu_choise == R.menu.menu_solo_exit) {
            inflater.inflate(R.menu.menu_solo_exit, menu);
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        if (menu_choise == R.menu.menu_uscite) {
            inflater.inflate(R.menu.menu_uscite, menu);
        } else if (menu_choise == R.menu.main) {
            inflater.inflate(R.menu.main, menu);
        } else if (menu_choise == R.menu.menu_report) {
            inflater.inflate(R.menu.menu_report, menu);
        } else if (menu_choise == R.menu.menu_elimina) {
            inflater.inflate(R.menu.menu_elimina, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_exit_spese) {
            menu_choise = R.menu.main;

            contentPane.removeAllViews();
            setContentView(R.layout.activity_main);
            setTitle(R.string.title_activity_main);
            contentPane = findViewById(R.id.contentPane);

            registraContextMenu();

            invalidateOptionsMenu();

            return true;
        } else if (item.getItemId() == android.R.id.home) {
            if (menu_choise == R.menu.main) {
                id = 7;
                msgBox();
                return true;
            }

            menu_choise = R.menu.main;

            contentPane.removeAllViews();
            setContentView(R.layout.activity_main);
            setTitle(R.string.title_activity_main);
            contentPane = findViewById(R.id.contentPane);

            registraContextMenu();

            invalidateOptionsMenu();
            return true;
        } else if (item.getItemId() == R.id.menu_elimina_varie) {
            eliminaVarie();
            return true;
        } else if (item.getItemId() == R.id.help) {
            id = 10;
            msgBox();
            return true;
        } else if (item.getItemId() == R.id.menu_info) {
            startInfo();
            return true;
        } else if (item.getItemId() == R.id.menu_salva_spese) {
            if (CURRENT_LAYOUT == R.layout.activity_uscite) {
                try {
                    database.beginTransaction();
                    ContentValues row = new ContentValues();

                    dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT);
                    dateObj = dateFormat.parse(mYear + "-" + (mMonth + 1) + "-" + mDay + " 00:00:00");
                    database.delete("MONEY", "data=? and uscita=" + ((uscita) ? 1 : 0), new String[]{dateFormat.format(dateObj)});

                    for (int i = 0; i < mySpesa.size(); i++) {
                        row.put("DATA", dateFormat.format(dateObj));
                        row.put("DESCRIZIONE", scaricaDati.getSpesaCodiceDescrizione(mySpesa.get(i).getSpesaName()));
                        row.put("PREZZO", mySpesa.get(i).getSpesaPrezzo());
                        row.put("USCITA", ((uscita) ? 1 : 0));

                        database.insert("MONEY", null, row);
                    }

                    database.setTransactionSuccessful();

                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                } finally {
                    database.endTransaction();

                    Toast.makeText(getApplicationContext(), R.string.spesaSalvataggioOk, Toast.LENGTH_SHORT).show();
                }

                return true;
            } else if (CURRENT_LAYOUT == R.layout.activity_automatiche) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setCancelable(false);
                builder.setView(R.layout.dialog_loading);
                progressDialog = builder.create();
                progressDialog.show();

                new Thread(() -> {
                    try {
                        message = null;
                        database.beginTransaction();
                        ContentValues row = new ContentValues();

                        database.delete("JOB_AUTOMATICI", "id_movimento in (select id from movimenti_automatici where uscita=" + ((uscita) ? 1 : 0) + ")", null);
                        database.delete("MOVIMENTI_AUTOMATICI", "uscita=" + ((uscita) ? 1 : 0), null);

                        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT);

                        for (int i = 0; i < myMovimento.size(); i++) {
                            int codiceFrequenza = scaricaDati.frequenzaToNumber(myMovimento.get(i).getAutomaticaFrequenza());
                            int idVoce = scaricaDati.getSpesaCodiceDescrizione(myMovimento.get(i).getAutomaticaVoce());

                            dateObj = dateFormat.parse(myMovimento.get(i).getAutomaticaStartDate().substring(6, 10) + "-" + myMovimento.get(i).getAutomaticaStartDate().substring(3, 5) + "-" + myMovimento.get(i).getAutomaticaStartDate().substring(0, 2) + " " + myMovimento.get(i).getAutomaticaStartDate().substring(11, 13) + ":" + myMovimento.get(i).getAutomaticaStartDate().substring(14, 16) + ":00");
                            c.setTime(dateObj);

                            int giorno, mese, anno, ore = c.get(Calendar.HOUR_OF_DAY), minuti = c.get(Calendar.MINUTE), secondi = c.get(Calendar.SECOND), giorniFrequenza = scaricaDati.getGiorniFrequenza(myMovimento.get(i).getAutomaticaFrequenza());

                            row.clear();
                            row.put("ID_FREQUENZA", codiceFrequenza);
                            row.put("DATA_START", dateFormat.format(dateObj));
                            row.put("ID_VOCE", idVoce);
                            row.put("IMPORTO", myMovimento.get(i).getAutomaticaImporto());
                            row.put("USCITA", ((uscita) ? 1 : 0));
                            database.insert("MOVIMENTI_AUTOMATICI", null, row);

                            String cancellaJob = scaricaDati.getIDMovimento(codiceFrequenza, dateFormat.format(dateObj), idVoce, myMovimento.get(i).getAutomaticaImporto(), uscita);

                            if (giorniFrequenza == 0) {
                                if (codiceFrequenza == 3) {
                                    for (int j = 0; j < 36; j++) {
                                        mese = c.get(Calendar.MONTH) + 1;
                                        anno = c.get(Calendar.YEAR);

                                        row.clear();
                                        row.put("ID_MOVIMENTO", cancellaJob);
                                        row.put("DATA_START", dateFormat.format(dateFormat.parse(anno + "-" + mese + "-" + "01" + " " + ore + ":" + minuti + ":00")));
                                        database.insert("JOB_AUTOMATICI", null, row);

                                        row.clear();
                                        row.put("ID_MOVIMENTO", cancellaJob);
                                        row.put("DATA_START", dateFormat.format(dateFormat.parse(anno + "-" + mese + "-" + "11" + " " + ore + ":" + minuti + ":00")));
                                        database.insert("JOB_AUTOMATICI", null, row);

                                        row.clear();
                                        row.put("ID_MOVIMENTO", cancellaJob);
                                        row.put("DATA_START", dateFormat.format(dateFormat.parse(anno + "-" + mese + "-" + "21" + " " + ore + ":" + minuti + ":00")));
                                        database.insert("JOB_AUTOMATICI", null, row);

                                        c.add(Calendar.DATE, 30);
                                    }

                                    database.delete("JOB_AUTOMATICI", "ID_MOVIMENTO = ? AND DATA_START < ? ", new String[]{cancellaJob, dateFormat.format(dateObj)});

                                }

                                if (codiceFrequenza == 7) for (int j = 0; j < 36; j++) {
                                    mese = c.get(Calendar.MONTH) + 1;
                                    anno = c.get(Calendar.YEAR);

                                    row.clear();
                                    row.put("ID_MOVIMENTO", cancellaJob);
                                    row.put("DATA_START", dateFormat.format(dateFormat.parse(anno + "-" + mese + "-" + "01" + " " + ore + ":" + minuti + ":00")));
                                    database.insert("JOB_AUTOMATICI", null, row);

                                    c.add(Calendar.DATE, 30);
                                }

                                if (codiceFrequenza == 8) for (int j = 0; j < 36; j++) {
                                    giorno = c.getActualMaximum(Calendar.DATE);
                                    mese = c.get(Calendar.MONTH) + 1;
                                    anno = c.get(Calendar.YEAR);

                                    row.clear();
                                    row.put("ID_MOVIMENTO", cancellaJob);
                                    row.put("DATA_START", dateFormat.format(dateFormat.parse(anno + "-" + mese + "-" + giorno + " " + ore + ":" + minuti + ":00")));
                                    database.insert("JOB_AUTOMATICI", null, row);

                                    c.add(Calendar.DATE, 30);
                                }
                            } else {
                                for (int j = 0; j < 1095; j++) {
                                    giorno = c.get(Calendar.DAY_OF_MONTH);
                                    mese = c.get(Calendar.MONTH) + 1;
                                    anno = c.get(Calendar.YEAR);

                                    row.clear();
                                    row.put("ID_MOVIMENTO", cancellaJob);
                                    row.put("DATA_START", dateFormat.format(dateFormat.parse(anno + "-" + mese + "-" + giorno + " " + ore + ":" + minuti + ":" + secondi)));
                                    database.insert("JOB_AUTOMATICI", null, row);

                                    c.add(Calendar.DATE, giorniFrequenza);
                                }
                            }
                        }

                        startJobAutomatici();
                        database.setTransactionSuccessful();
                        progressDialog.dismiss();

                    } catch (Exception e) {

                        message = e.getMessage();

                    } finally {
                        database.endTransaction();
                        message = null;
                    }

                    MainActivity.this.runOnUiThread(() -> {
                        if (message == null)
                            Toast.makeText(getApplicationContext(), R.string.spesaSalvataggioOk, Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                    });
                }).start();
            }
            return true;
        } else if (item.getItemId() == R.id.addDescrizione) {
            id = 4;
            msgBox();
            return true;
        } else if (item.getItemId() == R.id.modificaDescrizione) {
            id = 3;
            descrizioneSpesa = findViewById(R.id.descrizioneSpesa);
            if (descrizioneSpesa.getCount() > 0) msgBox();
            return true;
        } else if (item.getItemId() == R.id.deleteSpesa) {
            if (CURRENT_LAYOUT == R.layout.activity_uscite) {
                if (!mySpesa.isEmpty()) {
                    for (int i = mySpesa.size() - 1; i > -1; i--) {
                        if (mySpesa.get(i).getSpesaFlaggata()) {
                            mySpesa.remove(i);
                            j++;
                        }
                    }
                    myListView.setAdapter(myAdapter);

                    if (j == 0)
                        Toast.makeText(getApplicationContext(), R.string.spesaCancellaVoce, Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(getApplicationContext(), R.string.spesaCancellaVoce, Toast.LENGTH_SHORT).show();
                }

                return true;
            } else if (CURRENT_LAYOUT == R.layout.activity_automatiche) {
                if (!myMovimento.isEmpty()) {
                    for (int i = myMovimento.size() - 1; i > -1; i--) {
                        if (myMovimento.get(i).getAutomaticaFlaggata()) {
                            myMovimento.remove(i);
                            j++;
                        }
                    }
                    myListView.setAdapter(myAdapterAutomatica);

                    if (j == 0)
                        Toast.makeText(getApplicationContext(), R.string.spesaCancellaVoce, Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(getApplicationContext(), R.string.spesaCancellaVoce, Toast.LENGTH_SHORT).show();
                }

                return true;
            }

            return true;
        }

        return true;
    }

    public void onSearchBtnPress(View v) {
        Date dateObjA;
        try {
            if (mDateDisplayDa == null || mDateDisplayA == null || mDateDisplayDa.getText().toString().isEmpty() || mDateDisplayA.getText().toString().isEmpty()) {
                Toast.makeText(this, "Inserire le date richieste", Toast.LENGTH_SHORT).show();
                return;
            }

            dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT);
            dateObj = dateFormat.parse(mDateDisplayDa.getText().toString().substring(6, 10) + "-" + mDateDisplayDa.getText().toString().substring(3, 5) + "-" + mDateDisplayDa.getText().toString().substring(0, 2) + " 00:00:00");
            dateObjA = dateFormat.parse(mDateDisplayA.getText().toString().substring(6, 10) + "-" + mDateDisplayA.getText().toString().substring(3, 5) + "-" + mDateDisplayA.getText().toString().substring(0, 2) + " 00:00:00");

            if (dateObj == null || dateObjA == null) {
                Toast.makeText(this, "Errore nel parsing delle date", Toast.LENGTH_SHORT).show();
                return;
            }

        } catch (Exception e) {
            Log.e("onSearchBtnPress", "Errore nel parsing delle date", e);
            Toast.makeText(this, "Errore nel parsing delle date: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            descrizioneSpesa = findViewById(R.id.descrizioneSpesa);
            all = findViewById(R.id.selectAll);

            if (descrizioneSpesa == null || all == null) {
                Toast.makeText(this, "Errore: Componenti mancanti nel layout", Toast.LENGTH_SHORT).show();
                return;
            }

            if (all.isChecked() && descrizioneSpesa.getSelectedItem() != null) {
                cur = database.query("MONEY a INNER JOIN VARIE B ON (a.DESCRIZIONE=b.CONT)", new String[]{"a.DATA", "b.DESCRIZIONE", "a.prezzo", "a.uscita"}, "data>=? and data<=?", new String[]{dateFormat.format(dateObj), dateFormat.format(dateObjA)}, null, null, "a.DATA, a.uscita");
            } else if (descrizioneSpesa.getSelectedItem() != null) {
                cur = database.query("MONEY a INNER JOIN VARIE B ON (a.DESCRIZIONE=b.CONT)", new String[]{"a.DATA", "b.DESCRIZIONE", "a.prezzo", "a.uscita"}, "data>=? and data<=? and b.descrizione=?", new String[]{dateFormat.format(dateObj), dateFormat.format(dateObjA), descrizioneSpesa.getSelectedItem().toString()}, null, null, "a.DATA, a.uscita");
            } else {
                Toast.makeText(this, "Selezionare una descrizione", Toast.LENGTH_SHORT).show();
                return;
            }

            if (cur.getCount() == 0) {
                Toast.makeText(this, "Nessun dato trovato", Toast.LENGTH_SHORT).show();
                return;
            }

            myLista = new ArrayList<>();
            cur.moveToFirst();

            while (!cur.isAfterLast()) {
                try {
                    Date date = dateFormat.parse(cur.getString(0));
                    dateFormat.applyPattern("dd/MM/yyyy");
                    Boolean uscita = "1".equals(cur.getString(3));
                    myLista.add(new Lista(dateFormat.format(date), cur.getString(1), Double.parseDouble(cur.getString(2)), uscita));
                    cur.moveToNext();
                } catch (Exception e) {
                    Log.e("onSearchBtnPress", "Errore durante la lettura del database", e);
                }
            }

            cur.close();

            // Imposta l'adapter per la GridView
            myAdapterLista = new ListaMovimentiGridViewAdapter(this, myLista);
            myGridView = findViewById(R.id.listaMovimenti);

            if (myGridView != null) {
                myGridView.setAdapter(myAdapterLista);

                int screenHeight = getScreenHeight();

                LayoutParams lp = myGridView.getLayoutParams();
                lp.height = screenHeight * 70 / 100;
                myGridView.setLayoutParams(lp);

            } else {
                Toast.makeText(this, "Errore: GridView non trovata", Toast.LENGTH_SHORT).show();
            }

            // Calcolo Totali
            calculateTotals(dateObj, dateObjA, descrizioneSpesa, all);

        } catch (Exception e) {
            Log.e("onSearchBtnPress", "Errore durante l'elaborazione", e);
            Toast.makeText(this, "Errore durante l'elaborazione: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private int getScreenHeight() {
        int screenHeight;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Usa WindowMetrics per ottenere le dimensioni dello schermo
            WindowMetrics metrics = getWindowManager().getCurrentWindowMetrics();
            Rect bounds = metrics.getBounds();
            screenHeight = bounds.height();
        } else {
            // Usa DisplayMetrics per le versioni precedenti
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            screenHeight = displayMetrics.heightPixels;
        }
        return screenHeight;
    }

    private void calculateTotals(Date dateObj, Date dateObjA, Spinner descrizioneSpesa, CheckBox all) {
        try {
            TextView totUscite = findViewById(R.id.totaleUscite);
            TextView totEntrate = findViewById(R.id.totaleEntrate);

            if (totUscite == null || totEntrate == null) {
                Toast.makeText(this, "Errore: TextView per totali non trovate", Toast.LENGTH_SHORT).show();
                return;
            }

            String[] dateRange = {dateFormat.format(dateObj), dateFormat.format(dateObjA)};

            cur = all.isChecked() ? database.query("MONEY", new String[]{"SUM(prezzo)"}, "uscita=0 and data>=? and data<=?", dateRange, null, null, null) : database.query("MONEY a INNER JOIN VARIE B ON (a.DESCRIZIONE=b.CONT)", new String[]{"SUM(prezzo)"}, "uscita=0 and data>=? and data<=? and b.descrizione=?", new String[]{dateFormat.format(dateObj), dateFormat.format(dateObjA), descrizioneSpesa.getSelectedItem().toString()}, null, null, null);

            if (cur.moveToFirst() && cur.getString(0) != null) {
                totEntrate.setText(df.format(Double.parseDouble(cur.getString(0))));
            } else {
                totEntrate.setText("0");
            }

            if (cur != null) cur.close();

            cur = all.isChecked() ? database.query("MONEY", new String[]{"SUM(prezzo)"}, "uscita=1 and data>=? and data<=?", dateRange, null, null, null) : database.query("MONEY a INNER JOIN VARIE B ON (a.DESCRIZIONE=b.CONT)", new String[]{"SUM(prezzo)"}, "uscita=1 and data>=? and data<=? and b.descrizione=?", new String[]{dateFormat.format(dateObj), dateFormat.format(dateObjA), descrizioneSpesa.getSelectedItem().toString()}, null, null, null);

            if (cur.moveToFirst() && cur.getString(0) != null) {
                totUscite.setText(df.format(Double.parseDouble(cur.getString(0))));
            } else {
                totUscite.setText("0");
            }

            if (cur != null) cur.close();

        } catch (Exception e) {
            Log.e("calculateTotals", "Errore nel calcolo dei totali", e);
            Toast.makeText(this, "Errore nel calcolo dei totali", Toast.LENGTH_SHORT).show();
        }
    }

    private void resetCalendario() {
        // Imposta la data iniziale
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);

        // Crea i dialoghi
        dialogDate = new DatePickerDialog(this, new PickDate(), mYear, mMonth, mDay);
        dialogDateA = new DatePickerDialog(this, new PickDate(), mYear, mMonth, mDay);

        // Personalizza il primo dialogo
        dialogDate.setOnShowListener(dialog -> {
            styleDatePickerHeader(dialogDate, mMonth, mYear);
            styleDialogButtons(dialogDate);
        });

        // Personalizza il secondo dialogo
        dialogDateA.setOnShowListener(dialog -> {
            styleDatePickerHeader(dialogDateA, mMonth, mYear);
            styleDialogButtons(dialogDateA);
        });

        // Aggiorna la data nei dialoghi
        dialogDate.updateDate(mYear, mMonth, mDay);
        dialogDateA.updateDate(mYear, mMonth, mDay);

        // Aggiorna la visualizzazione
        updateDisplay(0);
    }

    private void styleDatePickerHeader(DatePickerDialog dialog, int month, int year) {

        int minSize = (int) dpToPxMax(this, 48);
        int padding = (int) dpToPxMax(this, 8);
        int backgroundColor = Color.parseColor("#004D40");
        int textColor = Color.WHITE;

        DatePicker datePicker = dialog.getDatePicker();
        datePicker.setContentDescription(getString(R.string.dataCalendario));

        ViewGroup datePickerViewGroup = (ViewGroup) datePicker.getChildAt(0);
        if (datePickerViewGroup != null) {

            View headerYearView = datePickerViewGroup.findViewById(Resources.getSystem().getIdentifier("date_picker_header_year", "id", "android"));
            if (headerYearView instanceof TextView yearTextView) {
                yearTextView.setMinimumHeight(minSize);
                yearTextView.setMinimumWidth(minSize);
                yearTextView.setTextColor(textColor);
                yearTextView.setBackgroundColor(backgroundColor);
                yearTextView.setPadding(padding, padding, padding, padding);
                yearTextView.setContentDescription(getString(R.string.header_year_accessibility_label));
            }

            View headerDateView = datePickerViewGroup.findViewById(Resources.getSystem().getIdentifier("date_picker_header_date", "id", "android"));

            if (headerDateView instanceof TextView dateTextView) {
                dateTextView.setMinimumHeight(minSize);
                dateTextView.setMinimumWidth(minSize);
                dateTextView.setTextColor(textColor);
                dateTextView.setBackgroundColor(backgroundColor);
                dateTextView.setPadding(padding, padding, padding, padding);
                dateTextView.setContentDescription(getString(R.string.header_date_accessibility_label));
            }

            View monthView = datePicker.findViewById(Resources.getSystem().getIdentifier("month_view", "id", "android"));
            if (monthView != null) {
                monthView.setContentDescription(getString(R.string.month_view_accessibility_label));
            }

            View headerView = datePickerViewGroup.findViewById(Resources.getSystem().getIdentifier("date_picker_header", "id", "android"));
            if (headerView instanceof TextView monthYearTextView) {
                monthYearTextView.setContentDescription(getString(R.string.date_picker_month_year, month + 1, year));
                monthYearTextView.setTextColor(Color.parseColor("#2E7D32"));
                monthYearTextView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                monthYearTextView.setTextSize(18);
                monthYearTextView.setPadding(padding, padding, padding, padding);
                monthYearTextView.setMinimumHeight(minSize);
                monthYearTextView.setMinimumWidth(minSize);
            }
        }
    }

    private void styleDialogButtons(DatePickerDialog dialog) {

        int minSize = (int) dpToPxMax(this, 48);

        Button positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        Button negativeButton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);

        if (positiveButton != null) {
            positiveButton.setContentDescription(getString(R.string.confirm_date_selection));
            positiveButton.setTextColor(Color.WHITE);
            positiveButton.setBackgroundColor(ContextCompat.getColor(this, R.color.teal_dark));
            positiveButton.setMinimumHeight(minSize);
            positiveButton.setMinimumWidth(minSize);
        }

        if (negativeButton != null) {
            negativeButton.setContentDescription(getString(R.string.cancel_date_selection));
            negativeButton.setTextColor(Color.WHITE);
            negativeButton.setBackgroundColor(ContextCompat.getColor(this, R.color.red_dark));
            negativeButton.setMinimumHeight(minSize);
            negativeButton.setMinimumWidth(minSize);
        }
    }

    private void resetTime() {
        mHourOfDay = c.get(Calendar.HOUR_OF_DAY);
        mMinute = c.get(Calendar.MINUTE);

        // Usa un tema compatibile con TimePicker Spinner
        Context context = new ContextThemeWrapper(this, android.R.style.Theme_Holo_Light_Dialog);
        dialogTime = new TimePickerDialog(context, new PickTime(), mHourOfDay, mMinute, false);

        dialogTime.setTitle(getString(R.string.time_picker_description));

        dialogTime.setOnShowListener(dialog -> {

            int alertTitleId = context.getResources().getIdentifier("alertTitle", "id", "android");
            TextView alertTitle = dialogTime.findViewById(alertTitleId);
            if (alertTitle != null) {
                alertTitle.setTextColor(Color.BLACK);
                alertTitle.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            }

            Button positiveButton = dialogTime.getButton(DialogInterface.BUTTON_POSITIVE);
            Button negativeButton = dialogTime.getButton(DialogInterface.BUTTON_NEGATIVE);

            if (positiveButton != null) {
                positiveButton.setTextColor(Color.WHITE);
                positiveButton.setBackgroundColor(ContextCompat.getColor(this, R.color.teal_dark));
            }

            if (negativeButton != null) {
                negativeButton.setTextColor(Color.WHITE);
                negativeButton.setBackgroundColor(ContextCompat.getColor(this, R.color.red_dark));
            }
        });

        dialogTime.show();
    }

    private void eliminaVarie() {
        id = 5;
        descrizioneSpesa = findViewById(R.id.descrizioneSpesa);
        if (descrizioneSpesa.getCount() > 0) msgBox();
    }

    public void caricaGridView(String varie) {
        TextView totUscite = findViewById(R.id.totaleUscite);
        TextView totEntrate = findViewById(R.id.totaleEntrate);

        if (!varie.isEmpty()) {
            myLista = new ArrayList<>();

            cur = database.query("MONEY a INNER JOIN VARIE B ON (a.DESCRIZIONE=b.CONT)", new String[]{"a.DATA", "b.DESCRIZIONE", "a.prezzo", "a.uscita"}, "b.descrizione=?", new String[]{varie}, null, null, "a.DATA, a.uscita");

            cur.moveToFirst();

            while (cur.getCount() > 0 && !cur.isAfterLast()) {
                try {
                    dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT);
                    Date date = dateFormat.parse(cur.getString(0));
                    dateFormat.applyPattern("dd/MM/yyyy");

                    Boolean a = (cur.getString(3).equals("1"));

                    myLista.add(new Lista(dateFormat.format(date), cur.getString(1), Double.parseDouble(cur.getString(2)), a));
                    cur.moveToNext();
                } catch (Exception ignored) {
                }
            }

            cur.close();

            myAdapterLista = new ListaMovimentiGridViewAdapter(this, myLista);
            myGridView = findViewById(R.id.listaMovimenti);
            myGridView.setAdapter(myAdapterLista);

            int screenHeight = getScreenHeight();

            LayoutParams lp = myGridView.getLayoutParams();
            lp.height = screenHeight * 70 / 100; // Calcola il 70% dell'altezza
            myGridView.setLayoutParams(lp);


            cur = database.query("MONEY", new String[]{"SUM(prezzo)"}, "uscita=0 and descrizione=?", new String[]{String.valueOf(scaricaDati.getSpesaCodiceDescrizione(varie))}, null, null, null);
            cur.moveToFirst();

            if (cur.getCount() > 0 && cur.getString(0) != null && Double.parseDouble(cur.getString(0)) > 0) {
                totEntrate.setText(df.format(Double.parseDouble(cur.getString(0))));
            } else totEntrate.setText("0");

            cur.close();

            cur = database.query("MONEY", new String[]{"SUM(prezzo)"}, "uscita=1 and descrizione=?", new String[]{String.valueOf(scaricaDati.getSpesaCodiceDescrizione(varie))}, null, null, null);
            cur.moveToFirst();

            if (cur.getCount() > 0 && cur.getString(0) != null && Double.parseDouble(cur.getString(0)) > 0)
                totUscite.setText(df.format(Double.parseDouble(cur.getString(0))));
            else totUscite.setText("0");

            cur.close();
        } else {
            myGridView.setAdapter(null);
            totUscite.setText("0");
            totEntrate.setText("0");
        }
    }

    public void caricaGridView(String mese, String anno) {
        if (!mese.isEmpty() && !anno.isEmpty()) {

            myLista = new ArrayList<>();

            cur = database.query("MONEY a INNER JOIN VARIE B ON (a.DESCRIZIONE=b.CONT)", new String[]{"a.DATA", "b.DESCRIZIONE", "a.prezzo", "a.uscita"}, "strftime('%Y', a.data)=? and strftime('%m', a.data)=?", new String[]{anno, mese}, null, null, "a.DATA, a.uscita");

            cur.moveToFirst();

            while (cur.getCount() > 0 && !cur.isAfterLast()) {
                try {
                    dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT);
                    Date date = dateFormat.parse(cur.getString(0));
                    dateFormat.applyPattern("dd/MM");

                    Boolean a = (cur.getString(3).equals("1"));

                    myLista.add(new Lista(dateFormat.format(date), cur.getString(1), Double.parseDouble(cur.getString(2)), a));
                    cur.moveToNext();

                } catch (Exception ignored) {
                }
            }

            cur.close();

            myAdapterLista = new ListaMovimentiGridViewAdapter(this, myLista);
            myGridView = findViewById(R.id.listaMovimenti);
            myGridView.setAdapter(myAdapterLista);

            int screenHeight = getScreenHeight();

            LayoutParams lp = myGridView.getLayoutParams();
            lp.height = screenHeight * 70 / 100; // Calcola il 70% dell'altezza
            myGridView.setLayoutParams(lp);

            cur = database.query("MONEY", new String[]{"SUM(prezzo)"}, "strftime('%Y', data)=? and strftime('%m', data)=? and uscita=0", new String[]{anno, mese}, null, null, null);
            cur.moveToFirst();

            TextView totEntrate = findViewById(R.id.totaleEntrate);

            if (cur.getCount() > 0 && cur.getString(0) != null && Double.parseDouble(cur.getString(0)) > 0)
                totEntrate.setText(df.format(Double.parseDouble(cur.getString(0))));
            else totEntrate.setText("0");

            cur.close();

            cur = database.query("MONEY", new String[]{"SUM(prezzo)"}, "strftime('%Y', data)=? and strftime('%m', data)=? and uscita=1", new String[]{anno, mese}, null, null, null);
            cur.moveToFirst();

            TextView totUscite = findViewById(R.id.totaleUscite);
            if (cur.getCount() > 0 && cur.getString(0) != null && Double.parseDouble(cur.getString(0)) > 0)
                totUscite.setText(df.format(Double.parseDouble(cur.getString(0))));
            else totUscite.setText("0");

            cur.close();
        }
    }

    public void caricaGridView() {
        myLista = new ArrayList<>();

        cur = database.query("MONEY", new String[]{"SUM(prezzo)"}, "uscita=0", null, null, null, null);
        cur.moveToFirst();

        TextView totEntrate = findViewById(R.id.totaleEntrate);

        if (cur.getCount() > 0 && cur.getString(0) != null && Double.parseDouble(cur.getString(0)) > 0)
            totEntrate.setText(df.format(Double.parseDouble(cur.getString(0))));
        else totEntrate.setText("0");

        cur.close();

        cur = database.query("MONEY", new String[]{"SUM(prezzo)"}, "uscita=1", null, null, null, null);
        cur.moveToFirst();

        TextView totUscite = findViewById(R.id.totaleUscite);
        if (cur.getCount() > 0 && cur.getString(0) != null && Double.parseDouble(cur.getString(0)) > 0)
            totUscite.setText(df.format(Double.parseDouble(cur.getString(0))));
        else totUscite.setText("0");

        cur = database.query("MONEY", new String[]{"SUM(prezzo)"}, "uscita=0", null, null, null, null);
        cur.moveToFirst();
        double a, b;
        if (cur.getCount() > 0 && cur.getString(0) != null && Double.parseDouble(cur.getString(0)) > 0)
            a = Double.parseDouble(cur.getString(0));
        else a = 0.0;

        cur = database.query("MONEY", new String[]{"SUM(prezzo)"}, "uscita=1", null, null, null, null);
        cur.moveToFirst();
        if (cur.getCount() > 0 && cur.getString(0) != null && Double.parseDouble(cur.getString(0)) > 0)
            b = Double.parseDouble(cur.getString(0));
        else b = 0.0;

        TextView saldo = findViewById(R.id.totaleSaldo);
        saldo.setText(df.format(a - b));

        cur.close();
    }

    public void selezionaVoce(String voce) {
        cur = database.query("VARIE", new String[]{"descrizione"}, null, null, null, null, "descrizione");
        cur.moveToFirst();

        cur.close();

        descrizioneSpesa = findViewById(R.id.descrizioneSpesa);
        descrizioneSpesa.setAdapter(new CustomSpinnerVarie(this, scaricaDati.caricaSpinnerCompleto(uscita.toString())));
        descrizioneSpesa.setSelection(new CustomSpinnerVarie(this, scaricaDati.caricaSpinnerCompleto(uscita.toString())).getPosition(voce));
    }

    public String getPassword() {
        Cursor cur = database.query("OPZIONI", new String[]{"VALORE"}, "OPZ='PASSWORD'", null, null, null, null, null);
        cur.moveToFirst();
        if (cur.getCount() > 0) return cur.getString(0);

        cur.close();
        return "impossibile che si verifichi";
    }

    public void caricaSpinnerAnno() {
        cur = database.query("MONEY", new String[]{"min(strftime('%Y', data))", "max(strftime('%Y', data))", "count(*)"}, null, null, null, null, null);
        if (cur.moveToFirst()) {
            if (Integer.parseInt(cur.getString(2)) > 0) {
                List<String> arraySpinner = new ArrayList<>();
                int j = -1;

                for (int i = Integer.parseInt(cur.getString(0)); i <= Integer.parseInt(cur.getString(1)); i++) {
                    arraySpinner.add(String.valueOf(i));
                    j += 1;
                }

                cur.close();

                Spinner s = findViewById(R.id.selAnno);
                ArrayAdapter<String> adapterAnno = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, arraySpinner);
                adapterAnno.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                s.setAdapter(adapterAnno);
                s.setSelection(j);
            }
        }
    }

    private void msgBox() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        input = new EditText(this);
        input.setHint(getString(R.string.addDescrizione_descrizione));
        // Set the minimum height to 48dp
        int minHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, getResources().getDisplayMetrics());
        input.setMinimumHeight(minHeight);

        // Optionally, set padding for better usability
        int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());
        input.setPadding(padding, padding, padding, padding);

        switch (id) {
            case 1:
                /* password_verify */
                layout = inflater.inflate(R.layout.dialog_verify_password, findViewById(R.id.root));
                AlertDialog dialog = alert.create();
                dialog.setOnShowListener(dialogInterface -> {
                    Button positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
                    Button negativeButton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);

                    if (positiveButton != null) {
                        positiveButton.setTextColor(Color.WHITE);
                        positiveButton.setBackgroundColor(Color.GREEN);
                        positiveButton.setPadding(20, 10, 20, 10);
                    }

                    if (negativeButton != null) {
                        negativeButton.setTextColor(Color.WHITE);
                        negativeButton.setBackgroundColor(ContextCompat.getColor(this, R.color.red_dark));
                        negativeButton.setPadding(20, 10, 20, 10);
                    }
                });
                dialog.show();
                alert.setView(layout);
                alert.setTitle(R.string.password_verify);
                break;

            case 2:
                /* ripristino backup */
                alert.setMessage(R.string.backupRestore);
                break;

            case 3:
                /* modifica descrizione */
                input.setText(descrizioneSpesa.getSelectedItem().toString());
                input.setHint(descrizioneSpesa.getSelectedItem().toString());
                alert.setView(input);
                alert.setTitle(R.string.spesaModificaVoce);
                break;

            case 4:
                /* add descrizione */
                alert.setView(input);
                alert.setTitle(R.string.spesaAggiungiVoce);
                break;

            case 5:
                /* cancella spesa */
                alert.setMessage(R.string.spesaDelete);
                break;

            case 6:
                /* inserimento password */
                layout = inflater.inflate(R.layout.dialog_password, findViewById(R.id.root));
                password1 = layout.findViewById(R.id.EditText_Pwd1);
                password2 = layout.findViewById(R.id.EditText_Pwd2);
                final TextView error = layout.findViewById(R.id.TextView_PwdProblem);
                error.setVisibility(View.GONE);

                password1.addTextChangedListener(new TextWatcher() {
                    public void afterTextChanged(Editable s) {
                        String strPass1 = password1.getText().toString();
                        String strPass2 = password2.getText().toString();
                        if (strPass1.isEmpty() || strPass2.isEmpty()) {
                            error.setText(R.string.settings_pwd_required);
                            error.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.red));
                            error.setVisibility(View.VISIBLE);
                        } else if (strPass2.equals(strPass1)) {
                            error.setText(R.string.settings_pwd_equal);
                            error.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.teal_dark));
                            error.setVisibility(View.VISIBLE);
                        } else {
                            error.setText(R.string.settings_pwd_not_equal);
                            error.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.red));
                            error.setVisibility(View.VISIBLE);
                        }
                    }

                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }
                });

                password2.addTextChangedListener(new TextWatcher() {
                    public void afterTextChanged(Editable s) {
                        String strPass1 = password1.getText().toString();
                        String strPass2 = password2.getText().toString();
                        if (strPass1.isEmpty() || strPass2.isEmpty()) {
                            error.setText(R.string.settings_pwd_required);
                            error.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.red));
                            error.setVisibility(View.VISIBLE);
                        } else if (strPass1.equals(strPass2)) {
                            error.setText(R.string.settings_pwd_equal);
                            error.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.teal_dark));
                            error.setVisibility(View.VISIBLE);
                        } else {
                            error.setText(R.string.settings_pwd_not_equal);
                            error.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.red));
                            error.setVisibility(View.VISIBLE);
                        }
                    }

                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }
                });

                dialog = alert.create();
                dialog.setOnShowListener(dialogInterface -> {
                    Button positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
                    Button negativeButton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);

                    if (positiveButton != null) {
                        positiveButton.setTextColor(Color.WHITE);
                        positiveButton.setBackgroundColor(Color.GREEN);
                        positiveButton.setPadding(20, 10, 20, 10);
                        positiveButton.setContentDescription(getString(R.string.OK));
                    }

                    if (negativeButton != null) {
                        negativeButton.setTextColor(Color.WHITE);
                        negativeButton.setBackgroundColor(Color.RED);
                        negativeButton.setPadding(20, 10, 20, 10);
                        negativeButton.setContentDescription(getString(R.string.cancel));
                    }
                });

                alert.setView(layout);
                alert.setTitle(R.string.setPassword);
                dialog.show();
                break;

            case 7:
                /* uscita da persomy */
                alert.setMessage(R.string.persomyExit);
                break;

            case 8:
                /* Update Automatico */
                alert.setTitle(R.string.setUpdate);
                alert.setMessage(R.string.confermaUpdate);
                break;

            case 9:
                alert.setMessage(R.string.new_update_found);
                break;

            case 10:
                layout = inflater.inflate(R.layout.dialog_legenda, findViewById(R.id.widget145));
                alert.setView(layout);
                alert.setTitle(R.string.legenda);
                break;
        }

        alert.setPositiveButton(R.string.OK, (dialog, whichButton) -> {
            switch (id) {
                case 1:
                    EditText passwordVerify = layout.findViewById(R.id.password_verify);
                    if (!passwordVerify.getText().toString().trim().equals(getPassword().trim()))
                        System.exit(0);
                    break;

                case 2:
                    File file = getDatabasePath("PersoMyDB.db");
                    File fileBackupDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "PersoMy/backup");

                    if (!fileBackupDir.exists() || !fileBackupDir.isDirectory()) {
                        Toast.makeText(this, "Backup directory not found you should create a backup first.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    File fileBackup = new File(fileBackupDir, "PersoMyDB.db");
                    if (!fileBackup.exists()) {
                        Toast.makeText(this, "Backup file not found you should create a backup first.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        copyFile(fileBackup, file);
                        Toast.makeText(this, "Backup restored successfully", Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        Log.e("startRestoreBackup", "Error restoring backup", e);
                        Toast.makeText(this, "Restore failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    break;

                case 3:
                    String bkpVoce = descrizioneSpesa.getSelectedItem().toString();

                    if (!input.getText().toString().trim().isEmpty()) {
                        if (!input.getText().toString().trim().equals(bkpVoce)) {
                            if (scaricaDati.getSpesaCodiceDescrizione(input.getText().toString().trim()) == -1) {
                                try {
                                    database.beginTransaction();
                                    ContentValues row = new ContentValues();
                                    row.put("DESCRIZIONE", input.getText().toString().trim());
                                    database.update("VARIE", row, "cont=?", new String[]{String.format(Locale.US, "%d", scaricaDati.getSpesaCodiceDescrizione(descrizioneSpesa.getSelectedItem().toString()))});

                                    database.setTransactionSuccessful();
                                } catch (Exception e) {
                                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                                } finally {
                                    database.endTransaction();

                                    if (CURRENT_LAYOUT == R.layout.activity_uscite) {
                                        if (!mySpesa.isEmpty())
                                            for (int i = mySpesa.size() - 1; i > -1; i--)
                                                if (mySpesa.get(i).getSpesaName().equals(bkpVoce))
                                                    mySpesa.set(i, new Spesa(input.getText().toString().trim(), mySpesa.get(i).getSpesaPrezzo(), false, false));

                                        myListView.setAdapter(myAdapter);
                                    } else if (CURRENT_LAYOUT == R.layout.activity_automatiche) {
                                        if (!myMovimento.isEmpty())
                                            for (int i = myMovimento.size() - 1; i > -1; i--)
                                                if (myMovimento.get(i).getAutomaticaVoce().equals(bkpVoce))
                                                    myMovimento.set(i, new Movimento(myMovimento.get(i).getAutomaticaFrequenza(), myMovimento.get(i).getAutomaticaStartDate(), input.getText().toString().trim(), myMovimento.get(i).getAutomaticaImporto(), uscita, false, false));

                                        myListView.setAdapter(myAdapterAutomatica);
                                    }
                                }

                                caricaSpinner();
                                selezionaVoce(input.getText().toString().trim());

                                Toast.makeText(getApplicationContext(), R.string.spesaSalvataggioOk, Toast.LENGTH_SHORT).show();
                            } else
                                Toast.makeText(getApplicationContext(), R.string.spesaVoceDuplicata, Toast.LENGTH_LONG).show();
                        } else
                            Toast.makeText(getApplicationContext(), R.string.spesaVoceDuplicata, Toast.LENGTH_LONG).show();
                    } else
                        Toast.makeText(getApplicationContext(), R.string.spesaVoceVuota, Toast.LENGTH_SHORT).show();
                    break;

                case 4:
                    if (!input.getText().toString().trim().isEmpty()) {
                        if (scaricaDati.getSpesaCodiceDescrizione(input.getText().toString().trim()) == -1) {
                            try {
                                database.beginTransaction();
                                ContentValues row = new ContentValues();
                                row.put("DESCRIZIONE", input.getText().toString().trim());
                                database.insert("VARIE", null, row);
                                database.setTransactionSuccessful();
                            } catch (Exception e) {
                                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                            } finally {
                                database.endTransaction();
                                caricaSpinner();
                                selezionaVoce(input.getText().toString().trim());
                                Toast.makeText(getApplicationContext(), R.string.spesaSalvataggioOk, Toast.LENGTH_SHORT).show();
                            }
                        } else
                            Toast.makeText(getApplicationContext(), R.string.spesaVoceDuplicata, Toast.LENGTH_LONG).show();
                    } else
                        Toast.makeText(getApplicationContext(), R.string.spesaVoceVuota, Toast.LENGTH_LONG).show();

                    break;

                case 5:
                    try {
                        descrizioneSpesa = findViewById(R.id.descrizioneSpesa);
                        myGridView = findViewById(R.id.listaMovimenti);

                        database.beginTransaction();
                        database.delete("MONEY", "descrizione=?", new String[]{String.valueOf(scaricaDati.getSpesaCodiceDescrizione(descrizioneSpesa.getSelectedItem().toString()))});
                        database.delete("VARIE", "cont=?", new String[]{String.valueOf(scaricaDati.getSpesaCodiceDescrizione(descrizioneSpesa.getSelectedItem().toString()))});
                        database.setTransactionSuccessful();

                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    } finally {
                        database.endTransaction();
                        caricaSpinner();
                        if (descrizioneSpesa.getCount() > 0)
                            caricaGridView(descrizioneSpesa.getSelectedItem().toString());
                        else caricaGridView("");

                        Toast.makeText(getApplicationContext(), R.string.spesaEliminazioneOk, Toast.LENGTH_SHORT).show();
                    }
                    break;

                case 6:
                    try {
                        strPassword1 = password1.getText().toString();
                        strPassword2 = password2.getText().toString();
                        if (!(strPassword1.isEmpty() || strPassword2.isEmpty())) {
                            if (strPassword1.equals(strPassword2)) {
                                database.beginTransaction();
                                ContentValues row = new ContentValues();
                                row.put("ATTIVO", "1");
                                row.put("VALORE", strPassword2.trim());
                                database.update("OPZIONI", row, "OPZ=?", new String[]{"PASSWORD"});

                                database.setTransactionSuccessful();
                                database.endTransaction();

                                Toast.makeText(getApplicationContext(), R.string.setPasswordOK, Toast.LENGTH_SHORT).show();

                                password.setChecked(true);
                            } else {
                                Toast.makeText(getApplicationContext(), R.string.password_verify_ko, Toast.LENGTH_LONG).show();

                                password.setChecked(false);
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), R.string.setPasswordVuota, Toast.LENGTH_LONG).show();

                            password.setChecked(false);
                        }
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();

                        password.setChecked(false);
                    }
                    break;

                case 7:
                    database.close();
                    finish();
                    break;

                case 8:
                    database.beginTransaction();
                    ContentValues row = new ContentValues();
                    row.put("ATTIVO", "1");
                    database.update("OPZIONI", row, "OPZ=?", new String[]{"UPDATE"});
                    database.setTransactionSuccessful();
                    database.endTransaction();

                    Toast.makeText(getApplicationContext(), R.string.setUpdateOK, Toast.LENGTH_SHORT).show();

                    update.setChecked(true);

                    break;

                case 9:
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        File f1 = new File(Environment.getExternalStorageDirectory() + "/PersoMy/backup/PersoMy.apk");
                        intent.setDataAndType(Uri.fromFile(f1), "application/vnd.android.package-archive");
                        startActivity(intent);
                    } catch (Exception e) {
                        Log.e("msgBox", "A generic error occurred", e);
                    }

                    break;
            }
        });

        if (id != 10) alert.setNegativeButton(R.string.cancel, (dialog, whichButton) -> {
            switch (id) {
                case 1:
                    System.exit(0);
                    break;

                case 2:

                case 9:

                case 7:

                case 5:

                case 3:

                case 4:
                    dialog.cancel();
                    break;

                case 6: /*  PASSWORD */
                    dialog.cancel();
                    password.setChecked(verify.isTherePassword(database));
                    break;

                case 8: /* AGGIORNAMENTI AUTOMATICI */
                    update.setChecked(verify.isThereUpdate(database));
                    break;
            }
        });

        alert.setCancelable(false);
        AlertDialog dialog = alert.create();
        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(this, R.color.dark_teal));
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(this, R.color.dark_teal));
        });
        dialog.show();

    }

    public void caricaSpinner() {
        cur = database.query("VARIE", new String[]{"descrizione"}, null, null, null, null, "descrizione");

        List<String> data = new ArrayList<>();

        while (cur.moveToNext()) {
            data.add(cur.getString(0));
        }

        cur.close();

        descrizioneSpesa = findViewById(R.id.descrizioneSpesa);
        CustomSpinnerVarie adapter = new CustomSpinnerVarie(this, data);
        descrizioneSpesa.setAdapter(adapter);
        /* descrizioneSpesa.setAdapter(new CustomSpinnerVarie(this, scaricaDati.caricaSpinnerCompleto(uscita.toString()))); */
    }

    /* *******************************************************************/
    /* START FORM */
    /* *******************************************************************/

    public void startOpzioni() {
        contentPane.removeAllViews();
        li = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        contentPane.addView(li.inflate(R.layout.activity_opzioni, contentPane, false));

        menu_choise = R.menu.menu_solo_exit;
        invalidateOptionsMenu();
        password = findViewById(R.id.password);

        if (verify.isTherePassword(database)) password.setChecked(true);

        password.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                id = 6;
                msgBox();
            } else {
                try {
                    database.beginTransaction();
                    ContentValues row = new ContentValues();
                    row.put("ATTIVO", "0");
                    database.update("OPZIONI", row, "OPZ=?", new String[]{"PASSWORD"});
                    database.setTransactionSuccessful();

                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                } finally {
                    database.endTransaction();
                    Toast.makeText(getApplicationContext(), R.string.setPasswordKO, Toast.LENGTH_SHORT).show();
                }
            }
        });

        update = findViewById(R.id.update);

        if (verify.isThereUpdate(database)) update.setChecked(true);

        update.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!update.isChecked()) {
                try {
                    database.beginTransaction();
                    ContentValues row = new ContentValues();
                    row.put("ATTIVO", "0");
                    database.update("OPZIONI", row, "OPZ=?", new String[]{"UPDATE"});
                    database.setTransactionSuccessful();

                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                } finally {
                    database.endTransaction();
                    Toast.makeText(getApplicationContext(), R.string.setUpdateKO, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void startInfo() {
        contentPane.removeAllViews();
        li = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        contentPane.addView(li.inflate(R.layout.activity_info, contentPane, false));

        menu_choise = R.menu.menu_solo_exit;
        invalidateOptionsMenu();

        TextView info = findViewById(R.id.versionPersoMy);

        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            info.append(String.valueOf(pInfo.versionName));

        } catch (Exception e) {
            Log.e("startInfo", "A generic error occurred", e);
        }
    }

    private void startJobAutomatici() {
        database.execSQL("insert into money (DATA, DESCRIZIONE, PREZZO, USCITA) select datetime(date(b.data_start)), a.ID_VOCE, a.IMPORTO, a.USCITA from MOVIMENTI_AUTOMATICI a inner join job_automatici b on (a.ID=b.id_movimento) where b.DATA_START < DateTime('Now')");
        database.execSQL("delete from job_automatici where rowid in (select b.rowid from MOVIMENTI_AUTOMATICI a inner join job_automatici b on (a.ID=b.id_movimento) where b.DATA_START < DateTime('Now'))");
    }

    public void startUscite() {
        CURRENT_LAYOUT = R.layout.activity_uscite;

        contentPane.removeAllViews();
        li = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        contentPane.addView(li.inflate(R.layout.activity_uscite, contentPane, false));

        menu_choise = R.menu.menu_uscite;

        TextView simboloEuro = findViewById(R.id.simboloEuro);
        Locale loc = new Locale("it", "IT");
        simboloEuro.setText(Currency.getInstance(loc).getSymbol());

        int minWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, getResources().getDisplayMetrics());
        simboloEuro.setMinimumWidth(minWidth);

        int minHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, getResources().getDisplayMetrics());
        simboloEuro.setMinimumHeight(minHeight);

        mDateDisplayUscite = findViewById(R.id.dateSpesa);
        resetCalendario();

        caricaSpinner();

        mySpesa = new ArrayList<>();
        myAdapter = new SpesaListViewAdapter(this, mySpesa);
        myListView = findViewById(R.id.listaSpesa);
        myListView.setAdapter(myAdapter);

        mDateDisplayUscite.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void afterTextChanged(Editable s) {
                try {
                    dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT);
                    dateObj = dateFormat.parse(mYear + "-" + (mMonth + 1) + "-" + mDay + " 00:00:00");
                } catch (Exception ignored) {
                } finally {
                    mySpesa.clear();
                }

                cur = database.query("MONEY", new String[]{"descrizione", "prezzo"}, "data=? and uscita=" + ((uscita) ? 1 : 0), new String[]{dateFormat.format(dateObj)}, null, null, null, null);
                cur.moveToFirst();

                while (!cur.isAfterLast()) {
                    mySpesa.add(new Spesa(scaricaDati.getSpesaDescrizione(cur.getString(0)), Double.parseDouble(cur.getString(1)), true, false));
                    cur.moveToNext();
                }

                cur.close();
                myListView.setAdapter(myAdapter);
            }
        });

        updateDisplay(0);

        invalidateOptionsMenu();
    }

    private void startRestoreBackup() {
        id = 2;
        msgBox();
    }

    private void startBackup() {
        File file = getDatabasePath("PersoMyDB.db");
        if (!file.exists()) {
            Toast.makeText(this, "Database file not found", Toast.LENGTH_SHORT).show();
            return;
        }

        File fileBackupDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "PersoMy/backup");
        if (!fileBackupDir.exists() && !fileBackupDir.mkdirs()) {
            Toast.makeText(this, "Failed to create backup directory", Toast.LENGTH_SHORT).show();
            return;
        }

        File fileBackup = new File(fileBackupDir, "PersoMyDB.db");
        try {
            copyFile(file, fileBackup);
            Toast.makeText(this, "Backup created successfully", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Log.e("startBackup", "Error creating backup", e);
            Toast.makeText(this, "Backup failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void startTotale() {

        CURRENT_LAYOUT = R.layout.activity_totale;

        contentPane.removeAllViews();
        li = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        contentPane.addView(li.inflate(R.layout.activity_totale, contentPane, false));

        TextView simboloEuro = findViewById(R.id.simboloEuro);

        Locale loc = new Locale("it", "IT");
        simboloEuro.setText(Currency.getInstance(loc).getSymbol());

        TextView simboloEuro2 = findViewById(R.id.simboloEuro2);

        simboloEuro2.setText(Currency.getInstance(loc).getSymbol());

        caricaGridView();

        invalidateOptionsMenu();
    }

    private void startAnno() {

        CURRENT_LAYOUT = R.layout.activity_anno;

        contentPane.removeAllViews();
        li = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        contentPane.addView(li.inflate(R.layout.activity_anno, contentPane, false));

        TextView simboloEuro = findViewById(R.id.simboloEuro);

        Locale loc = new Locale("it", "IT");
        simboloEuro.setText(Currency.getInstance(loc).getSymbol());

        simboloEuro.setText(Currency.getInstance(loc).getSymbol());
        TextView simboloEuro2 = findViewById(R.id.simboloEuro2);
        simboloEuro2.setText(Currency.getInstance(loc).getSymbol());

        caricaSpinnerAnno();

        anno = findViewById(R.id.selAnno);

        if (anno.getCount() > 0) {
            anno.setOnItemSelectedListener(new OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                    Object item = parent.getItemAtPosition(pos);
                    myListaReportAnno = new ArrayList<>();

                    for (int i = 1; i < 13; i++) {
                        myListaReportAnno.add(new ListaReportAnno(scaricaDati.getMonth(i), 0, 0));
                    }

                    cur = database.query("MONEY", new String[]{"strftime('%m',data) as month", "sum(prezzo)"}, "strftime('%Y', data)=? and uscita=0", new String[]{item.toString()}, "month", null, "month");
                    cur.moveToFirst();

                    while (cur.getCount() > 0 && !cur.isAfterLast()) {
                        try {
                            myListaReportAnno.set(Integer.parseInt(cur.getString(0)) - 1, new ListaReportAnno(myListaReportAnno.get(Integer.parseInt(cur.getString(0)) - 1).getListaMese(), Double.parseDouble(cur.getString(1)), myListaReportAnno.get(Integer.parseInt(cur.getString(0)) - 1).getListaEntrata()));
                            cur.moveToNext();

                        } catch (Exception ex) {
                            Log.d("PersoMy: ", ex.toString());
                        }
                    }

                    cur.close();

                    cur = database.query("MONEY", new String[]{"strftime('%m',data) month", "sum(prezzo)"}, "strftime('%Y', data)=? and uscita=1", new String[]{item.toString()}, "month", null, "month");
                    cur.moveToFirst();

                    while (cur.getCount() > 0 && !cur.isAfterLast()) {
                        try {
                            myListaReportAnno.set(Integer.parseInt(cur.getString(0)) - 1, new ListaReportAnno(myListaReportAnno.get(Integer.parseInt(cur.getString(0)) - 1).getListaMese(), myListaReportAnno.get(Integer.parseInt(cur.getString(0)) - 1).getListaEntrata(), Double.parseDouble(cur.getString(1))));
                            cur.moveToNext();

                        } catch (Exception ex) {
                            Log.d("PersoMy: ", ex.toString());
                        }
                    }

                    cur.close();

                    cur = database.query("MONEY", new String[]{"SUM(prezzo)"}, "uscita=0 and strftime('%Y', data)=?", new String[]{item.toString()}, null, null, null);
                    cur.moveToFirst();

                    if (cur.getCount() > 0 && cur.getString(0) != null && Double.parseDouble(cur.getString(0)) > 0) {
                        try {
                            TextView totEntrate = findViewById(R.id.totaleEntrate);
                            totEntrate.setText(df.format(Double.parseDouble(cur.getString(0))));

                        } catch (Exception ex) {
                            Log.d("PersoMy: ", ex.toString());
                        }
                    }

                    cur.close();

                    cur = database.query("MONEY", new String[]{"SUM(prezzo)"}, "uscita=1 and strftime('%Y', data)=?", new String[]{item.toString()}, null, null, null);
                    cur.moveToFirst();

                    if (cur.getCount() > 0 && cur.getString(0) != null && Double.parseDouble(cur.getString(0)) > 0) {
                        try {
                            TextView totUscite = findViewById(R.id.totaleUscite);
                            totUscite.setText(df.format(Double.parseDouble(cur.getString(0))));

                        } catch (Exception ex) {
                            Log.d("PersoMy: ", ex.toString());
                        }
                    }

                    cur.close();

                    myAdapterListaReporAnno = new ReportAnnoGridViewAdapter(getApplicationContext(), myListaReportAnno);
                    myGridView = findViewById(R.id.listaMovimentiAnno);
                    myGridView.setAdapter(myAdapterListaReporAnno);
                }

                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        }

        invalidateOptionsMenu();
    }

    private void startMensile() {

        CURRENT_LAYOUT = R.layout.activity_mensile;

        contentPane.removeAllViews();
        li = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        contentPane.addView(li.inflate(R.layout.activity_mensile, contentPane, false));

        invalidateOptionsMenu();

        TextView simboloEuro = findViewById(R.id.simboloEuro);

        Locale loc = new Locale("it", "IT");
        simboloEuro.setText(Currency.getInstance(loc).getSymbol());

        TextView simboloEuro2 = findViewById(R.id.simboloEuro2);

        simboloEuro2.setText(Currency.getInstance(loc).getSymbol());

        caricaSpinnerAnno();

        mese = findViewById(R.id.selMese);
        anno = findViewById(R.id.selAnno);

        ArrayAdapter<String> myAdapterMese = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, scaricaDati.caricaSpinnerMese());
        myAdapterMese.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mese.setAdapter(myAdapterMese);

        if (anno.getCount() > 0) {
            mese.setSelection(c.get(Calendar.MONTH));
            mese.setOnItemSelectedListener(new OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                    Object item = parent.getItemAtPosition(pos);
                    caricaGridView(scaricaDati.meseToNumber(item.toString()), anno.getSelectedItem().toString());
                }

                public void onNothingSelected(AdapterView<?> parent) {
                }
            });

            anno.setOnItemSelectedListener(new OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                    Object item = parent.getItemAtPosition(pos);
                    caricaGridView(scaricaDati.meseToNumber(mese.getSelectedItem().toString()), (String) item);
                }

                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        }
    }

    private void startPeriodo() {

        CURRENT_LAYOUT = R.layout.activity_periodo;
        da = 0;

        contentPane.removeAllViews();
        li = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        contentPane.addView(li.inflate(R.layout.activity_periodo, contentPane, false));

        invalidateOptionsMenu();

        TextView simboloEuro = findViewById(R.id.simboloEuro);
        Locale loc = new Locale("it", "IT");
        simboloEuro.setText(Currency.getInstance(loc).getSymbol());

        TextView simboloEuro2 = findViewById(R.id.simboloEuro2);
        simboloEuro2.setText(Currency.getInstance(loc).getSymbol());

        DisplayMetrics dm = this.getResources().getDisplayMetrics();
        LinearLayout myBar = findViewById(R.id.widget45);
        LayoutParams params = myBar.getLayoutParams();
        params.height = dm.heightPixels - 180;
        myBar.setLayoutParams(new LinearLayout.LayoutParams(params));

        mDateDisplayDa = findViewById(R.id.dataAutomaticaTxtDa);
        mDateDisplayDa.setContentDescription(getString(R.string.dataCalendarioAutoTxt1));
        mDateDisplayA = findViewById(R.id.dataAutomaticaTxtA);
        resetCalendario();

        descrizioneSpesa = findViewById(R.id.descrizioneSpesa);
        descrizioneSpesa.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, scaricaDati.caricaSpinnerCompleto("null")));

        all = findViewById(R.id.selectAll);
        all.setOnClickListener(v -> {
            descrizioneSpesa = findViewById(R.id.descrizioneSpesa);
            descrizioneSpesa.setEnabled(!all.isChecked());
        });
    }

    private void startOpAutomatiche() {
        CURRENT_LAYOUT = R.layout.activity_automatiche;

        contentPane.removeAllViews();
        li = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        contentPane.addView(li.inflate(R.layout.activity_automatiche, contentPane, false));

        menu_choise = R.menu.menu_uscite;

        TextView simboloEuro = findViewById(R.id.simboloEuro);
        Locale loc = new Locale("it", "IT");
        simboloEuro.setText(Currency.getInstance(loc).getSymbol());

        mDateDisplay = findViewById(R.id.dataAutomaticaTxt);
        mTimeDisplay = findViewById(R.id.oraAutomaticaTxt);
        mHourOfDay = c.get(Calendar.HOUR_OF_DAY);
        mMinute = c.get(Calendar.MINUTE);
        dialogTime = new TimePickerDialog(this, R.style.CustomTimePickerTheme, new PickTime(), mHourOfDay, mMinute, DateFormat.is24HourFormat(this));
        resetCalendario();
        resetTime();
        updateDisplay(1);

        voceAutomatica = findViewById(R.id.descrizioneSpesa);
        voceAutomatica.setAdapter(new CustomSpinnerVarie(this, scaricaDati.caricaSpinnerCompleto(uscita.toString())));

        /* listview */
        myMovimento = new ArrayList<>();
        myAdapterAutomatica = new MovimentoListViewAdapter(this, myMovimento);
        myListView = findViewById(R.id.listaAutomatica);

        myMovimento.clear();

        int a = (uscita) ? 1 : 0;

        cur = database.query("MOVIMENTI_AUTOMATICI a INNER JOIN VARIE c ON (a.ID_VOCE = c.CONT)", new String[]{"a.ID_FREQUENZA-1", "a.DATA_START", "c.DESCRIZIONE", "a.IMPORTO", "a.USCITA"}, "a.uscita=? ", new String[]{String.valueOf(a)}, null, null, "a.DATA_START");
        cur.moveToFirst();

        try {
            while (cur.getCount() > 0 && !cur.isAfterLast()) {
                dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ROOT);
                dateObj = dateFormat.parse(cur.getString(1).substring(0, cur.getString(1).length() - 3));
                dateFormat.applyPattern("dd/MM/yyyy HH:mm");
                myMovimento.add(new Movimento(myFrequenza.get(cur.getInt(0)).descrizioneFrequenza(), dateFormat.format(dateObj), cur.getString(2), Double.parseDouble(cur.getString(3)), uscita, true, false));
                cur.moveToNext();
            }
        } catch (Exception e) {
            Log.e("startOpAutomatiche", "A generic error occurred", e);
        } finally {
            cur.close();
        }

        myListView.setAdapter(myAdapterAutomatica);

        RadioGroup grpRB = findViewById(R.id.radioGroup1);
        entrateRB = findViewById(R.id.usciteRB);
        usciteRB = findViewById(R.id.entrateRB);
        usciteRB.setContentDescription(getString(R.string.autoExpenses));
        entrateRB.setContentDescription(getString(R.string.autoIncomes));

        grpRB.setOnCheckedChangeListener((rg, checkedId) -> {
            if (entrateRB.getId() == checkedId) uscita = true;

            if (usciteRB.getId() == checkedId) uscita = false;

            voceAutomatica = findViewById(R.id.descrizioneSpesa);
            voceAutomatica.setAdapter(new CustomSpinnerVarie(activity, scaricaDati.caricaSpinnerCompleto(uscita.toString())));

            myMovimento.clear();

            int a1 = (uscita) ? 1 : 0;

            cur = database.query("MOVIMENTI_AUTOMATICI a INNER JOIN VARIE c ON (a.ID_VOCE = c.CONT)", new String[]{"a.ID_FREQUENZA-1", "a.DATA_START", "c.DESCRIZIONE", "a.IMPORTO", "a.USCITA"}, "a.uscita=? ", new String[]{String.valueOf(a1)}, null, null, "a.DATA_START");
            cur.moveToFirst();

            try {
                while (cur.getCount() > 0 && !cur.isAfterLast()) {
                    dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ROOT);
                    dateObj = dateFormat.parse(cur.getString(1).substring(0, cur.getString(1).length() - 3));
                    dateFormat.applyPattern("dd/MM/yyyy HH:mm");

                    myMovimento.add(new Movimento(myFrequenza.get(cur.getInt(0)).descrizioneFrequenza(), dateFormat.format(dateObj), cur.getString(2), Double.parseDouble(cur.getString(3)), uscita, true, false));
                    cur.moveToNext();
                }
            } catch (Exception e) {
                Log.e("startOpAutomatiche", "A generic error occurred", e);
            } finally {
                cur.close();
            }

            myListView.setAdapter(myAdapterAutomatica);
        });

        importoAutomatica = findViewById(R.id.importoAutomatica);
        frequenza = findViewById(R.id.frequenza);

        invalidateOptionsMenu();
    }

    private void startVoce() {

        contentPane.removeAllViews();
        li = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        contentPane.addView(li.inflate(R.layout.activity_voce, contentPane, false));

        ActionBar bar = getActionBar();
        if (bar != null) {
            bar.hide();
            bar.show();
        }

        invalidateOptionsMenu();

        TextView simboloEuro = findViewById(R.id.simboloEuro);
        simboloEuro.setContentDescription(getString(R.string.euro_symbol_voce));
        Locale loc = new Locale("it", "IT");
        simboloEuro.setText(Currency.getInstance(loc).getSymbol());

        TextView simboloEuro2 = findViewById(R.id.simboloEuro2);
        simboloEuro2.setText(Currency.getInstance(loc).getSymbol());

        descrizioneSpesa = findViewById(R.id.descrizioneSpesa);
        descrizioneSpesa.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, scaricaDati.caricaSpinnerCompleto("null")));
        descrizioneSpesa.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                Object item = parent.getItemAtPosition(pos);
                caricaGridView(item.toString());
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        if (menu_choise == R.menu.menu_elimina) {
            DisplayMetrics dm = this.getResources().getDisplayMetrics();
            LinearLayout myBar = findViewById(R.id.widget45);
            LayoutParams params = myBar.getLayoutParams();
            params.height = dm.heightPixels * 68 / 100;
            myBar.setLayoutParams(new LinearLayout.LayoutParams(params));
        } else {
            DisplayMetrics dm = this.getResources().getDisplayMetrics();
            LinearLayout myBar = findViewById(R.id.widget45);
            LayoutParams params = myBar.getLayoutParams();
            params.height = dm.heightPixels - 140;
            myBar.setLayoutParams(new LinearLayout.LayoutParams(params));
        }
    }

    /* *******************************************************************/
    /* CLICK BOTTONI */
    /* *******************************************************************/

    public void onAggiungiSpesaBtnPress(View v) {

        if (importoAutomatica.length() != 0 && Double.parseDouble(importoAutomatica.getText().toString()) > 0) {

            String frequenzaText = frequenza.getSelectedItem() != null ? frequenza.getSelectedItem().toString() : "";
            String mDateDisplayText = mDateDisplay.getText() != null ? mDateDisplay.getText().toString() : "";
            String nTimeDisplayText = mTimeDisplay.getText() != null ? mTimeDisplay.getText().toString() : "";
            String voceAutomaticaText = voceAutomatica.getSelectedItem() != null ? voceAutomatica.getSelectedItem().toString() : "";
            double importoValue = Double.parseDouble(importoAutomatica.getText().toString());

            myMovimento.add(new Movimento(frequenzaText, mDateDisplayText + " " + nTimeDisplayText, voceAutomaticaText, importoValue, uscita, false, false));

            myAdapterAutomatica = new MovimentoListViewAdapter(this, myMovimento);
            myListView.setAdapter(myAdapterAutomatica);
            importoAutomatica.setText("");
        } else
            Toast.makeText(getApplicationContext(), R.string.spesaControlloImporto, Toast.LENGTH_SHORT).show();
    }

    public void onAddSpesaBtnPress(View v) {

        EditText soldiSpesa = findViewById(R.id.soldiSpesa);
        if (soldiSpesa != null) {
            soldiSpesa.setHint(getString(R.string.soldiSpesaDesc));
        } else {
            Log.e("MainActivity", "soldiSpesa è null. Controlla l'ID e il layout associato.");
            return;
        }
        Spinner descrizioneSpesa = findViewById(R.id.descrizioneSpesa);

        if (descrizioneSpesa != null && descrizioneSpesa.getSelectedItem() != null) {
            if (!descrizioneSpesa.getSelectedItem().toString().isEmpty()) {
                if (soldiSpesa.length() != 0 && Double.parseDouble(soldiSpesa.getText().toString()) > 0) {
                    mySpesa.add(new Spesa(descrizioneSpesa.getSelectedItem().toString(), Double.parseDouble(soldiSpesa.getText().toString()), false, false));
                    myAdapter = new SpesaListViewAdapter(this, mySpesa);
                    myListView.setAdapter(myAdapter);
                    soldiSpesa.setText("");
                } else {
                    Toast.makeText(getApplicationContext(), R.string.spesaControlloImporto, Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getApplicationContext(), R.string.menu_selezione_voce, Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void onDataUsciteBtnPress(View v) {
        dialogDate.show();
    }

    public void onDataAutomaticaBtnPress(View v) {
        dialogDate.show();
    }

    public void onDataPeriodoDaBtnPress(View v) {
        da = 1;
        dialogDate.show();
    }

    public void onDataPeriodoABtnPress(View v) {
        da = 2;
        dialogDateA.show();
    }

    public void onOraAutomaticaBtnPress(View v) {
        dialogTime.show();
    }

    public void onBackupBtnPress(View v) {
        menu_choise = R.menu.menu_backup;
        openContextMenu(v);
    }

    public void onOptionsBtnPress(View v) {
        setTitle(getString(R.string.title_activity_main) + " - " + getString(R.string.menu_opzioni));
        startOpzioni();
    }

    public void onEliminaBtnPress(View v) {
        menu_choise = R.menu.menu_elimina;
        setTitle(getString(R.string.title_activity_main) + " - " + getString(R.string.menu_elimina));
        startVoce();
    }

    public void onExitBtnPress(View v) {
        id = 7;
        msgBox();
    }

    private void updateDisplay(int scelta) {
        switch (scelta) {
            case 0:
                if (mDay < 10) giorni = "0" + mDay;
                else giorni = String.valueOf(mDay);

                if (mMonth < 9) mesi = "0" + (mMonth + 1);
                else mesi = String.valueOf(mMonth + 1);
                break;

            case 1:
                if (mHourOfDay < 10) ora = "0" + mHourOfDay;
                else ora = String.valueOf(mHourOfDay);

                if (mMinute < 10) minuti = "0" + mMinute;
                else minuti = String.valueOf(mMinute);
                break;
        }

        if (CURRENT_LAYOUT == R.layout.activity_uscite) {
            mDateDisplayUscite.setText(new StringBuilder().append(giorni).append("/").append(mesi).append("/").append(mYear));
            mDateDisplayUscite.setTextSize(25);
        } else if (CURRENT_LAYOUT == R.layout.activity_automatiche) {
            mDateDisplay.setText(new StringBuilder().append(giorni).append("/").append(mesi).append("/").append(mYear));
            mDateDisplay.setTextSize(25);
            mTimeDisplay.setText(new StringBuilder().append(ora).append(":").append(minuti));
            mTimeDisplay.setTextSize(25);
        } else if (CURRENT_LAYOUT == R.layout.activity_periodo) {
            if (da == 0) {
                mDateDisplayDa.setText(new StringBuilder().append(giorni).append("/").append(mesi).append("/").append(mYear));
                mDateDisplayDa.setTextSize(18);
                mDateDisplayA.setText(new StringBuilder().append(giorni).append("/").append(mesi).append("/").append(mYear));
                mDateDisplayA.setTextSize(18);
            } else if (da == 1) {
                mDateDisplayDa.setText(new StringBuilder().append(giorni).append("/").append(mesi).append("/").append(mYear));
                mDateDisplayDa.setTextSize(18);
            } else if (da == 2) {
                mDateDisplayA.setText(new StringBuilder().append(giorni).append("/").append(mesi).append("/").append(mYear));
                mDateDisplayA.setTextSize(18);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        chiamaBottoni();
    }

    private void chiamaBottoni() {
        Button usciteBtn = findViewById(R.id.usciteBtn);
        if (usciteBtn != null) {
            usciteBtn.setOnClickListener(v -> {
                uscita = true;
                setTitle(getString(R.string.title_activity_main) + " - " + getString(R.string.menu_uscite));
                startUscite();
            });
        } else {
            Log.e("MainActivity", "usciteBtn non trovato nel layout.");
        }

        Button entrateBtn = findViewById(R.id.entrateBtn);
        if (entrateBtn != null) {
            entrateBtn.setOnClickListener(v -> {
                uscita = false;
                setTitle(getString(R.string.title_activity_main) + " - " + getString(R.string.menu_entrate));
                startUscite();
            });
        } else {
            Log.e("MainActivity", "entrateBtn non trovato nel layout.");
        }

        Button reportBtn = findViewById(R.id.reportBtn);
        if (reportBtn != null) {
            reportBtn.setOnClickListener(v -> {
                menu_choise = R.menu.menu_report;
                openContextMenu(v);
            });
        } else {
            Log.e("MainActivity", "reportBtn non trovato nel layout.");
        }

        Button automaticheBtn = findViewById(R.id.automaticheBtn);
        if (automaticheBtn != null) {
            automaticheBtn.setOnClickListener(v -> {
                setTitle(getString(R.string.title_activity_main) + " - " + getString(R.string.menu_operazioni_automatiche));
                startOpAutomatiche();
            });
        } else {
            Log.e("MainActivity", "automaticheBtn non trovato nel layout.");
        }
    }


    /* *******************************************************************/
    /* DISPLAY*/
    /* *******************************************************************/
    private class PickDate implements DatePickerDialog.OnDateSetListener {
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            view.updateDate(year, monthOfYear, dayOfMonth);
            mDay = dayOfMonth;
            mMonth = monthOfYear;
            mYear = year;
            updateDisplay(0);
            dialogDate.hide();
            dialogDateA.hide();
        }
    }

    private class PickTime implements TimePickerDialog.OnTimeSetListener {
        public void onTimeSet(TimePicker view, int hour, int minute) {
            mHourOfDay = hour;
            mMinute = minute;
            updateDisplay(1);
            dialogTime.hide();
        }
    }

}
