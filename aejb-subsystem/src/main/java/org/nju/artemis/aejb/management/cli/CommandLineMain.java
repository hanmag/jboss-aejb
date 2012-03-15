package org.nju.artemis.aejb.management.cli;

import java.io.Console;
import java.io.File;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicLong;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.nju.artemis.aejb.management.client.AEjbClient;
import org.nju.artemis.aejb.management.client.AEjbClientImpl.AEjbStatus;

/**
 * A command line utility to manage aejbs.
 * 
 * @author <a href="wangjue1199@gmail.com">Jason</a>
 */
public class CommandLineMain {
	private static final String NEW_LINE = "\n";
	private static final String SPACE = " ";
	private static final String TAB = "; ";
	private static final String POINT = ".";
	private static final AtomicLong TICK = new AtomicLong(1500);
	private static final int MAXCONNECTTIMES = 20;
//	private static final String CLIENT_JNDI = "java:global/aejb/client";
	private static String classpath = null;
	private static final Console theConsole = System.console();
	private State nextState;
	FilenameTabCompleter pathCompleter;
	static final String CLIENT_EJB_JNDI = "ejb:" + "/" + "aejb-client" + "/" + "/" + "AEjbClientBean" + "!" + "org.nju.artemis.aejb.client.AEjbClient";
	static AEjbClient client;
	
	private CommandLineMain(){
		if (theConsole == null) {
            throw new IllegalStateException("No java.io.Console available to interact with user.");
        }
		pathCompleter = isWindows() ? new WindowsFilenameTabCompleter() : new DefaultFilenameTabCompleter();
        nextState = new ConnectState();
	}

	private void run() {
        while ((nextState = nextState.execute()) != null) {
        }
    }
	
	/**
     * @param args
     */
    public static void main(String[] args) {

        final int argsLen = args.length;
        if (argsLen >= 1) {
        	for (int i = 0, argsLength = argsLen; i < argsLength; i++) {
				final String arg = args[i];
				if (arg.charAt(0) == '-') {
					if ("-cp".equals(arg) || "-classpath".equals(arg)) {
						classpath = args[++i];
						AccessController.doPrivileged(new PropertyWriteAction("java.class.path", classpath));
					}
				}
        	}
        }
        new CommandLineMain().run();
    }
    
    private class FunctionPrompt implements State {

        @Override
        public State execute() {
//            Values values = new Values();
            theConsole.printf(NEW_LINE);
            theConsole.printf(ManagementMessages.FunctionPrompt);
            theConsole.printf(NEW_LINE);

            while (true) {
                String temp = theConsole.readLine("[choose /]: ");
                if (temp == null) {
                    /*
                     * This will return user to the command prompt so add a new line to
                     * ensure the command prompt is on the next line.
                     */
                    theConsole.printf(NEW_LINE);
                    return null;
                }
                if (temp.length() > 0) {
					if (temp.length() == 1) {
						switch (temp.charAt(0)) {
						case 'a':
						case 'A':
							return new DeploymentsDisplayer();
						case 'b':
						case 'B':
							return new DeploymentFilePathInput();
						case 'c':
						case 'C':
							return new DeploymentFileNameInput();
						case 'd':
						case 'D':
							return new AEjbNameInput(true);
						case 'e':
						case 'E':
							return new AEjbNameInput(false);
						}
					}
					theConsole.printf(ManagementMessages.UnexpectedCommandPrompt + SPACE + "'" + temp + "'.");
					theConsole.printf(NEW_LINE);
                }
            }
        }
    }
    
    /**
     * Input deployment file path.
     */
    private class DeploymentFilePathInput implements State {

        private DeploymentFilePathInput() {
        }

        @Override
        public State execute() {
        	theConsole.printf(NEW_LINE);
        	theConsole.printf(ManagementMessages.InputFilePathPrompt);
            theConsole.printf(NEW_LINE);
            
            while (true) {
                String temp = theConsole.readLine("[filePath /]: ");
                if (temp == null) {
                    theConsole.printf(NEW_LINE);
                    return null;
                }
                if (temp.length() > 0) {
                    return new DeploymentFileFinder(temp);
                }
            }
        }
    }
    
