#!/bin/bash

test $# -eq 5 || {
    echo "usage: $0 db user password lang installation type"
    exit 1
}

db=$1
user=$2
pw=$3
obm_lang=$4
obm_installation_type=$5

if [ $obm_installation_type = "full" ]; then

  echo "*** Database creation"
  
  echo "  Delete old database if exists"
  mysql -u $user -p$pw -e "DROP DATABASE IF EXISTS $db"
  
  echo "  Create new $db database"
  mysql -u $user -p$pw -e "CREATE DATABASE $db CHARACTER SET utf8 COLLATE utf8_general_ci"
fi

echo "  Create new $db database model"
mysql -u $user -p$pw $db < create_obmdb_2.4.mysql.sql
test $? -eq 0 || {
    echo "error running mysql script"
    exit 1
}
echo "*** Database filling"

# Default data insertion
mysql --default-character-set='UTF8' -u $user -p$pw $db < obmdb_default_values_2.4.sql
test $? -eq 0 || {
    echo "error running mysql script"
    exit 1
}

# Dictionnary data insertion
echo "  Dictionnary data insertion"
mysql --default-character-set='UTF8' -u $user -p$pw $db < data-$obm_lang/obmdb_ref_2.4.sql
test $? -eq 0 || {
    echo "error running mysql script"
    exit 1
}

# Company Naf Code data insertion
echo "  Company Naf Code data insertion"
mysql --default-character-set='UTF8' -u $user -p$pw $db < data-$obm_lang/obmdb_nafcode_2.4.sql
test $? -eq 0 || {
    echo "error running mysql script"
    exit 1
}

# Preferences data insertion & Update default lang to .ini value
echo "  Default preferences data insertion"
mysql --default-character-set='UTF8' -u $user -p$pw $db < obmdb_prefs_values_2.4.sql
test $? -eq 0 || {
    echo "error running mysql script"
    exit 1
}

echo "UPDATE UserObmPref set userobmpref_value='$obm_lang' where userobmpref_option='set_lang'" | mysql -u $user -p$pw $db 


if [ -d updates ]; then
  pushd updates
  for i in `ls -v1 *mysql.sql`; do
    echo " Insert Update sql file ${i}"
    phpfile=`echo $i | sed 's/mysql.sql/pre.php/g'`
    if test -f "$phpfile"; then
      $PHP "$phpfile"
    fi  
    mysql --default-character-set='UTF8' -u $user -p$pw $db < $i
    phpfile=`echo $i | sed 's/mysql.sql/post.php/g'`
    if test -f "$phpfile"; then
      $PHP "$phpfile"
    fi    
  done
  popd
fi

