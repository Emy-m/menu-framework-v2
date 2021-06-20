package martin.framework;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;

import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.Button;
import com.googlecode.lanterna.gui2.DefaultWindowManager;
import com.googlecode.lanterna.gui2.Direction;
import com.googlecode.lanterna.gui2.EmptySpace;
import com.googlecode.lanterna.gui2.LinearLayout;
import com.googlecode.lanterna.gui2.MultiWindowTextGUI;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.dialogs.MessageDialog;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.terminal.swing.TerminalEmulatorAutoCloseTrigger;

public class Menu {

	private ArrayList<Accion> clasesDelMenu = new ArrayList<Accion>();

	public Menu(String rutaArchivoDePropiedades) {
		try (InputStream implFile = getClass().getResourceAsStream(rutaArchivoDePropiedades)) {
			Properties properties = new Properties();
			properties.load(implFile);
			String clases[] = properties.getProperty("acciones").split(";");

			for (int i = 0; i < clases.length; i++) {
				Class clase = Class.forName(clases[i].trim());
				Accion claseAccion = (Accion) clase.getDeclaredConstructor().newInstance();
				this.clasesDelMenu.add(claseAccion);
			}

			crearMenu();
		} catch (IOException e) {
			throw new RuntimeException("Error cargando el archivo de propiedades.", e);
		} catch (Exception e) {
			throw new RuntimeException("Error en el nombre de una clase en el archivo de propiedades.", e);
		}
	}

	private void crearMenu() {
		// Setup terminal and screen layers
		DefaultTerminalFactory defaultTerminal;
		Terminal terminal;
		try {
			defaultTerminal = new DefaultTerminalFactory();
			defaultTerminal
					.setTerminalEmulatorFrameAutoCloseTrigger(TerminalEmulatorAutoCloseTrigger.CloseOnExitPrivateMode);
			terminal = defaultTerminal.createTerminal();

			Screen screen = new TerminalScreen(terminal);
			screen.startScreen();

			MultiWindowTextGUI gui = new MultiWindowTextGUI(screen, new DefaultWindowManager(),
					new EmptySpace(TextColor.ANSI.BLUE));

			// Create panel to hold components
			Panel panel = new Panel();
			panel.setLayoutManager(new LinearLayout(Direction.VERTICAL));

			for (Accion accion : clasesDelMenu) {
				Button botonAccion = new Button(accion.nombreItemMenu(), new Runnable() {
					@Override
					public void run() {
						accion.ejecutar();
						MessageDialog.showMessageDialog(gui, "Ejecutando Accion", accion.descripcionItemMenu());
					}
				});
				panel.addComponent(botonAccion);
			}

			Button botonSalir = new Button("Salir", new Runnable() {
				@Override
				public void run() {
					try {
						terminal.exitPrivateMode();
					} catch (IOException e) {
						MessageDialog.showMessageDialog(gui, "Error", e.getMessage());
					}
				}
			});
			panel.addComponent(botonSalir);

			// Create window to hold the panel
			BasicWindow window = new BasicWindow("Menu con Lanterna");
			window.setComponent(panel);

			// Start gui
			gui.addWindowAndWait(window);
		} catch (IOException e) {
			throw new RuntimeException("Error al crear la gui", e);
		}
	}
}