    /**
     * Input deployment file name.
     */
    private class DeploymentFileNameInput implements State {

        private DeploymentFileNameInput() {
        }

        @Override
        public State execute() {
        	theConsole.printf(NEW_LINE);
        	theConsole.printf(ManagementMessages.InputFileNamePrompt);
            theConsole.printf(NEW_LINE);
            
            while (true) {
                String temp = theConsole.readLine("[fileName /]: ");
                if (temp == null) {
                    theConsole.printf(NEW_LINE);
                    return null;
                }
                if (temp.length() > 0) {
                    return new DeploymentFileFinder(temp, true);
                }
            }
        }
    }
    
    /**
     * Input aejb name.
     */
    private class AEjbNameInput implements State {

    	boolean passivate;
    	
        private AEjbNameInput(boolean passivate) {
        	this.passivate = passivate;
        }

        @Override
        public State execute() {
        	theConsole.printf(NEW_LINE);
        	theConsole.printf(ManagementMessages.AEjbNamePrompt);
            theConsole.printf(NEW_LINE);
            
            while (true) {
                String temp = theConsole.readLine("[aejbname /]: ");
                if (temp == null) {
                    theConsole.printf(NEW_LINE);
                    return null;
                }
                if (temp.length() > 0) {
                	if(passivate)
                		return new AEjbBlocker(temp);
                	else 
                		return new AEjbRecorver(temp);
                }
            }
        }
    }
    
    private class AEjbBlocker implements State {

    	String aejbName;
    	
        private AEjbBlocker(String aejbName) {
        	this.aejbName = aejbName;
        }
        
		@Override
		public State execute() {
			boolean exsit = false;
			try {
				exsit = blockAEjb(aejbName);
			} catch (Exception e) {
				theConsole.printf(NEW_LINE);
				theConsole.printf("## ");
				theConsole.printf(e.getMessage());
				return new ErrorState(ManagementMessages.NotConnectServer, new FunctionPrompt());
			}
			if(exsit == false) {
				theConsole.printf(NEW_LINE);
	        	theConsole.printf(ManagementMessages.AEjbNotExist);
	            theConsole.printf(NEW_LINE);
	            return new FunctionPrompt();
			}
			return new AEjbResponseWaiting(aejbName);
		}
    }
    
    private class AEjbRecorver implements State {

    	String aejbName;
    	
        private AEjbRecorver(String aejbName) {
        	this.aejbName = aejbName;
        }
        
		@Override
		public State execute() {
			boolean exsit = false;
			try {
				exsit = recorverAEjb(aejbName);
			} catch (Exception e) {
				theConsole.printf(NEW_LINE);
				theConsole.printf("## ");
				theConsole.printf(e.getMessage());
				return new ErrorState(ManagementMessages.NotConnectServer, new FunctionPrompt());
			}
			if(exsit == false) {
				theConsole.printf(NEW_LINE);
	        	theConsole.printf(ManagementMessages.AEjbNotExist);
	            theConsole.printf(NEW_LINE);
	            return new FunctionPrompt();
			}
			return new AEjbResponseWaiting(aejbName);
		}
    }
    
    /**
     * Responsible for waiting the response.
     */
    private class AEjbResponseWaiting implements State {

    	String aejbName;
    	
        private AEjbResponseWaiting(String aejbName) {
        	this.aejbName = aejbName;
        }
        
