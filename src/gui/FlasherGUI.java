package gui;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.factories.FormFactory;
import java.io.File; 
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;  
import java.io.IOException;
import java.util.*;
import java.net.URI;
import foxtrot.Job;
import foxtrot.Worker;
import javax.swing.JButton;
import org.adb.APKUtility;
import org.adb.AdbUtility;
import org.logger.MyLogger;
import org.plugins.PluginActionListener;
import org.plugins.PluginActionListenerAbout;
import org.plugins.PluginInterface;
import org.system.AdbPhoneThread;
import org.system.ClassPath;
import org.system.CommentedPropertiesFile;
import org.system.Device;
import org.system.DeviceChangedListener;
import org.system.DeviceEntry;
import org.system.DeviceProperties;
import org.system.Devices;
import org.system.FileDrop;
import org.system.GlobalConfig;
import org.system.OS;
import org.system.OsRun;
import org.system.PropertiesFile;
import org.system.RunStack;
import org.system.Shell;
import org.system.StatusEvent;
import org.system.StatusListener;
import org.system.TextFile;
import java.util.Iterator;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JScrollPane;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.ButtonGroup;
import javax.swing.JTextPane;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import org.lang.Language;
import flashsystem.Bundle;
import flashsystem.BundleException;
import flashsystem.BytesUtil;
import flashsystem.Command;
import flashsystem.FlasherConsole;
import flashsystem.HexDump;
import flashsystem.SeusSinTool;
import flashsystem.TaEntry;
import flashsystem.TaFile;
import flashsystem.TaParseException;
import flashsystem.X10flash;
import gui.EncDecGUI.MyFile;
import javax.swing.JProgressBar;
import java.awt.SystemColor;
import java.lang.reflect.Constructor;
import javax.swing.JToolBar;
import javax.swing.ImageIcon;
import java.awt.Toolkit;

import joptsimple.OptionParser;
import joptsimple.OptionSet;


public class FlasherGUI extends JFrame {

	/**
	 * 
	 */
	public static FlasherGUI _root;
	public static boolean guimode=true;
	private static String fsep = OS.getFileSeparator();
	private static final long serialVersionUID = 1L;
	private static JToolBar toolBar;
	private static JTextPane textArea = new JTextPane();
	private static AdbPhoneThread phoneWatchdog;
	private JPanel contentPane;
	private Bundle bundle;
	private ButtonGroup buttonGroupLog = new ButtonGroup();
	private ButtonGroup buttonGroupLang = new ButtonGroup();
	private JButton flashBtn;
	private JButton btnRoot;
	private JButton btnAskRootPerms;
	private JButton btnCleanroot;
	private JButton custBtn;
	private JButton btnXrecovery;
	private JButton btnKernel;
	private JMenuItem mntmInstallBusybox;
	private JMenuItem mntmDumpProperties;
	private JMenuItem mntmClearCache;
	private JMenuItem mntmBuildpropEditor;
	private JMenuItem mntmBuildpropRebrand;
	private JMenuItem mntmSetDefaultRecovery;
	private JMenuItem mntmRebootDefaultRecovery;
	private JMenuItem mntmRebootIntoRecoveryT;
	private JMenuItem mntmSetDefaultKernel;
	private JMenuItem mntmRebootCustomKernel;
	private JMenuItem mntmRebootDefaultKernel;
	private JMenuItem mntmRootPsneuter;
	private JMenuItem mntmRootzergRush;
	private JMenuItem mntmBackupSystemApps;
	private JMenu mnPlugins;
	private String lang;
	private String ftfpath="";
	private String ftfname="";
	//private StatusListener phoneStatus;

	private static void setSystemLookAndFeel() {
		try {
			UIManager.setLookAndFeel(new com.jgoodies.looks.plastic.Plastic3DLookAndFeel());
		}
		catch (Exception e) {}
	}

	private static void initLogger() throws FileNotFoundException {
		MyLogger.appendTextArea(textArea);
		MyLogger.setLevel(GlobalConfig.getProperty("loglevel").toUpperCase());
	}

	static public void runAdb() throws Exception {
		if (!OS.getName().equals("windows")) {
			OsRun giveRights = new OsRun(new String[] {"chmod", "755", OS.getAdbPath()});
			giveRights.run();
			giveRights = new OsRun(new String[] {"chmod", "755", OS.getFastBootPath()});
			giveRights.run();
		}
		killAdbandFastboot();
	}

	public static void addToolbar(JButton button) {
		toolBar.add(button);
	}

