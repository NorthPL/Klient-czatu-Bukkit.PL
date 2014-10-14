package me.northpl93;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import me.northpl93.cmd.CommandManager;
import me.northpl93.cmd.commands.*;
import me.northpl93.gui.ChatPanel;
import me.northpl93.gui.ChatWindow;
import me.northpl93.gui.PanelsEnum;

public class Main
{
	public static Thread chatListener            = null;
	public static Thread usersListener           = null;
	public static Thread wathDogThread           = null;
	public static JFrame window                  = null;
	public static CommandManager cmdMngr         = null;
	public static CookieManager cookieHandler    = null;
	public static Configuration config           = null;
	
	public static File configFile                = null;
	
	public static String loggedUserName          = "";
	
	public static boolean debugOnChat            = false;
	public static boolean rollOnNewPost          = true;
	public static boolean saveSession            = false;
	
	public static final String VERSION           = "1.4.0 INDEV";
	
	
	public static void main(String[] args)
	{
		System.out.println("start "+VERSION);
		switchPanel(PanelsEnum.LOADING_PANEL.getInstance()); //Ustawienie panelu na wczytywanie
        cookieHandler = new CookieManager();
        cookieHandler.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(cookieHandler);
		
		chatListener = new ChatListenThread();
		usersListener = new UsersListenThread();
		cmdMngr = new CommandManager();
		registerCommands();
		
		initializeConfig();
		
		if(config.getStoredCookies().size() >= 2) //Ciasteczka s� zwykle 3 wi�c powinno by� ok
		{
			debug("Znaleziono klucz sesji. Ustawianie panelu na SESSION_RESTORE_PANEL");
			switchPanel(PanelsEnum.SESSION_RESTORE_PANEL.getInstance()); //Ustawienie panelu na przywracanie sesji
		}
		else
		{
			debug("Nie znaleziono klucza sesji. Ustawianie panelu na logowanie");
			switchPanel(PanelsEnum.WELCOME_PANEL.getInstance()); //Ustawienie panelu na logowanie
		}
	}
	
	/**
	 * Wy�wietla wiadomo�� na konsoli (zawsze) i w oknie czatu (tylko gdy w��czone debugowanie na czat)
	 * 
	 * @param message Wiadomo�� do wy�wietlenia
	 */
	public static void debug(String message)
	{
		System.out.println(message);
		
		if(debugOnChat)
		{
			((ChatPanel)PanelsEnum.CHAT_PANEL.getInstance()).addMessage("[DEBUG] "+message);
		}
	}
	
	private static void registerCommands()
	{
		cmdMngr.registerCommand(new Help());
		cmdMngr.registerCommand(new ToggleVisibility());
	}
	
	/**
	 * Zmienia aktualnie widoczny panel
	 * 
	 * @param newPane Nowy panel do ustawienia. Najlepiej pobrany z PanelsEnum
	 * @see   PanelsEnum
	 */
	public static void switchPanel(final JPanel newPane)
	{
		if(window == null)
		{
			window = new ChatWindow();
		}
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				window.setContentPane(newPane);
				window.revalidate();
			}
		});
	}
	
	public static void initializeConfig()
	{
		String workingDirectory;
		String OS = (System.getProperty("os.name")).toUpperCase();
		if (OS.contains("WIN"))
		{
		    workingDirectory = System.getenv("AppData");
		}
		else
		{
		    workingDirectory = System.getProperty("user.home");
		    workingDirectory += "/Library/Application Support";
		}
		workingDirectory += "/bukkitpl-chat-northpl";
		
		debug("workingDirectory=="+workingDirectory);
		
		File wk = new File(workingDirectory);
		if(!wk.exists())
		{
			debug("working directory nie istnieje. Tworz�...");
			wk.mkdirs();
		}
		
		configFile = new File(workingDirectory+="/config.dat");
		debug("configFile=="+configFile);
		
		if(!configFile.isDirectory() && !configFile.exists())
		{
			debug("Plik configu nie istnieje. Tworz� nowy...");
			try
			{
				configFile.createNewFile();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		
		try
		{
			FileInputStream fin = new FileInputStream(configFile);
			ObjectInputStream ois = new ObjectInputStream(fin);
			config = (Configuration) ois.readObject();
			ois.close();
		}
		catch (Exception e)
		{
			debug("B��d podczas wczytywania pliku konfiguracyjnego (pierwsze odpalenie programu?)");
			debug("Za�adowany zostanie domy�lny config..");
			
			config = new Configuration();
			config.setDefaults();
		}
	}
	
	public static void saveConfig()
	{
		debug("Zapisywanie configu...");
		try
		{
			FileOutputStream fout = new FileOutputStream(configFile);
			ObjectOutputStream oos = new ObjectOutputStream(fout);
			oos.writeObject(config);
			oos.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