		@Override
		public State execute() {
			theConsole.printf(NEW_LINE);
        	theConsole.printf(ManagementMessages.WaitingPrompt);
        	AEjbStatus status = AEjbStatus.BLOCKING;
        	int times = 0;
			do {
				try {
					Thread.sleep(TICK.get());
					status = getAEjbStatus(aejbName);
					times++;
					theConsole.printf(POINT);
				} catch (InterruptedException e) {
					theConsole.printf(NEW_LINE);
		        	theConsole.printf(ManagementMessages.ThreadExceptionPrompt);
		        	theConsole.printf(NEW_LINE);
					break;
				} catch (Exception e) {
					theConsole.printf(NEW_LINE);
					theConsole.printf("## ");
		        	theConsole.printf(e.getMessage());
		        	theConsole.printf(NEW_LINE);
					break;
				}
			} while (status != null && times < MAXCONNECTTIMES);
			
			if(status != null && times == MAXCONNECTTIMES) 
				return new ErrorState(ManagementMessages.OperationTimeOutFailed, new FunctionPrompt());
			if(status != null)
				return new ErrorState(ManagementMessages.OperationFailed, new FunctionPrompt());
			
			theConsole.printf(NEW_LINE);
			theConsole.printf(ManagementMessages.OperationSuccess);
			theConsole.printf(NEW_LINE);
			return new FunctionPrompt();
		}
    }
    
    /**
     * Responsible for searching for the relevent deployment files.
     */
    private class DeploymentFileFinder implements State {

    	String buffer;
    	boolean file;
    	
        private DeploymentFileFinder(String buffer) {
			this.buffer = buffer;
			this.file = false;
        }
        
        private DeploymentFileFinder(String buffer, boolean file) {
			this.buffer = buffer;
			this.file = file;
        }

        @Override
        public State execute() {
            String jbossHome = System.getenv("JBOSS_HOME");
            if (jbossHome == null) {
                return new ErrorState(ManagementMessages.JbossHomeNotSet, null);
            }
            
            File deployment = null;
            if(file) {
            	buffer = jbossHome + "/standalone/deployments/" + buffer;
            }
            
			try {
				deployment = pathCompleter.getFile(buffer);
			} catch (IllegalArgumentException e) {
				return new ErrorState(e.getMessage(), new FunctionPrompt());
			}
			
			if(deployment == null) {
				return new ErrorState(ManagementMessages.FilePathNotExist + ": " + buffer, new FunctionPrompt());
			}
			
			if(file) {
				deployment.delete();
			} else {
				FileOperations.copyFile(deployment, jbossHome + "/standalone/deployments/" + deployment.getName());
			}
			
			return new FunctionPrompt();
        }
    }
    
    /**
     * Display deployments which contains AEjb in Server.
     */
    private class DeploymentsDisplayer implements State {

        private DeploymentsDisplayer() {
        }

        @Override
        public State execute() {
        	Map<String,String[]> info;
        	try {
        		info = getAllDeploymentsInfo();
			} catch (Exception e) {
				theConsole.printf(NEW_LINE);
				theConsole.printf("## ");
				theConsole.printf(e.getMessage());
				return new ErrorState(ManagementMessages.NotConnectServer, new FunctionPrompt());
			}
        	
        	theConsole.printf(NEW_LINE);
        	theConsole.printf(SPACE);
        	theConsole.printf("## ");
        	theConsole.printf(ManagementMessages.DisplayDeployments);
        	theConsole.printf(SPACE);
        	theConsole.printf("## ");
            theConsole.printf(NEW_LINE);
            
            if(info.isEmpty()) {
            	theConsole.printf(NEW_LINE);
            	theConsole.printf(ManagementMessages.EmptyDeployments);
            	theConsole.printf(NEW_LINE);
            	return new FunctionPrompt();
            }
            
            Iterator<Entry<String, String[]>> itrator = info.entrySet().iterator();
            while(itrator.hasNext()) {
            	Entry<String, String[]> entry = itrator.next();
            	theConsole.printf(NEW_LINE);
            	theConsole.printf("deploymentunit: ");
            	theConsole.printf(entry.getKey());
            	theConsole.printf(NEW_LINE);
            	theConsole.printf("aejbs: ");
            	for(String aejbName:entry.getValue()) {
            		theConsole.printf(aejbName);
            		theConsole.printf(TAB);
            	}
            }
            theConsole.printf(NEW_LINE);
            return new FunctionPrompt();
        }
    }

