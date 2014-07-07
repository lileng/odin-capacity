..
#!/bin/bash
#This is a test cron script
#Author : Nikhil Mone
(echo ========== Hostname (minutely) ========= ; hostname ; echo ========== Date ========== ; date ; echo ========== Grep PHP procs ========= ; ps -ef | grep php ; echo =========================) > $OPENSHIFT_REPO_DIR/php/foo.txt &
..

