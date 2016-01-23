# NAME
    Odin Capacity

# SYNOPSIS
    TBD

# DESCRIPTION
    Utility to report remaining capacity vs. remaining hours for individuals.
    * The capacity tool feeds on a capacity grid entered into a Google Spreadsheet. Each individual typically updates a weekly capacity in hours at the start of every sprint.
    * The sprint configuration is done in a local MySQL database
    * Remaining hours on JIRA tickets are added up based on queries to JIRA.
    * If calculated capacity is either higher or lower than remaining hours, an email is sent to the individual.
    
# STATUS


#  EXAMPLES
 Run the end to end process
```java -jar /home/usr_home/agile/capacity.jar```
 
Manual update of current capacity
 ```java ReadGoogleSpreadsheet Sprint 2014.11```

#ENVIRONMENT
	A MYSQL database is used for configuration and for all data. The META-INF/persistence.xml contains URLs and other information.
	You can connect to an external MySQL instance by setting up an SSL tunnel:
		ssh -L 5433:ec2-172-16-139-19.us-west-1.compute.amazonaws.com:5432 \
		deploy@ec2-174-129-17-196.compute-1.amazonaws.com 
	
# DEPLOYMENT
    Using Maven to build standard jar file

# SEE ALSO
    n/a
    
# BUGS
    See https://github.com/lileng/odin-capacity/issues

# AUTHORS / CONTRIBUTORS
    [Morten Lileng](mailto:odin@lileng.com)