#!/bin/bash
minute=$(date '+%M')
hour=$(date '+%H')

if [ $minute != 46 ]; then
   echo $minute 
   exit
fi

if [ $hour != 21 ]; then
   echo $hour
   exit
fi

date > $OPENSHIFT_JBOSSEWS_LOG_DIR/last_date_cron_ran
