package configuration;

import org.testng.Assert;

import java.text.SimpleDateFormat;
import java.util.Date;

public class AutoLogger {
	private String tag = "";

	private final static int ERROR = 0;
	private final static int DEBUG = 2;
	private final static int INFO = 1;

	public AutoLogger(Class<?> clazz) {
		tag = clazz.getName();
		if (tag.contains(".")) {
			tag = tag.substring(tag.lastIndexOf(".") + 1);
		}
	}

	public void e(Object msg, Throwable e) {
		String printmsg = "";
		if (msg != null) {
			printmsg += msg.toString() + " ";
		}

		if (e != null) {
			printmsg += "Error: " + e.getMessage() + "<br/>";
			printmsg += getStackTrace(e);
		}

		finalLog(printmsg, ERROR);
	}

	private String getStackTrace(Object e) {
		String stackTrace = "";

		for (StackTraceElement ste : ((Throwable) e).getStackTrace()) {
			stackTrace += ste.toString() + "<br/>";
		}

		return "<div style='color:red; font-style:italic; padding-left:5em;'>"
				+ stackTrace + "</div>";
	}

	// Overload for e
	public void e(Exception e) {
		e(null, e);
	}

	// Overload for e
	public void e(Object msg) {
		e(msg, null);
	}

	public void i(String msg) {
		finalLog(msg, INFO);
	}

	public void i(String msg, Object... args) {
		i(String.format(msg, args));
	}

	public void d(String msg) {
		finalLog(msg, DEBUG);
	}

	public void d(String msg, Object... args) {
		d(String.format(msg, args));
	}

	private void finalLog(String msg, int level) {
		String printmsg="";
		printmsg += (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS"))
				.format(new Date());
		printmsg+="->";

		switch (level) {
			case DEBUG:
			case INFO:
				printmsg+=msg;
				break;
			case ERROR:
				printmsg+="\n";
				printmsg+="Fail As:-******* ";
				printmsg+=msg;
				/*Assert.fail(printmsg);*/
				break;
		}

		System.out.println(printmsg);
	}

}
