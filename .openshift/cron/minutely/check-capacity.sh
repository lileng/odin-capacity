#!/bin/bash
minute=$(date '+%M')
hour=$(date '+%H')

if [ $minute != 35 ]; then
    exit
fi

minute=$(date '+%M')
if [ $minute != 21 ]; then
    exit
fi

date > $OPENSHIFT_JBOSSEWS_LOG_DIR/last_date_cron_ran
