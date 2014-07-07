#!/bin/bash
minute=$(date '+%M')
hour=$(date '+%H')

if [ $minute != 39 ]; then
    exit
fi

if [ $hour != 21 ]; then
    exit
fi

date > $OPENSHIFT_JBOSSEWS_LOG_DIR/last_date_cron_ran
