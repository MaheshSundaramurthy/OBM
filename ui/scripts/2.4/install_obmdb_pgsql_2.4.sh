#!/bin/bash

test $# -eq 5 || {
    echo "usage: $0 db user password lang installationtype"
    exit 1
}

db=$1
user=$2
pw=$3
obm_lang=$4
host=localhost
obm_installation_type=$5

export PGPASSWORD=$pw

if [ $obm_installation_type = "full" ]; then
  echo "  Delete old database"
  su - postgres -c "dropdb ${db}"
  
  
  su - postgres -c "dropuser ${user}"
  
  echo "Creating role '${user}' (pw: ${pw}) & db '${db}' (lang: ${obm_lang})..."
  su - postgres -c "createuser --createdb --no-superuser --no-createrole --login ${user}"
  
  su - postgres -c "psql template1 <<EOF
ALTER USER ${user} WITH PASSWORD '${pw}'
\q
EOF"
  
  echo "  Create new $DB database"
  
  su - postgres -c "createdb -O ${user} --encoding=UTF-8 ${db}"
fi

su - postgres -c "psql ${db} <<EOF
CREATE LANGUAGE plpgsql;
ALTER DATABASE ${db} SET TIMEZONE='GMT';
\q
EOF"

psql -U ${user} -h ${host} ${db} -f \
create_obmdb_2.4.pgsql.sql > /tmp/data_insert.log 2>&1
grep -i error /tmp/data_insert.log && {
    echo "error in pg script"
    exit 1
}

psql -U ${user} -h ${host} ${db} -f \
obmdb_default_values_2.4.sql >> /tmp/data_insert.log 2>&1
grep -i error /tmp/data_insert.log && {
    echo "error in pg script"
    exit 1
}

psql -U ${user} -h ${host} ${db} -f \
obmdb_triggers_2.4.pgsql.sql >> /tmp/data_insert.log 2>&1
grep -i error /tmp/data_insert.log && {
    echo "error in pg script"
    exit 1
}

echo "  Dictionnary data insertion"
psql -U ${user} -h ${host} ${db} -f \
data-${obm_lang}/obmdb_ref_2.4.sql >> /tmp/data_insert.log 2>&1
grep -i error /tmp/data_insert.log && {
    echo "error in pg script"
    exit 1
}

echo "  Company Naf Code data insertion"
psql -U ${user} -h ${host} ${db} -f \
data-${obm_lang}/obmdb_nafcode_2.4.sql >> /tmp/data_insert.log 2>&1
grep -i error /tmp/data_insert.log && {
    echo "error in pg script"
    exit 1
}

echo "  Default preferences data insertion"
psql -U ${user} -h ${host} ${db} -f \
obmdb_prefs_values_2.4.sql >> /tmp/data_insert.log 2>&1
grep -i error /tmp/data_insert.log && {
    echo "error in pg script"
    exit 1
}

psql -U ${user} -h ${host} -q ${db} <<EOF
UPDATE UserObmPref SET userobmpref_value='${obm_lang}' WHERE userobmpref_option='set_lang'
\q
EOF

if [ -d updates ]; then
  pushd updates
  for i in `ls -v1 *pgsql.sql`; do
    echo " Insert Update sql file ${i}"
    phpfile=`echo $i | sed 's/pgsql.sql/pre.php/g'`
    if test -f "$phpfile"; then
      $PHP "$phpfile"
    fi    
    psql -U ${user} -h ${host} ${db} -f \
      $i >> /tmp/data_insert.log 2>&1
    phpfile=`echo $i | sed 's/pgsql.sql/post.php/g'`
    if test -f "$phpfile"; then
      $PHP "$phpfile"
    fi      
  done
  popd
fi


echo "DONE."