    /**
     * State to report an error to the user, optionally a nextState can be supplied so the process can continue even though an
     * error has been reported.
     */
    private class ErrorState implements State {

        private final State nextState;
        private final String errorMessage;

        private ErrorState(String errorMessage) {
            this(errorMessage, null);
        }

        private ErrorState(String errorMessage, State nextState) {
            this.errorMessage = errorMessage;
            this.nextState = nextState;
        }

        @Override
        public State execute() {
			theConsole.printf(NEW_LINE);
			theConsole.printf(" * Error * ");
			theConsole.printf(NEW_LINE);

			theConsole.printf(errorMessage);
			theConsole.printf(NEW_LINE);
			theConsole.printf(NEW_LINE);
			return nextState;
		}
    }
    
    private class ConnectState implements State {

		@Override
		public State execute() {
			theConsole.printf(ManagementMessages.ConnectPrompt);
			theConsole.printf(NEW_LINE);
			while (true) {
                String temp = theConsole.readLine("[disconnected /]: ");
                if (temp == null) {
                    theConsole.printf(NEW_LINE);
                    return null;
                }
                if (temp.length() > 0) {
                    if(temp.equals("connect"))
                    	break;
                    else {
                    	theConsole.printf(ManagementMessages.UnexpectedCommandPrompt + SPACE + "'" + temp + "'.");
                    	theConsole.printf(NEW_LINE);
                    }
                }
            }
			try {
				getAndInvokeClientEjb();
			} catch (Exception e) {
				theConsole.printf(NEW_LINE);
				return new ErrorState(ManagementMessages.NotConnectServer, new ConnectState());
			}
			return new FunctionPrompt();
		}
    }
    
    private static boolean blockAEjb(String aejbName) throws Exception {
    	return getAndManageClientEjb(aejbName, "blockAEjb");
    }
    
    private static boolean recorverAEjb(String aejbName) throws Exception {
    	return getAndManageClientEjb(aejbName, "resumeAEjb");
    }
    
    private static AEjbStatus getAEjbStatus(String aejbName) throws Exception {
    	getAndInvokeClientEjb();
    	return client.getAEjbStatus(aejbName);
    }
    
    private static Map<String,String[]> getAllDeploymentsInfo() throws Exception {
    	// This version do not support remoting JNDI...
//    	Hashtable env = new Hashtable();
//    	env.put("java.naming.factory.url.pkgs", "org.jboss.as.naming.interfaces:org.jboss.ejb.client.namings");
//    	InitialContext initCtx = new InitialContext(env);
//    	AEjbClient client = (AEjbClient) initCtx.lookup(CLIENT_JNDI);
    	getAndInvokeClientEjb();
    	return client.listAEjbs();
    }
    
	private static void getAndInvokeClientEjb() throws Exception {
		final Hashtable jndiProperties = new Hashtable();
		jndiProperties.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
		final Context context = new InitialContext(jndiProperties);
		Object remoteClient = context.lookup(CLIENT_EJB_JNDI);
		Method method = getClientMethod(remoteClient, "getAEjbClient");
		client = (AEjbClient) method.invoke(remoteClient);
	}
	
	private static boolean getAndManageClientEjb(String aejbName, String methodName) throws Exception {
		final Hashtable jndiProperties = new Hashtable();
		jndiProperties.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
		final Context context = new InitialContext(jndiProperties);
		Object remoteClient = context.lookup(CLIENT_EJB_JNDI);
		Method method = getClientMethod(remoteClient, methodName);
		return (Boolean) method.invoke(remoteClient, aejbName);
	}
	
	private static Method getClientMethod(Object remoteClient, String methodName) {
		for (Method me : remoteClient.getClass().getMethods()) {
			if (me.getName().contains(methodName)) {
				return me;
			}
		}
		return null;
	}
	
    private interface State {
        State execute();
    }
    
    public static boolean isWindows() {
        return SecurityActions.getSystemProperty("os.name").toLowerCase().indexOf("windows") >= 0;
    }
}