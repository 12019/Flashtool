package org.adb;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Properties;
import java.util.Scanner;
import java.util.Vector;

import org.logger.MyLogger;
import org.system.Devices;
import org.system.GlobalConfig;
import org.system.OS;
import org.system.OsRun;
import org.system.Shell;

public class AdbUtility  {

	static Properties build = new Properties();
	static boolean rootnative=false;
	static boolean rootperms=false;
	
	private static String fsep = OS.getFileSeparator();
	private static String shellpath = OS.getWorkDir()+fsep+"custom"+fsep+"shells";
	private static String adbpath = OS.getAdbPath();
	private static String shpath ="";

	public static String getShellPath() {
		return shellpath;
	}
	
	public static void setShellPath(String path) {
		shellpath = path;
	}
	
	public static String getShPath(boolean force) {
		if (shpath==null || force) {
			try {
				OsRun command = new OsRun(new String[] {adbpath,"shell", "ls /system/xbin/sh"});
				command.run();
				if (command.getStdOut().contains("No such")) {
					OsRun command1 = new OsRun(new String[] {adbpath,"shell", "'echo $0'"});
					command.run();
					shpath = command.getStdOut().trim();
				}
				else
					shpath = "/system/xbin/sh";
			}
			catch (Exception e) {
				shpath = "";
			}
		}
		MyLogger.getLogger().debug("Default shell for scripts : "+shpath);
		return shpath;
	}

	public static boolean hasRootNative() {
		try {
			if (rootnative) return true;
			OsRun command = new OsRun(new String[] {adbpath,"shell","id"});
			command.run();
			rootnative=command.getStdOut().contains("uid=0");
		}
		catch (Exception e) {
		}
		return rootnative;
	}
	
	public static void forward(String type,String local, String remote) throws Exception {
		OsRun command = new OsRun(new String[] {adbpath,"forward "+type.toUpperCase()+":"+local+" "+type.toUpperCase()+":"+remote});
		command.run();
	}
	
	public static HashSet<String> listSysApps() throws Exception {
		OsRun command = new OsRun(new String[] {adbpath,"shell", "ls /system/app/*"});
		command.run();
		String[] result = command.getStdOut().split("\n");
		HashSet<String> set = new HashSet<String>();
		for (int i=0;i<result.length;i++) {
			set.add(result[i].substring(result[i].lastIndexOf('/')+1));
		}
		return set;
	}
	
	public static HashSet<String> listKernels() throws Exception {
		OsRun command = new OsRun(new String[] {adbpath,"shell", "find /system/kernel -name 'kernel.desc' -type f"});
		command.run();
		String[] result = command.getStdOut().split("\n");
		HashSet<String> set = new HashSet<String>();
		for (int i=0;i<result.length;i++) {
			int first = result[i].indexOf('/', 1);
			first = result[i].indexOf('/', first+1);
			int last = result[i].indexOf('/', first+1);
			set.add(result[i].substring(first+1,last));
		}
		return set;
	}

	public static HashSet<String> listRecoveries() throws Exception {
		OsRun command = new OsRun(new String[] {adbpath,"shell", "find /system/recovery -name 'recovery.desc' -type f"});
		command.run();
		String[] result = command.getStdOut().split("\n");
		HashSet<String> set = new HashSet<String>();
		for (int i=0;i<result.length;i++) {
			int first = result[i].indexOf('/', 1);
			first = result[i].indexOf('/', first+1);
			int last = result[i].indexOf('/', first+1);
			set.add(result[i].substring(first+1,last));
		}
		return set;
	}

	/*public static boolean isSystemMounted() throws Exception {
		if (systemmounted==null) {
			systemmounted = isMounted("/system");
		}
		return systemmounted.booleanValue();
	}*/
	
	public static void init() {
		rootnative=false;
		rootperms=false;
		shpath=null;
	}
	