	public static void main(String[] args) throws Exception {
		
		OptionParser parser = new OptionParser();
		OptionSet options;
        parser.accepts( "console" );
        try {
        	options = parser.parse(args);
        }
        catch (Exception e) {
        	parser.accepts("action").withRequiredArg().required();
        	parser.accepts("file").withOptionalArg().defaultsTo("");
        	parser.accepts("method").withOptionalArg().defaultsTo("auto");
        	parser.accepts("wipedata").withOptionalArg().defaultsTo("yes");
        	parser.accepts("wipecache").withOptionalArg().defaultsTo("yes");
        	parser.accepts("baseband").withOptionalArg().defaultsTo("yes");
        	parser.accepts("system").withOptionalArg().defaultsTo("yes");
        	parser.accepts("kernel").withOptionalArg().defaultsTo("yes");
            options = parser.parse(args);        	
        }
        Language.Init(GlobalConfig.getProperty("language").toLowerCase());
        if (options.has("console")) {
        	String action=(String)options.valueOf("action");
        	
        	if (action.toLowerCase().equals("flash")) {
        		FlasherConsole.init(false);
        		FlasherConsole.doFlash((String)options.valueOf("file"), options.valueOf("wipedata").equals("yes"), options.valueOf("wipecache").equals("yes"), options.valueOf("baseband").equals("no"), options.valueOf("kernel").equals("no"), options.valueOf("system").equals("no"));
        	}

        	if (action.toLowerCase().equals("imei")) {
        		FlasherConsole.init(false);
        		FlasherConsole.doGetIMEI();
        	}

        	if (action.toLowerCase().equals("root")) {
        		FlasherConsole.init(true);
        		FlasherConsole.doRoot();
        	}
        	
        	if (action.toLowerCase().equals("blunlock")) {
        		FlasherConsole.init(true);
        		FlasherConsole.doBLUnlock();
        		
        	}
        	
        	FlasherConsole.exit();
        }
        else {
			initLogger();
			setSystemLookAndFeel();
			runAdb();
			MyLogger.getLogger().info("Flashtool "+About.getVersion());
			MyLogger.getLogger().info("You can drag and drop ftf files here to start flashing them");
			String userdir = System.getProperty("user.dir");
			String pathsep = System.getProperty("path.separator");
			System.setProperty("java.library.path", OS.getWinDir()+pathsep+OS.getSystem32Dir()+pathsep+userdir+fsep+"x10flasher_lib");
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					try {
						FlasherGUI frame = new FlasherGUI();
						frame.setVisible(true);
					} catch (Exception e) {}
				}
			});
        }
	}

	public FlasherGUI() {		
		setIconImage(Toolkit.getDefaultToolkit().getImage(FlasherGUI.class.getResource("/gui/ressources/icons/flash_32.png")));
		_root=this;
		setName("FlasherGUI");
		setTitle("SonyEricsson X10 Flasher by Bin4ry & Androxyde");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 845, 480);

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				exitProgram();
			}
		});

		new FileDrop( null, textArea, 
        		new FileDrop.Listener() {
        			public void filesDropped(final java.io.File[] files ) {
        				if (files.length==1) {
        					if (files[0].getAbsolutePath().toUpperCase().endsWith("FTF")) {
        						try {
        							EventQueue.invokeLater(new Runnable() {
        								public void run() {
        									try {
        										doFlashmode(files[0].getParentFile().getAbsolutePath(),files[0].getName());
        									}
        									catch (Exception e) {}
        								}
        							});
        						}
        						catch (Exception e) {}
        					}
        					else
        						MyLogger.getLogger().error("You can only drop ftf files");
        				}
        				else
        					MyLogger.getLogger().error("You dropped more than one file");
            }   // end filesDropped
        }); // end FileDrop.Listener

		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		JMenu mnFile = new JMenu("File");
		mnFile.setName("mnFile");
		menuBar.add(mnFile);

		JMenuItem mntmExit = new JMenuItem("Exit");
		mntmExit.setName("mntmExit");
		mnFile.add(mntmExit);

		JMenu mnAdvanced = new JMenu("Advanced");
		mnAdvanced.setName("mnAdvanced");
		menuBar.add(mnAdvanced);

		JMenu mnLang = new JMenu("Language");
		mnLang.setName("mnLang");
		menuBar.add(mnLang);

		Enumeration<String> listlang = Language.getLanguages();
		while (listlang.hasMoreElements()) {
			lang = listlang.nextElement();
			PropertiesFile plang = Language.getProperties(lang);
			JRadioButtonMenuItem menu = new JRadioButtonMenuItem(plang.getProperty("rdbtnmntm"+lang));
			menu.setName("rdbtnmntm"+lang);
			menu.setText(Language.getMenuItem("rdbtnmntm"+lang));
			buttonGroupLang.add(menu);
			mnLang.add(menu);
			menu.setSelected(GlobalConfig.getProperty("language").equals(lang));
			menu.addActionListener(new LangActionListener(lang,buttonGroupLang,_root));
		}

		JMenuItem mntmEncryptDecrypt = new JMenuItem("Decrypt Files");
		mntmEncryptDecrypt.setName("mntmEncryptDecrypt");
		mntmEncryptDecrypt.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doEncDec();
			}
		});

		mntmInstallBusybox = new JMenuItem("Install BusyBox");
		mntmInstallBusybox.setName("mntmInstallBusybox");
		mntmInstallBusybox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doInstallBusyBox();
			}
		});

		if (GlobalConfig.getProperty("devfeatures").equals("yes")) {
			mntmDumpProperties = new JMenuItem("TA Editor");
			mntmDumpProperties.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try {
						doDumpProperties();
					}
					catch (Exception e1) {}
				}
			});
		}
		
		JMenu mnRoot = new JMenu("Root");
		mnAdvanced.add(mnRoot);

		mntmRootPsneuter = new JMenuItem("Force psneuter");
		mntmRootPsneuter.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				doRootpsneuter();
			}
		});

		mntmRootzergRush = new JMenuItem("Force zergRush");
		mntmRootzergRush.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				doRootzergRush();
			}
		});

		mnRoot.add(mntmRootPsneuter);
		mnRoot.add(mntmRootzergRush);
		
		JMenu mnClean = new JMenu("Clean");
		mnClean.setName("mnClean");
		mnAdvanced.add(mnClean);

		mntmClearCache = new JMenuItem("Clear cache");
		mnClean.add(mntmClearCache);
		mntmClearCache.setName("mntmClearCache");

		mntmClearCache.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				doClearCache();
			}
		});
		mntmClearCache.setEnabled(false);

		//mntmCleanUninstalled = new JMenuItem("Clean Uninstalled");
		//mnClean.add(mntmCleanUninstalled);
		//mntmCleanUninstalled.setName("mntmCleanUninstalled");
		//mntmCleanUninstalled.setEnabled(false);

		/*mntmCleanUninstalled.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				doCleanUninstall();
			}
		});*/

		JMenu mnXrecovery = new JMenu("Recovery");
		mnAdvanced.add(mnXrecovery);
		
		
		mntmSetDefaultRecovery = new JMenuItem("Set default recovery");
		mntmSetDefaultRecovery.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				doSetDefaultRecovery();
			}
		});
		mnXrecovery.add(mntmSetDefaultRecovery);
		

		/*mntmRecoveryControler = new JMenuItem("Recovery Controler");
		mntmRecoveryControler.setName("mntmRecoveryControler");
		mntmRecoveryControler.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				RecovControl control = new RecovControl();
				control.setVisible(true);
			}
		});
		mnXrecovery.add(mntmRecoveryControler);
		mntmRecoveryControler.setEnabled(false);*/
		

		JMenu mnKernel = new JMenu("Kernel");
		mnAdvanced.add(mnKernel);

		//mntmInstallBootkit = new JMenuItem("Install bootkit");
		//mntmInstallBootkit.addActionListener(new ActionListener() {
		//	public void actionPerformed(ActionEvent arg0) {
		//		doInstallBootKit();
		//	}
		//});
		//mntmInstallBootkit.setEnabled(false);
		//mnKernel.add(mntmInstallBootkit);
		
		mntmBackupSystemApps = new JMenuItem("Backup System Apps");
		mntmBackupSystemApps.setName("mntmBackupSystemApps");
		mnAdvanced.add(mntmBackupSystemApps);
		mntmBackupSystemApps.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				doBackupSystem();
			}
		});
		mntmBackupSystemApps.setEnabled(false);


		/*JMenuItem mntmInstallOnline = new JMenuItem("Download latest version");
		mntmInstallOnline.setName("mntmInstallOnline");
		mntmInstallOnline.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				doDownloadXRecovery();
			}
		});
		mnXrecovery.add(mntmInstallOnline);*/
		mnAdvanced.add(mntmInstallBusybox);
		if (GlobalConfig.getProperty("devfeatures").equals("yes"))
			mnAdvanced.add(mntmDumpProperties);

		mntmBuildpropEditor = new JMenuItem("Build.prop Editor");
		mntmBuildpropEditor.setName("mntmBuildpropEditor");
		mntmBuildpropEditor.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				BuildPropGUI propsEdit = new BuildPropGUI();
				propsEdit.setVisible(true);
			}
		});
		
		if (GlobalConfig.getProperty("devfeatures").equals("yes")) {
			JMenuItem mntmTaBackupRestore = new JMenuItem("TA Backup & Restore");
			mnAdvanced.add(mntmTaBackupRestore);
			mntmTaBackupRestore.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try {
						BackupRestore();
					}
					catch (Exception e1) {}
				}
			});
		}
		
		mnAdvanced.add(mntmBuildpropEditor);

		mntmBuildpropRebrand = new JMenuItem("Rebrand");
		//mntmBuildpropRebrand.setName("mntmBuildpropEditor");
		mntmBuildpropRebrand.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doRebrand();
			}
		});
		mnAdvanced.add(mntmBuildpropRebrand);
		
		mnAdvanced.add(mntmEncryptDecrypt);

		/*JMenuItem mntmFilemanager = new JMenuItem("FileManager");
		mntmFilemanager.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					FileManager manager = new FileManager();
					manager.setVisible(true);
				}
				catch (Exception emanager) {}
			}
		});
		mnAdvanced.add(mntmFilemanager);*/

		JMenuItem mntmBundleCreation = new JMenuItem("Bundle Creation");
		mntmBundleCreation.setName("mntmBundleCreation");
		mntmBundleCreation.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				doBundle();
			}
		});
		mnAdvanced.add(mntmBundleCreation);

		JMenu mnReboot = new JMenu("Reboot");
		mnAdvanced.add(mnReboot);
		
		JMenu mnRRecovery = new JMenu("Recovery");
		JMenu mnRKernel = new JMenu("Kernel");
		mnReboot.add(mnRRecovery);
		mnReboot.add(mnRKernel);

		mntmRebootDefaultRecovery = new JMenuItem("Reboot default version");
		mntmRebootDefaultRecovery.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				doRebootRecovery();
			}
		});
		mnRRecovery.add(mntmRebootDefaultRecovery);

		mntmRebootIntoRecoveryT = new JMenuItem("Reboot specific version");
		mntmRebootIntoRecoveryT.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				doRebootRecoveryT();
			}
		});
		mnRRecovery.add(mntmRebootIntoRecoveryT);

		mntmRebootIntoRecoveryT.setEnabled(false);
		mntmRebootDefaultRecovery.setEnabled(false);
		mntmSetDefaultRecovery.setEnabled(false);

		mntmSetDefaultKernel = new JMenuItem("Set default kernel");
		mntmSetDefaultKernel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doSetDefaultKernel();
			}
		});
		mnKernel.add(mntmSetDefaultKernel);
		mntmSetDefaultKernel.setEnabled(false);

		mntmRebootDefaultKernel = new JMenuItem("Reboot default version");
		mntmRebootDefaultKernel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doReboot();
			}
		});
		mnRKernel.add(mntmRebootDefaultKernel);

		mntmRebootDefaultKernel.setEnabled(false);
		mntmRebootCustomKernel = new JMenuItem("Reboot specific version");
		mntmRebootCustomKernel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doRebootKexec();
			}
		});
		mnRKernel.add(mntmRebootCustomKernel);
		mntmRebootCustomKernel.setEnabled(false);

		JMenu mnHelp = new JMenu("Help");
		mnHelp.setName("mnHelp");
		mnPlugins = new JMenu("Plugins");
		menuBar.add(mnPlugins);
		menuBar.add(mnHelp);

		JMenu mnLoglevel = new JMenu("Loglevel");
		mnLoglevel.setName("mnLoglevel");
		mnHelp.add(mnLoglevel);

		/*JMenuItem mntmTestFlashMode = new JMenuItem("Test Flash Mode");
		mntmTestFlashMode.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				doTestFlash();
			}
		});
		mnHelp.add(mntmTestFlashMode);*/

		JMenuItem mntmCheckDrivers = new JMenuItem("Check Drivers");
		mntmCheckDrivers.setName("mntmCheckDrivers");
		mntmCheckDrivers.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Device.CheckAdbDrivers();
			}
		});
		mnHelp.add(mntmCheckDrivers);

		JMenuItem mntmAbout = new JMenuItem("About");
		mntmAbout.setName("mntmAbout");
		mnHelp.add(mntmAbout);	

		JRadioButtonMenuItem rdbtnmntmError = new JRadioButtonMenuItem("errors");
		rdbtnmntmError.setName("mntmError");
		buttonGroupLog.add(rdbtnmntmError);
		mnLoglevel.add(rdbtnmntmError);
		rdbtnmntmError.setSelected(GlobalConfig.getProperty("loglevel").toUpperCase().equals("ERROR"));

		JRadioButtonMenuItem rdbtnmntmWarnings = new JRadioButtonMenuItem("warnings");
		rdbtnmntmWarnings.setName("mntmWarnings");
		buttonGroupLog.add(rdbtnmntmWarnings);
		mnLoglevel.add(rdbtnmntmWarnings);
		rdbtnmntmWarnings.setSelected(GlobalConfig.getProperty("loglevel").toUpperCase().equals("WARN"));

		JRadioButtonMenuItem rdbtnmntmInfos = new JRadioButtonMenuItem("infos");
		rdbtnmntmInfos.setName("mntmInfos");
		buttonGroupLog.add(rdbtnmntmInfos);
		mnLoglevel.add(rdbtnmntmInfos);
		rdbtnmntmInfos.setSelected(GlobalConfig.getProperty("loglevel").toUpperCase().equals("INFO"));

		JRadioButtonMenuItem rdbtnmntmDebug = new JRadioButtonMenuItem("debug");
		rdbtnmntmDebug.setName("mntmDebug");
		buttonGroupLog.add(rdbtnmntmDebug);
		mnLoglevel.add(rdbtnmntmDebug);
		rdbtnmntmDebug.setSelected(GlobalConfig.getProperty("loglevel").toUpperCase().equals("DEBUG"));

		rdbtnmntmError.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				MyLogger.setLevel("ERROR");
			}
		});
		
		rdbtnmntmWarnings.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				MyLogger.setLevel("WARN");
			}
		});
		
		rdbtnmntmInfos.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				MyLogger.setLevel("INFO");
			}
		});

		rdbtnmntmDebug.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				MyLogger.setLevel("DEBUG");
			}
		});

		mntmExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				RunStack.killAll();
				exitProgram();
			}
		});

		mntmAbout.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				About about = new About();
				about.setVisible(true);
			}
		});

		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("max(50dlu;default):grow"),
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("max(78dlu;default)"),
				FormFactory.RELATED_GAP_COLSPEC,},
			new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("default:grow"),
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,}));						
		
		toolBar = new JToolBar();
		toolBar.setFloatable(false);
		contentPane.add(toolBar, "2, 2, left, fill");

		flashBtn = new JButton("");
		flashBtn.setToolTipText("Flash");
		flashBtn.setIcon(new ImageIcon(FlasherGUI.class.getResource("/gui/ressources/icons/flash_32.png")));
		toolBar.add(flashBtn);
		
				btnRoot = new JButton("");
				btnRoot.setToolTipText("Root");
				btnRoot.setIcon(new ImageIcon(FlasherGUI.class.getResource("/gui/ressources/icons/root_32.png")));
				toolBar.add(btnRoot);
				btnRoot.setEnabled(false);
				
						btnAskRootPerms = new JButton("");
						btnAskRootPerms.setIcon(new ImageIcon(FlasherGUI.class.getResource("/gui/ressources/icons/askroot_32.png")));
						btnAskRootPerms.setToolTipText("Ask Root Perms");
						toolBar.add(btnAskRootPerms);
						btnAskRootPerms.setBackground(SystemColor.control);
						btnAskRootPerms.setEnabled(false);
						
								btnCleanroot = new JButton("");
								btnCleanroot.setToolTipText("Clean (Root Needed)");
								btnCleanroot.setIcon(new ImageIcon(FlasherGUI.class.getResource("/gui/ressources/icons/clean_32.png")));
								toolBar.add(btnCleanroot);
								
										btnCleanroot.addActionListener(new ActionListener() {
											public void actionPerformed(ActionEvent arg0) {
												doCleanRoot();
											}
										});
										btnCleanroot.setEnabled(false);
										custBtn = new JButton("");
										custBtn.setIcon(new ImageIcon(FlasherGUI.class.getResource("/gui/ressources/icons/customize_32.png")));
										custBtn.setToolTipText("APK Installer");
										toolBar.add(custBtn);
										custBtn.setEnabled(false);
														
														btnXrecovery = new JButton("");
														btnXrecovery.setToolTipText("Recovery Installer");
														btnXrecovery.setIcon(new ImageIcon(FlasherGUI.class.getResource("/gui/ressources/icons/recovery_32.png")));
														toolBar.add(btnXrecovery);
														btnXrecovery.addActionListener(new ActionListener() {
															public void actionPerformed(ActionEvent e) {
																doInstallXRecovery();
															}
														});
														btnXrecovery.setEnabled(false);
														
														btnKernel = new JButton("");
														btnKernel.setToolTipText("Kernel Installer");
														btnKernel.setIcon(new ImageIcon(FlasherGUI.class.getResource("/gui/ressources/icons/kernel_32.png")));
														toolBar.add(btnKernel);
														btnKernel.addActionListener(new ActionListener() {
															public void actionPerformed(ActionEvent e) {
																doInstallKernel();
															}
														});
														btnKernel.setEnabled(false);
										
												custBtn.addActionListener(new ActionListener() {
													public void actionPerformed(ActionEvent arg0) {
														doCustomize();
													}
												});
						btnAskRootPerms.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								doAskRoot();
							}
						});
				
						btnRoot.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent arg0) {
								doRoot();
							}
						});
		flashBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					doFlash();
				}
				catch (Exception eflash) {}
			}
		});
		
		JToolBar toolBar_1 = new JToolBar();
		toolBar_1.setFloatable(false);
		contentPane.add(toolBar_1, "6, 2, right, center");
		
		JButton btnDonate = new JButton("");
		toolBar_1.add(btnDonate);
		btnDonate.setIcon(new ImageIcon(FlasherGUI.class.getResource("/gui/ressources/icons/paypal.png")));
		btnDonate.setToolTipText("Donate");
		//btnDonate.setName("btnDonate");
		btnDonate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				doConnectPaypal();
			}
		});
		
		JScrollPane scrollPane = new JScrollPane();
		contentPane.add(scrollPane, "2, 8, 5, 1, fill, fill");
		
		scrollPane.setViewportView(textArea);
		
		JButton btnSaveLog = new JButton("Save log");
		btnSaveLog.setName("btnSaveLog");
		btnSaveLog.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MyLogger.writeFile();
			}
		});
		contentPane.add(btnSaveLog, "6, 10, right, center");
		UIManager.put("ProgressBar.background", Color.WHITE); //colour of the background
	    UIManager.put("ProgressBar.foreground", Color.LIGHT_GRAY);  //colour of progress bar
	    UIManager.put("ProgressBar.selectionBackground",Color.BLACK);  //colour of percentage counter on black background
	    UIManager.put("ProgressBar.selectionForeground",Color.BLACK);  //colour of precentage counter on red background
		JProgressBar progressBar = new JProgressBar();
		MyLogger.registerProgressBar(progressBar);
		contentPane.add(progressBar, "2, 12, 5, 1");
		setLanguage();
		mntmInstallBusybox.setEnabled(false);
		mntmBuildpropEditor.setEnabled(false);
		mntmBuildpropRebrand.setEnabled(false);
	}

	public void setVisible(boolean visible) {
		super.setVisible(visible);
		StatusListener phoneStatus = new StatusListener() {
			public void statusChanged(StatusEvent e) {
				if (!e.isDriverOk()) {
					MyLogger.getLogger().error("Drivers need to be installed for connected device.");
					MyLogger.getLogger().error("You can find them in the drivers folder of Flashtool.");
				}
				else {
					if (e.getNew().equals("adb")) {
						MyLogger.getLogger().info("Device connected with USB debugging on");
						MyLogger.getLogger().debug("Device connected, continuing with identification");
						doIdent();
					}
					if (e.getNew().equals("none")) {
						MyLogger.getLogger().info("Device disconnected");
						doDisableIdent();
					}
					if (e.getNew().equals("flash")) {
						MyLogger.getLogger().info("Device connected in flash mode");
						doDisableIdent();
					}
					if (e.getNew().equals("fastboot")) {
						MyLogger.getLogger().info("Device connected in fastboot mode");
						doDisableIdent();
					}
					if (e.getNew().equals("normal")) {
						MyLogger.getLogger().info("Device connected with USB debugging off");
						MyLogger.getLogger().info("For 2011 devices line, be sure you are not in MTP mode");
						doDisableIdent();
					}
				}
			}
		};
		phoneWatchdog = new AdbPhoneThread();
		phoneWatchdog.start();
		phoneWatchdog.addStatusListener(phoneStatus);
	}

	public void setLanguage() {
		Language.translate(this);
	}
		
	public void exitProgram() {
		try {
			MyLogger.getLogger().info("Stopping watchdogs and exiting ...");
			if (GlobalConfig.getProperty("killadbonexit").equals("yes")) {
				killAdbandFastboot();
			}
			System.exit(0);
		}
		catch (Exception e) {}		
	}

	public static void killAdbLinux() {
		try {
			OsRun cmd = new OsRun(new String[] {"/usr/bin/killall", "adb"});
			cmd.run();				
		}
		catch (Exception e) {
		}
	}
	
	public static void killAdbWindows() {
		try {
			OsRun adb = new OsRun(new String[] {"taskkill", "/F", "/T", "/IM", "adb*"});
			adb.run();
		}
		catch (Exception e) {
		}
	}	

	public static void stopPhoneWatchdog() {
		DeviceChangedListener.stop();
		if (phoneWatchdog!=null) {
			phoneWatchdog.done();
			try {
				phoneWatchdog.join();
			}
			catch (Exception e) {
			}
		}
	}
	
	public static void killAdbandFastboot() {
			stopPhoneWatchdog();
	}

	public void doCleanUninstall() {
		Worker.post(new Job() {
			public Object run() {
				try {
						PropertiesFile safeList = new PropertiesFile("org/adb/config/safelist.properties","."+fsep+"custom"+fsep+"clean"+fsep+"safelist.properties");
						HashSet<String> set = AdbUtility.listSysApps();
						Iterator<Object> keys = safeList.keySet().iterator();
						while (keys.hasNext()) {
							String key = (String)keys.next();
							if (safeList.getProperty(key).equals("safe") && !set.contains(key)) {
								MyLogger.getLogger().debug(key);
								if (TextFile.exists("."+fsep+"custom"+fsep+"apps_saved"+fsep+key)) {
									String packageName = APKUtility.getPackageName("."+fsep+"custom"+fsep+"apps_saved"+fsep+key);
									MyLogger.getLogger().debug(packageName);
									AdbUtility.uninstall(packageName,false);
								}
							}
						}
						MyLogger.getLogger().info("Clean Finished");
				} catch (Exception e) {}
				return null;
			}
		});
	}


	public void doDumpProperties() throws Exception {
		
		//firmSelect sel = new firmSelect(config);
		//bundle = sel.getBundle();
		
		bundle = new Bundle();
		if (bundle!=null) {
			Worker.post(new Job() {
				public Object run() {
						X10flash flash=null;
						try {
					    	bundle.setSimulate(GlobalConfig.getProperty("simulate").toLowerCase().equals("yes"));
							flash = new X10flash(bundle);
							MyLogger.getLogger().info("Please connect your device into flashmode.");
							if ((new WaitDeviceFlashmodeGUI(flash)).deviceFound(_root)) {
								flash.init();
								Vector<TaEntry> v=flash.dumpProperties();
								if (v.size()>0) {
									TaEditor edit = new TaEditor(flash,v);
									edit.setVisible(true);
									DeviceChangedListener.pause(false);
									flash.closeDevice();
								}
							}
						}
						catch (Exception e) {
							MyLogger.getLogger().error(e.getMessage());
						}
						bundle.close();
						return null;
					}
				});
			}
	}

	public void BackupRestore() throws Exception {
		TaModeSelectGUI tamode = new TaModeSelectGUI();
		String select = tamode.selectMode();
		if (select.equals("backup"))
			doBackupTa();
		if (select.equals("restore"))
			doRestoreTa();
	}	
	
	public void doBackupTa() throws Exception {
		bundle = new Bundle();
		if (bundle!=null) {
			Worker.post(new Job() {
				public Object run() {
						X10flash flash=null;
						try {
					    	bundle.setSimulate(GlobalConfig.getProperty("simulate").toLowerCase().equals("yes"));
							flash = new X10flash(bundle);
							MyLogger.getLogger().info("Please connect your device into flashmode.");
							if ((new WaitDeviceFlashmodeGUI(flash)).deviceFound(_root)) {
								flash.init();
								flash.BackupTA();
							}
						}
						catch (Exception e) {
							MyLogger.getLogger().error(e.getMessage());
						}
						bundle.close();
						return null;
					}
				});
			}
	}
	
	public void doRestoreTa() throws Exception {
		bundle = new Bundle();
		if (bundle!=null) {
			Worker.post(new Job() {
				public Object run() {
						X10flash flash=null;
						try {
					    	bundle.setSimulate(GlobalConfig.getProperty("simulate").toLowerCase().equals("yes"));
							flash = new X10flash(bundle);
							MyLogger.getLogger().info("Please connect your device into flashmode.");
							if ((new WaitDeviceFlashmodeGUI(flash)).deviceFound(_root)) {
								flash.init();
								TaSelectGUI tasel = new TaSelectGUI(".ta",flash.getPhoneProperty("MSN"));
								String result = tasel.getTa();
								if (result.length()>0) {
									String tafile = OS.getWorkDir()+"/custom/ta/"+result;
									flash.RestoreTA(tafile);
								}
								else {
									MyLogger.getLogger().info("Action canceled");
								}
								flash.closeDevice();
							}
						}
						catch (Exception e) {
							MyLogger.getLogger().error(e.getMessage());
						}
						bundle.close();
						return null;
					}
				});
			}
	}
	
	public void doFlash() throws Exception {
		
		BootModeSelectGUI bootmode = new BootModeSelectGUI();
		String select = bootmode.selectMode();
		if (select.equals("flashmode"))
			doFlashmode("","");
		if (select.equals("fastboot"))
			doFastBoot();
	}
	
	public void doFastBoot() throws Exception {
		FastBootToolboxGUI box = new FastBootToolboxGUI();
		box.setVisible(true);
	}
	
	public void doFlashmode(String pftfpath, String pftfname) throws Exception {
		ftfpath=pftfpath;
		ftfname=pftfname;
		Worker.post(new Job() {
			public Object run() {
				firmSelect sel = new firmSelect(ftfpath,ftfname);
				try {
					bundle = sel.getBundle();
				}
				catch (IOException ioe) {
					bundle=null;
				}
				if (bundle!=null) {
					if (!bundle.hasLoader())
						bundle.setLoader(new File(Devices.getCurrent().getLoader()));
					X10flash flash=null;
					try {
			    		MyLogger.getLogger().info("Preparing files for flashing");
			    		bundle.open();
				    	bundle.setSimulate(GlobalConfig.getProperty("simulate").toLowerCase().equals("yes"));
						flash = new X10flash(bundle);
						MyLogger.getLogger().info("Please connect your device into flashmode.");
						if ((new WaitDeviceFlashmodeGUI(flash)).deviceFound(_root)) {
							flash.flashDevice();
						}
						else MyLogger.getLogger().info("Flash canceled");
					}
					catch (BundleException ioe) {
						MyLogger.getLogger().error("Error preparing files");
					}
					catch (Exception e) {
						MyLogger.getLogger().error(e.getMessage());
					}
					bundle.close();
				}
				else MyLogger.getLogger().info("Flash canceled");
				return null;
			}
		});
	}

	public void doRoot() {
		if (Devices.getCurrent().getVersion().contains("2.3")) {
			doRootzergRush();
		}
		else 
			doRootpsneuter();
	}
	
	public void doRootzergRush() {
		Worker.post(new Job() {
			public Object run() {
				try {
					AdbUtility.push(Devices.getCurrent().getBusybox(false), GlobalConfig.getProperty("deviceworkdir")+"/busybox");
					Shell shell = new Shell("busyhelper");
					shell.run(true);
					AdbUtility.push(new File("."+fsep+"custom"+fsep+"root"+fsep+"zergrush.tar.uue").getAbsolutePath(),GlobalConfig.getProperty("deviceworkdir"));
					shell = new Shell("rootit");
					MyLogger.getLogger().info("Running part1 of Root Exploit, please wait");
					shell.run(true);
					Devices.waitForReboot(true);
					MyLogger.getLogger().info("Running part2 of Root Exploit");
					shell = new Shell("rootit2");
					shell.run(false);
					MyLogger.getLogger().info("Finished!.");
					MyLogger.getLogger().info("Root should be available after reboot!");		
				}
				catch (Exception e) {
					MyLogger.getLogger().error(e.getMessage());}
				return null;
			}
		});
	}

	public void doRootpsneuter() {
		Worker.post(new Job() {
			public Object run() {
				try {
					AdbUtility.push(Devices.getCurrent().getBusybox(false), GlobalConfig.getProperty("deviceworkdir")+"/busybox");
					Shell shell = new Shell("busyhelper");
					shell.run(true);
					AdbUtility.push("."+fsep+"custom"+fsep+"root"+fsep+"psneuter.tar.uue",GlobalConfig.getProperty("deviceworkdir"));
					shell = new Shell("rootit");
					MyLogger.getLogger().info("Running part1 of Root Exploit, please wait");
					shell.run(false);
					Devices.waitForReboot(true);
					MyLogger.getLogger().info("Running part2 of Root Exploit");
					shell = new Shell("rootit2");
					shell.run(false);
					MyLogger.getLogger().info("Finished!.");
					MyLogger.getLogger().info("Root should be available after reboot!");		
				}
				catch (Exception e) {
					MyLogger.getLogger().error(e.getMessage());}
				return null;
			}
		});
	}

	public void doCustomize() {
		Worker.post(new Job() {
			public Object run() {
				try {
						ApkInstallGUI instgui = new ApkInstallGUI("."+fsep+"custom"+fsep+"apps");
						String folder = instgui.getFolder();
						if (folder.length()>0) {
							File files = new File(folder);
							File[] chld = files.listFiles();
							for(int i = 0; i < chld.length; i++){
								if (chld[i].getName().endsWith(".apk"))
									org.adb.AdbUtility.install(chld[i].getPath());
							}
							MyLogger.getLogger().info("APK Installation finished");
						}
						else MyLogger.getLogger().info("APK Installation canceled");
					}
				catch (Exception e) {}
				return null;
			}
		});
	}

	public void doRebrand() {
		Worker.post(new Job() {
			public Object run() {
			try {
						Devices.getCurrent().doBusyboxHelper();
						if (AdbUtility.Sysremountrw()) {
							AdbUtility.pull("/system/build.prop", Devices.getCurrent().getWorkDir()+fsep+"build.prop");
							CommentedPropertiesFile build = new CommentedPropertiesFile();
							build.load(new File(Devices.getCurrent().getWorkDir()+fsep+"build.prop"));
							String current = build.getProperty("ro.semc.version.cust");
							if (current!=null) {
								rebrandGUI gui = new rebrandGUI(current);
								String newid = gui.getId();
								if (newid.length()>0) {							
									build.store(new FileOutputStream(new File(Devices.getCurrent().getWorkDir()+fsep+"buildnew.prop")), "");
									TextFile tf = new TextFile(Devices.getCurrent().getWorkDir()+fsep+"buildnew.prop","ISO-8859-1");
									tf.setProperty(current,newid);
									AdbUtility.push(Devices.getCurrent().getWorkDir()+fsep+"buildnew.prop",GlobalConfig.getProperty("deviceworkdir")+"/build.prop");
									Shell shell = new Shell("rebrand");
									shell.runRoot();
									MyLogger.getLogger().info("Rebrand finished. Rebooting phone ...");
								}
							}
							else {MyLogger.getLogger().error("You are not on a stock ROM");}
						}
						else MyLogger.getLogger().error("Error mounting /system rw");
				}
				catch (Exception e) {
					MyLogger.getLogger().error(e.getMessage());
				}
				return null;
			}
		});
	}
	
	public void doCleanRoot() {
		Worker.post(new Job() {
			public Object run() {
				try {
					Devices.getCurrent().doBusyboxHelper();
					if (AdbUtility.Sysremountrw()) {
						apkClean sel = new apkClean();
						sel.setVisible(true);
						boolean somethingdone = false;
						if (TextFile.exists(OS.getWorkDir()+fsep+"custom"+fsep+"clean"+fsep+"listappsadd")) {
							TextFile t = new TextFile(OS.getWorkDir()+fsep+"custom"+fsep+"clean"+fsep+"listappsadd","ASCII");
							Iterator<String> i = t.getLines().iterator();
							while (i.hasNext()) {
								if (!TextFile.exists(OS.getWorkDir()+fsep+"custom"+fsep+"apps_saved"+fsep+i.next())) {
									t.close();
									throw new Exception("File "+OS.getWorkDir()+fsep+"custom"+fsep+"apps_saved"+fsep+i.next()+" does not exist");
								}
							}
							t.close();
						}
						if (TextFile.exists(OS.getWorkDir()+fsep+"custom"+fsep+"clean"+fsep+"listappsadd")) {
							AdbUtility.push(OS.getWorkDir()+fsep+"custom"+fsep+"clean"+fsep+"listappsadd", GlobalConfig.getProperty("deviceworkdir"));
							TextFile t = new TextFile(OS.getWorkDir()+fsep+"custom"+fsep+"clean"+fsep+"listappsadd","ASCII");
							Iterator<String> i = t.getLines().iterator();
							while (i.hasNext()) {
								AdbUtility.push(OS.getWorkDir()+fsep+"custom"+fsep+"apps_saved"+fsep+i.next(), GlobalConfig.getProperty("deviceworkdir"));
							}
							t.delete();
							Shell shell1 = new Shell("sysadd");
							shell1.runRoot();
							somethingdone = true;
						}
						if (TextFile.exists(OS.getWorkDir()+fsep+"custom"+fsep+"clean"+fsep+"listappsremove")) {
							AdbUtility.push(OS.getWorkDir()+fsep+"custom"+fsep+"clean"+fsep+"listappsremove", GlobalConfig.getProperty("deviceworkdir"));
							TextFile t = new TextFile(OS.getWorkDir()+fsep+"custom"+fsep+"clean"+fsep+"listappsremove","ASCII");
							Iterator<String> i = t.getLines().iterator();
							while (i.hasNext()) {
								AdbUtility.pull("/system/app/"+i.next(),OS.getWorkDir()+fsep+"custom"+fsep+"apps_saved");
							}
							Shell shell2 = new Shell("sysremove");
							shell2.runRoot();
							t.delete();
							somethingdone = true;
						}
						if (somethingdone) {
							AdbUtility.clearcache();
							MyLogger.getLogger().info("Clean finished. Rebooting phone ...");
						}
						else MyLogger.getLogger().info("Clean canceled");
					}
					else 
						MyLogger.getLogger().info("Error mounting /system rw");
				} catch (Exception e) {
					MyLogger.getLogger().error(e.getMessage());
				}
				return null;
			}
		});
	}
	
	public void doRebootRecoveryT() {
		Worker.post(new Job() {
			public Object run() {
				try {
					Devices.getCurrent().rebootSelectedRecovery();
				}
				catch (Exception e) {}
				return null;
			}
		});		
	}

	public void doSetDefaultRecovery() {
		Worker.post(new Job() {
			public Object run() {
				try {
					Devices.getCurrent().setDefaultRecovery();
				}
				catch (Exception e) {}
				return null;
			}
		});		
	}

	public void doSetDefaultKernel() {
		Worker.post(new Job() {
			public Object run() {
				try {
					KernelBootSelectGUI rsel = new KernelBootSelectGUI();
					String current = rsel.getVersion();
					if (current.length()>0) {
						if (AdbUtility.Sysremountrw()) {
						MyLogger.getLogger().info("Setting default kernel");
						Shell shell = new Shell("setdefaultkernel");
						shell.setProperty("KERNELTOBOOT", current);
						shell.runRoot();
						MyLogger.getLogger().info("Done");
						}
					}
				}
				catch (Exception e) {
					MyLogger.getLogger().error(e.getMessage());
				}
				return null;
			}
		});		
	}

	public void doRebootRecovery() {
		Worker.post(new Job() {
			public Object run() {
				try {
					MyLogger.getLogger().info("Rebooting into recovery mode");
					Shell shell = new Shell("rebootrecovery");
					shell.runRoot();
					MyLogger.getLogger().info("Phone will reboot into recovery mode");
				}
				catch (Exception e) {}
				return null;
			}
		});		
	}

	public void doRebootKexec() {
		Worker.post(new Job() {
			public Object run() {
				try {
					KernelBootSelectGUI ksel = new KernelBootSelectGUI();
					String current = ksel.getVersion();
					if (current.length()>0) {
						MyLogger.getLogger().info("Rebooting into kexec mode");
						Shell shell = new Shell("rebootkexect");
						shell.setProperty("KERNELTOBOOT", current);
						shell.runRoot();
						MyLogger.getLogger().info("Phone will reboot into kexec mode");
					}
					else {
						MyLogger.getLogger().info("Reboot canceled");
					}
				}
				catch (Exception e) {}
				return null;
			}
		});		
	}

	public void doReboot() {
		Worker.post(new Job() {
			public Object run() {
				try {
						MyLogger.getLogger().info("Rebooting into stock mode");
						Shell shell = new Shell("reboot");
						shell.runRoot();
						MyLogger.getLogger().info("Phone will reboot now");
				}
				catch (Exception e) {}
				return null;
			}
		});		
	}

	public void doInstallXRecovery() {
		Worker.post(new Job() {
			public Object run() {
				try {
						MyLogger.getLogger().info("Installing Recovery to device...");
						Devices.getCurrent().doBusyboxHelper();
						if (AdbUtility.Sysremountrw()) {
							RecoverySelectGUI sel = new RecoverySelectGUI(Devices.getCurrent().getId());
							String selVersion = sel.getVersion();
							if (selVersion.length()>0) {
								doInstallCustKit();
								AdbUtility.push("./devices/"+Devices.getCurrent().getId()+"/recovery/"+selVersion+"/recovery.tar",GlobalConfig.getProperty("deviceworkdir")+"/recovery.tar");
								Shell shell = new Shell("installrecovery");
								shell.runRoot();
								MyLogger.getLogger().info("Recovery successfully installed");
							}
							else {
								MyLogger.getLogger().info("Canceled");
							}
						}
						else MyLogger.getLogger().error("Error mounting /system rw");
					}
				catch (Exception e) {
					MyLogger.getLogger().error(e.getMessage());
				}
				return null;
				
			}
		});
	}	

    public void doEncDec() {
		Worker.post(new Job() {
			public Object run() {
	        	EncDecGUI encdec = new EncDecGUI();
	        	encdec.setVisible(true);
	        	Object[] list = encdec.getList();
	        	if (list!=null) {
	        		String folder=null;
    				for (int i=0;i<list.length;i++) {
    					MyLogger.getLogger().info("Decrypting "+list[i]);
    					folder = ((MyFile)list[i]).getParent();
    	        		SeusSinTool.decrypt(((MyFile)list[i]).getAbsolutePath());
    				}

    				MyLogger.getLogger().info("Decryption finished");
    				try {
					BundleGUI bcre = new BundleGUI(folder);
					Bundle b = bcre.getBundle("flashmode");
					if (b!=null) {
    					MyLogger.getLogger().info("Starting bundle creation");
    					b.createFTF();
    					MyLogger.getLogger().info("Finished bundle creation");
					}
    				}
    				catch (Exception e) {}
    				
	        	}
	 			return null;
			}
		});
   }

    public void doInstallBusyBox() {
		Worker.post(new Job() {
			public Object run() {
	        	try {
	        		String busybox = Devices.getCurrent().getBusybox(true);
	        		if (busybox.length()>0) {
		        		AdbUtility.push(busybox, GlobalConfig.getProperty("deviceworkdir"));
		        		Shell shell = new Shell("busyhelper");
		        		shell.run(false);
		        		shell = new Shell("instbusybox");
						shell.setProperty("BUSYBOXINSTALLPATH", Devices.getCurrent().getBusyBoxInstallPath());
						shell.runRoot();
				        MyLogger.getLogger().info("Installed version of busybox : " + AdbUtility.getBusyboxVersion(Devices.getCurrent().getBusyBoxInstallPath()));
				        MyLogger.getLogger().info("Finished");
	        		}
	        		else {
	        			MyLogger.getLogger().info("Busybox installation canceled");
	        		}
		        }
	        	catch (Exception e) {
	        		MyLogger.getLogger().error(e.getMessage());
	        	}
	 			return null;
			}
		});
    }

    public void doClearCache() {
		Worker.post(new Job() {
			public Object run() {
	        	try {
						AdbUtility.clearcache();
						MyLogger.getLogger().info("Finished");
				}
				catch (Exception e) {}
	 			return null;
			}
		});
	}

    public void doConnectPaypal() {
    	showInBrowser("https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=PPWH7M9MNCEPA");
    }

    private boolean showInBrowser(String url){
		try {
			  Desktop.getDesktop().browse(new URI(url));
		} 
		catch (Exception e) {
		} 
        return true;
        // some mod here
	}

    public void doDisableIdent() {
    	if (guimode) {
			btnCleanroot.setEnabled(false);
			mntmInstallBusybox.setEnabled(false);
			mntmClearCache.setEnabled(false);
			mntmRootzergRush.setEnabled(false);
			mntmRootPsneuter.setEnabled(false);
			mntmBuildpropEditor.setEnabled(false);
			mntmBuildpropRebrand.setEnabled(false);
			mntmRebootIntoRecoveryT.setEnabled(false);
			mntmRebootDefaultRecovery.setEnabled(false);
			mntmSetDefaultRecovery.setEnabled(false);
			mntmSetDefaultKernel.setEnabled(false);
			mntmRebootCustomKernel.setEnabled(false);
			mntmRebootDefaultKernel.setEnabled(false);
			//mntmInstallBootkit.setEnabled(false);
			btnRoot.setEnabled(false);
			btnXrecovery.setEnabled(false);
			btnKernel.setEnabled(false);
			btnAskRootPerms.setEnabled(false);
			custBtn.setEnabled(false);
			//mntmCleanUninstalled.setEnabled(false);
	    	mntmBackupSystemApps.setEnabled(false);
    	}
	}
 
    public void doIdent() {
		        	if (guimode) {
		        		Enumeration<Object> e = Devices.listDevices(true);
		        		if (!e.hasMoreElements()) {
		        			MyLogger.getLogger().error("No device is registered in Flashtool.");
		        			MyLogger.getLogger().error("You can only flash devices.");
		        			return;
		        		}
		        		boolean found = false;
		        		Properties founditems = new Properties();
		        		founditems.clear();
		        		Properties buildprop = new Properties();
		        		buildprop.clear();
		        		while (e.hasMoreElements()) {
		        			DeviceEntry current = Devices.getDevice((String)e.nextElement());
		        			String prop = current.getBuildProp();
		        			if (!buildprop.containsKey(prop)) {
		        				String readprop = DeviceProperties.getProperty(prop);
		        				buildprop.setProperty(prop,readprop);
		        			}
		        			Iterator<String> i = current.getRecognitionList().iterator();
		        			String localdev = buildprop.getProperty(prop);
		        			while (i.hasNext()) {
		        				String pattern = i.next().toUpperCase();
		        				if (localdev.toUpperCase().contains(pattern)) {
		        					founditems.put(current.getId(), current.getName());
		        				}
		        			}
		        		}
		        		if (founditems.size()==1) {
		        			found = true;
		        			Devices.setCurrent((String)founditems.keys().nextElement());
		        			if (!Devices.isWaitingForReboot())
		        				MyLogger.getLogger().info("Connected device : " + Devices.getCurrent().getId());
		        		}
		        		else {
		        			MyLogger.getLogger().error("Cannot identify your device.");
			        		MyLogger.getLogger().info("Selecting from user input");
			        		String devid="";
			        		deviceSelectGui devsel = new deviceSelectGui(null);
			        		devid = devsel.getDevice(founditems);
			    			if (devid.length()>0) {
			        			found = true;
			        			Devices.setCurrent(devid);
				        		String reply = AskBox.getReplyOf("Do you want to permanently identify this device as \n"+Devices.getCurrent().getName()+"?");
				        		if (reply.equals("yes")) {
				        			String prop = DeviceProperties.getProperty(Devices.getCurrent().getBuildProp());
				        			Devices.getCurrent().addRecognitionToList(prop);
				        		}
				        		if (!Devices.isWaitingForReboot())
				        			MyLogger.getLogger().info("Connected device : " + Devices.getCurrent().getId());
			        		}
			        		else {
			        			MyLogger.getLogger().error("You can only flash devices.");
			        		}
		        		}
		        		if (found) {
		        			if (!Devices.isWaitingForReboot()) {
		        				MyLogger.getLogger().info("Installed version of busybox : " + Devices.getCurrent().getInstalledBusyboxVersion());
		        				MyLogger.getLogger().info("Android version : "+Devices.getCurrent().getVersion()+" / kernel version : "+Devices.getCurrent().getKernelVersion());
		        			}
		        			if (Devices.getCurrent().isRecovery()) {
		        				MyLogger.getLogger().info("Phone in recovery mode");
		        				btnRoot.setEnabled(false);
		        				btnAskRootPerms.setEnabled(false);
		        				doGiveRoot();
		        			}
		        			else {
		        				boolean hasSU = Devices.getCurrent().hasSU();
		        				btnRoot.setEnabled(!hasSU);
		        				if (hasSU) {
		        					boolean hasRoot = Devices.getCurrent().hasRoot();
		        					if (hasRoot) {
		        						doInstFlashtool();
		        						doGiveRoot();
		        					}
		        					btnAskRootPerms.setEnabled(!hasRoot);
		        				}
		        			}
		        			MyLogger.getLogger().debug("Now setting buttons availability - btnRoot");
		        			MyLogger.getLogger().debug("mtmRootzergRush menu");
		        			mntmRootzergRush.setEnabled(true);
		        			MyLogger.getLogger().debug("mtmRootPsneuter menu");
		        			mntmRootPsneuter.setEnabled(true);
		        			boolean flash = Devices.getCurrent().canFlash();
		        			MyLogger.getLogger().debug("flashBtn button "+flash);
		        			flashBtn.setEnabled(flash);
		        			MyLogger.getLogger().debug("custBtn button");
		        			custBtn.setEnabled(true);
		        			MyLogger.getLogger().debug("Now adding plugins");
		        			mnPlugins.removeAll();
		        			addDevicesPlugins();
		        			addGenericPlugins();
		        			MyLogger.getLogger().debug("Stop waiting for device");
		        			if (Devices.isWaitingForReboot())
		        				Devices.stopWaitForReboot();
		        			MyLogger.getLogger().debug("End of identification");
		        		}
		        	}
    }

    public void doGiveRoot() {
		btnCleanroot.setEnabled(true);
		mntmInstallBusybox.setEnabled(true);
		mntmClearCache.setEnabled(true);
		mntmBuildpropEditor.setEnabled(true);
		if (new File(OS.getWorkDir()+fsep+"devices"+fsep+Devices.getCurrent().getId()+fsep+"rebrand").isDirectory())
			mntmBuildpropRebrand.setEnabled(true);
		mntmRebootIntoRecoveryT.setEnabled(Devices.getCurrent().canRecovery());
		mntmRebootDefaultRecovery.setEnabled(true);
		mntmSetDefaultRecovery.setEnabled(Devices.getCurrent().canRecovery());
		mntmSetDefaultKernel.setEnabled(Devices.getCurrent().canKernel());
		mntmRebootCustomKernel.setEnabled(Devices.getCurrent().canKernel());
		mntmRebootDefaultKernel.setEnabled(true);
		//mntmInstallBootkit.setEnabled(true);
		//mntmRecoveryControler.setEnabled(true);
		mntmBackupSystemApps.setEnabled(true);
		btnXrecovery.setEnabled(Devices.getCurrent().canRecovery());
		btnKernel.setEnabled(Devices.getCurrent().canKernel());
		if (!Devices.isWaitingForReboot())
			MyLogger.getLogger().info("Root Access Allowed");    	
    }
    
    public void doAskRoot() {
		Worker.post(new Job() {
			public Object run() {
				MyLogger.getLogger().warn("Please check your Phone and 'ALLOW' Superuseraccess!");
        		if (!AdbUtility.hasRootPerms()) {
        			MyLogger.getLogger().error("Please Accept root permissions on the phone");
        		}
        		else {
        			doGiveRoot();
        		}
        		return null;
			}
		});
	}

    public void doBundle() {
		Worker.post(new Job() {
			public Object run() {
				try {
					BundleGUI bcre = new BundleGUI();
					Bundle b = null;
					//BootModeSelectGUI bootmode = new BootModeSelectGUI();
					//String select = bootmode.selectMode();
					//if (select.equals("flashmode"))
						b = bcre.getBundle("flashmode");
					//if (select.equals("fastboot"))
						//b = bcre.getBundle("fastboot");
					if (b!=null) {
    					MyLogger.getLogger().info("Starting bundle creation");
    					b.createFTF();
    					MyLogger.getLogger().info("Finished bundle creation");
					}
				}
				catch (Exception e) {
					MyLogger.getLogger().error(e.getMessage());}
				return null;
			}
		});
    }


    public void doBackupSystem() {
		Worker.post(new Job() {
			public Object run() {
				try {
					X10Apps apps = new X10Apps();
					Iterator<String> ic = apps.getCurrent().iterator();
					while (ic.hasNext()) {
						String app = ic.next();
						try {
							AdbUtility.pull("/system/app/"+app, "."+fsep+"custom"+fsep+"apps_saved");
						}
						catch (Exception e) {}
					}
					MyLogger.getLogger().info("Backup Finished");
				}
				catch (Exception e) {
					MyLogger.getLogger().error(e.getMessage());}
				return null;
			}
		});
	}

    public void doInstallCustKit() {
		Worker.post(new Job() {
			public Object run() {
				try {
						MyLogger.getLogger().info("Installing chargemon feature / kernel bootkit to device...");
						Devices.getCurrent().doBusyboxHelper();
						if (AdbUtility.Sysremountrw()) {
							AdbUtility.push("."+fsep+"devices"+fsep+Devices.getCurrent().getId()+fsep+"bootkit"+fsep+"bootkit.tar",GlobalConfig.getProperty("deviceworkdir"));
							Shell shell = new Shell("installbootkit");
							shell.runRoot();
							MyLogger.getLogger().info("bootkit successfully installed");
						}
						else MyLogger.getLogger().error("Error mounting /system rw");
					}
				catch (Exception e) {
					MyLogger.getLogger().error(e.getMessage());}
				return null;
			}
		});
    }

    public void doInstallKernel() {
		Worker.post(new Job() {
			public Object run() {
				try {
						MyLogger.getLogger().info("Installing kernel to device...");
						Devices.getCurrent().doBusyboxHelper();
						if (AdbUtility.Sysremountrw()) {
							KernelSelectGUI sel = new KernelSelectGUI(Devices.getCurrent().getId());
							String selVersion = sel.getVersion();
							if (selVersion.length()>0) {
								doInstallCustKit();
								AdbUtility.push("."+fsep+"devices"+fsep+Devices.getCurrent().getId()+fsep+"kernel"+fsep+selVersion+fsep+"kernel.tar",GlobalConfig.getProperty("deviceworkdir"));
								Shell shell = new Shell("installkernel");
								shell.runRoot();
								MyLogger.getLogger().info("kernel successfully installed");
							}
							else {
								MyLogger.getLogger().info("Canceled");
							}
						}
						else MyLogger.getLogger().error("Error mounting /system rw");
					}
				catch (Exception e) {
					MyLogger.getLogger().error(e.getMessage());}
				return null;
			}
		});
    }
    
    public void addDevicesPlugins() {
    	try {
	    	File dir = new File(Devices.getCurrent().getDeviceDir()+fsep+"features");
		    File[] chld = dir.listFiles();
		    MyLogger.getLogger().debug("Found "+chld.length+" device plugins to add");
		    for(int i = 0; i < chld.length; i++){
		    	if (chld[i].isDirectory()) {
		    		try {
		    			Properties p = new Properties();
		    			p.load(new FileInputStream(new File(chld[i].getAbsolutePath()+fsep+"feature.properties")));
		    			MyLogger.getLogger().debug("Registering "+p.getProperty("classname"));
		    			ClassPath.addFile(chld[i].getAbsolutePath()+fsep+p.getProperty("plugin"));
		    			registerPlugin("device",p.getProperty("classname"),chld[i].getAbsolutePath());
		    		}
		    		catch (IOException ioe) {
		    		}
		    	}
		    }
    	}
    	catch (Exception e) {}
    }

    public void addGenericPlugins() {
    	try {
	    	File dir = new File(OS.getWorkDir()+fsep+"custom"+fsep+"features");
		    File[] chld = dir.listFiles();
		    MyLogger.getLogger().debug("Found "+chld.length+" generic plugins to add");
		    for(int i = 0; i < chld.length; i++){
		    	if (chld[i].isDirectory()) {
		    		try {
		    			Properties p = new Properties();
		    			p.load(new FileInputStream(new File(chld[i].getAbsolutePath()+fsep+"feature.properties")));
		    			ClassPath.addFile(chld[i].getAbsolutePath()+fsep+p.getProperty("plugin"));
		    			registerPlugin("generic",p.getProperty("classname"),chld[i].getAbsolutePath());
		    		}
		    		catch (IOException ioe) {
		    		}
		    	}
		    }
    	}
    	catch (Exception e) {
    		MyLogger.getLogger().debug(e.getMessage());
    	}
    }

    public void registerPlugin(String type, String classname, String workdir) {
	    try {
	    	MyLogger.getLogger().debug("Creating instance of "+classname);
	    	Class<?> pluginClass = Class.forName(classname);
	    	MyLogger.getLogger().debug("Getting constructor of "+classname);
            Constructor<?> constr = pluginClass.getConstructor();
            MyLogger.getLogger().debug("Now instanciating object of class "+classname);
            PluginInterface pluginObject = (PluginInterface)constr.newInstance();
            MyLogger.getLogger().debug("Setting plugin workdir");
            pluginObject.setWorkdir(workdir);
            MyLogger.getLogger().debug("Now giving rights to plugin");
            boolean aenabled = false;
            String aversion = Devices.getCurrent().getVersion();
            Enumeration <String> e1 = pluginObject.getCompatibleAndroidVersions();
            while (e1.hasMoreElements()) {
            	String pversion = e1.nextElement();
            	if (aversion.startsWith(pversion) || pversion.equals("any")) aenabled=true;
            }
            
            boolean kenabled = false;
            String kversion = Devices.getCurrent().getKernelVersion();
            Enumeration <String> e2 = pluginObject.getCompatibleKernelVersions();
            while (e2.hasMoreElements()) {
            	String pversion = e2.nextElement();
            	if (kversion.equals(pversion) || pversion.equals("any")) kenabled=true;
            }
            
            boolean denabled = false;
            if (type.equals("generic")) {
	            String currdevid = Devices.getCurrent().getId();
	            Enumeration <String> e3 = pluginObject.getCompatibleDevices();
	            while (e3.hasMoreElements()) {
	            	String pversion = e3.nextElement();
	            	if (currdevid.equals(pversion) || pversion.equals("any")) denabled=true;
	            }
            }
            else
            	denabled=true;

            boolean hasroot=false;
            if (pluginObject.isRootNeeded()) hasroot=Devices.getCurrent().hasRoot();
            else hasroot = true;
            JMenu pluginmenu = new JMenu(pluginObject.getName());

            JMenuItem run = new JMenuItem("Run");
            run.setEnabled(aenabled&&kenabled&&denabled&&hasroot);
            PluginActionListener p =  new PluginActionListener(pluginObject);
            run.addActionListener(p);

            JMenuItem about = new JMenuItem("About");
            PluginActionListenerAbout p1 = new PluginActionListenerAbout(pluginObject);
            about.addActionListener(p1);
            pluginmenu.add(run);
            pluginObject.setMenu(pluginmenu);
            pluginmenu.addSeparator();
            pluginmenu.add(about);

            if (type.equals("device")&&aenabled&&kenabled&&denabled&&hasroot) {
            	JMenu deviceMenu = new JMenu(Devices.getCurrent().getId());
            	deviceMenu.add(pluginmenu);
            	mnPlugins.add(deviceMenu);
            }
            else
            	if (aenabled&&kenabled&&denabled&&hasroot)
            		mnPlugins.add(pluginmenu);
	    }
	    catch (Exception e) {
	    	MyLogger.getLogger().error(e.getMessage());
	    }    	
    }

    public void doInstFlashtool() {
		try {
			if (!AdbUtility.exists("/system/flashtool")) {
				Devices.getCurrent().doBusyboxHelper();
				if (AdbUtility.Sysremountrw()) {
					MyLogger.getLogger().info("Installing toolbox to device...");
					AdbUtility.push(OS.getWorkDir()+fsep+"custom"+fsep+"root"+fsep+"ftkit.tar",GlobalConfig.getProperty("deviceworkdir"));
					Shell shell = new Shell("installftkit");
					shell.runRoot();
				}
				else MyLogger.getLogger().error("Error mounting /system rw");
			}
		}
		catch (Exception e) {
			MyLogger.getLogger().error(e.getMessage());
		}
    }

}