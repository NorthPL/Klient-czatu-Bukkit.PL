package me.northpl93;

import me.northpl93.gui.ChatPanel;
import me.northpl93.gui.PanelsEnum;
import me.northpl93.utils.JsonResponseParser;
import me.northpl93.utils.NotificationWindow;
import me.northpl93.utils.PostExecute;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.gson.Gson;

public class ChatListenThread extends Thread
{
	private String account = "";
	private int latestMessage = 0;
	private Gson gson = null;
	private JsonResponseParser obj2 = null;
	private Document document = null;
	private Elements as = null;
	private String rawData = "";
	private String lol = "";

	public ChatListenThread()
	{
	}

	@SuppressWarnings("deprecation")
	@Override
	public void run()
	{
		gson = new Gson();

		while (true)
		{
			rawData = "_xfToken=" + account + "&" + "lastrefresh="
					+ latestMessage;

			lol = PostExecute.excutePost(
					"http://bukkit.pl/index.php/taigachat/list.json", rawData);

			if (lol == null) // Błąd z pobieraniem zawartości
			{
				Main.debug("Nie można pobrać nowych wiadomości: Wystąpił problem z połączeniem. Wątek zostanie zatrzymany...\n");
				if (Main.getConfig().getNotificationType() == Configuration.NotificationType.WINDOW
						&& !Main.getMainWindow().isActive())
				{
					new NotificationWindow(
							Main.getMsgHeader(),
							"Wystąpił problem podczas pobierania wiadomości. Postaramy się to naprawić!",
							NotificationWindow.Icons.USER.getIco());
				}
				else if (Main.getConfig().getNotificationType() == Configuration.NotificationType.BALLON
						&& !Main.getMainWindow().isActive())
				{
					Main.getTray()
							.showMessage(
									"Wystąpił problem podczas pobierania wiadomości. Postaramy się to naprawić!");
				}
				this.stop();
				return;
			}

			obj2 = gson.fromJson(lol, JsonResponseParser.class);

			document = Jsoup.parse(obj2.getHtml());
			as = document.select("li");

			for (Element element : as)
			{
				/*
				 * System.out.println("debug2");
				 * if(getFirstStringObject(element.getElementsByClass("username")).equalsIgnoreCase(""))
				 * {
				 * System.out.println("debug3");
				 * break;
				 * }
				 * System.out.println("debug4");
				 */

				StringBuilder sb = new StringBuilder();
				boolean isBreak = false;

				/*
				 * sb.append(getFirstStringObject(element.getElementsByClass("username")));
				 * sb.append(": ");
				 */

				for (Element el : element.getElementsByClass("username"))
				{
					if (!Main.getLoggedUserName().equalsIgnoreCase(el.text())
							&& !Main.getMainWindow().isActive())
					{

					}

					if (Main.getConfig().getBlockedUsers().contains(el.text()))
					{
						isBreak = true;
						break;
					}

					if (latestMessage != 0
							&& !Main.getLoggedUserName().equalsIgnoreCase(
									el.text())
							&& Main.getConfig().getNotificationType() == Configuration.NotificationType.WINDOW
							&& Main.getConfig().isNotification_newPost()
							&& !Main.getMainWindow().isActive())
					{
						new NotificationWindow(Main.getMsgHeader(),
								"Nowa wiadomość od użytkownika " + el.text(),
								NotificationWindow.Icons.CHAT.getIco());
					}
					else if (latestMessage != 0
							&& Main.getConfig().getNotificationType() == Configuration.NotificationType.BALLON
							&& Main.getConfig().isNotification_newPost()
							&& !Main.getMainWindow().isActive())
					{
						Main.getTray().showMessage(
								"Nowa wiadomość od użytkownika " + el.text());
					}

					sb.append(el.text());
					sb.append(": ");
					break;
				}

				if (isBreak)
				{
					continue;
				}

				/*
				 * Elements chatMessage = element.getElementsByClass("taigachat_messagetext");
				 * sb.append(getFirstStringObject(chatMessage));
				 * if(getFirstElement(chatMessage) != null)
				 * {
				 * for(Element ell : getFirstElement(chatMessage).getElementsByTag("img"))
				 * {
				 * sb.append(" ");
				 * sb.append(ell.attr("alt"));
				 * }
				 * }
				 * sb.append("\n");
				 */

				for (Element el : element
						.getElementsByClass("taigachat_messagetext"))
				{
					sb.append(el.text());

					for (Element ell : el.getElementsByTag("img"))
					{
						sb.append(" ");
						sb.append(ell.attr("alt"));
					}

					sb.append("\n");
					break;
				}

				((ChatPanel) PanelsEnum.CHAT_PANEL.getInstance()).addMessage(sb
						.toString());
			}

			latestMessage = obj2.getLatestPostId();

			try
			{
				Thread.sleep(2000);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}

	public String getLoggedUser()
	{
		return account;
	}

	public void setLoggedUser(String xfToken)
	{
		account = xfToken;
	}

	public int getLatestMessage()
	{
		return latestMessage;
	}

	public void setLatestMessage(int newLatestMessage)
	{
		latestMessage = newLatestMessage;
	}

	@SuppressWarnings("unused")
	private String getFirstStringObject(Elements elements)
	{
		if (elements.size() == 0)
		{
			return "";
		}
		return elements.get(0).text();
	}

	@SuppressWarnings("unused")
	private Element getFirstElement(Elements elements)
	{
		if (elements.size() == 0)
		{
			return null;
		}
		return elements.get(0);
	}
}
