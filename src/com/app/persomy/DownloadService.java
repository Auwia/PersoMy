package com.app.persomy;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

public class DownloadService extends IntentService {

  private int result = Activity.RESULT_CANCELED;
  private int version=-1; 

  
  public DownloadService() 
  {
    super("DownloadService");
  }

  @Override
  protected void onHandleIntent(Intent intent) 
  {
	  Uri data = intent.getData();
	  String urlPath = null;
	  String fileName = data.getLastPathSegment();
	  File file = new File(Environment.getExternalStorageDirectory() + "/PersoMy/backup/", fileName + ".apk");
	  
	  try 
	  {
		  URL url = new URL("http://www.aptoide.com/webservices/listRepository/private/d5ba835055ba108823524d7e1082fc7a20d258c750864b5e662d7/auwia/orderby/recent/0/0/xml");
		  HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		  conn.setRequestMethod("GET");
		  conn.setRequestProperty("Accept", "application/json");
		  
		  BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
		  
		  String output = null;
		  if (br.readLine() != null) //VERIFICA LA VERSIONE (INUTILE PER IL BUSINESS)
			  if ((output=br.readLine()) != null )
				  {
				  	//PRENDO L'XML DI RITORNO
				  }

		  DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		  InputSource is = new InputSource();
		  is.setCharacterStream(new StringReader(output));
		  
		  Document doc = db.parse(is);
		  
		  NodeList nList = doc.getElementsByTagName("entry");
		  
		  /* for (int temp = 0; temp < nList.getLength(); temp++) { //PRENDE SOLO LA PRIMA RIGA, TANTO L'ORDINAMENTO E' DESC
		   * 
		   * Node nNode = nList.item(temp); */
		  
		  Node nNode = nList.item(0);
		  
		  if (nNode.getNodeType() == Node.ELEMENT_NODE) 
		  {
			  Element eElement = (Element) nNode;
			  
			  //	System.out.println("name : " + eElement.getElementsByTagName("name").item(0).getTextContent());
			  urlPath = eElement.getElementsByTagName("path").item(0).getTextContent();
			  //	System.out.println("ver : " + eElement.getElementsByTagName("ver").item(0).getTextContent());
			  version = Integer.parseInt(eElement.getElementsByTagName("vercode").item(0).getTextContent());
			  //	System.out.println("apkid : " + eElement.getElementsByTagName("apkid").item(0).getTextContent());
			  //	System.out.println("date : " + eElement.getElementsByTagName("date").item(0).getTextContent());
		  }
//			}
		  conn.disconnect();
		  
		  } catch (UnknownHostException e) 
		  {
			  Log.d("PersoMy: ", getString(R.string.err_UnknownHostException));
			  
		  } catch (MalformedURLException e) 
		  {
			  e.printStackTrace();
		  } catch (IOException e) 
		  {
			  e.printStackTrace();
		  } catch (ParserConfigurationException e ) 
		  {
			  e.printStackTrace();
		  } catch (Exception e ) 
		  {
			  e.printStackTrace();
		  }
	  
	  if (MainActivity.CURRENT_VERSION < version )
	  {
		  File verificaDir = new File(Environment.getExternalStorageDirectory() + "/PersoMy/backup/");
          
          if (!verificaDir.exists())
          {
        	  verificaDir.mkdirs();
          }
          
		  if (file.exists()) 
		  {
			  file.delete();
		  }
		  
		  InputStream stream = null;
		  FileOutputStream fos = null;
		  
		  try
		  {
			  URL url = new URL(urlPath);
			  URLConnection connection = url.openConnection();
			  connection.connect();
			  
			  InputStream input = new BufferedInputStream(url.openStream());
			  
			  OutputStream output = new FileOutputStream(file.getPath());
			  
			  byte[] buffer = new byte[1024];
			  int len1;
			  while ((len1 = input.read(buffer)) != -1)
			  {
				  if (len1 != 0) 
				  {
					  output.write(buffer, 0, len1);
				  }
			  }
			  
			  output.flush();
			  output.close();
			  input.close();
			  
			  result = Activity.RESULT_OK;
		  } catch (Exception e)
		  {
			  e.printStackTrace();
		  } finally
		  {
			  if (stream != null)
			  {
				  try
				  {
					  stream.close();
				  } catch (IOException e)
				  {
					  e.printStackTrace();
				  }
			  }
			  if (fos != null)
			  {
				  try
				  {
					  fos.close();
				  } catch (IOException e)
				  {
					  e.printStackTrace();
				  }
			  }
		  }
	  }
	  
	  Bundle extras = intent.getExtras();
	  if (extras != null)
	  {
		  Messenger messenger = (Messenger) extras.get("MESSENGER");
		  Message msg = Message.obtain();
		  msg.arg1 = result;
		  msg.obj = file.getAbsolutePath();
		  try
		  {
			  messenger.send(msg);
		  } catch (android.os.RemoteException e1)
		  {
			  Log.w(getClass().getName(), "Exception sending message", e1);
		  }
	  }
  }
} 