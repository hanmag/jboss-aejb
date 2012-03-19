package org.nju.artemis.aejb.management.cli;

/**
 * @author <a href="wangjue1199@gmail.com">Jason</a>
 */
public interface ManagementMessages {

	String FunctionPrompt = "What type of function do you wish to ues? (Only for AEJB) \n a) Overview (List all AEJBs in the server) \n b) Deploy AEJB \t c) Undeploy AEJB \n d) Block AEJB \t\t e) Resume AEJB \n f) Replace AEJB \t g) Switch AEJB";

	String JbossHomeNotSet = "JBOSS_HOME environment variable not set.";

	String InputFilePathPrompt = "Input the deployment file path.";

	String InputFileNamePrompt = "Input the deployment file name.";

	String FilePathNotCorrect = "File path must be absolutly path.";

	String FilePathNotExist = "File path does not exist.";

	String DisplayDeployments = "All deploymentunits in server.";

	String NotConnectServer = "AEjb client is not available, Please deploy the aejb client first.";

	String EmptyDeployments = "None deploymentunit contains AEjbs.";

	String AEjbNamePrompt = "Input the aejb name.";

	String AEjbNotExist = "AEjb name dose not exist.";

	String WaitingPrompt = "Waiting.";

	String ThreadExceptionPrompt = "Thread interrupted unexpected.";

	String OperationFailed = "The operation to AEjb failed.";

	String OperationTimeOutFailed = "The operation to AEjb time out failed.";

	String OperationSuccess = "The operation to AEjb completed.";

	String ConnectPrompt = "You are disconnected at the moment. Type 'connect' to connect to the server.";

	String ProtocolPrompt = "What type of protocol do you wish to ues? \n a) quiescence \n b) tranquility";

	String UnexpectedCommandPrompt = "Unexpected command";

	String UnexpectedSameName = "Two names can not be equal.";
}
