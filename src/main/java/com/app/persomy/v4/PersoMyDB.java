package com.app.persomy.v4;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CountDownLatch;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class PersoMyDB extends SQLiteOpenHelper {
	private static final String DATABASE_NAME = "PersoMyDB.db";
	private static final int DATABASE_VERSION = 31;

	public static CountDownLatch latch;

	public static final String TABLE_OPZIONI = "OPZIONI";
	public static final String TABLE_MONEY = "MONEY";
	public static final String TABLE_ORARIO = "ORARIO";
	public static final String TABLE_SEDE = "SEDE";
	public static final String TABLE_VARIE = "VARIE";
	public static final String TABLE_JOB_AUTOMATICI = "JOB_AUTOMATICI";
	public static final String TABLE_MOVIMENTI_AUTOMATICI = "MOVIMENTI_AUTOMATICI";

	public static final String COLUMN_CONT = "CONT";
	public static final String COLUMN_DATA = "DATA";
	public static final String COLUMN_DESCRIZIONE = "DESCRIZIONE";
	public static final String COLUMN_PREZZO = "PREZZO";
	public static final String COLUMN_USCITA = "USCITA";
	public static final String COLUMN_WORKDATE = "WORKDATE";
	public static final String COLUMN_INGRESSO = "INGRESSO";
	public static final String COLUMN_SEDE = "SEDE";
	public static final String COLUMN_MALATTIA = "MALATTIA";
	public static final String COLUMN_FERIE = "FERIE";
	public static final String COLUMN_ID_SEDE = "ID_SEDE";
	public static final String COLUMN_LUOGO = "LUOGO";
	public static final String COLUMN_OPZ = "OPZ";
	public static final String COLUMN_ATTIVO = "ATTIVO";
	public static final String COLUMN_VALORE = "VALORE";
	public static final String COLUMN_ID_FREQUENZA = "ID_FREQUENZA";
	public static final String COLUMN_ID_MOVIMENTO = "ID_MOVIMENTO";
	public static final String COLUMN_DATA_START = "DATA_START";
	public static final String COLUMN_ID = "ID";
	public static final String COLUMN_ID_VOCE = "ID_VOCE";
	public static final String COLUMN_IMPORTO = "IMPORTO";

	private static final String CREATE_TABLE_MOVIMENTI_AUTOMATICI = "create table "
			+ TABLE_MOVIMENTI_AUTOMATICI
			+ "("
			+ COLUMN_ID
			+ " integer primary key autoincrement NOT NULL, "
			+ COLUMN_ID_FREQUENZA
			+ " INTEGER  NOT NULL, "
			+ COLUMN_DATA_START
			+ " DATA  NOT NULL, "
			+ COLUMN_ID_VOCE
			+ " INTEGER  NOT NULL, "
			+ COLUMN_IMPORTO
			+ " FLOAT NOT NULL, "
			+ COLUMN_USCITA
			+ " bit DEFAULT 1 " + " );";

	private static final String CREATE_TABLE_JOB_AUTOMATICI = "create table "
			+ TABLE_JOB_AUTOMATICI + "(" + COLUMN_CONT
			+ " INTEGER  NOT NULL PRIMARY KEY AUTOINCREMENT, "
			+ COLUMN_ID_MOVIMENTO + " INTEGER  NOT NULL, " + COLUMN_DATA_START
			+ " DATetime  NOT NULL " + " );";

	private static final String CREATE_TABLE_OPZIONI = "create table "
			+ TABLE_OPZIONI + "(" + COLUMN_OPZ
			+ " VARCHAR(10) NOT NULL PRIMARY KEY, " + COLUMN_ATTIVO
			+ " BOOLEAN, " + COLUMN_VALORE + " VARCHAR(20) " + " );";

	private static final String CREATE_TABLE_MONEY = "create table "
			+ TABLE_MONEY + "(" + COLUMN_CONT
			+ " integer primary key autoincrement NOT NULL, " + COLUMN_DATA
			+ " datetime, " + COLUMN_DESCRIZIONE
			+ " nvarchar(100) COLLATE NOCASE, " + COLUMN_PREZZO + " numeric, "
			+ COLUMN_USCITA + " bit DEFAULT 1 " + " );";

	private static final String CREATE_TABLE_ORARIO = "create table "
			+ TABLE_ORARIO + "(" + COLUMN_WORKDATE + " datetime NOT NULL, "
			+ COLUMN_INGRESSO + " datetime, " + COLUMN_USCITA + " datetime, "
			+ COLUMN_SEDE + " integer, " + COLUMN_MALATTIA + " bit DEFAULT 0, "
			+ COLUMN_FERIE + " bit DEFAULT 0, PRIMARY KEY (" + COLUMN_WORKDATE
			+ " ));";

	private static final String CREATE_TABLE_SEDE = "create table "
			+ TABLE_SEDE + "(" + COLUMN_ID_SEDE
			+ " integer PRIMARY KEY AUTOINCREMENT NOT NULL, "
			+ COLUMN_DESCRIZIONE + " nvarchar(100) COLLATE NOCASE, "
			+ COLUMN_LUOGO + " nvarchar(100) COLLATE NOCASE " + " );";

	private static final String CREATE_TABLE_VARIE = "create table "
			+ TABLE_VARIE + "(" + COLUMN_CONT
			+ " integer PRIMARY KEY AUTOINCREMENT NOT NULL, "
			+ COLUMN_DESCRIZIONE + " nvarchar(100) UNIQUE NOT NULL" + " );";

	private static final String CREATE_INDEX_MONEY = "CREATE UNIQUE INDEX [IDX_MONEY_CONT] ON [Money] ([cont] ASC);";
	private static final String CREATE_INDEX_ORARIO = "CREATE UNIQUE INDEX [IDX_ORARIO_DATA] ON [Orario] ([workDate] DESC);";
	private static final String CREATE_INDEX_SEDE = "CREATE UNIQUE INDEX [IDX_SEDE__ID] ON [Sede] ([id_sede] ASC);";
	private static final String CREATE_INDEX_VARIE = "CREATE UNIQUE INDEX [IDX_VARIE_CONT] ON [Varie] ([cont] ASC);";

	private static final String CREATE_INDEX_JOB_AUTOMATICI = "CREATE INDEX [IDX_JOB_AUTOMATICI_ID_MOVIMENTO] ON [JOB_AUTOMATICI] ([ID_MOVIMENTO] ASC);";
	private static final String CREATE_INDEX_IDX_MOVIMENTI_AUTOMATICI_ID = "CREATE UNIQUE INDEX [IDX_MOVIMENTI_AUTOMATICI_ID] ON [MOVIMENTI_AUTOMATICI] ([ID] ASC);";

	private Context myContext;
	private SQLiteDatabase myDatabase;

	public PersoMyDB(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.myContext = context;
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		this.myDatabase = database;

		latch = new CountDownLatch(2);

		createTableIndex();

		new Thread(new Runnable() {
			public void run() {
				insertFasamentoIniziale(R.raw.popola_money);
				latch.countDown();
			}
		}).start();

		new Thread(new Runnable() {
			public void run() {
				insertFasamentoIniziale(R.raw.popola_orario);
				latch.countDown();
			}
		}).start();

		new Thread(new Runnable() {
			public void run() {
				insertFasamentoIniziale(R.raw.popola_sede);
				latch.countDown();
			}
		}).start();

		new Thread(new Runnable() {
			public void run() {
				insertFasamentoIniziale(R.raw.popola_varie);
				latch.countDown();
			}
		}).start();

		new Thread(new Runnable() {
			public void run() {
				insertFasamentoIniziale(R.raw.popola_opzioni);
				latch.countDown();
			}
		}).start();
	}

	@Override
	public void onUpgrade(SQLiteDatabase database, int oldVersion,
			int newVersion) {
		if (database.getVersion() < 18) {
			database.execSQL(CREATE_TABLE_OPZIONI);
			insertFasamentoIniziale(R.raw.popola_opzioni);
			database.execSQL("update OPZIONI set valore=(select password from access), ATTIVO=(select count(*) from ACCESS) where OPZ='PASSWORD'");
			database.execSQL("drop table ACCESS");
		}

		if (database.getVersion() < 22) {
			database.execSQL("drop index Money_UQ__Money__00000000000000A3");
			database.execSQL("drop index Orario_UQ__Orario__000000000000001A");
			database.execSQL("drop index Sede_UQ__Sede__0000000000000064");
			database.execSQL("drop index Varie_UQ__Varie__00000000000000B1");

			database.execSQL(CREATE_INDEX_MONEY);
			database.execSQL(CREATE_INDEX_ORARIO);
			database.execSQL(CREATE_INDEX_SEDE);
			database.execSQL(CREATE_INDEX_VARIE);

			database.execSQL(CREATE_TABLE_MOVIMENTI_AUTOMATICI);
			database.execSQL(CREATE_TABLE_JOB_AUTOMATICI);

			database.execSQL(CREATE_INDEX_JOB_AUTOMATICI);
			database.execSQL(CREATE_INDEX_IDX_MOVIMENTI_AUTOMATICI_ID);
		}

		if (database.getVersion() < 31) {
			database.execSQL("update money set data = datetime(date(data))");
		}
	}

	public void createTableIndex() {
		// CREAZIONE TABELLE
		myDatabase.execSQL(CREATE_TABLE_MONEY);
		myDatabase.execSQL(CREATE_TABLE_ORARIO);
		myDatabase.execSQL(CREATE_TABLE_SEDE);
		myDatabase.execSQL(CREATE_TABLE_VARIE);
		myDatabase.execSQL(CREATE_TABLE_OPZIONI);
		myDatabase.execSQL(CREATE_TABLE_MOVIMENTI_AUTOMATICI);
		myDatabase.execSQL(CREATE_TABLE_JOB_AUTOMATICI);

		// CREAZIONE INDICI
		myDatabase.execSQL(CREATE_INDEX_MONEY);
		myDatabase.execSQL(CREATE_INDEX_ORARIO);
		myDatabase.execSQL(CREATE_INDEX_SEDE);
		myDatabase.execSQL(CREATE_INDEX_VARIE);
		myDatabase.execSQL(CREATE_INDEX_JOB_AUTOMATICI);
		myDatabase.execSQL(CREATE_INDEX_IDX_MOVIMENTI_AUTOMATICI_ID);
	}

	public void insertFasamentoIniziale(int id) {
		try {
			InputStream in = myContext.getResources().openRawResource(id);
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(in, null);
			doc.getDocumentElement().normalize();

			NodeList nList = doc.getElementsByTagName("INSERT");

			myDatabase.beginTransaction();

			for (int temp = 0; temp < nList.getLength(); temp++) {
				Node nNode = nList.item(temp);

				Element eElement = (Element) nNode;
				String nodeName = eElement.getNodeName();
				String nodeValue = eElement.getFirstChild().getNodeValue();

				if (nodeName.equals("INSERT")) {
					SQLiteStatement insert = myDatabase
							.compileStatement(nodeValue);
					if (insert != null) {
						insert.execute();
					}
				}
			}

			myDatabase.setTransactionSuccessful();

		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} finally {
			myDatabase.endTransaction();
		}
	}

	@Override
	public void onDowngrade(SQLiteDatabase database, int oldVersion,
			int newVersion) {
	}

}
