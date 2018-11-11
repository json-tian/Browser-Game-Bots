import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;

public abstract class Main implements ActionListener {

	static String login_url = "";
	static String pyramids_url = "";

	public static String password = "";
	public static String username = "";

	private static JPasswordField textUsername = new JPasswordField();
	private static JPasswordField textPassword = new JPasswordField();
	private static JLabel labelUsername = new JLabel("Enter Username: ");
	private static JLabel labelPassword = new JLabel("Enter Password: ");
	private static JButton start = new JButton("Play Pyramids!");
	private static JFrame frame = new JFrame("Pyramids Bot");
	
	public static Scanner inputFile = null;
	public static Formatter outputFile = null;

	public static void main(String[] args) {
		frame.setSize(800, 500);
		frame.setResizable(false);
		frame.setLayout(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		Font font = new Font("Arial", Font.PLAIN, 40);
		Font fontSmall = new Font("Arial", Font.PLAIN, 20);

		textUsername.setBounds(250, 100, 400, 50);
		textUsername.setLayout(null);
		textUsername.setFont(font);
		textUsername.setEchoChar((char) 0);

		labelUsername.setBounds(45, 100, 200, 50);
		labelUsername.setLayout(null);
		labelUsername.setFont(fontSmall);

		textPassword.setBounds(250, 200, 400 ,50);
		textPassword.setLayout(null);
		textPassword.setFont(font);

		labelPassword.setBounds(45, 200, 200, 50);
		labelPassword.setLayout(null);
		labelPassword.setFont(fontSmall);

		start.setBounds(250, 300, 300, 50);
		start.setLayout(null);
		start.setFont(fontSmall);
		start.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frame.dispose();
				password = String.copyValueOf(textPassword.getPassword());
				username = String.copyValueOf(textUsername.getPassword());
				try {
					runBrowser(username, password);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		});

		frame.add(textUsername);
		frame.add(textPassword);
		frame.add(labelUsername);
		frame.add(labelPassword);
		frame.add(start);

		frame.setVisible(true);
	}

	public static void runBrowser(String username, String password) throws InterruptedException {

		//System.setProperty("webdriver.chrome.driver", "C:\\SeleniumDrivers\\chromedriver.exe");
		//URL driverURL = Main.class.getResource("/drivers/chromedriver.exe");
		//System.setProperty("webdriver.chrome.driver", "/drivers/chromedriver.exe");
		//ChromeOptions options = new ChromeOptions();
		//URL adblockURL = Main.class.getResource("/extensions/Adblock-Plus_v3.4.crx");
		//options.addExtensions(new File("Adblock-Plus_v3.4.crx"));
		ChromeDriver driver = new ChromeDriver();

		driver.get(login_url);

		driver.findElement(By.xpath("//div[@class='welcomeLoginUsername']//input[@name='username']")).sendKeys(username);
		driver.findElement(By.xpath("//div[@class='welcomeLoginPassword']//input[@name='password']")).sendKeys(password);
		driver.findElement(By.xpath("//div[@class='welcomeLogin']//input[@type='submit']")).click();

		boolean go = true;
		boolean gameOver = false;
		boolean gotPoint = false;

		
		while (go) {
			driver.get(pyramids_url);
			try {
				driver.findElement(By.xpath("//td[@class='content']//input[@type='submit']")).click();
			} catch(Exception e){
			}

			gameOver = false;
			gotPoint = false;
			while (!gameOver) {
				//Turn
				gotPoint = false;
				String[] numbers = getInfo(driver);
				List<WebElement> cards = driver.findElement(By.xpath("//table[@width='400']")).findElements(By.cssSelector("td a"));

				try {
					if (driver.findElement(By.xpath("//div[@class='frame']")).findElement(By.xpath("//font[@color='orangered']")).findElements(By.cssSelector("a")).size() > 0) {
						driver.findElement(By.xpath("//div[@class='frame']")).findElement(By.xpath("//font[@color='orangered']")).findElement(By.cssSelector("a")).click();
						gameOver = true;
						break;
					}
				} catch(Exception e) {
				}

				String currentCard = driver.findElement(By.xpath("//table[@cellpadding='3']")).findElement(By.xpath("//td[@valign='top']")).findElements(By.cssSelector("img")).get(1).getAttribute("src");
				System.out.println("currentCard: " + currentCard);
				int current = Integer.parseInt(currentCard.substring(39, currentCard.indexOf("_")));

				ArrayList<Integer> src = new ArrayList<>();
				List<WebElement> names = driver.findElement(By.xpath("//table[@width='400']")).findElements(By.cssSelector("td a img"));
				for (int i = 0; i < names.size(); i ++) {
					src.add(Integer.parseInt(names.get(i).getAttribute("src").substring(39, names.get(i).getAttribute("src").indexOf("_"))));
					System.out.println("cards: " + src.get(i));
				}

				int highestElement = -1;
				if (current == 14) {
					if (src.contains(13)) {
						gotPoint = true;
						highestElement = src.lastIndexOf(13);
					}
					if (src.contains(2)) {
						gotPoint = true;
						if (src.lastIndexOf(2) > highestElement)
							highestElement = src.lastIndexOf(2);
					}
				} else if (current == 2) {
					if (src.contains(14)) {
						gotPoint = true;
						highestElement = src.lastIndexOf(14);
					}
					if (src.contains(3)) {
						gotPoint = true;
						if (src.lastIndexOf(3) > highestElement)
							highestElement = src.lastIndexOf(3);
					}
				} else {
					if (src.contains(current + 1)) {
						gotPoint = true;
						highestElement = src.lastIndexOf(current + 1);
					}
					if (src.contains(current - 1)) {
						gotPoint = true;
						if (src.lastIndexOf(current - 1) > highestElement)
							highestElement = src.lastIndexOf(current - 1);
					}
				}

				if (!gotPoint)
					driver.findElement(By.xpath("//table[@cellpadding='3']")).findElement(By.xpath("//td[@valign='top']")).findElement(By.cssSelector("a")).click();
				else
					driver.findElement(By.xpath("//table[@width='400']")).findElements(By.cssSelector("td a")).get(highestElement).click();

			}
		}

	}

	public static String[] getInfo(ChromeDriver driver) {
		List<WebElement> info = driver.findElement(By.xpath("//tr[@bgcolor='white']")).findElements(By.cssSelector("td"));
		String[] numbers = new String[4];
		for (int i = 0; i < info.size(); i ++) {
			numbers[i] = info.get(i).getAttribute("innerHTML");
		}

		return numbers;

	}

}
