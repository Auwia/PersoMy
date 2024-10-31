package com.app.persomy.v4;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Currency;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@TargetApi(14)
public class MainActivity extends Activity {

    // VARIABILI DATA BASE
    private static final String DATABASE_NAME = "PersoMyDB.db";
    public static int CURRENT_VERSION = -1;
    private static Boolean uscita = true;
    // CALENDARI E OROLOGI
    private final Calendar c = Calendar.getInstance();
    private int mYear, mMonth, mDay, mHourOfDay, mMinute,
            menu_choise = R.menu.main, CURRENT_LAYOUT, id, j, da;
    private String giorni = String.valueOf(mDay),
            mesi = String.valueOf(mMonth), ora = String.valueOf(mHourOfDay),
            minuti = String.valueOf(mMinute);
    private String array_spinner[], strPassword1, strPassword2, message = null;
    private LayoutInflater li;
    private Activity activity;
    private ProgressDialog progressDialog;
    private DecimalFormat df = new DecimalFormat("###,##0.00");
    // COMPONENTI FISICI DI LAYOUT
    private TableLayout contentPane;
    private ListView myListView;
    private Spinner descrizioneSpesa, voceAutomatica, mese, anno, frequenza;
    private TextView mDateDisplayUscite, mDateDisplay, mTimeDisplay,
            mDateDisplayDa, mDateDisplayA;
    private RadioButton entrateRB, usciteRB;
    private EditText importoAutomatica, password1, password2, input;
    private AlertDialog.Builder alert;
    private View layout;
    private GridView myGridView;
    private Switch password, update;
    private CheckBox password233, update233, all;
    // ARRAY
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
    private PersoMyDataSource datasource;
    private Cursor cur;
    // CLASSI DI SUPPORTO
    private ScaricaDati scaricaDati;
    private Verify verify = new Verify();
    private DatePickerDialog dialogDate, dialogDateA = null;
    private TimePickerDialog dialogTime = null;
    private SimpleDateFormat dateFormat;
    private Date dateObj, dateObjA;

    private Handler mHandler;

    private static void copyFile(File src, File dst) throws IOException {
        FileChannel inChannel = null;
        FileChannel outChannel = null;

        try {
            inChannel = new FileInputStream(src).getChannel();
            outChannel = new FileOutputStream(dst).getChannel();
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } finally {
            if (inChannel != null)
                inChannel.close();
            if (outChannel != null)
                outChannel.close();
        }
    }

