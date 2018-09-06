package com.ru.tgra.asgmt2.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.ru.tgra.asgmt2.Pong;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();

		config.title = "Pong"; // or whatever you like
		config.width = 1024;  //experiment with
		config.height = 768;  //the window size

		new LwjglApplication(new Pong(), config);
		//new LwjglApplication(new HLUTI2(), config);
		//new LwjglApplication(new HLUTI3(), config);
	}
}
