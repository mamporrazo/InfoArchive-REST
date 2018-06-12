  _____ _   _ ______ ____          _____   _____ _    _ _______      ________   _  _   
 |_   _| \ | |  ____/ __ \   /\   |  __ \ / ____| |  | |_   _\ \    / /  ____| | || |  
   | | |  \| | |__ | |  | | /  \  | |__) | |    | |__| | | |  \ \  / /| |__    | || |_ 
   | | | . ` |  __|| |  | |/ /\ \ |  _  /| |    |  __  | | |   \ \/ / |  __|   |__   _|
  _| |_| |\  | |   | |__| / ____ \| | \ \| |____| |  | |_| |_   \  /  | |____     | |  
 |_____|_| \_|_|    \____/_/    \_\_|  \_\\_____|_|  |_|_____|   \/   |______|    |_|  
                                                                                       
                                                                                       
/**
* Restful Java Class sample for InfoArchive 4.
* 
* @author EMEA SPT
* @version 1.0
*/


INTRODUCTION
------------

The class included with this eclipse project demostrates how to interact with the new powerful Restful services included with InfoArchive 4.

The flow represented demostrates how to get all the Restful resources required in order to execute a query, Retrieving and formatting the results, while any attachment detected is downloaded to the local machine.


BEFORE STARTING
---------------

BEFORE EXECUTING THE SAMPLE go to com/emc/ecd/spt/restfulsamples and update config.properties with your InfoArchive instance information.

In order to run the sample class succesfully Phonecalls holding has to be installed and deployed before executing the class

EXECUTING THE SAMPLE
--------------------

To execute the sample go to the root directory of the project and execute "runRestfulSample.bat". The class will ask about a CustomerID to be used as a criteria. The parameter is optional and omitting it will retrieve all the records existing in InfoArchive