	public static boolean isMounted(String mountpoint) throws Exception {
		boolean result = false;
		OsRun command = new OsRun(new String[] {adbpath,"shell", "mount"});
		command.run();
		Scanner sc = new Scanner(command.getStdOut());
		while (sc.hasNextLine()) {
			String line = sc.nextLine();
			if (line.contains(mountpoint)) {
				result = true;
			}
		}
		return result;
	}
	
	public static boolean hasSU() {
		boolean result = true;
		try {
		OsRun command = new OsRun(new String[] {adbpath,"shell", "type su"});
		command.run();
		Scanner sc = new Scanner(command.getStdOut());
		while (sc.hasNextLine()) {
			String line = sc.nextLine();
			if (line.toLowerCase().contains("not found")) {
				result = false;
			}
		}
		}
		catch (Exception e) {
			return false;
		}
		return result;	
	}

	public static String getProperty(String key) {
		try {
			boolean systemmounted = isMounted("/system");
			String result = "";
			if (!systemmounted) mount("/system","ro","yaffs2");
			OsRun command = new OsRun(new String[] {adbpath,"shell", "getprop "+key});
			command.run();
			result = command.getStdOut().replaceAll("\n","");
			if (!systemmounted) umount("/system");
			return result;
		}
		catch (Exception e) {
			return "";
		}
	}
	
	public static String getFilePerms(String file) {
		try {
			OsRun command = new OsRun(new String[] {adbpath,"shell", "stat "+file});
			command.run();
			Scanner sc = new Scanner(command.getStdOut());
			while (sc.hasNextLine()) {
				String line = sc.nextLine();
				if (line.contains("Uid")) {
					return line;
				}
			}
			return "   ";
		}
		catch (Exception e) {
			return "   ";
		}
	}
	
	public static ByteArrayInputStream getBuildProp() throws Exception {
		OsRun command = new OsRun(new String[] {adbpath,"shell", "cat /system/build.prop"});
		command.run();
		return new ByteArrayInputStream(command.getStdOut().getBytes());
	}
	
	public static boolean hasRootPerms() {
		if (hasRootNative()) return true;
		if (rootperms) return true;
		try {
			Shell shell = new Shell("checkperms");
			String result=shell.runRoot(false);
			while (result.toLowerCase().contains("segmentation fault")) {
				Thread.sleep(10000);
				result=shell.runRoot(false);
			}
			rootperms=result.contains("uid=0");
			return rootperms;
		}
		catch (Exception e) {
			return false;
		}
	}

	public static HashSet<String> ls(String basedir,String type) throws Exception {
		OsRun command = new OsRun(new String[] {adbpath+"shell", "find "+basedir+" -maxdepth 1 -type "+type});
		command.run();
		String[] result = command.getStdOut().split("\n");
		HashSet<String> set = new HashSet<String>();
		for (int i=0;i<result.length;i++) {
			if (result[i].substring(result[i].lastIndexOf('/')+1).length()>0 && !result[i].substring(result[i].lastIndexOf('/')+1).equals("/"))
				set.add(result[i].substring(result[i].lastIndexOf('/')+1));
		}
		return set;		
	}
	
	public static void uninstall(String apk, boolean silent) throws Exception {
		if (!silent)
			MyLogger.getLogger().info("Uninstalling "+apk);
		OsRun command = new OsRun(new String[] {adbpath,"uninstall",apk});
		command.run();
	}

	public static void killServer() throws Exception {
		MyLogger.getLogger().info("Killing adb service");
		OsRun command = null;
		if (OS.getName().equals("windows"))
			command = new OsRun(new String[] {adbpath,"kill-server"});
		else
			command = new OsRun(new String[] {"killall","adb"});
		command.run();
	}

	public static void startServer() throws Exception {
			MyLogger.getLogger().info("Starting adb service");
			OsRun command = new OsRun(new String[] {adbpath,"start-server"});
			command.run();
	}

	public static void push(String source, String destination) throws Exception {
		push(source, destination, true);
	}
	