    private void displayResult(final String result) {
        mHandler.post(new Runnable() {
            public void run() {

                setProgressBarIndeterminateVisibility(false);

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(getBaseContext());

        String deviceId = Utils.getId(getApplicationContext());
        Log.i("Main:onCreate", deviceId);

        myFrequenza = new ArrayList<Frequenza>();
        myFrequenza.add(new Frequenza(1, "Giornaliero", 1));
        myFrequenza.add(new Frequenza(2, "Settimanale", 7));
        myFrequenza.add(new Frequenza(3, "Decadale", 0));
        myFrequenza.add(new Frequenza(4, "Mensile", 30));
        myFrequenza.add(new Frequenza(5, "Semestrale", 180));
        myFrequenza.add(new Frequenza(6, "Annuale", 365));
        myFrequenza.add(new Frequenza(7, "Primo giorno del mese", 0));
        myFrequenza.add(new Frequenza(8, "Ultimo giorno del mese", 0));

        setContentView(R.layout.activity_main);
        contentPane = (TableLayout) findViewById(R.id.contentPane);

        datasource = new PersoMyDataSource(getApplicationContext());
        datasource.open();
        database = openOrCreateDatabase(DATABASE_NAME,
                Context.MODE_PRIVATE, null);

        try {
            if (PersoMyDB.latch != null)
                PersoMyDB.latch.await();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (verify.isTherePassword(database)) {
            id = 1;
            msgBox();
        }

        if (verify.isThereJobAutomatico(database))
            startJobAutomatici();

        if (verify.isTablet(this))
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        scaricaDati = new ScaricaDati(database);

        activity = this;

        registraContextMenu();

        if (Build.VERSION.SDK_INT > 11) {
            ActionBar bar = getActionBar();
            bar.setDisplayHomeAsUpEnabled(true);
            bar.setDisplayShowTitleEnabled(true);
        }
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
            contentPane = (TableLayout) findViewById(R.id.contentPane);

            registraContextMenu();

            if (Build.VERSION.SDK_INT > 11)
                invalidateOptionsMenu();
            else
                openOptionsMenu();

            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_MENU)
            if (Build.VERSION.SDK_INT > 11)
                return true;

        return super.onKeyDown(keyCode, event);
    }

    // ******************************************************************
    // MENU'
    // ******************************************************************
    private void registraContextMenu() {
        registerForContextMenu(findViewById(R.id.reportBtn));
        registerForContextMenu(findViewById(R.id.backupBtn));
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        MenuInflater inflater = getMenuInflater();

        if (menu_choise == R.menu.menu_report) {
            inflater.inflate(R.menu.menu_report, menu);
        } else if (menu_choise == R.menu.menu_backup) {
            inflater.inflate(R.menu.menu_backup, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.menu_mensile) {
            setTitle(getString(R.string.title_activity_main) + " - "
                    + getString(R.string.menu_report) + ": "
                    + getString(R.string.menu_mensile));
            menu_choise = 0;
            startMensile();
        } else if (item.getItemId() == R.id.menu_periodo) {
            setTitle(getString(R.string.title_activity_main) + " - "
                    + getString(R.string.menu_report) + ": "
                    + getString(R.string.menu_periodo));
            // menu_choise = R.menu.menu_solo_exit;
            menu_choise = 0;
            startPeriodo();
        } else if (item.getItemId() == R.id.menu_anno) {
            setTitle(getString(R.string.title_activity_main) + " - "
                    + getString(R.string.menu_report) + ": "
                    + getString(R.string.menu_anno));
            // menu_choise = R.menu.menu_solo_exit;
            menu_choise = 0;
            startAnno();
        } else if (item.getItemId() == R.id.menu_totale) {
            setTitle(getString(R.string.title_activity_main) + " - "
                    + getString(R.string.menu_report) + ": "
                    + getString(R.string.menu_totale));
            // menu_choise = R.menu.menu_solo_exit;
            menu_choise = 0;
            startTotale();
        } else if (item.getItemId() == R.id.menu_voce) {
            // menu_choise = R.menu.menu_solo_exit;
            menu_choise = 0;
            setTitle(getString(R.string.title_activity_main) + " - "
                    + getString(R.string.menu_report) + ": "
                    + getString(R.string.menu_voce));
            startVoce();
        } else if (item.getItemId() == R.id.menu_backup) {
            startBackup();
        } else if (item.getItemId() == R.id.menu_ripristino_backup) {
            startRestoreBackup();
        }

        return true;
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
            menu_choise = R.menu.menu_elimina;
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
            menu_choise = R.menu.menu_elimina;
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
            contentPane = (TableLayout) findViewById(R.id.contentPane);

            registraContextMenu();

            if (Build.VERSION.SDK_INT > 11)
                invalidateOptionsMenu();
            else
                openOptionsMenu();

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
            contentPane = (TableLayout) findViewById(R.id.contentPane);

            registraContextMenu();

            if (Build.VERSION.SDK_INT > 11)
                invalidateOptionsMenu();
            else
                openOptionsMenu();
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

                    dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                            Locale.ROOT);
                    dateObj = dateFormat.parse(mYear + "-" + (mMonth + 1) + "-"
                            + mDay + " 00:00:00");
                    database.delete("MONEY", "data=? and uscita="
                                    + ((uscita) ? 1 : 0),
                            new String[]{dateFormat.format(dateObj)});

                    for (int i = 0; i < mySpesa.size(); i++) {
                        row.put("DATA", dateFormat.format(dateObj));
                        row.put("DESCRIZIONE", scaricaDati
                                .getSpesaCodiceDescrizione(mySpesa.get(i)
                                        .getSpesaName()));
                        row.put("PREZZO", mySpesa.get(i).getSpesaPrezzo());
                        row.put("USCITA", ((uscita) ? 1 : 0));

                        database.insert("MONEY", null, row);
                    }

                    database.setTransactionSuccessful();

                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), e.getMessage(),
                            Toast.LENGTH_LONG).show();
                } finally {
                    database.endTransaction();

                    Toast.makeText(getApplicationContext(),
                                    R.string.spesaSalvataggioOk, Toast.LENGTH_SHORT)
                            .show();
                }

                return true;
            } else if (CURRENT_LAYOUT == R.layout.activity_automatiche) {
                progressDialog = new ProgressDialog(this);
                progressDialog.setMessage(this.getText(R.string.loading));
                progressDialog.show();

                new Thread(new Runnable() {
                    public void run() {
                        try {
                            message = null;
                            database.beginTransaction();
                            ContentValues row = new ContentValues();

                            database.delete("JOB_AUTOMATICI",
                                    "id_movimento in (select id from movimenti_automatici where uscita="
                                            + ((uscita) ? 1 : 0) + ")", null);
                            database.delete("MOVIMENTI_AUTOMATICI", "uscita="
                                    + ((uscita) ? 1 : 0), null);

                            dateFormat = new SimpleDateFormat(
                                    "yyyy-MM-dd HH:mm:ss", Locale.ROOT);

                            int i = 0;
                            for (i = 0; i < myMovimento.size(); i++) {
                                int codiceFrequenza = scaricaDati
                                        .frequenzaToNumber(myMovimento.get(i)
                                                .getAutomaticaFrequenza());
                                int idVoce = scaricaDati
                                        .getSpesaCodiceDescrizione(myMovimento
                                                .get(i).getAutomaticaVoce());

                                dateObj = dateFormat.parse(myMovimento.get(i)
                                        .getAutomaticaStartDate()
                                        .substring(6, 10)
                                        + "-"
                                        + myMovimento.get(i)
                                        .getAutomaticaStartDate()
                                        .substring(3, 5)
                                        + "-"
                                        + myMovimento.get(i)
                                        .getAutomaticaStartDate()
                                        .substring(0, 2)
                                        + " "
                                        + myMovimento.get(i)
                                        .getAutomaticaStartDate()
                                        .substring(11, 13)
                                        + ":"
                                        + myMovimento.get(i)
                                        .getAutomaticaStartDate()
                                        .substring(14, 16) + ":00");
                                c.setTime(dateObj);

                                int giorno, mese, anno, ore = c
                                        .get(Calendar.HOUR_OF_DAY), minuti = c
                                        .get(Calendar.MINUTE), secondi = c
                                        .get(Calendar.SECOND), giorniFrequenza = scaricaDati
                                        .getGiorniFrequenza(myMovimento.get(i)
                                                .getAutomaticaFrequenza());

                                row.clear();
                                row.put("ID_FREQUENZA", codiceFrequenza);
                                row.put("DATA_START",
                                        dateFormat.format(dateObj));
                                row.put("ID_VOCE", idVoce);
                                row.put("IMPORTO", myMovimento.get(i)
                                        .getAutomaticaImporto());
                                row.put("USCITA", ((uscita) ? 1 : 0));
                                database.insert("MOVIMENTI_AUTOMATICI", null,
                                        row);

                                String cancellaJob = scaricaDati
                                        .getIDMovimento(
                                                codiceFrequenza,
                                                dateFormat.format(dateObj),
                                                idVoce,
                                                myMovimento.get(i)
                                                        .getAutomaticaImporto(),
                                                uscita);

                                if (giorniFrequenza == 0) {
                                    if (codiceFrequenza == 3) {
                                        for (int j = 0; j < 36; j++) {
                                            mese = c.get(Calendar.MONTH) + 1;
                                            anno = c.get(Calendar.YEAR);

                                            row.clear();
                                            row.put("ID_MOVIMENTO", cancellaJob);
                                            row.put("DATA_START", dateFormat
                                                    .format(dateFormat
                                                            .parse(anno + "-"
                                                                    + mese
                                                                    + "-"
                                                                    + "01"
                                                                    + " " + ore
                                                                    + ":"
                                                                    + minuti
                                                                    + ":00")));
                                            database.insert("JOB_AUTOMATICI",
                                                    null, row);

                                            row.clear();
                                            row.put("ID_MOVIMENTO", cancellaJob);
                                            row.put("DATA_START", dateFormat
                                                    .format(dateFormat
                                                            .parse(anno + "-"
                                                                    + mese
                                                                    + "-"
                                                                    + "11"
                                                                    + " " + ore
                                                                    + ":"
                                                                    + minuti
                                                                    + ":00")));
                                            database.insert("JOB_AUTOMATICI",
                                                    null, row);

                                            row.clear();
                                            row.put("ID_MOVIMENTO", cancellaJob);
                                            row.put("DATA_START", dateFormat
                                                    .format(dateFormat
                                                            .parse(anno + "-"
                                                                    + mese
                                                                    + "-"
                                                                    + "21"
                                                                    + " " + ore
                                                                    + ":"
                                                                    + minuti
                                                                    + ":00")));
                                            database.insert("JOB_AUTOMATICI",
                                                    null, row);

                                            c.add(Calendar.DATE, 30);
                                        }

                                        database.delete(
                                                "JOB_AUTOMATICI",
                                                "ID_MOVIMENTO = ? AND DATA_START < ? ",
                                                new String[]{
                                                        cancellaJob,
                                                        dateFormat
                                                                .format(dateObj)});

                                    }

                                    if (codiceFrequenza == 7)
                                        for (int j = 0; j < 36; j++) {
                                            mese = c.get(Calendar.MONTH) + 1;
                                            anno = c.get(Calendar.YEAR);

                                            row.clear();
                                            row.put("ID_MOVIMENTO", cancellaJob);
                                            row.put("DATA_START", dateFormat
                                                    .format(dateFormat
                                                            .parse(anno + "-"
                                                                    + mese
                                                                    + "-"
                                                                    + "01"
                                                                    + " " + ore
                                                                    + ":"
                                                                    + minuti
                                                                    + ":00")));
                                            database.insert("JOB_AUTOMATICI",
                                                    null, row);

                                            c.add(Calendar.DATE, 30);
                                        }

                                    if (codiceFrequenza == 8)
                                        for (int j = 0; j < 36; j++) {
                                            giorno = c
                                                    .getActualMaximum(Calendar.DATE);
                                            mese = c.get(Calendar.MONTH) + 1;
                                            anno = c.get(Calendar.YEAR);

                                            row.clear();
                                            row.put("ID_MOVIMENTO", cancellaJob);
                                            row.put("DATA_START", dateFormat
                                                    .format(dateFormat
                                                            .parse(anno + "-"
                                                                    + mese
                                                                    + "-"
                                                                    + giorno
                                                                    + " " + ore
                                                                    + ":"
                                                                    + minuti
                                                                    + ":00")));
                                            database.insert("JOB_AUTOMATICI",
                                                    null, row);

                                            c.add(Calendar.DATE, 30);
                                        }
                                } else {
                                    for (int j = 0; j < 1095; j++) {
                                        giorno = c.get(Calendar.DAY_OF_MONTH);
                                        mese = c.get(Calendar.MONTH) + 1;
                                        anno = c.get(Calendar.YEAR);

                                        row.clear();
                                        row.put("ID_MOVIMENTO", cancellaJob);
                                        row.put("DATA_START", dateFormat
                                                .format(dateFormat.parse(anno
                                                        + "-" + mese + "-"
                                                        + giorno + " " + ore
                                                        + ":" + minuti + ":"
                                                        + secondi)));
                                        database.insert("JOB_AUTOMATICI", null,
                                                row);

                                        c.add(Calendar.DATE, giorniFrequenza);
                                    }
                                }
                            }

                            startJobAutomatici();
                            database.setTransactionSuccessful();
                            progressDialog.dismiss();

                        } catch (Exception e) {
                            e.printStackTrace();
                            message = e.getMessage();

                        } finally {
                            database.endTransaction();
                            message = null;
                        }

                        MainActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                if (message == null)
                                    Toast.makeText(getApplicationContext(),
                                            R.string.spesaSalvataggioOk,
                                            Toast.LENGTH_SHORT).show();
                                else
                                    Toast.makeText(getApplicationContext(),
                                            message.toString(),
                                            Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }).start();
            }
            return true;
        } else if (item.getItemId() == R.id.addDescrizione) {
            id = 4;
            msgBox();
            return true;
        } else if (item.getItemId() == R.id.modificaDescrizione) {
            id = 3;
            descrizioneSpesa = (Spinner) findViewById(R.id.descrizioneSpesa);
            if (descrizioneSpesa.getCount() > 0)
                msgBox();
            return true;
        } else if (item.getItemId() == R.id.deleteSpesa) {
            if (CURRENT_LAYOUT == R.layout.activity_uscite) {
                if (mySpesa.size() > 0) {
                    for (int i = mySpesa.size() - 1; i > -1; i--) {
                        if (mySpesa.get(i).getSpesaFlaggata()) {
                            mySpesa.remove(i);
                            j++;
                        }
                    }
                    myListView.setAdapter(myAdapter);

                    if (j == 0)
                        Toast.makeText(getApplicationContext(),
                                        R.string.spesaCancellaVoce, Toast.LENGTH_SHORT)
                                .show();

                } else {
                    Toast.makeText(getApplicationContext(),
                                    R.string.spesaCancellaVoce, Toast.LENGTH_SHORT)
                            .show();
                }

                return true;
            } else if (CURRENT_LAYOUT == R.layout.activity_automatiche) {
                if (myMovimento.size() > 0) {
                    for (int i = myMovimento.size() - 1; i > -1; i--) {
                        if (myMovimento.get(i).getAutomaticaFlaggata()) {
                            myMovimento.remove(i);
                            j++;
                        }
                    }
                    myListView.setAdapter(myAdapterAutomatica);

                    if (j == 0)
                        Toast.makeText(getApplicationContext(),
                                        R.string.spesaCancellaVoce, Toast.LENGTH_SHORT)
                                .show();

                } else {
                    Toast.makeText(getApplicationContext(),
                                    R.string.spesaCancellaVoce, Toast.LENGTH_SHORT)
                            .show();
                }

                return true;
            }

            return true;
        }

        return true;
    }

    // ******************************************************************
    // OPERAZIONI
    // ******************************************************************
    public void onSearchBtnPress(View v) {

        TextView totUscite = (TextView) findViewById(R.id.totaleUscite);
        TextView totEntrate = (TextView) findViewById(R.id.totaleEntrate);

        try {
            dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                    Locale.ROOT);
            dateObj = dateFormat.parse(mDateDisplayDa.getText().toString()
                    .substring(6, 10)
                    + "-"
                    + mDateDisplayDa.getText().toString().substring(3, 5)
                    + "-"
                    + mDateDisplayDa.getText().toString().substring(0, 2)
                    + " 00:00:00");
            dateObjA = dateFormat.parse(mDateDisplayA.getText().toString()
                    .substring(6, 10)
                    + "-"
                    + mDateDisplayA.getText().toString().substring(3, 5)
                    + "-"
                    + mDateDisplayA.getText().toString().substring(0, 2)
                    + " 00:00:00");
        } catch (Exception ignored) {
        } finally {
        }

        descrizioneSpesa = (Spinner) findViewById(R.id.descrizioneSpesa);
        all = (CheckBox) findViewById(R.id.selectAll);
        if (all.isChecked())
            cur = database.query(
                    "MONEY a INNER JOIN VARIE B ON (a.DESCRIZIONE=b.CONT)",
                    new String[]{"a.DATA", "b.DESCRIZIONE", "a.prezzo",
                            "a.uscita"},
                    "data>=? and data<=?",
                    new String[]{dateFormat.format(dateObj),
                            dateFormat.format(dateObjA)}, null, null,
                    "a.DATA, a.uscita");
        else
            cur = database.query(
                    "MONEY a INNER JOIN VARIE B ON (a.DESCRIZIONE=b.CONT)",
                    new String[]{"a.DATA", "b.DESCRIZIONE", "a.prezzo",
                            "a.uscita"},
                    "data>=? and data<=? and b.descrizione=?",
                    new String[]{dateFormat.format(dateObj),
                            dateFormat.format(dateObjA),
                            descrizioneSpesa.getSelectedItem().toString()},
                    null, null, "a.DATA, a.uscita");

        myLista = new ArrayList<Lista>();

        cur.moveToFirst();

        while (cur.getCount() > 0 && !cur.isAfterLast()) {
            try {
                dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                        Locale.ROOT);
                Date date = dateFormat.parse(cur.getString(0));
                dateFormat.applyPattern("dd/MM/yyyy");

                Boolean a = (cur.getString(3).equals("1"));

                myLista.add(new Lista(dateFormat.format(date),
                        cur.getString(1), Double.parseDouble(cur.getString(2)),
                        a));
                cur.moveToNext();
            } catch (Exception ignored) {
            }
        }

        cur.close();

        myAdapterLista = new ListaMovimentiGridViewAdapter(this, myLista);
        myGridView = (GridView) findViewById(R.id.listaMovimenti);
        myGridView.setAdapter(myAdapterLista);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        LayoutParams lp = (LayoutParams) myGridView.getLayoutParams();
        lp.height = size.y * 70 / 100;
        myGridView.setLayoutParams(lp);

        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT);
        if (all.isChecked())
            cur = database.query(
                    "MONEY",
                    new String[]{"SUM(prezzo)"},
                    "uscita=0 and data>=? and data<=?",
                    new String[]{dateFormat.format(dateObj),
                            dateFormat.format(dateObjA)}, null, null, null);
        else
            cur = database.query(
                    "MONEY a INNER JOIN VARIE B ON (a.DESCRIZIONE=b.CONT)",
                    new String[]{"SUM(prezzo)"},
                    "uscita=0 and data>=? and data<=? and b.descrizione=?",
                    new String[]{dateFormat.format(dateObj),
                            dateFormat.format(dateObjA),
                            descrizioneSpesa.getSelectedItem().toString()},
                    null, null, null);
        cur.moveToFirst();

        if (cur.getCount() > 0 && cur.getString(0) != null
                && Double.parseDouble(cur.getString(0)) > 0) {
            totEntrate.setText(String.valueOf(df.format(Double.parseDouble(cur
                    .getString(0)))));
        } else
            totEntrate.setText("0");

        cur.close();

        if (all.isChecked())
            cur = database.query(
                    "MONEY",
                    new String[]{"SUM(prezzo)"},
                    "uscita=1 and data>=? and data<=?",
                    new String[]{dateFormat.format(dateObj),
                            dateFormat.format(dateObjA)}, null, null, null);
        else
            cur = database.query(
                    "MONEY a INNER JOIN VARIE B ON (a.DESCRIZIONE=b.CONT)",
                    new String[]{"SUM(prezzo)"},
                    "uscita=1 and data>=? and data<=? and b.descrizione=?",
                    new String[]{dateFormat.format(dateObj),
                            dateFormat.format(dateObjA),
                            descrizioneSpesa.getSelectedItem().toString()},
                    null, null, null);
        cur.moveToFirst();

        if (cur.getCount() > 0 && cur.getString(0) != null
                && Double.parseDouble(cur.getString(0)) > 0)
            totUscite.setText(String.valueOf(df.format(Double.parseDouble(cur
                    .getString(0)))));
        else
            totUscite.setText("0");

        cur.close();
    }

    private void resetCalendario() {
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);
        dialogDate = new DatePickerDialog(this, new PickDate(), mYear, mMonth,
                mDay);
        dialogDateA = new DatePickerDialog(this, new PickDate(), mYear, mMonth,
                mDay);
        dialogDate.updateDate(mYear, mMonth, mDay);
        dialogDateA.updateDate(mYear, mMonth, mDay);
        updateDisplay(0);
    }

    private void resetTime() {
        mHourOfDay = c.get(Calendar.HOUR_OF_DAY);
        mMinute = c.get(Calendar.MINUTE);
        dialogTime = new TimePickerDialog(this, new PickTime(), mHourOfDay,
                mMinute, DateFormat.is24HourFormat(this));
        dialogTime.updateTime(mHourOfDay, mMinute);
        updateDisplay(1);
    }

    private void eliminaVarie() {
        id = 5;
        descrizioneSpesa = (Spinner) findViewById(R.id.descrizioneSpesa);
        if (descrizioneSpesa.getCount() > 0)
            msgBox();
    }

    public void caricaGridView(String varie) {
        TextView totUscite = (TextView) findViewById(R.id.totaleUscite);
        TextView totEntrate = (TextView) findViewById(R.id.totaleEntrate);

        if (!varie.equals("")) {
            myLista = new ArrayList<Lista>();

            cur = database.query(
                    "MONEY a INNER JOIN VARIE B ON (a.DESCRIZIONE=b.CONT)",
                    new String[]{"a.DATA", "b.DESCRIZIONE", "a.prezzo",
                            "a.uscita"}, "b.descrizione=?",
                    new String[]{varie}, null, null, "a.DATA, a.uscita");

            cur.moveToFirst();

            while (cur.getCount() > 0 && !cur.isAfterLast()) {
                try {
                    dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                            Locale.ROOT);
                    Date date = dateFormat.parse(cur.getString(0));
                    dateFormat.applyPattern("dd/MM/yyyy");

                    Boolean a = (cur.getString(3).equals("1"));

                    myLista.add(new Lista(dateFormat.format(date), cur
                            .getString(1),
                            Double.parseDouble(cur.getString(2)), a));
                    cur.moveToNext();
                } catch (Exception ignored) {
                }
            }

            cur.close();

            myAdapterLista = new ListaMovimentiGridViewAdapter(this, myLista);
            myGridView = (GridView) findViewById(R.id.listaMovimenti);
            myGridView.setAdapter(myAdapterLista);

            if (Build.VERSION.SDK_INT > 11) {
                Display display = getWindowManager().getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);
                LayoutParams lp = (LayoutParams) myGridView.getLayoutParams();
                lp.height = size.y * 78 / 100;
                myGridView.setLayoutParams(lp);

            }

            cur = database.query("MONEY", new String[]{"SUM(prezzo)"},
                    "uscita=0 and descrizione=?", new String[]{String
                            .valueOf(scaricaDati
                            .getSpesaCodiceDescrizione(varie))}, null,
                    null, null);
            cur.moveToFirst();

            if (cur.getCount() > 0 && cur.getString(0) != null
                    && Double.parseDouble(cur.getString(0)) > 0) {
                totEntrate.setText(String.valueOf(df.format(Double
                        .parseDouble(cur.getString(0)))));
            } else
                totEntrate.setText("0");

            cur.close();

            cur = database.query("MONEY", new String[]{"SUM(prezzo)"},
                    "uscita=1 and descrizione=?", new String[]{String
                            .valueOf(scaricaDati
                            .getSpesaCodiceDescrizione(varie))}, null,
                    null, null);
            cur.moveToFirst();

            if (cur.getCount() > 0 && cur.getString(0) != null
                    && Double.parseDouble(cur.getString(0)) > 0)
                totUscite.setText(String.valueOf(df.format(Double
                        .parseDouble(cur.getString(0)))));
            else
                totUscite.setText("0");

            cur.close();
        } else {
            myGridView.setAdapter(null);
            totUscite.setText("0");
            totEntrate.setText("0");
        }
    }

    public void caricaGridView(String mese, String anno) {
        if (!mese.equals("") && !anno.equals("")) {

            myLista = new ArrayList<Lista>();

            cur = database
                    .query("MONEY a INNER JOIN VARIE B ON (a.DESCRIZIONE=b.CONT)",
                            new String[]{"a.DATA", "b.DESCRIZIONE",
                                    "a.prezzo", "a.uscita"},
                            "strftime('%Y', a.data)=? and strftime('%m', a.data)=?",
                            new String[]{anno, mese}, null, null,
                            "a.DATA, a.uscita");

            cur.moveToFirst();

            while (cur.getCount() > 0 && !cur.isAfterLast()) {
                try {
                    dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                            Locale.ROOT);
                    Date date = dateFormat.parse(cur.getString(0));
                    dateFormat.applyPattern("dd/MM");

                    Boolean a = (cur.getString(3).equals("1"));

                    myLista.add(new Lista(dateFormat.format(date), cur
                            .getString(1),
                            Double.parseDouble(cur.getString(2)), a));
                    cur.moveToNext();

                } catch (Exception ignored) {
                }
            }

            cur.close();

            myAdapterLista = new ListaMovimentiGridViewAdapter(this, myLista);
            myGridView = (GridView) findViewById(R.id.listaMovimenti);
            myGridView.setAdapter(myAdapterLista);

            if (Build.VERSION.SDK_INT > 11) {
                Display display = getWindowManager().getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);
                LayoutParams lp = (LayoutParams) myGridView.getLayoutParams();
                lp.height = size.y * 78 / 100;
                myGridView.setLayoutParams(lp);
            }

            cur = database
                    .query("MONEY",
                            new String[]{"SUM(prezzo)"},
                            "strftime('%Y', data)=? and strftime('%m', data)=? and uscita=0",
                            new String[]{anno, mese}, null, null, null);
            cur.moveToFirst();

            TextView totEntrate = (TextView) findViewById(R.id.totaleEntrate);

            if (cur.getCount() > 0 && cur.getString(0) != null
                    && Double.parseDouble(cur.getString(0)) > 0)
                totEntrate.setText(String.valueOf(df.format(Double
                        .parseDouble(cur.getString(0)))));
            else
                totEntrate.setText("0");

            cur.close();

            cur = database
                    .query("MONEY",
                            new String[]{"SUM(prezzo)"},
                            "strftime('%Y', data)=? and strftime('%m', data)=? and uscita=1",
                            new String[]{anno, mese}, null, null, null);
            cur.moveToFirst();

            TextView totUscite = (TextView) findViewById(R.id.totaleUscite);
            if (cur.getCount() > 0 && cur.getString(0) != null
                    && Double.parseDouble(cur.getString(0)) > 0)
                totUscite.setText(String.valueOf(df.format(Double
                        .parseDouble(cur.getString(0)))));
            else
                totUscite.setText("0");

            cur.close();
        }
    }

    public void caricaGridView() {
        myLista = new ArrayList<Lista>();

        cur = database.query("MONEY", new String[]{"SUM(prezzo)"},
                "uscita=0", null, null, null, null);
        cur.moveToFirst();

        TextView totEntrate = (TextView) findViewById(R.id.totaleEntrate);

        if (cur.getCount() > 0 && cur.getString(0) != null
                && Double.parseDouble(cur.getString(0)) > 0)
            totEntrate.setText(String.valueOf(df.format(Double.parseDouble(cur
                    .getString(0)))));
        else
            totEntrate.setText("0");

        cur.close();

        cur = database.query("MONEY", new String[]{"SUM(prezzo)"},
                "uscita=1", null, null, null, null);
        cur.moveToFirst();

        TextView totUscite = (TextView) findViewById(R.id.totaleUscite);
        if (cur.getCount() > 0 && cur.getString(0) != null
                && Double.parseDouble(cur.getString(0)) > 0)
            totUscite.setText(String.valueOf(df.format(Double.parseDouble(cur
                    .getString(0)))));
        else
            totUscite.setText("0");

        cur = database.query("MONEY", new String[]{"SUM(prezzo)"},
                "uscita=0", null, null, null, null);
        cur.moveToFirst();
        Double a, b;
        if (cur.getCount() > 0 && cur.getString(0) != null
                && Double.parseDouble(cur.getString(0)) > 0)
            a = Double.parseDouble(cur.getString(0));
        else
            a = 0.0;

        cur = database.query("MONEY", new String[]{"SUM(prezzo)"},
                "uscita=1", null, null, null, null);
        cur.moveToFirst();
        if (cur.getCount() > 0 && cur.getString(0) != null
                && Double.parseDouble(cur.getString(0)) > 0)
            b = Double.parseDouble(cur.getString(0));
        else
            b = 0.0;

        TextView saldo = (TextView) findViewById(R.id.totaleSaldo);
        saldo.setText(String.valueOf(df.format(a - b)));

        cur.close();
    }

    public void selezionaVoce(String voce) {
        cur = database.query("VARIE", new String[]{"descrizione"}, null,
                null, null, null, "descrizione");
        cur.moveToFirst();
        int i = 0;
        array_spinner = new String[cur.getCount()];

        while (!cur.isAfterLast()) {
            array_spinner[i] = cur.getString(0);
            i += 1;
            cur.moveToNext();
        }

        cur.close();

        descrizioneSpesa = (Spinner) findViewById(R.id.descrizioneSpesa);
        descrizioneSpesa.setAdapter(new CustomSpinnerVarie(this, scaricaDati
                .caricaSpinnerCompleto(uscita.toString())));
        descrizioneSpesa.setSelection(new CustomSpinnerVarie(this, scaricaDati
                .caricaSpinnerCompleto(uscita.toString())).getPosition(voce));
    }

    public String getPassword() {
        Cursor cur = database.query("OPZIONI", new String[]{"VALORE"},
                "OPZ='PASSWORD'", null, null, null, null, null);
        cur.moveToFirst();
        if (cur.getCount() > 0)
            return cur.getString(0);

        cur.close();
        return "impossibile che si verifichi";
    }

    public void caricaSpinnerAnno() {
        cur = database.query("MONEY", new String[]{
                "min(strftime('%Y', data))", "max(strftime('%Y', data))",
                "count(*)"}, null, null, null, null, null);
        if (cur.moveToFirst()) {
            if (Integer.parseInt(cur.getString(2)) > 0) {
                List<String> arraySpinner = new ArrayList<String>();
                int j = -1;

                for (int i = Integer.parseInt(cur.getString(0)); i <= Integer
                        .parseInt(cur.getString(1)); i++) {
                    arraySpinner.add(String.valueOf(i));
                    j += 1;
                }

                cur.close();

                Spinner s = (Spinner) findViewById(R.id.selAnno);
                ArrayAdapter<String> adapterAnno = new ArrayAdapter<String>(
                        this, android.R.layout.simple_spinner_item,
                        arraySpinner);
                adapterAnno
                        .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                s.setAdapter(adapterAnno);
                s.setSelection(j);
            }
        }
    }

    private void msgBox() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        alert = new AlertDialog.Builder(this);
        input = new EditText(this);

        switch (id) {
            case 1:
                // password_verify
                layout = inflater.inflate(R.layout.dialog_verify_password,
                        (ViewGroup) findViewById(R.id.root));
                alert.setView(layout);
                alert.setTitle(R.string.password_verify);
                break;

            case 2:
                // ripristino backup
                alert.setMessage(R.string.backupRestore);
                break;

            case 3:
                // modifica descrizione
                input.setText(descrizioneSpesa.getSelectedItem().toString());
                alert.setView(input);
                alert.setTitle(R.string.spesaModificaVoce);
                break;

            case 4:
                // add descrizione
                alert.setView(input);
                alert.setTitle(R.string.spesaAggiungiVoce);
                break;

            case 5:
                // cancella spesa
                alert.setMessage(R.string.spesaDelete);
                break;

            case 6:
                // inserimento password
                layout = inflater.inflate(R.layout.dialog_password,
                        (ViewGroup) findViewById(R.id.root));
                password1 = (EditText) layout.findViewById(R.id.EditText_Pwd1);
                password2 = (EditText) layout.findViewById(R.id.EditText_Pwd2);
                final TextView error = (TextView) layout
                        .findViewById(R.id.TextView_PwdProblem);

                password1.addTextChangedListener(new TextWatcher() {
                    public void afterTextChanged(Editable s) {
                        String strPass1 = password1.getText().toString();
                        String strPass2 = password2.getText().toString();
                        if (strPass2.equals(strPass1)) {
                            error.setText(R.string.settings_pwd_equal);
                        } else {
                            error.setText(R.string.settings_pwd_not_equal);
                        }
                    }

                    public void beforeTextChanged(CharSequence s, int start,
                                                  int count, int after) {
                    }

                    public void onTextChanged(CharSequence s, int start,
                                              int before, int count) {
                    }
                });

                password2.addTextChangedListener(new TextWatcher() {
                    public void afterTextChanged(Editable s) {
                        String strPass1 = password1.getText().toString();
                        String strPass2 = password2.getText().toString();
                        if (strPass1.equals(strPass2)) {
                            error.setText(R.string.settings_pwd_equal);
                        } else {
                            error.setText(R.string.settings_pwd_not_equal);
                        }
                    }

                    public void beforeTextChanged(CharSequence s, int start,
                                                  int count, int after) {
                    }

                    public void onTextChanged(CharSequence s, int start,
                                              int before, int count) {
                    }
                });

                alert.setView(layout);
                alert.setTitle(R.string.setPassword);
                break;

            case 7:
                // uscita da persomy
                alert.setMessage(R.string.persomyExit);
                break;

            case 8:
                // Update Automatico
                alert.setTitle(R.string.setUpdate);
                alert.setMessage(R.string.confermaUpdate);
                break;

            case 9:
                alert.setMessage(R.string.new_update_found);
                break;

            case 10:
                layout = inflater.inflate(R.layout.dialog_legenda,
                        (ViewGroup) findViewById(R.id.widget145));
                alert.setView(layout);
                alert.setTitle(R.string.legenda);
                break;
        }

        alert.setPositiveButton(R.string.OK,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        switch (id) {
                            case 1:
                                EditText passwordVerify = (EditText) layout
                                        .findViewById(R.id.password_verify);
                                if (!passwordVerify.getText().toString().trim()
                                        .equals(getPassword().trim()))
                                    System.exit(0);
                                break;

                            case 2:
                                if (verify.IsSdPresent()) {
                                    File file = new File(
                                            Environment.getDataDirectory()
                                                    + "/data/com.app.persomy.v4/databases/PersoMyDB.db");
                                    File fileBackupDir = new File(Environment
                                            .getExternalStorageDirectory(),
                                            "/PersoMy/backup");

                                    if (!fileBackupDir.exists())
                                        Toast.makeText(getApplicationContext(),
                                                R.string.backupNotFound,
                                                Toast.LENGTH_SHORT).show();

                                    if (file.exists()) {
                                        File fileBackup = new File(fileBackupDir,
                                                "PersoMyDB.db");
                                        try {
                                            fileBackup.createNewFile();
                                            copyFile(fileBackup, file);

                                            Toast.makeText(getApplicationContext(),
                                                    R.string.backupRestoredOK,
                                                    Toast.LENGTH_SHORT).show();

                                        } catch (FileNotFoundException ex) {
                                            Toast.makeText(getApplicationContext(),
                                                    R.string.backupNotFound,
                                                    Toast.LENGTH_SHORT).show();
                                            System.exit(0);
                                        } catch (IOException e) {
                                            Toast.makeText(
                                                    getApplicationContext(),
                                                    R.string.backupGenericErrorFile
                                                            + e.getMessage(),
                                                    Toast.LENGTH_SHORT).show();
                                        } catch (Exception e) {
                                            Toast.makeText(
                                                    getApplicationContext(),
                                                    R.string.backupGenericError
                                                            + e.getMessage(),
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                } else {
                                    Toast.makeText(getApplicationContext(),
                                            R.string.backupSDCardError,
                                            Toast.LENGTH_SHORT).show();
                                    System.exit(0);
                                }
                                break;

                            case 3:
                                String bkpVoce = descrizioneSpesa.getSelectedItem()
                                        .toString();

                                if (input.getText().toString().trim().length() > 0) {
                                    if (!input.getText().toString().trim()
                                            .equals(bkpVoce)) {
                                        if (scaricaDati
                                                .getSpesaCodiceDescrizione(input
                                                        .getText().toString()
                                                        .trim()) == -1) {
                                            try {
                                                database.beginTransaction();
                                                ContentValues row = new ContentValues();
                                                row.put("DESCRIZIONE", input
                                                        .getText().toString()
                                                        .trim());
                                                database.update(
                                                        "VARIE",
                                                        row,
                                                        "cont=?",
                                                        new String[]{String
                                                                .format("%d",
                                                                scaricaDati
                                                                        .getSpesaCodiceDescrizione(descrizioneSpesa
                                                                                .getSelectedItem()
                                                                                .toString()))});
                                                database.setTransactionSuccessful();
                                            } catch (Exception e) {
                                                Toast.makeText(
                                                        getApplicationContext(),
                                                        e.getMessage(),
                                                        Toast.LENGTH_LONG).show();
                                            } finally {
                                                database.endTransaction();

                                                if (CURRENT_LAYOUT == R.layout.activity_uscite) {
                                                    if (mySpesa.size() > 0)
                                                        for (int i = mySpesa.size() - 1; i > -1; i--)
                                                            if (mySpesa
                                                                    .get(i)
                                                                    .getSpesaName()
                                                                    .equals(bkpVoce))
                                                                mySpesa.set(
                                                                        i,
                                                                        new Spesa(
                                                                                input.getText()
                                                                                        .toString()
                                                                                        .trim(),
                                                                                mySpesa.get(
                                                                                                i)
                                                                                        .getSpesaPrezzo(),
                                                                                false,
                                                                                false));

                                                    myListView
                                                            .setAdapter(myAdapter);
                                                } else if (CURRENT_LAYOUT == R.layout.activity_automatiche) {
                                                    if (myMovimento.size() > 0)
                                                        for (int i = myMovimento
                                                                .size() - 1; i > -1; i--)
                                                            if (myMovimento
                                                                    .get(i)
                                                                    .getAutomaticaVoce()
                                                                    .equals(bkpVoce))
                                                                myMovimento
                                                                        .set(i,
                                                                                new Movimento(
                                                                                        myMovimento
                                                                                                .get(i)
                                                                                                .getAutomaticaFrequenza(),
                                                                                        myMovimento
                                                                                                .get(i)
                                                                                                .getAutomaticaStartDate(),
                                                                                        input.getText()
                                                                                                .toString()
                                                                                                .trim(),
                                                                                        myMovimento
                                                                                                .get(i)
                                                                                                .getAutomaticaImporto(),
                                                                                        uscita,
                                                                                        false,
                                                                                        false));

                                                    myListView
                                                            .setAdapter(myAdapterAutomatica);
                                                }
                                            }

                                            caricaSpinner();
                                            selezionaVoce(input.getText()
                                                    .toString().trim());

                                            Toast.makeText(getApplicationContext(),
                                                    R.string.spesaSalvataggioOk,
                                                    Toast.LENGTH_SHORT).show();
                                        } else
                                            Toast.makeText(getApplicationContext(),
                                                    R.string.spesaVoceDuplicata,
                                                    Toast.LENGTH_LONG).show();
                                    } else
                                        Toast.makeText(getApplicationContext(),
                                                R.string.spesaVoceDuplicata,
                                                Toast.LENGTH_LONG).show();
                                } else
                                    Toast.makeText(getApplicationContext(),
                                            R.string.spesaVoceVuota,
                                            Toast.LENGTH_SHORT).show();
                                break;

                            case 4:
                                if (input.getText().toString().trim().length() > 0) {
                                    if (scaricaDati.getSpesaCodiceDescrizione(input
                                            .getText().toString().trim()) == -1) {
                                        try {
                                            database.beginTransaction();
                                            ContentValues row = new ContentValues();
                                            row.put("DESCRIZIONE", input.getText()
                                                    .toString().trim());
                                            database.insert("VARIE", null, row);
                                            database.setTransactionSuccessful();
                                        } catch (Exception e) {
                                            Toast.makeText(getApplicationContext(),
                                                    e.getMessage(),
                                                    Toast.LENGTH_LONG).show();
                                        } finally {
                                            database.endTransaction();
                                            caricaSpinner();
                                            selezionaVoce(input.getText()
                                                    .toString().trim());
                                            Toast.makeText(getApplicationContext(),
                                                    R.string.spesaSalvataggioOk,
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    } else
                                        Toast.makeText(getApplicationContext(),
                                                R.string.spesaVoceDuplicata,
                                                Toast.LENGTH_LONG).show();
                                } else
                                    Toast.makeText(getApplicationContext(),
                                            R.string.spesaVoceVuota,
                                            Toast.LENGTH_LONG).show();

                                break;

                            case 5:
                                try {
                                    descrizioneSpesa = (Spinner) findViewById(R.id.descrizioneSpesa);
                                    myGridView = (GridView) findViewById(R.id.listaMovimenti);

                                    database.beginTransaction();
                                    database.delete(
                                            "MONEY",
                                            "descrizione=?",
                                            new String[]{String.valueOf(scaricaDati
                                                    .getSpesaCodiceDescrizione(descrizioneSpesa
                                                            .getSelectedItem()
                                                            .toString()))});
                                    database.delete(
                                            "VARIE",
                                            "cont=?",
                                            new String[]{String.valueOf(scaricaDati
                                                    .getSpesaCodiceDescrizione(descrizioneSpesa
                                                            .getSelectedItem()
                                                            .toString()))});
                                    database.setTransactionSuccessful();

                                } catch (Exception e) {
                                    Toast.makeText(getApplicationContext(),
                                                    e.getMessage(), Toast.LENGTH_LONG)
                                            .show();
                                } finally {
                                    database.endTransaction();
                                    caricaSpinner();
                                    if (descrizioneSpesa.getCount() > 0)
                                        caricaGridView(descrizioneSpesa
                                                .getSelectedItem().toString());
                                    else
                                        caricaGridView("");

                                    Toast.makeText(getApplicationContext(),
                                            R.string.spesaEliminazioneOk,
                                            Toast.LENGTH_SHORT).show();
                                }
                                break;

                            case 6:
                                try {
                                    strPassword1 = password1.getText().toString();
                                    strPassword2 = password2.getText().toString();
                                    if (!(strPassword1.equals("") || strPassword2
                                            .equals(""))) {
                                        if (strPassword1.equals(strPassword2)) {
                                            database.beginTransaction();
                                            ContentValues row = new ContentValues();
                                            row.put("ATTIVO", "1");
                                            row.put("VALORE", strPassword2.trim());
                                            database.update("OPZIONI", row,
                                                    "OPZ=?",
                                                    new String[]{"PASSWORD"});

                                            database.setTransactionSuccessful();
                                            database.endTransaction();

                                            Toast.makeText(getApplicationContext(),
                                                    R.string.setPasswordOK,
                                                    Toast.LENGTH_SHORT).show();

                                            if (Build.VERSION.SDK_INT > 13)
                                                password.setChecked(true);
                                            else
                                                password233.setChecked(true);
                                        } else {
                                            Toast.makeText(getApplicationContext(),
                                                    R.string.password_verify_ko,
                                                    Toast.LENGTH_LONG).show();

                                            if (Build.VERSION.SDK_INT > 13)
                                                password.setChecked(false);
                                            else
                                                password233.setChecked(false);
                                        }
                                    } else {
                                        Toast.makeText(getApplicationContext(),
                                                R.string.setPasswordVuota,
                                                Toast.LENGTH_LONG).show();

                                        if (Build.VERSION.SDK_INT > 13)
                                            password.setChecked(false);
                                        else
                                            password233.setChecked(false);
                                    }
                                } catch (Exception e) {
                                    Toast.makeText(getApplicationContext(),
                                                    e.getMessage(), Toast.LENGTH_LONG)
                                            .show();

                                    if (Build.VERSION.SDK_INT > 13)
                                        password.setChecked(false);
                                    else
                                        password233.setChecked(false);
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
                                database.update("OPZIONI", row, "OPZ=?",
                                        new String[]{"UPDATE"});
                                database.setTransactionSuccessful();
                                database.endTransaction();

                                Toast.makeText(getApplicationContext(),
                                                R.string.setUpdateOK, Toast.LENGTH_SHORT)
                                        .show();

                                if (Build.VERSION.SDK_INT > 13)
                                    update.setChecked(true);
                                else
                                    update233.setChecked(true);

                                break;

                            case 9:
                                try {
                                    Intent intent = new Intent(Intent.ACTION_VIEW);
                                    File f1 = new File(Environment
                                            .getExternalStorageDirectory()
                                            + "/PersoMy/backup/PersoMy.apk");
                                    intent.setDataAndType(Uri.fromFile(f1),
                                            "application/vnd.android.package-archive");
                                    startActivity(intent);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                break;
                        }
                    }
                });

        if (id != 10) // non visualizza il pulsante ANNULLA in caso di
            // visualizzazione della GUIDA
            alert.setNegativeButton(R.string.cancel,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,
                                            int whichButton) {
                            switch (id) {
                                case 1:
                                    System.exit(0);
                                    break;

                                case 2:
                                    dialog.cancel();
                                    break;

                                case 3:
                                    dialog.cancel();
                                    break;

                                case 4:
                                    dialog.cancel();
                                    break;

                                case 5:
                                    dialog.cancel();
                                    break;

                                case 6: // PASSWORD
                                    dialog.cancel();
                                    if (verify.isTherePassword(database))
                                        if (Build.VERSION.SDK_INT > 13)
                                            password.setChecked(true);
                                        else
                                            password233.setChecked(true);
                                    else if (Build.VERSION.SDK_INT > 13)
                                        password.setChecked(false);
                                    else
                                        password233.setChecked(false);
                                    break;

                                case 7:
                                    dialog.cancel();
                                    break;

                                case 8: // AGGIORNAMENTI AUTOMATICI
                                    if (verify.isThereUpdate(database))
                                        if (Build.VERSION.SDK_INT > 13)
                                            update.setChecked(true);
                                        else
                                            update233.setChecked(true);
                                    else if (Build.VERSION.SDK_INT > 13)
                                        update.setChecked(false);
                                    else
                                        update233.setChecked(false);
                                    break;

                                case 9:
                                    dialog.cancel();
                                    break;
                            }
                        }
                    });

        alert.setCancelable(false);
        alert.show();

    }

    public void caricaSpinner() {
        cur = database.query("VARIE", new String[]{"descrizione"}, null,
                null, null, null, "descrizione");
        cur.moveToFirst();
        int i = 0;
        array_spinner = new String[cur.getCount()];

        while (!cur.isAfterLast()) {
            array_spinner[i] = cur.getString(0);
            i += 1;
            cur.moveToNext();
        }

        cur.close();

        descrizioneSpesa = (Spinner) findViewById(R.id.descrizioneSpesa);
        descrizioneSpesa.setAdapter(new CustomSpinnerVarie(this, scaricaDati
                .caricaSpinnerCompleto(uscita.toString())));
    }

    // ******************************************************************
    // START FORM
    // ******************************************************************

    public void startOpzioni() {
        contentPane.removeAllViews();
        li = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        contentPane.addView(li.inflate(R.layout.activity_opzioni, null));

        menu_choise = R.menu.menu_solo_exit;
        if (Build.VERSION.SDK_INT > 11)
            invalidateOptionsMenu();
        else
            openOptionsMenu();
        if (Build.VERSION.SDK_INT > 11)
            password = (Switch) findViewById(R.id.password);
        else
            password233 = (CheckBox) findViewById(R.id.password);

        if (verify.isTherePassword(database))
            if (Build.VERSION.SDK_INT > 11)
                password.setChecked(true);
            else
                password233.setChecked(true);

        if (Build.VERSION.SDK_INT > 11) {
            password.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView,
                                             boolean isChecked) {
                    if (isChecked) {
                        id = 6;
                        msgBox();
                    } else {
                        try {
                            database.beginTransaction();
                            ContentValues row = new ContentValues();
                            row.put("ATTIVO", "0");
                            database.update("OPZIONI", row, "OPZ=?",
                                    new String[]{"PASSWORD"});
                            database.setTransactionSuccessful();

                        } catch (Exception e) {
                            Toast.makeText(getApplicationContext(),
                                    e.getMessage(), Toast.LENGTH_LONG).show();
                        } finally {
                            database.endTransaction();
                            Toast.makeText(getApplicationContext(),
                                            R.string.setPasswordKO, Toast.LENGTH_SHORT)
                                    .show();
                        }
                    }
                }
            });
        } else {
            password233.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (password233.isChecked()) {
                        id = 6;
                        msgBox();
                    } else {
                        try {
                            database.beginTransaction();
                            ContentValues row = new ContentValues();
                            row.put("ATTIVO", "0");
                            database.update("OPZIONI", row, "OPZ=?",
                                    new String[]{"PASSWORD"});
                            database.setTransactionSuccessful();

                        } catch (Exception e) {
                            Toast.makeText(getApplicationContext(),
                                    e.getMessage(), Toast.LENGTH_LONG).show();
                        } finally {
                            database.endTransaction();
                            Toast.makeText(getApplicationContext(),
                                            R.string.setPasswordKO, Toast.LENGTH_SHORT)
                                    .show();
                        }
                    }
                }
            });
        }

        if (Build.VERSION.SDK_INT > 11)
            update = (Switch) findViewById(R.id.update);
        else
            update233 = (CheckBox) findViewById(R.id.update);

        if (verify.isThereUpdate(database))
            if (Build.VERSION.SDK_INT > 11)
                update.setChecked(true);
            else
                update233.setChecked(true);

        if (Build.VERSION.SDK_INT > 11) {
            update.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView,
                                             boolean isChecked) {
                    if (update.isChecked()) {
                    } else {
                        try {
                            database.beginTransaction();
                            ContentValues row = new ContentValues();
                            row.put("ATTIVO", "0");
                            database.update("OPZIONI", row, "OPZ=?",
                                    new String[]{"UPDATE"});
                            database.setTransactionSuccessful();

                        } catch (Exception e) {
                            Toast.makeText(getApplicationContext(),
                                    e.getMessage(), Toast.LENGTH_LONG).show();
                        } finally {
                            database.endTransaction();
                            Toast.makeText(getApplicationContext(),
                                            R.string.setUpdateKO, Toast.LENGTH_SHORT)
                                    .show();
                        }
                    }
                }
            });
        } else {
            update233.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (update233.isChecked()) {
                        id = 8;
                        msgBox();
                    } else {
                        try {
                            database.beginTransaction();
                            ContentValues row = new ContentValues();
                            row.put("ATTIVO", "0");
                            database.update("OPZIONI", row, "OPZ=?",
                                    new String[]{"UPDATE"});
                            database.setTransactionSuccessful();

                        } catch (Exception e) {
                            Toast.makeText(getApplicationContext(),
                                    e.getMessage(), Toast.LENGTH_LONG).show();
                        } finally {
                            database.endTransaction();
                            Toast.makeText(getApplicationContext(),
                                            R.string.setUpdateKO, Toast.LENGTH_SHORT)
                                    .show();
                        }
                    }
                }
            });
        }
    }

    public void startInfo() {
        contentPane.removeAllViews();
        li = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        contentPane.addView(li.inflate(R.layout.activity_info, null));

        menu_choise = R.menu.menu_solo_exit;
        if (Build.VERSION.SDK_INT > 11)
            invalidateOptionsMenu();
        else
            openOptionsMenu();

        TextView info = (TextView) findViewById(R.id.versionPersoMy);

        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(
                    getPackageName(), 0);
            info.append(String.valueOf(pInfo.versionName));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startJobAutomatici() {
        database.execSQL("insert into money (DATA, DESCRIZIONE, PREZZO, USCITA) select datetime(date(b.data_start)), a.ID_VOCE, a.IMPORTO, a.USCITA from MOVIMENTI_AUTOMATICI a inner join job_automatici b on (a.ID=b.id_movimento) where b.DATA_START < DateTime('Now')");
        database.execSQL("delete from job_automatici where rowid in (select b.rowid from MOVIMENTI_AUTOMATICI a inner join job_automatici b on (a.ID=b.id_movimento) where b.DATA_START < DateTime('Now'))");
    }

    public void startUscite() {
        CURRENT_LAYOUT = R.layout.activity_uscite;

        contentPane.removeAllViews();
        li = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        contentPane.addView(li.inflate(R.layout.activity_uscite, null));

        menu_choise = R.menu.menu_uscite;

        TextView simboloEuro = (TextView) findViewById(R.id.simboloEuro);

        Locale loc = new Locale("it", "IT");
        simboloEuro.setText(Currency.getInstance(loc).getSymbol());

        mDateDisplayUscite = (TextView) findViewById(R.id.dateSpesa);
        resetCalendario();

        caricaSpinner();

        mySpesa = new ArrayList<Spesa>();
        myAdapter = new SpesaListViewAdapter(this, mySpesa);
        myListView = (ListView) findViewById(R.id.listaSpesa);
        myListView.setAdapter(myAdapter);

        mDateDisplayUscite.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            public void afterTextChanged(Editable s) {
                try {
                    dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                            Locale.ROOT);
                    dateObj = dateFormat.parse(mYear + "-" + (mMonth + 1) + "-"
                            + mDay + " 00:00:00");
                } catch (Exception ignored) {
                } finally {
                    mySpesa.clear();
                }

                cur = database.query("MONEY", new String[]{"descrizione",
                                "prezzo"}, "data=? and uscita=" + ((uscita) ? 1 : 0),
                        new String[]{dateFormat.format(dateObj)}, null,
                        null, null, null);
                cur.moveToFirst();

                while (!cur.isAfterLast()) {
                    mySpesa.add(new Spesa(scaricaDati.getSpesaDescrizione(cur
                            .getString(0)), Double.valueOf(cur.getString(1)),
                            true, false));
                    cur.moveToNext();
                }

                cur.close();
                myListView.setAdapter(myAdapter);
            }
        });

        updateDisplay(0);

        if (Build.VERSION.SDK_INT > 11)
            invalidateOptionsMenu();
        else
            openOptionsMenu();
    }

    private void startRestoreBackup() {
        id = 2;
        msgBox();
    }

    private void startBackup() {
        boolean writeable = verify.IsSdPresent();
        if (writeable) {
            File file = new File(Environment.getDataDirectory()
                    + "/data/com.app.persomy.v4/databases/PersoMyDB.db");
            File fileBackupDir = new File(
                    Environment.getExternalStorageDirectory(),
                    "/PersoMy/backup");

            if (!fileBackupDir.exists())
                fileBackupDir.mkdirs();

            if (file.exists()) {
                File fileBackup = new File(fileBackupDir, "PersoMyDB.db");
                try {
                    fileBackup.createNewFile();
                    copyFile(file, fileBackup);

                    Toast.makeText(getApplicationContext(),
                            R.string.backupOKDescr, Toast.LENGTH_LONG).show();

                } catch (FileNotFoundException ex) {
                    Toast.makeText(getApplicationContext(),
                            R.string.backupNoFile, Toast.LENGTH_SHORT).show();
                    System.exit(0);
                } catch (IOException e) {
                    Toast.makeText(getApplicationContext(),
                            R.string.backupGenericErrorFile + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(),
                            R.string.backupGenericError + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            }
        } else
            Toast.makeText(getApplicationContext(), R.string.backupSDCardError,
                    Toast.LENGTH_SHORT).show();
    }

    private void startTotale() {

        CURRENT_LAYOUT = R.layout.activity_totale;

        contentPane.removeAllViews();
        li = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        contentPane.addView(li.inflate(R.layout.activity_totale, null));

        TextView simboloEuro = (TextView) findViewById(R.id.simboloEuro);

        Locale loc = new Locale("it", "IT");
        simboloEuro.setText(Currency.getInstance(loc).getSymbol());

        TextView simboloEuro2 = (TextView) findViewById(R.id.simboloEuro2);

        simboloEuro2.setText(Currency.getInstance(loc).getSymbol());

        caricaGridView();

        if (Build.VERSION.SDK_INT > 11)
            invalidateOptionsMenu();
        else
            openOptionsMenu();
    }

    private void startAnno() {

        CURRENT_LAYOUT = R.layout.activity_anno;

        contentPane.removeAllViews();
        li = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        contentPane.addView(li.inflate(R.layout.activity_anno, null));

        TextView simboloEuro = (TextView) findViewById(R.id.simboloEuro);

        Locale loc = new Locale("it", "IT");
        simboloEuro.setText(Currency.getInstance(loc).getSymbol());

        simboloEuro.setText(Currency.getInstance(loc).getSymbol());
        TextView simboloEuro2 = (TextView) findViewById(R.id.simboloEuro2);
        simboloEuro2.setText(Currency.getInstance(loc).getSymbol());

        caricaSpinnerAnno();

        anno = (Spinner) findViewById(R.id.selAnno);

        if (anno.getCount() > 0) {
            anno.setOnItemSelectedListener(new OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> parent, View view,
                                           int pos, long id) {
                    Object item = parent.getItemAtPosition(pos);
                    myListaReportAnno = new ArrayList<ListaReportAnno>();

                    for (int i = 1; i < 13; i++) {
                        myListaReportAnno.add(new ListaReportAnno(scaricaDati
                                .getMonth(i), 0, 0));
                    }

                    cur = database.query("MONEY", new String[]{
                                    "strftime('%m',data) as month", "sum(prezzo)"},
                            "strftime('%Y', data)=? and uscita=0",
                            new String[]{item.toString()}, "month", null,
                            "month");
                    cur.moveToFirst();

                    while (cur.getCount() > 0 && !cur.isAfterLast()) {
                        try {
                            myListaReportAnno.set(
                                    Integer.parseInt(cur.getString(0)) - 1,
                                    new ListaReportAnno(myListaReportAnno
                                            .get(Integer.parseInt(cur
                                                    .getString(0)) - 1)
                                            .getListaMese(), Double
                                            .parseDouble(cur.getString(1)),
                                            myListaReportAnno.get(
                                                            Integer.parseInt(cur
                                                                    .getString(0)) - 1)
                                                    .getListaEntrata()));
                            cur.moveToNext();

                        } catch (Exception ex) {
                            Log.d("PersoMy: ", ex.toString());
                        }
                    }

                    cur.close();

                    cur = database.query("MONEY", new String[]{
                                    "strftime('%m',data) month", "sum(prezzo)"},
                            "strftime('%Y', data)=? and uscita=1",
                            new String[]{item.toString()}, "month", null,
                            "month");
                    cur.moveToFirst();

                    while (cur.getCount() > 0 && !cur.isAfterLast()) {
                        try {
                            myListaReportAnno.set(
                                    Integer.parseInt(cur.getString(0)) - 1,
                                    new ListaReportAnno(myListaReportAnno
                                            .get(Integer.parseInt(cur
                                                    .getString(0)) - 1)
                                            .getListaMese(), myListaReportAnno
                                            .get(Integer.parseInt(cur
                                                    .getString(0)) - 1)
                                            .getListaEntrata(), Double
                                            .parseDouble(cur.getString(1))));
                            cur.moveToNext();

                        } catch (Exception ex) {
                            Log.d("PersoMy: ", ex.toString());
                        }
                    }

                    cur.close();

                    cur = database.query("MONEY",
                            new String[]{"SUM(prezzo)"},
                            "uscita=0 and strftime('%Y', data)=?",
                            new String[]{item.toString()}, null, null, null);
                    cur.moveToFirst();

                    if (cur.getCount() > 0 && cur.getString(0) != null
                            && Double.parseDouble(cur.getString(0)) > 0) {
                        try {
                            TextView totEntrate = (TextView) findViewById(R.id.totaleEntrate);
                            totEntrate.setText(String.valueOf(df.format(Double
                                    .parseDouble(cur.getString(0)))));

                        } catch (Exception ex) {
                            Log.d("PersoMy: ", ex.toString());
                        }
                    }

                    cur.close();

                    cur = database.query("MONEY",
                            new String[]{"SUM(prezzo)"},
                            "uscita=1 and strftime('%Y', data)=?",
                            new String[]{item.toString()}, null, null, null);
                    cur.moveToFirst();

                    if (cur.getCount() > 0 && cur.getString(0) != null
                            && Double.parseDouble(cur.getString(0)) > 0) {
                        try {
                            TextView totUscite = (TextView) findViewById(R.id.totaleUscite);
                            totUscite.setText(String.valueOf(df.format(Double
                                    .parseDouble(cur.getString(0)))));

                        } catch (Exception ex) {
                            Log.d("PersoMy: ", ex.toString());
                        }
                    }

                    cur.close();

                    myAdapterListaReporAnno = new ReportAnnoGridViewAdapter(
                            getApplicationContext(), myListaReportAnno);
                    myGridView = (GridView) findViewById(R.id.listaMovimentiAnno);
                    myGridView.setAdapter(myAdapterListaReporAnno);
                }

                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        }

        if (Build.VERSION.SDK_INT > 11)
            invalidateOptionsMenu();
        else
            openOptionsMenu();
    }

    private void startMensile() {

        CURRENT_LAYOUT = R.layout.activity_mensile;

        contentPane.removeAllViews();
        li = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        contentPane.addView(li.inflate(R.layout.activity_mensile, null));

        if (Build.VERSION.SDK_INT > 11)
            invalidateOptionsMenu();
        else
            openOptionsMenu();

        TextView simboloEuro = (TextView) findViewById(R.id.simboloEuro);

        Locale loc = new Locale("it", "IT");
        simboloEuro.setText(Currency.getInstance(loc).getSymbol());

        TextView simboloEuro2 = (TextView) findViewById(R.id.simboloEuro2);

        simboloEuro2.setText(Currency.getInstance(loc).getSymbol());

        //
        // DisplayMetrics dm = this.getResources().getDisplayMetrics();
        // LinearLayout myBar = (LinearLayout) findViewById(R.id.widget45);
        // ViewGroup.LayoutParams params = myBar.getLayoutParams();
        // params.height = dm.heightPixels - 140;
        // myBar.setLayoutParams(new LinearLayout.LayoutParams(params));

        caricaSpinnerAnno();

        mese = (Spinner) findViewById(R.id.selMese);
        anno = (Spinner) findViewById(R.id.selAnno);

        ArrayAdapter<String> myAdapterMese = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item,
                scaricaDati.caricaSpinnerMese());
        myAdapterMese
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mese.setAdapter(myAdapterMese);

        if (anno.getCount() > 0) {
            mese.setSelection(c.get(Calendar.MONTH));
            mese.setOnItemSelectedListener(new OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> parent, View view,
                                           int pos, long id) {
                    Object item = parent.getItemAtPosition(pos);
                    caricaGridView(scaricaDati.meseToNumber(item.toString()),
                            anno.getSelectedItem().toString());
                }

                public void onNothingSelected(AdapterView<?> parent) {
                }
            });

            anno.setOnItemSelectedListener(new OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> parent, View view,
                                           int pos, long id) {
                    Object item = parent.getItemAtPosition(pos);
                    caricaGridView(scaricaDati.meseToNumber(mese
                            .getSelectedItem().toString()), (String) item);
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
        li = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        contentPane.addView(li.inflate(R.layout.activity_periodo, null));

        if (Build.VERSION.SDK_INT > 11)
            invalidateOptionsMenu();
        else
            openOptionsMenu();

        TextView simboloEuro = (TextView) findViewById(R.id.simboloEuro);
        Locale loc = new Locale("it", "IT");
        simboloEuro.setText(Currency.getInstance(loc).getSymbol());

        TextView simboloEuro2 = (TextView) findViewById(R.id.simboloEuro2);
        simboloEuro2.setText(Currency.getInstance(loc).getSymbol());

        DisplayMetrics dm = this.getResources().getDisplayMetrics();
        LinearLayout myBar = (LinearLayout) findViewById(R.id.widget45);
        LayoutParams params = myBar.getLayoutParams();
        params.height = dm.heightPixels - 180;
        myBar.setLayoutParams(new LinearLayout.LayoutParams(params));

        mDateDisplayDa = (TextView) findViewById(R.id.dataAutomaticaTxtDa);
        mDateDisplayA = (TextView) findViewById(R.id.dataAutomaticaTxtA);
        resetCalendario();

        descrizioneSpesa = (Spinner) findViewById(R.id.descrizioneSpesa);
        descrizioneSpesa.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, scaricaDati
                .caricaSpinnerCompleto("null")));

        all = (CheckBox) findViewById(R.id.selectAll);
        all.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                descrizioneSpesa = (Spinner) findViewById(R.id.descrizioneSpesa);
                if (all.isChecked()) {
                    descrizioneSpesa.setEnabled(false);
                } else {
                    descrizioneSpesa.setEnabled(true);
                }
            }
        });
    }

    private void startOperazioniAutomatiche() {
        CURRENT_LAYOUT = R.layout.activity_automatiche;

        contentPane.removeAllViews();
        li = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        contentPane.addView(li.inflate(R.layout.activity_automatiche, null));

        menu_choise = R.menu.menu_uscite;

        TextView simboloEuro = (TextView) findViewById(R.id.simboloEuro);
        Locale loc = new Locale("it", "IT");
        simboloEuro.setText(Currency.getInstance(loc).getSymbol());

        mDateDisplay = (TextView) findViewById(R.id.dataAutomaticaTxt);
        mTimeDisplay = (TextView) findViewById(R.id.oraAutomaticaTxt);
        mHourOfDay = c.get(Calendar.HOUR_OF_DAY);
        mMinute = c.get(Calendar.MINUTE);
        dialogTime = new TimePickerDialog(this, new PickTime(), mHourOfDay,
                mMinute, DateFormat.is24HourFormat(this));
        resetCalendario();
        resetTime();
        updateDisplay(1);

        voceAutomatica = (Spinner) findViewById(R.id.descrizioneSpesa);
        voceAutomatica.setAdapter(new CustomSpinnerVarie(this, scaricaDati
                .caricaSpinnerCompleto(uscita.toString())));

        // listview
        myMovimento = new ArrayList<Movimento>();
        myAdapterAutomatica = new MovimentoListViewAdapter(this, myMovimento);
        myListView = (ListView) findViewById(R.id.listaAutomatica);

        myMovimento.clear();

        int a = (uscita) ? 1 : 0;

        cur = database
                .query("MOVIMENTI_AUTOMATICI a INNER JOIN VARIE c ON (a.ID_VOCE = c.CONT)",
                        new String[]{"a.ID_FREQUENZA-1", "a.DATA_START",
                                "c.DESCRIZIONE", "a.IMPORTO", "a.USCITA"},
                        "a.uscita=? ", new String[]{String.valueOf(a)},
                        null, null, "a.DATA_START");
        cur.moveToFirst();

        try {
            while (cur.getCount() > 0 && !cur.isAfterLast()) {
                dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm",
                        Locale.ROOT);
                dateObj = dateFormat.parse(cur.getString(1).substring(0,
                        cur.getString(1).length() - 3));
                dateFormat.applyPattern("dd/MM/yyyy HH:mm");
                myMovimento.add(new Movimento(myFrequenza.get(cur.getInt(0))
                        .getDescrizioneFrequenza(), dateFormat.format(dateObj),
                        cur.getString(2), Double.parseDouble(cur.getString(3)),
                        uscita, true, false));
                cur.moveToNext();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            cur.close();
        }

        myListView.setAdapter(myAdapterAutomatica);

        RadioGroup grpRB = (RadioGroup) findViewById(R.id.radioGroup1);
        entrateRB = (RadioButton) findViewById(R.id.usciteRB);
        usciteRB = (RadioButton) findViewById(R.id.entrateRB);

        grpRB.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup rg, int checkedId) {
                if (entrateRB.getId() == checkedId)
                    uscita = true;

                if (usciteRB.getId() == checkedId)
                    uscita = false;

                voceAutomatica = (Spinner) findViewById(R.id.descrizioneSpesa);
                voceAutomatica.setAdapter(new CustomSpinnerVarie(activity,
                        scaricaDati.caricaSpinnerCompleto(uscita.toString())));

                myMovimento.clear();

                int a = (uscita) ? 1 : 0;

                cur = database
                        .query("MOVIMENTI_AUTOMATICI a INNER JOIN VARIE c ON (a.ID_VOCE = c.CONT)",
                                new String[]{"a.ID_FREQUENZA-1",
                                        "a.DATA_START", "c.DESCRIZIONE",
                                        "a.IMPORTO", "a.USCITA"},
                                "a.uscita=? ",
                                new String[]{String.valueOf(a)}, null, null,
                                "a.DATA_START");
                cur.moveToFirst();

                try {
                    while (cur.getCount() > 0 && !cur.isAfterLast()) {
                        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm",
                                Locale.ROOT);
                        dateObj = dateFormat.parse(cur.getString(1).substring(
                                0, cur.getString(1).length() - 3));
                        dateFormat.applyPattern("dd/MM/yyyy HH:mm");

                        myMovimento.add(new Movimento(myFrequenza.get(
                                cur.getInt(0)).getDescrizioneFrequenza(),
                                dateFormat.format(dateObj), cur.getString(2),
                                Double.parseDouble(cur.getString(3)), uscita,
                                true, false));
                        cur.moveToNext();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                } finally {
                    cur.close();
                }

                myListView.setAdapter(myAdapterAutomatica);
            }
        });

        importoAutomatica = (EditText) findViewById(R.id.importoAutomatica);
        frequenza = (Spinner) findViewById(R.id.frequenza);

        if (Build.VERSION.SDK_INT > 11)
            invalidateOptionsMenu();
        else
            openOptionsMenu();
    }

    private void startVoce() {

        contentPane.removeAllViews();
        li = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        contentPane.addView(li.inflate(R.layout.activity_voce, null));

        if (Build.VERSION.SDK_INT > 11) {
            ActionBar bar = getActionBar();
            bar.hide();
            bar.show();
        }

        if (Build.VERSION.SDK_INT > 11)
            invalidateOptionsMenu();
        else
            openOptionsMenu();

        TextView simboloEuro = (TextView) findViewById(R.id.simboloEuro);
        Locale loc = new Locale("it", "IT");
        simboloEuro.setText(Currency.getInstance(loc).getSymbol());

        TextView simboloEuro2 = (TextView) findViewById(R.id.simboloEuro2);
        simboloEuro2.setText(Currency.getInstance(loc).getSymbol());

        descrizioneSpesa = (Spinner) findViewById(R.id.descrizioneSpesa);
        descrizioneSpesa.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, scaricaDati
                .caricaSpinnerCompleto("null")));
        descrizioneSpesa
                .setOnItemSelectedListener(new OnItemSelectedListener() {
                    public void onItemSelected(AdapterView<?> parent,
                                               View view, int pos, long id) {
                        Object item = parent.getItemAtPosition(pos);
                        caricaGridView(item.toString());
                    }

                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });

        if (menu_choise == R.menu.menu_elimina) {
            DisplayMetrics dm = this.getResources().getDisplayMetrics();
            LinearLayout myBar = (LinearLayout) findViewById(R.id.widget45);
            LayoutParams params = myBar.getLayoutParams();
            params.height = dm.heightPixels * 68 / 100;
            myBar.setLayoutParams(new LinearLayout.LayoutParams(params));
        } else {
            DisplayMetrics dm = this.getResources().getDisplayMetrics();
            LinearLayout myBar = (LinearLayout) findViewById(R.id.widget45);
            LayoutParams params = myBar.getLayoutParams();
            params.height = dm.heightPixels - 140;
            myBar.setLayoutParams(new LinearLayout.LayoutParams(params));
        }
    }

    // ******************************************************************
    // CLICK BOTTONI
    // ******************************************************************

    public boolean modificaDescrizioneBtnClick(View v) {
        id = 3;
        descrizioneSpesa = (Spinner) findViewById(R.id.descrizioneSpesa);
        if (descrizioneSpesa.getCount() > 0)
            msgBox();
        return true;
    }

    public void onAggiungiSpesaBtnPress(View v) {

        if (importoAutomatica.length() != 0
                && Double.parseDouble(importoAutomatica.getText().toString()) > 0) {
            myMovimento.add(new Movimento(frequenza.getSelectedItem()
                    .toString(), mDateDisplay.getText() + " "
                    + mTimeDisplay.getText(), voceAutomatica.getSelectedItem()
                    .toString(), Double.parseDouble(importoAutomatica.getText()
                    .toString()), uscita, false, false));
            myAdapterAutomatica = new MovimentoListViewAdapter(this,
                    myMovimento);
            myListView.setAdapter(myAdapterAutomatica);
            importoAutomatica.setText("");
        } else
            Toast.makeText(getApplicationContext(),
                    R.string.spesaControlloImporto, Toast.LENGTH_SHORT).show();
    }

    public void onAddSpesaBtnPress(View v) {

        EditText soldiSpesa = (EditText) findViewById(R.id.soldiSpesa);
        Spinner descrizioneSpesa = (Spinner) findViewById(R.id.descrizioneSpesa);

        if (!descrizioneSpesa.getSelectedItem().toString().equals(""))
            if (soldiSpesa.length() != 0
                    && Double.parseDouble(soldiSpesa.getText().toString()) > 0) {
                mySpesa.add(new Spesa(descrizioneSpesa.getSelectedItem()
                        .toString(), Double.parseDouble(soldiSpesa.getText()
                        .toString()), false, false));
                myAdapter = new SpesaListViewAdapter(this, mySpesa);
                myListView.setAdapter(myAdapter);
                soldiSpesa.setText("");
            } else {
                Toast.makeText(getApplicationContext(),
                                R.string.spesaControlloImporto, Toast.LENGTH_SHORT)
                        .show();
            }
        else
            Toast.makeText(getApplicationContext(),
                    R.string.menu_selezione_voce, Toast.LENGTH_SHORT).show();
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

    public void onUsciteBtnPress(View v) {
        uscita = true;
        setTitle(getString(R.string.title_activity_main) + " - "
                + getString(R.string.menu_uscite));
        startUscite();
    }

    public void onEntrateBtnPress(View v) {
        setTitle(getString(R.string.title_activity_main) + " - "
                + getString(R.string.menu_entrate));
        uscita = false;
        startUscite();
    }

    public void onReportBtnPress(View v) {
        menu_choise = R.menu.menu_report;
        openContextMenu(v);
    }

    public void onBackupBtnPress(View v) {
        menu_choise = R.menu.menu_backup;
        openContextMenu(v);
    }

    public void onAutomaticheBtnPress(View v) {
        setTitle(getString(R.string.title_activity_main) + " - "
                + getString(R.string.menu_operazioni_automatiche));
        startOperazioniAutomatiche();
    }

    public void onOptionsBtnPress(View v) {
        setTitle(getString(R.string.title_activity_main) + " - "
                + getString(R.string.menu_opzioni));
        startOpzioni();
    }

    public void onEliminaBtnPress(View v) {
        menu_choise = R.menu.menu_elimina;
        setTitle(getString(R.string.title_activity_main) + " - "
                + getString(R.string.menu_elimina));
        startVoce();
    }

    public void onExitBtnPress(View v) {
        id = 7;
        msgBox();
    }

    private void updateDisplay(int scelta) {
        switch (scelta) {
            case 0:
                if (mDay < 10)
                    giorni = "0" + mDay;
                else
                    giorni = String.valueOf(mDay);

                if (mMonth < 9)
                    mesi = "0" + (mMonth + 1);
                else
                    mesi = String.valueOf(mMonth + 1);
                break;

            case 1:
                if (mHourOfDay < 10)
                    ora = "0" + mHourOfDay;
                else
                    ora = String.valueOf(mHourOfDay);

                if (mMinute < 10)
                    minuti = "0" + mMinute;
                else
                    minuti = String.valueOf(mMinute);
                break;
        }

        if (CURRENT_LAYOUT == R.layout.activity_uscite) {
            mDateDisplayUscite.setText(new StringBuilder().append(giorni)
                    .append("/").append(mesi).append("/").append(mYear));
            mDateDisplayUscite.setTextSize(25);
        } else if (CURRENT_LAYOUT == R.layout.activity_automatiche) {
            mDateDisplay.setText(new StringBuilder().append(giorni).append("/")
                    .append(mesi).append("/").append(mYear));
            mDateDisplay.setTextSize(25);
            mTimeDisplay.setText(new StringBuilder().append(ora).append(":")
                    .append(minuti));
            mTimeDisplay.setTextSize(25);
        } else if (CURRENT_LAYOUT == R.layout.activity_periodo) {
            if (da == 0) {
                mDateDisplayDa.setText(new StringBuilder().append(giorni)
                        .append("/").append(mesi).append("/").append(mYear));
                mDateDisplayDa.setTextSize(18);
                mDateDisplayA.setText(new StringBuilder().append(giorni)
                        .append("/").append(mesi).append("/").append(mYear));
                mDateDisplayA.setTextSize(18);
            } else if (da == 1) {
                mDateDisplayDa.setText(new StringBuilder().append(giorni)
                        .append("/").append(mesi).append("/").append(mYear));
                mDateDisplayDa.setTextSize(18);
            } else if (da == 2) {
                mDateDisplayA.setText(new StringBuilder().append(giorni)
                        .append("/").append(mesi).append("/").append(mYear));
                mDateDisplayA.setTextSize(18);
            }
        }
    }

    // ******************************************************************
    // DISPLAY
    // ******************************************************************
    private class PickDate implements DatePickerDialog.OnDateSetListener {
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
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