	public static void push(String source, String destination, boolean log) throws Exception {
		File f = new File(source);
		if (!f.exists()) throw new AdbException(source+" : Not found");
		if (log) MyLogger.getLogger().info("Pushing "+f.getAbsolutePath()+" to "+destination);
		else MyLogger.getLogger().debug("Pushing "+f.getAbsolutePath()+" to "+destination);
		OsRun command = new OsRun(new String[] {adbpath,"push",f.getAbsolutePath(),destination});
		command.run();
		if (command.getReturnCode()!=0) {
			throw new AdbException(command.getStdOut()+ " " + command.getStdErr());
		}
	}

	
	public static String getBusyboxVersion(String path) {
		try {
			OsRun command;
			if (isMounted("/system")) {
				command = new OsRun(new String[] {adbpath,"shell",path+"/busybox"});
			}
			else {
				command = new OsRun(new String[] {adbpath,"shell","/sbin/busybox"});
			}
			command.run();
			Scanner sc = new Scanner(command.getStdOut());
			if (sc.hasNextLine()) {	
				String line = sc.nextLine();
				if (line.contains("BusyBox v1") && line.contains("multi-call")) return line;
			}
			return "";
		}
		catch (Exception e) {
			return "";
		}
	}
	
	public static void mount(String mountpoint,String options, String type) throws Exception {
		if (hasRootNative()) {
			OsRun command = new OsRun(new String[] {adbpath,"shell","mount -o "+options+" -t "+type+" "+mountpoint});
			command.run();			
		}
	}
	
	public static void umount(String mountpoint) throws Exception {
		if (hasRootNative()) {
			OsRun command = new OsRun(new String[] {adbpath,"shell", "umount "+mountpoint});
			command.run();
		}		
	}
	
	public static void pull(String source, String destination) throws Exception {
		MyLogger.getLogger().info("Pulling "+source+" to "+destination);
		OsRun command = new OsRun(new String[] {adbpath,"pull",source, destination});
		command.run();
		if (command.getReturnCode()!=0) {
			throw new AdbException(command.getStdOut()+ " " + command.getStdErr());
		}
	}

	public static String getKernelVersion(boolean hasbusybox) {
		try {
			String result = "";
			if (!hasbusybox) {
				AdbUtility.push(Devices.getCurrent().getBusybox(false), GlobalConfig.getProperty("deviceworkdir")+"/busybox1",false);
				AdbUtility.run("chmod 755 "+GlobalConfig.getProperty("deviceworkdir")+"/busybox1");
				result = run(GlobalConfig.getProperty("deviceworkdir")+"/busybox1 uname -r");
				run("rm -r "+GlobalConfig.getProperty("deviceworkdir")+"/busybox1");
			}
			else result = run("busybox uname -r");
			return result;
		}
		catch (Exception e) {
			return "";
		}
	}
	
	public static String run(Shell shell, boolean debug) throws Exception {
		push(shell.getPath(),GlobalConfig.getProperty("deviceworkdir")+"/"+shell.getName(),false);
		if (debug)
			MyLogger.getLogger().debug("Running "+shell.getName());
		else
			MyLogger.getLogger().info("Running "+shell.getName());
		OsRun command = new OsRun(new String[] {adbpath,"shell", "sh "+GlobalConfig.getProperty("deviceworkdir")+"/"+shell.getName()+";exit $?"});
		command.run();
		if (command.getStdOut().contains("FTError")) throw new Exception(command.getStdErr()+" "+command.getStdOut());
		return command.getStdOut();
	}

	public static String run(String com, boolean debug) throws Exception {
		if (debug)
			MyLogger.getLogger().debug("Running "+ com + " command");
		else
			MyLogger.getLogger().info("Running "+ com + " command");
		OsRun command = new OsRun(new String[] {adbpath,"shell",com});
		command.run();
		return command.getStdOut().trim();
	}

	public static String run(String com) throws Exception {
		return run(com,true);
	}

	public static String runRoot(Shell shell) throws Exception {
		return runRoot(shell,true);
	}
	
	public static String runRoot(Shell shell,boolean log) throws Exception {
		Shell s=new Shell("sysrun");
		s.save();
		push(s.getPath(),GlobalConfig.getProperty("deviceworkdir")+"/sysrun",false);
		s.clean();
		push(shell.getPath(),GlobalConfig.getProperty("deviceworkdir")+"/runscript",false);
		if (log)
			MyLogger.getLogger().info("Running "+shell.getName()+"  as root thru sysrun");
		else
			MyLogger.getLogger().debug("Running "+shell.getName()+"  as root thru sysrun");
		OsRun command;
		if (rootnative)
			command=new OsRun(new String[] {adbpath,"shell", "sh "+GlobalConfig.getProperty("deviceworkdir")+"/sysrun"});
		else
			command=new OsRun(new String[] {adbpath,"shell", "su -c 'sh "+GlobalConfig.getProperty("deviceworkdir")+"/sysrun'"});
		command.run();
		return command.getStdOut();
	}

	public static boolean Sysremountrw() throws Exception {
		MyLogger.getLogger().info("Remounting system read-write");
		Shell shell = new Shell("remount");
		return !shell.runRoot(false).contains("FTError");
	}

	public static void clearcache() throws Exception {
		MyLogger.getLogger().info("Clearing dalvik cache");
		Shell shell = new Shell("clearcache");
		shell.runRoot(false);
	}

	public static void install(String apk) throws Exception {
		MyLogger.getLogger().info("Installing "+apk);
		OsRun command = new OsRun(new String[] {adbpath,"install", "\""+apk+"\""});
		command.run();
		if (command.getStdOut().contains("Failure")) {
			uninstall(APKUtility.getPackageName(apk),true);
			command = new OsRun(new String[] {adbpath,"install","\""+apk+"\""});
			command.run();
			if (command.getStdOut().contains("Failure")) {
				Scanner sc = new Scanner(command.getStdOut());
				sc.nextLine();
				MyLogger.getLogger().error(sc.nextLine());
			}
		}
	}

	public static void scanStatus() throws Exception {
		OsRun command = new OsRun(new String[] {adbpath,"status-window"});
		command.run();
	}

	public static boolean isConnected() {
		try {
			MyLogger.getLogger().debug("Testing if device is connected");
			return AdbUtility.getDevices().hasMoreElements();
		}
		catch (Exception e) {
			return false;
		}
	}
/*	public static boolean isConnected() {
		try {
			MyLogger.getLogger().info("Searching Adb Device");
			String res =Device.AdbId();
			if (res.equals("ErrNotPlugged")) {
				MyLogger.error("Please plug your device with USB Debugging and Unknown sources on");
				return false;
			}
			else if (res.equals("ErrDriverError")) {
				MyLogger.error("ADB drivers are not installed");
				return false;
			}
			boolean connected = false;
			OsRun command = new OsRun(adbpath+" devices");
			command.run();
			String[] result = command.getStdOut().split("\n");
			for (int i=1;i<result.length; i++) {
				connected=result[i].contains("device");
			}
			if (!connected) {
				MyLogger.error("Please plug your device with USB Debugging and Unknown sources turned on");
			}
			return connected;
		}
		catch (Exception e) {
			MyLogger.error("Please plug your device with USB Debugging and Unknown sources turned on");
			return false;
		}
	}*/

	public static Enumeration<String> getDevices() {
		Vector<String> v = new Vector<String>();
		try {
			OsRun command = new OsRun(new String[] {adbpath,"devices"});
			command.run();
			Scanner sc = new Scanner(command.getStdOut());
			while (sc.hasNextLine()) {
				String line = sc.nextLine();
				if (!line.startsWith("List")) {
					v.add(line.trim().substring(0,line.indexOf(9)));
				}
			}
		}
		catch (Exception e) {}
		return v.elements();
	}
}