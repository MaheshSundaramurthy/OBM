--/////////////////////////////////////////////////////////////////////////////
--// OBM - File : create_obmdb_1.2.mysql.sql                                 //
--//     - Desc : MySQL Database 1.2 creation script                         //
--// 2005-06-08 ALIACOM                                                      //
--/////////////////////////////////////////////////////////////////////////////
-- $Id$
--/////////////////////////////////////////////////////////////////////////////

-------------------------------------------------------------------------------
-- Global Information table
-------------------------------------------------------------------------------

--
-- Table structure for table 'ObmInfo'
--
CREATE TABLE ObmInfo (
  obminfo_name   varchar(32) NOT NULL default '',
  obminfo_value  varchar(255) default '',
  PRIMARY KEY (obminfo_name)
);


-------------------------------------------------------------------------------
-- User, Preferences tables
-------------------------------------------------------------------------------

--
-- Table structure for table 'ObmSession'
--
CREATE TABLE ObmSession (
  obmsession_sid         varchar(32) NOT NULL default '',
  obmsession_timeupdate  timestamp(14),
  obmsession_name        varchar(32) NOT NULL default '',
  obmsession_data        text,
  PRIMARY KEY (obmsession_sid, obmsession_name)
);


--
-- Table structure for table 'ActiveUserObm'
--
CREATE TABLE ActiveUserObm (
  activeuserobm_sid            varchar(32) NOT NULL default '',
  activeuserobm_session_name   varchar(32) NOT NULL default '',
  activeuserobm_userobm_id     int(11) default NULL,
  activeuserobm_timeupdate     timestamp(14),
  activeuserobm_timecreate     timestamp(14),
  activeuserobm_nb_connexions  int(11) NOT NULL default 0,
  activeuserobm_lastpage       varchar(64) NOT NULL default '0',
  activeuserobm_ip             varchar(32) NOT NULL default '0',
  PRIMARY KEY (activeuserobm_sid)
);


--
-- Table structure for table 'UserObm_SessionLog'
--
CREATE TABLE UserObm_SessionLog (
  userobm_sessionlog_sid            varchar(32) NOT NULL default '',
  userobm_sessionlog_session_name   varchar(32) NOT NULL default '',
  userobm_sessionlog_userobm_id     int(11) default NULL,
  userobm_sessionlog_timeupdate     timestamp(14),
  userobm_sessionlog_timecreate     timestamp(14),
  userobm_sessionlog_nb_connexions  int(11) NOT NULL default 0,
  userobm_sessionlog_lastpage       varchar(32) NOT NULL default '0',
  userobm_sessionlog_ip             varchar(32) NOT NULL default '0',
  PRIMARY KEY (userobm_sessionlog_sid)
);


--
-- Table structure for table 'UserObm'
--
CREATE TABLE UserObm (
  userobm_id                int(8) auto_increment,
  userobm_timeupdate        timestamp(14),
  userobm_timecreate        timestamp(14),
  userobm_userupdate        int(8),
  userobm_usercreate        int(8),
  userobm_local             int(1) default 1,
  userobm_ext_id            varchar(16),
  userobm_login             varchar(32) DEFAULT '' NOT NULL,
  userobm_password          varchar(64) DEFAULT '' NOT NULL,
  userobm_perms             varchar(254),
  userobm_calendar_version  timestamp(14),
  userobm_datebegin         date,
  userobm_archive           char(1) not null default '0',
  userobm_lastname          varchar(32) default '',
  userobm_firstname         varchar(48) default '',
  userobm_phone             varchar(32),
  userobm_phone2            varchar(32),
  userobm_fax               varchar(32),
  userobm_fax2              varchar(32),
  userobm_email             varchar(60),
  userobm_description       varchar(255),
  userobm_location          varchar(100),
  userobm_timelastaccess    timestamp(14),
  PRIMARY KEY (userobm_id),
  UNIQUE k_login_user (userobm_login)
);


--
-- Table structure for table 'UserObmPref'
--
CREATE TABLE UserObmPref (
   userobmpref_user_id  int(8) DEFAULT 0 NOT NULL,
   userobmpref_option   varchar(50) NOT NULL,
   userobmpref_value    varchar(50) NOT NULL
);


--
-- New table 'DisplayPref'
--
CREATE TABLE DisplayPref (
  display_user_id     int(8) NOT NULL default 0,
  display_entity      varchar(32) NOT NULL default '',
  display_fieldname   varchar(64) NOT NULL default '',
  display_fieldorder  int(3) unsigned default NULL,
  display_display     int(1) unsigned NOT NULL default 1,
  PRIMARY KEY (display_user_id, display_entity, display_fieldname),
  INDEX idx_user (display_user_id),
  INDEX idx_entity (display_entity)
) TYPE=MyISAM;


-------------------------------------------------------------------------------
-- References Tables
-------------------------------------------------------------------------------
--
-- Table structure for the table 'DataSource'
--
CREATE TABLE DataSource (
  datasource_id          int(8) auto_increment,
  datasource_timeupdate  timestamp(14),
  datasource_timecreate  timestamp(14),
  datasource_userupdate  int(8),
  datasource_usercreate  int(8),
  datasource_name        varchar(64),
  PRIMARY KEY (datasource_id)
);


--
-- Table structure for the table 'Country'
--
CREATE TABLE Country (
  country_timeupdate  timestamp(14),
  country_timecreate  timestamp(14),
  country_userupdate  int(8),
  country_usercreate  int(8),
  country_iso3166     char(2) NOT NULL,
  country_name        varchar(64),
  country_lang        char(2) NOT NULL,
  country_phone       varchar(4),
  PRIMARY KEY (country_iso3166, country_lang)
);


-------------------------------------------------------------------------------
-- Company module tables
-------------------------------------------------------------------------------
-- 
-- Table structure for table 'CompanyType'
--
CREATE TABLE CompanyType (
  companytype_id          int(8) auto_increment,
  companytype_timeupdate  timestamp(14),
  companytype_timecreate  timestamp(14),
  companytype_userupdate  int(8),
  companytype_usercreate  int(8),
  companytype_code        varchar(10) default '', 
  companytype_label       char(12),
  PRIMARY KEY (companytype_id)
);


-- 
-- Table structure for table 'CompanyActivity'
--
CREATE TABLE CompanyActivity (
  companyactivity_id          int(8) auto_increment,
  companyactivity_timeupdate  timestamp(14),
  companyactivity_timecreate  timestamp(14),
  companyactivity_userupdate  int(8),
  companyactivity_usercreate  int(8),
  companyactivity_code        varchar(10) default '', 
  companyactivity_label       varchar(64),
  PRIMARY KEY (companyactivity_id)
);


-- 
-- Table structure for table 'CompanyNafCode'
--
CREATE TABLE CompanyNafCode (
  companynafcode_id          int(8) auto_increment,
  companynafcode_timeupdate  timestamp(14),
  companynafcode_timecreate  timestamp(14),
  companynafcode_userupdate  int(8),
  companynafcode_usercreate  int(8),
  companynafcode_title       int(1) NOT NULL DEFAULT 0,
  companynafcode_code        varchar(4),
  companynafcode_label       varchar(128),
  PRIMARY KEY (companynafcode_id)
);


--
-- Table structure for table 'Company'
--
CREATE TABLE Company (
  company_id                   int(8) auto_increment,
  company_timeupdate           timestamp(14),
  company_timecreate           timestamp(14),
  company_userupdate           int(8),
  company_usercreate           int(8),
  company_datasource_id        int(8) DEFAULT 0,
  company_number               varchar(32),
  company_vat                  varchar(20),
  company_archive              char(1) DEFAULT '0' NOT NULL,
  company_name                 varchar(96) DEFAULT '' NOT NULL,
  company_aka                  varchar(255),
  company_sound                varchar(48),
  company_type_id              int(8),
  company_activity_id          int(8),
  company_nafcode_id           int(8),
  company_marketingmanager_id  int(8),
  company_address1             varchar(64),
  company_address2             varchar(64),
  company_address3             varchar(64),
  company_zipcode              varchar(14),
  company_town                 varchar(64),
  company_expresspostal        varchar(16),
  company_country_iso3166      char(2) DEFAULT '0',
  company_phone                varchar(32),
  company_fax                  varchar(32),
  company_web                  varchar(64),
  company_email                varchar(64),
  company_contact_number       int(5) DEFAULT 0 NOT NULL,
  company_deal_number          int(5) DEFAULT 0 NOT NULL,
  company_deal_total           int(5) DEFAULT 0 NOT NULL,
  company_comment              text,
  PRIMARY KEY (company_id)
);


--
-- Table structure for table 'CompanyCategory1'
--
CREATE TABLE CompanyCategory1 (
  companycategory1_id          int(8) auto_increment,
  companycategory1_timeupdate  timestamp(14),
  companycategory1_timecreate  timestamp(14),
  companycategory1_userupdate  int(8) NOT NULL default 0,
  companycategory1_usercreate  int(8) NOT NULL default 0,
  companycategory1_code        varchar(10) NOT NULL default '',
  companycategory1_label       varchar(100) NOT NULL default '',
  PRIMARY KEY (companycategory1_id)
);


--
-- Table structure for table 'CompanyCategory1Link'
--
CREATE TABLE CompanyCategory1Link (
  companycategory1link_category_id  int(8) NOT NULL default 0,
  companycategory1link_company_id   int(8) NOT NULL default 0,
  PRIMARY KEY (companycategory1link_category_id,companycategory1link_company_id),
  INDEX compcat1_idx_comp (companycategory1link_company_id)
);


-------------------------------------------------------------------------------
-- Contact module tables
-------------------------------------------------------------------------------
--
-- Table structure for table 'Contact'
--
CREATE TABLE Contact (
  contact_id                   int(8) auto_increment,
  contact_timeupdate           timestamp(14),
  contact_timecreate           timestamp(14),
  contact_userupdate           int(8),
  contact_usercreate           int(8),
  contact_datasource_id        int(8) DEFAULT 0,
  contact_company_id           int(8),
  contact_company              varchar(64),
  contact_kind_id              int(8),
  contact_marketingmanager_id  int(8),
  contact_lastname             varchar(64) DEFAULT '' NOT NULL,
  contact_firstname            varchar(64),
  contact_aka                  varchar(255),
  contact_sound                varchar(48),
  contact_service              varchar(64),
  contact_address1             varchar(64),
  contact_address2             varchar(64),
  contact_address3             varchar(64),
  contact_zipcode              varchar(14),
  contact_town                 varchar(64),
  contact_expresspostal        varchar(16),
  contact_country_iso3166      char(2) DEFAULT '0',
  contact_function_id          int(8),
  contact_title                varchar(64),
  contact_phone                varchar(32),
  contact_homephone            varchar(32),
  contact_mobilephone          varchar(32),
  contact_fax                  varchar(32),
  contact_email                varchar(128),
  contact_email2               varchar(128),
  contact_mailing_ok           char(1) DEFAULT '0',
  contact_archive              char(1) DEFAULT '0',
  contact_privacy              int(2) NOT NULL DEFAULT 0,
  contact_date                 timestamp(14),
  contact_comment              text,
  contact_comment2             text,
  contact_comment3             text,
  contact_category5_id         int(8) DEFAULT 0,
  PRIMARY KEY (contact_id)
);


--
-- Table structure for table 'Kind'
--
CREATE TABLE Kind (
  kind_id          int(8) auto_increment,
  kind_timeupdate  timestamp(14),
  kind_timecreate  timestamp(14),
  kind_userupdate  int(8),
  kind_usercreate  int(8),
  kind_minilabel   varchar(64),
  kind_header      varchar(64),
  kind_lang        char(2),
  kind_default     int(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (kind_id)
);


--
-- Table structure for the table 'ContactFunction'
--
CREATE TABLE ContactFunction (
  contactfunction_id          int(8) auto_increment,
  contactfunction_timeupdate  timestamp(14),
  contactfunction_timecreate  timestamp(14),
  contactfunction_userupdate  int(8),
  contactfunction_usercreate  int(8),
  contactfunction_code        varchar(10) default '',
  contactfunction_label       varchar(64),
  PRIMARY KEY (contactfunction_id)
);


--
-- Table structure for table 'ContactCategory1'
--
CREATE TABLE ContactCategory1 (
  contactcategory1_id          int(8) auto_increment,
  contactcategory1_timeupdate  timestamp(14),
  contactcategory1_timecreate  timestamp(14),
  contactcategory1_userupdate  int(8) default 0,
  contactcategory1_usercreate  int(8) default 0,
  contactcategory1_code        varchar(10) default '',
  contactcategory1_label       varchar(100) NOT NULL default '',
  PRIMARY KEY (contactcategory1_id)
);


--
-- Table structure for table 'ContactCategory1Link'
--
CREATE TABLE ContactCategory1Link (
  contactcategory1link_category_id  int(8) NOT NULL default 0,
  contactcategory1link_contact_id   int(8) NOT NULL default 0,
  PRIMARY KEY (contactcategory1link_category_id,contactcategory1link_contact_id),
  INDEX contcat1_idx_cont (contactcategory1link_contact_id)
);


--
-- Table structure for table 'ContactCategory2'
--
CREATE TABLE ContactCategory2 (
  contactcategory2_id          int(8) auto_increment,
  contactcategory2_timeupdate  timestamp(14),
  contactcategory2_timecreate  timestamp(14),
  contactcategory2_userupdate  int(8) default 0,
  contactcategory2_usercreate  int(8) default 0,
  contactcategory2_code        varchar(10) default '',
  contactcategory2_label       varchar(100) NOT NULL default '',
  PRIMARY KEY (contactcategory2_id)
);


--
-- Table structure for table 'ContactCategory2Link'
--
CREATE TABLE ContactCategory2Link (
  contactcategory2link_category_id  int(8) NOT NULL default 0,
  contactcategory2link_contact_id   int(8) NOT NULL default 0,
  PRIMARY KEY (contactcategory2link_category_id,contactcategory2link_contact_id),
  INDEX contcat2_idx_cont (contactcategory2link_contact_id)
);


--
-- Table structure for table 'ContactCategory3'
--
CREATE TABLE ContactCategory3 (
  contactcategory3_id          int(8) auto_increment,
  contactcategory3_timeupdate  timestamp(14),
  contactcategory3_timecreate  timestamp(14),
  contactcategory3_userupdate  int(8) default 0,
  contactcategory3_usercreate  int(8) default 0,
  contactcategory3_code        varchar(10) default '',
  contactcategory3_label       varchar(100) NOT NULL default '',
  PRIMARY KEY (contactcategory3_id)
);


--
-- Table structure for table 'ContactCategory3Link'
--
CREATE TABLE ContactCategory3Link (
  contactcategory3link_category_id  int(8) NOT NULL default 0,
  contactcategory3link_contact_id   int(8) NOT NULL default 0,
  PRIMARY KEY (contactcategory3link_category_id,contactcategory3link_contact_id),
  INDEX contcat3_idx_cont (contactcategory3link_contact_id)
);

--
-- Table structure for table 'ContactCategory4'
--
CREATE TABLE ContactCategory4 (
  contactcategory4_id          int(8) auto_increment,
  contactcategory4_timeupdate  timestamp(14),
  contactcategory4_timecreate  timestamp(14),
  contactcategory4_userupdate  int(8) default 0,
  contactcategory4_usercreate  int(8) default 0,
  contactcategory4_code        varchar(10) default '',
  contactcategory4_label       varchar(100) NOT NULL default '',
  PRIMARY KEY (contactcategory4_id)
);


--
-- Table structure for table 'ContactCategory4Link'
--
CREATE TABLE ContactCategory4Link (
  contactcategory4link_category_id  int(8) NOT NULL default 0,
  contactcategory4link_contact_id   int(8) NOT NULL default 0,
  PRIMARY KEY (contactcategory4link_category_id,contactcategory4link_contact_id),
  INDEX contcat4_idx_cont (contactcategory4link_contact_id)
);

--
-- Table structure for table 'ContactCategory5'
--
CREATE TABLE ContactCategory5 (
  contactcategory5_id          int(8) auto_increment,
  contactcategory5_timeupdate  timestamp(14),
  contactcategory5_timecreate  timestamp(14),
  contactcategory5_userupdate  int(8) default 0,
  contactcategory5_usercreate  int(8) default 0,
  contactcategory5_code        varchar(10) default '',
  contactcategory5_label       varchar(100) NOT NULL default '',
  PRIMARY KEY (contactcategory5_id)
);


-------------------------------------------------------------------------------
-- Lead module tables
-------------------------------------------------------------------------------
--
-- Table structure for the table 'LeadSource'
--
CREATE TABLE LeadSource (
  leadsource_id          int(8) auto_increment,
  leadsource_timeupdate  timestamp(14),
  leadsource_timecreate  timestamp(14),
  leadsource_userupdate  int(8),
  leadsource_usercreate  int(8),
  leadsource_code        varchar(10) default '',
  leadsource_label       varchar(100) NOT NULL default '',
  PRIMARY KEY (leadsource_id)
);


--
-- Table structure for the table 'Lead'
--
CREATE TABLE Lead (
  lead_id          int(8) auto_increment,
  lead_timeupdate  timestamp(14),
  lead_timecreate  timestamp(14),
  lead_userupdate  int(8),
  lead_usercreate  int(8),
  lead_source_id   int(8),
  lead_manager_id  int(8),
  lead_company_id  int(8) NOT NULL DEFAULT 0,
  lead_privacy     int(2) NOT NULL DEFAULT 0,
  lead_name        varchar(64),
  lead_date        date,
  lead_datealarm   date,
  lead_archive     char(1) DEFAULT '0',
  lead_todo        varchar(128),
  lead_comment     text,
  PRIMARY KEY (lead_id)
);


-------------------------------------------------------------------------------
-- Deal module tables
-------------------------------------------------------------------------------
--
-- Table structure for table 'ParentDeal'
--
CREATE TABLE ParentDeal (
  parentdeal_id                   int(8) auto_increment,
  parentdeal_timeupdate           timestamp(14),
  parentdeal_timecreate           timestamp(14),
  parentdeal_userupdate           int(8),
  parentdeal_usercreate           int(8),
  parentdeal_label                varchar(128) NOT NULL,
  parentdeal_marketingmanager_id  int(8),
  parentdeal_technicalmanager_id  int(8),
  parentdeal_archive              char(1) DEFAULT '0',
  parentdeal_comment              text,
  PRIMARY KEY (parentdeal_id)
);


--
-- Table structure for table 'Deal'
--
CREATE TABLE Deal (
  deal_id                   int(8) auto_increment,
  deal_timeupdate           timestamp(14),
  deal_timecreate           timestamp(14),
  deal_userupdate           int(8),
  deal_usercreate           int(8),
  deal_number               varchar(32),
  deal_label                varchar(128),
  deal_datebegin            date,
  deal_parentdeal_id        int(8),
  deal_type_id              int(8),
  deal_tasktype_id          int(8),
  deal_company_id           int(8) DEFAULT 0 NOT NULL,
  deal_contact1_id          int(8),
  deal_contact2_id          int(8),
  deal_marketingmanager_id  int(8),
  deal_technicalmanager_id  int(8),
  deal_source_id            int(8) DEFAULT 0 NOT NULL,
  deal_source               varchar(64),
  deal_dateproposal         date,
  deal_dateexpected         date,
  deal_datealarm            date,
  deal_dateend              date,
  deal_amount               decimal(12,2),
  deal_commission           decimal(5,2) DEFAULT 0,
  deal_hitrate              int(3) DEFAULT 0,
  deal_status_id            int(2),
  deal_archive              char(1) DEFAULT '0',
  deal_todo                 varchar(128),
  deal_privacy              int(2) NOT NULL DEFAULT 0,
  deal_comment              text,
  PRIMARY KEY (deal_id)
);


--
-- Table structure for table 'DealStatus'
--
CREATE TABLE DealStatus (
  dealstatus_id          int(2) auto_increment,
  dealstatus_timeupdate  timestamp(14),
  dealstatus_timecreate  timestamp(14),
  dealstatus_userupdate  int(8),
  dealstatus_usercreate  int(8),
  dealstatus_label       varchar(24),
  dealstatus_order       int(2),
  dealstatus_hitrate     char(3),
  PRIMARY KEY (dealstatus_id)
);


--
-- Table structure for table 'DealType'
--
CREATE TABLE DealType (
  dealtype_id          int(8) auto_increment,
  dealtype_timeupdate  timestamp(14),
  dealtype_timecreate  timestamp(14),
  dealtype_userupdate  int(8),
  dealtype_usercreate  int(8),
  dealtype_label       varchar(16),
  dealtype_inout       varchar(1) DEFAULT '-',
  PRIMARY KEY (dealtype_id)
);


--
-- Table structure for the table 'DealCompanyRole'
--
CREATE TABLE DealCompanyRole (
  dealcompanyrole_id          int(8) auto_increment,
  dealcompanyrole_timeupdate  timestamp(14),
  dealcompanyrole_timecreate  timestamp(14),
  dealcompanyrole_userupdate  int(8) default NULL,
  dealcompanyrole_usercreate  int(8) default NULL,
  dealcompanyrole_code        varchar(10) default '',
  dealcompanyrole_label       varchar(64) NOT NULL default '',
  PRIMARY KEY (dealcompanyrole_id)
);


--
-- Table structure for the table 'DealCompany'
--
CREATE TABLE DealCompany (
  dealcompany_id          int(8) auto_increment,
  dealcompany_timeupdate  timestamp(14),
  dealcompany_timecreate  timestamp(14),
  dealcompany_userupdate  int(8) default NULL,
  dealcompany_usercreate  int(8) default NULL,
  dealcompany_deal_id     int(8) NOT NULL default 0,
  dealcompany_company_id  int(8) NOT NULL default 0,
  dealcompany_role_id     int(8) NOT NULL default 0,
  PRIMARY KEY (dealcompany_id),
  INDEX dealcompany_idx_deal (dealcompany_deal_id)
);


--
-- Table structure for table 'DealCategory1'
--
CREATE TABLE DealCategory1 (
  dealcategory1_id          int(8) auto_increment,
  dealcategory1_timeupdate  timestamp(14),
  dealcategory1_timecreate  timestamp(14),
  dealcategory1_userupdate  int(8) default 0,
  dealcategory1_usercreate  int(8) default 0,
  dealcategory1_code        varchar(10) default '',
  dealcategory1_label       varchar(100) NOT NULL default '',
  PRIMARY KEY (dealcategory1_id)
);


-- Table structure for table 'DealCategory1Link'
--
CREATE TABLE DealCategory1Link (
  dealcategory1link_category_id  int(8) NOT NULL default 0,
  dealcategory1link_deal_id      int(8) NOT NULL default 0,
  PRIMARY KEY (dealcategory1link_category_id,dealcategory1link_deal_id)
);


-------------------------------------------------------------------------------
-- List module tables
-------------------------------------------------------------------------------
--
-- Table structure for table 'List'
--
CREATE TABLE List (
  list_id          	 int(8) auto_increment,
  list_timeupdate  	 timestamp(14),
  list_timecreate  	 timestamp(14),
  list_userupdate  	 int(8),
  list_usercreate  	 int(8),
  list_privacy     	 int(2) NOT NULL DEFAULT 0,
  list_name        	 varchar(64) NOT NULL,
  list_subject     	 varchar(128),
  list_email       	 varchar(128),
  list_mode       	 int(1) DEFAULT 0,
  list_mailing_ok  	 int(1) DEFAULT 0,
  list_contact_archive	 int(1) DEFAULT 0,
  list_info_publication  int(1) DEFAULT 0,
  list_static_nb   	 int(10) DEFAULT 0,
  list_query_nb    	 int(10) DEFAULT 0,
  list_query       	 text,
  list_structure   	 text, 
  PRIMARY KEY (list_id),
  UNIQUE list_name (list_name)
);


--
-- Table structure for table 'ContactList'
--
CREATE TABLE ContactList (
  contactlist_list_id     int(8) DEFAULT 0 NOT NULL,
  contactlist_contact_id  int(8) DEFAULT 0 NOT NULL
);

-------------------------------------------------------------------------------
-- Calendar module tables
-------------------------------------------------------------------------------
--
-- Table structure for the table  'CalendarEvent'
--
CREATE TABLE CalendarEvent (
  calendarevent_id               int(8) auto_increment,
  calendarevent_timeupdate       timestamp(14),
  calendarevent_timecreate       timestamp(14),
  calendarevent_userupdate       int(8) default NULL,
  calendarevent_usercreate       int(8) default NULL,
  calendarevent_owner	         int(8) default NULL, 
  calendarevent_title            varchar(255) default NULL,
  calendarevent_location         varchar(100) default NULL,
  calendarevent_category1_id     int(8) default 0,
  calendarevent_priority         int(2) default NULL,
  calendarevent_privacy          int(2) NOT NULL default 0,
  calendarevent_date             timestamp(14) NOT NULL,
  calendarevent_duration         int(8) NOT NULL default 0,
  calendarevent_allday	         int(1) NOT NULL default 0,
  calendarevent_repeatkind       varchar(20) default NULL,
  calendarevent_repeatfrequence  int(3) default NULL,
  calendarevent_repeatdays       varchar(7) default NULL,
  calendarevent_endrepeat        timestamp(14) NOT NULL,
  calendarevent_description      text,
  PRIMARY KEY (calendarevent_id)
);


--
-- Table structure for the table  'EntityEvent'
--
CREATE TABLE EventEntity (
  evententity_timeupdate   timestamp(14),
  evententity_timecreate   timestamp(14),
  evententity_userupdate   int(8) default NULL,
  evententity_usercreate   int(8) default NULL,
  evententity_event_id     int(8) NOT NULL default 0,
  evententity_entity_id    int(8) NOT NULL default 0,
  evententity_entity       varchar(32) NOT NULL default 0,
  evententity_state        char(1) NOT NULL default 0,
  evententity_required     int(1) NOT NULL default 0,
  PRIMARY KEY (evententity_event_id,evententity_entity_id,evententity_entity)
);

--
-- Table structure for table 'CalendarException'
--
CREATE TABLE CalendarException (
  calendarexception_timeupdate  timestamp(14),
  calendarexception_timecreate  timestamp(14),
  calendarexception_userupdate  int(8) default NULL,
  calendarexception_usercreate  int(8) default NULL,
  calendarexception_event_id    int(8) auto_increment,
  calendarexception_date        timestamp(14) NOT NULL,
  PRIMARY KEY (calendarexception_event_id,calendarexception_date)
);

  
--
-- Table structure for table 'CalendarCategory1'
--
CREATE TABLE CalendarCategory1 (
  calendarcategory1_id          int(8) auto_increment,
  calendarcategory1_timeupdate  timestamp(14),
  calendarcategory1_timecreate  timestamp(14),
  calendarcategory1_userupdate  int(8) default NULL,
  calendarcategory1_usercreate  int(8) default NULL,
  calendarcategory1_code        varchar(10) default '',
  calendarcategory1_label       varchar(128) default NULL,
  PRIMARY KEY (calendarcategory1_id)
);

--
-- Table structure for table 'EntityRight'
--
CREATE TABLE EntityRight (
  entityright_entity        varchar(32) NOT NULL default '',
  entityright_entity_id     int(8) NOT NULL default 0,
  entityright_consumer      varchar(32) NOT NULL default '',
  entityright_consumer_id   int(8) NOT NULL default 0,
  entityright_read          int(1) NOT NULL default 0,
  entityright_write         int(1) NOT NULL default 0,
  PRIMARY KEY (entityright_entity, entityright_entity_id, entityright_consumer, entityright_consumer_id),
  INDEX entright_idx_ent_id (entityright_entity_id),
  INDEX entright_idx_ent (entityright_entity),
  INDEX entright_idx_con_id (entityright_consumer_id),
  INDEX entright_idx_con (entityright_consumer)
);


-------------------------------------------------------------------------------
-- Todo
-------------------------------------------------------------------------------
-- Create new table
CREATE TABLE Todo (
  todo_id          int(8) auto_increment,
  todo_timeupdate  timestamp(14),
  todo_timecreate  timestamp(14),
  todo_userupdate  int(8),
  todo_usercreate  int(8),
  todo_user        int(8),
  todo_privacy     int(2) NOT NULL DEFAULT 0,
  todo_date        timestamp(14),
  todo_deadline    timestamp(14),
  todo_dateend     timestamp(14),
  todo_priority    int(8),
  todo_percent     int(8),
  todo_title       varchar(80),
  todo_status      varchar(32),
  todo_webpage     varchar(255),
  todo_content     text,
  PRIMARY KEY (todo_id)
);


-------------------------------------------------------------------------------
-- Publication module tables
-------------------------------------------------------------------------------
--
-- Table structure for table 'Publication'
--
CREATE TABLE Publication (
  publication_id             int(8) auto_increment,
  publication_timeupdate     timestamp(14),
  publication_timecreate     timestamp(14),
  publication_userupdate     int(8),
  publication_usercreate     int(8),
  publication_title          varchar(64) NOT NULL,
  publication_type_id        int(8),
  publication_year           int(4),
  publication_lang           varchar(30),
  publication_desc           text,
  PRIMARY KEY (publication_id)
);

--
-- Table structure for table 'PublicationType'
--
CREATE TABLE PublicationType (
  publicationtype_id          int(8) auto_increment,
  publicationtype_timeupdate  timestamp(14),
  publicationtype_timecreate  timestamp(14),
  publicationtype_userupdate  int(8),
  publicationtype_usercreate  int(8),
  publication_code            varchar(10) default '',
  publicationtype_label       varchar(64),
  PRIMARY KEY (publicationtype_id)
);


--
-- Table structure for table 'Subscription'
--
CREATE TABLE Subscription (
  subscription_id               int(8) auto_increment,
  subscription_publication_id 	int(8) NOT NULL,
  subscription_contact_id       int(8) NOT NULL,
  subscription_timeupdate       timestamp(14),
  subscription_timecreate       timestamp(14),
  subscription_userupdate       int(8),
  subscription_usercreate       int(8),
  subscription_quantity       	int(8),
  subscription_renewal          int(1) DEFAULT 0 NOT NULL,
  subscription_reception_id     int(8) DEFAULT 0 NOT NULL,
  subscription_date_begin       timestamp(14),
  subscription_date_end         timestamp(14),
  PRIMARY KEY (subscription_id)
);


--
-- Table structure for table 'SubscriptionReception'
--
CREATE TABLE SubscriptionReception ( 
  subscriptionreception_id          int(8) auto_increment,
  subscriptionreception_timeupdate  timestamp(14),
  subscriptionreception_timecreate  timestamp(14),
  subscriptionreception_userupdate  int(8),
  subscriptionreception_usercreate  int(8),
  subscriptionreception_code        varchar(10) default '',
  subscriptionreception_label       char(12),
  PRIMARY KEY (subscriptionreception_id)
);	


-------------------------------------------------------------------------------
-- Document module tables
-------------------------------------------------------------------------------
--
-- Table structure for table 'Document'
--
CREATE TABLE Document (
  document_id          	 int(8) auto_increment,
  document_timeupdate  	 timestamp(14),
  document_timecreate  	 timestamp(14),
  document_userupdate  	 int(8) default NULL,
  document_usercreate  	 int(8) default NULL,
  document_title       	 varchar(255) default NULL,
  document_name        	 varchar(255) default NULL,
  document_kind        	 int(2) default NULL,
  document_mimetype_id	 int(8) not null default 0,
  document_category1_id  int(8) not null default 0,
  document_category2_id  int(8) not null default 0,
  document_privacy     	 int(2) not null default 0,
  document_size        	 int(15) default NULL,
  document_author      	 varchar(255) default NULL,
  document_path        	 text default NULL,
  document_acl        	 text default NULL,
  PRIMARY KEY (document_id)
);


--
-- Table structure for table 'DocumentCategory1'
--
CREATE TABLE DocumentCategory1 (
  documentcategory1_id          int(8) auto_increment,
  documentcategory1_timeupdate  timestamp(14),
  documentcategory1_timecreate  timestamp(14),
  documentcategory1_userupdate  int(8) default NULL,
  documentcategory1_usercreate  int(8) default NULL,
  documentcategory1_code        varchar(10) default '',
  documentcategory1_label       varchar(255) default NULL,
  PRIMARY KEY (documentcategory1_id)
);


--
-- Table structure for table 'DocumentCategory2'
--
CREATE TABLE DocumentCategory2 (
  documentcategory2_id          int(8) auto_increment,
  documentcategory2_timeupdate  timestamp(14),
  documentcategory2_timecreate  timestamp(14),
  documentcategory2_userupdate  int(8) default NULL,
  documentcategory2_usercreate  int(8) default NULL,
  documentcategory2_code        varchar(10) default '',
  documentcategory2_label       varchar(255) default NULL,
  PRIMARY KEY (documentcategory2_id)
);


--
-- Table structure for table 'DocumentMimeType'
--
CREATE TABLE DocumentMimeType (
  documentmimetype_id          int(8) auto_increment,
  documentmimetype_timeupdate  timestamp(14),
  documentmimetype_timecreate  timestamp(14),
  documentmimetype_userupdate  int(8) default NULL,
  documentmimetype_usercreate  int(8) default NULL,
  documentmimetype_label       varchar(255) default NULL,
  documentmimetype_extension   varchar(10) default NULL,
  documentmimetype_mime        varchar(255) default NULL,
  PRIMARY KEY (documentmimetype_id)
);


--
-- Table structure for table 'DocumentEntity'
--
CREATE TABLE DocumentEntity (
  documententity_document_id  int(8) NOT NULL,
  documententity_entity_id    int(8) NOT NULL,
  documententity_entity       varchar(255) NOT NULL,
  PRIMARY KEY (documententity_document_id, documententity_entity_id, documententity_entity)
);


-------------------------------------------------------------------------------
-- Project module tables
-------------------------------------------------------------------------------
--
-- Table structure for table 'Project'
--
CREATE TABLE Project (
  project_id             int(8) auto_increment,
  project_timeupdate     timestamp(14),
  project_timecreate     timestamp(14),
  project_userupdate     int(8),
  project_usercreate     int(8),
  project_name           varchar(128),
  project_tasktype_id    int(8),
  project_company_id     int(8),
  project_deal_id        int(8),
  project_soldtime       int(8) DEFAULT NULL,
  project_estimatedtime  int(8) DEFAULT NULL,
  project_datebegin      date,
  project_dateend        date,
  project_archive        char(1) DEFAULT '0',
  project_comment        text,
  PRIMARY KEY (project_id),
  INDEX project_idx_comp (project_company_id),
  INDEX project_idx_deal (project_deal_id)
);


--
-- Table structure for table 'ProjectTask'
--
CREATE TABLE ProjectTask (
  projecttask_id             int(8) auto_increment,
  projecttask_project_id     int(8) NOT NULL,
  projecttask_timeupdate     timestamp(14),
  projecttask_timecreate     timestamp(14),
  projecttask_userupdate     int(8) default NULL,
  projecttask_usercreate     int(8) default NULL,
  projecttask_label          varchar(128) default NULL,
  projecttask_parenttask_id  int(8) default 0,
  projecttask_rank           int(8) default NULL,
  PRIMARY KEY (projecttask_id),
  INDEX pt_idx_pro (projecttask_project_id)
);


--
-- Table structure for table 'ProjectUser'
--
CREATE TABLE ProjectUser (
  projectuser_id              int(8) auto_increment,
  projectuser_project_id      int(8) NOT NULL,
  projectuser_user_id         int(8) NOT NULL,
  projectuser_projecttask_id  int(8),
  projectuser_timeupdate      timestamp(14),
  projectuser_timecreate      timestamp(14),
  projectuser_userupdate      int(8) default NULL,
  projectuser_usercreate      int(8) default NULL,
  projectuser_projectedtime   int(8) default NULL,
  projectuser_missingtime     int(8) default NULL,
  projectuser_validity        timestamp(14),
  projectuser_soldprice       int(8) default NULL,
  projectuser_manager         int(1) default NULL,
  PRIMARY KEY (projectuser_id),
  INDEX pu_idx_pro (projectuser_project_id),
  INDEX pu_idx_user (projectuser_user_id),
  INDEX pu_idx_pt (projectuser_projecttask_id)
);


--
-- Table structure for table 'ProjectStat'
--
CREATE TABLE ProjectStat (
  projectstat_project_id     int(8) NOT NULL,
  projectstat_usercreate     int(8) NOT NULL,
  projectstat_date           timestamp(14) NOT NULL,
  projectstat_useddays       int(8) default NULL,
  projectstat_remainingdays  int(8) default NULL,
  PRIMARY KEY (projectstat_project_id, projectstat_usercreate, projectstat_date)
);


-------------------------------------------------------------------------------
-- Timemanagement tables
-------------------------------------------------------------------------------
--
-- Task table
--
CREATE TABLE TimeTask (
  timetask_id              int(8) auto_increment,
  timetask_timeupdate      timestamp(14),
  timetask_timecreate      timestamp(14),
  timetask_userupdate      int(8) default NULL,
  timetask_usercreate      int(8) default NULL,
  timetask_user_id         int(8) default NULL,
  timetask_date            timestamp(14) NOT NULL,
  timetask_projecttask_id  int(8) default NULL,
  timetask_length          int(2) default NULL,
  timetask_tasktype_id     int(8) default NULL,
  timetask_label           varchar(255) default NULL,
  timetask_status          int(1) default NULL,
  PRIMARY KEY (timetask_id),
  INDEX tt_idx_pt (timetask_projecttask_id)
) TYPE=MyISAM;


--
-- TaskType table
--
CREATE TABLE TaskType (
  tasktype_id          int(8) auto_increment,
  tasktype_timeupdate  timestamp(14),
  tasktype_timecreate  timestamp(14),
  tasktype_userupdate  int(8) default NULL,
  tasktype_usercreate  int(8) default NULL,
  tasktype_internal    int(1) NOT NULL,
  tasktype_label       varchar(32) default NULL,
  PRIMARY KEY (tasktype_id)
) TYPE=MyISAM;


-------------------------------------------------------------------------------
-- Support tables
-------------------------------------------------------------------------------
--
-- New table 'Contract'
--
CREATE TABLE Contract (
  contract_id                int(8) auto_increment,
  contract_timeupdate        timestamp(14),
  contract_timecreate        timestamp(14),
  contract_userupdate        int(8) default NULL,
  contract_usercreate        int(8) default NULL,
  contract_deal_id           int(8) default NULL,
  contract_company_id        int(8) default NULL,
  contract_label             varchar(128) default NULL,
  contract_number            varchar(20) default NULL,
  contract_datesignature     date default NULL ,
  contract_datebegin         date default NULL,
  contract_dateexp           date default NULL,
  contract_daterenew         date default NULL,
  contract_datecancel        date default NULL,
  contract_type_id           int(8) default NULL,
  contract_priority_id       int(8) NOT NULL default 0,
  contract_status_id         int(8) NOT NULL default 0,
  contract_kind              int(2) NULL default 0,
  contract_format            int(2) NULL default 0,
  contract_ticketnumber      int(8) NULL default 0,
  contract_duration          int(8) NULL default 0,
  contract_autorenewal       int(2) NULL default 0,
  contract_contact1_id       int(8) default NULL,
  contract_contact2_id       int(8) default NULL,
  contract_techmanager_id    int(8) default NULL,
  contract_marketmanager_id  int(8) default NULL,
  contract_privacy           int(2) NULL default 0,
  contract_archive           int(1) default 0,
  contract_clause            text,
  contract_comment           text,
  PRIMARY KEY (contract_id)
) TYPE=MyISAM;


--
-- New table 'ContractType'
--
CREATE TABLE ContractType (
  contracttype_id          int(8) auto_increment,
  contracttype_timeupdate  timestamp(14),
  contracttype_timecreate  timestamp(14),
  contracttype_userupdate  int(8) default NULL,
  contracttype_usercreate  int(8) default NULL,
  contracttype_code        varchar(10) default '',
  contracttype_label       varchar(64) default NULL,
  PRIMARY KEY (contracttype_id)
) TYPE=MyISAM;


--
-- New table 'ContractPriority'
--
CREATE TABLE ContractPriority (
  contractpriority_id          int(8) auto_increment,
  contractpriority_timeupdate  timestamp(14),
  contractpriority_timecreate  timestamp(14),
  contractpriority_userupdate  int(8) default NULL,
  contractpriority_usercreate  int(8) default NULL,
  contractpriority_code        varchar(10) default '',
  contractpriority_color       varchar(6) default NULL,
  contractpriority_label       varchar(64) default NULL,
  PRIMARY KEY (contractpriority_id)
);


--
-- New table 'ContractStatus'
--
CREATE TABLE ContractStatus (
  contractstatus_id     	int(8) auto_increment,
  contractstatus_timeupdate  	timestamp(14),
  contractstatus_timecreate  	timestamp(14),
  contractstatus_userupdate  	int(8) default NULL,
  contractstatus_usercreate  	int(8) default NULL,
  contractstatus_code           varchar(10) default '',
  contractstatus_label  	varchar(64) default NULL,
PRIMARY KEY (contractstatus_id)
);


--
-- New table 'Incident'
--
CREATE TABLE Incident (
  incident_id               int(8) auto_increment,
  incident_timeupdate       timestamp(14),
  incident_timecreate       timestamp(14),
  incident_userupdate       int(8) default NULL,
  incident_usercreate       int(8) default NULL,
  incident_contract_id      int(8) NOT NULL,
  incident_label            varchar(100) default NULL,
  incident_date             date default NULL,
  incident_priority_id      int(8) default NULL,
  incident_status_id        int(8) default NULL,
  incident_category1_id     int(8) default 0,
  incident_category2_id     int(8) default 0,
  incident_logger           int(8) default NULL,
  incident_owner            int(8) default NULL,
  incident_duration         char(4) default '0',
  incident_archive          char(1) NOT NULL default '0',
  incident_comment          text, 
  incident_resolution       text,
  PRIMARY KEY (incident_id)
) TYPE=MyISAM;


--
-- New table 'IncidentPriority'
--
CREATE TABLE IncidentPriority (
  incidentpriority_id          int(8) auto_increment,
  incidentpriority_timeupdate  timestamp(14),
  incidentpriority_timecreate  timestamp(14),
  incidentpriority_userupdate  int(8) default NULL,
  incidentpriority_usercreate  int(8) default NULL,
  incidentpriority_code        varchar(10) default '',
  incidentpriority_label       varchar(32) default NULL,
  incidentpriority_color       char(6),
  PRIMARY KEY (incidentpriority_id)
) TYPE=MyISAM;


--
-- New table 'IncidentStatus'
--
CREATE TABLE IncidentStatus (
  incidentstatus_id          int(8) auto_increment,
  incidentstatus_timeupdate  timestamp(14),
  incidentstatus_timecreate  timestamp(14),
  incidentstatus_userupdate  int(8) default NULL,
  incidentstatus_usercreate  int(8) default NULL,
  incidentstatus_code       varchar(10) default '',
  incidentstatus_label       varchar(32) default NULL,
  PRIMARY KEY (incidentstatus_id)
) TYPE=MyISAM;


--
-- New table 'IncidentCategory1'
--
CREATE TABLE IncidentCategory1 (
  incidentcategory1_id          int(8) auto_increment,
  incidentcategory1_timeupdate  timestamp(14),
  incidentcategory1_timecreate  timestamp(14),
  incidentcategory1_userupdate  int(8) default NULL,
  incidentcategory1_usercreate  int(8) default NULL,
  incidentcategory1_code        varchar(10) default '',
  incidentcategory1_label       varchar(32) default NULL,
PRIMARY KEY (incidentcategory1_id)
);


--
-- New table 'IncidentCategory2'
--
CREATE TABLE IncidentCategory2 (
  incidentcategory2_id          int(8) auto_increment,
  incidentcategory2_timeupdate  timestamp(14),
  incidentcategory2_timecreate  timestamp(14),
  incidentcategory2_userupdate  int(8) default NULL,
  incidentcategory2_usercreate  int(8) default NULL,
  incidentcategory2_code        varchar(10) default '',
  incidentcategory2_label       varchar(32) default NULL,
PRIMARY KEY (incidentcategory2_id)
);


-------------------------------------------------------------------------------
-- Accounting Section tables
-------------------------------------------------------------------------------
--
-- New table 'Invoice'
--
CREATE TABLE Invoice ( 
  invoice_id                int(8) auto_increment,
  invoice_timeupdate        timestamp(14),
  invoice_timecreate        timestamp(14),
  invoice_userupdate        int(8),
  invoice_usercreate        int(8),
  invoice_company_id        int(8) NOT NULL,
  invoice_deal_id           int(8) default NULL,
  invoice_project_id        int(8) default NULL,
  invoice_number            varchar(10) DEFAULT '0',
  invoice_label             varchar(40) NOT NULL DEFAULT '',
  invoice_amount_ht         double(10,2),
  invoice_amount_ttc        double(10,2),
  invoice_status_id         int(4) DEFAULT 0 NOT NULL,
  invoice_date              date not NULL DEFAULT '0000-00-00',
  invoice_expiration_date   date,
  invoice_payment_date      date,
  invoice_inout             char(1),
  invoice_archive           char(1) NOT NULL DEFAULT '0',
  invoice_comment           text,
  PRIMARY KEY (invoice_id)
);


--
-- New table 'InvoiceStatus'
--
CREATE TABLE InvoiceStatus (
  invoicestatus_id       int(8) auto_increment,
  invoicestatus_payment  int(1) DEFAULT 0 NOT NULL,
  invoicestatus_created  int(1) DEFAULT 0 NOT NULL,
  invoicestatus_archive  int(1) DEFAULT 0 NOT NULL,
  invoicestatus_label    varchar(24) default '' NOT NULL,
  PRIMARY KEY (invoicestatus_id)
);


--
-- New table 'Payment'
--
CREATE TABLE Payment (
  payment_id              int(8) auto_increment,
  payment_timeupdate      timestamp(14),
  payment_timecreate      timestamp(14),
  payment_userupdate      int(8),
  payment_usercreate      int(8),
  payment_company_id      int(8) NOT NULL,
  payment_account_id      int(8),
  payment_paymentkind_id  int(8) NOT NULL,
  payment_amount          double(10,2) DEFAULT '0.0' NOT NULL,
  payment_date            date,
  payment_inout           char(1) NOT NULL DEFAULT '+',
  payment_number          varchar(24) DEFAULT '',
  payment_checked         char(1) NOT NULL DEFAULT '0',
  payment_comment         text,
  PRIMARY KEY (payment_id)
);


--
-- New table 'PaymentKind'
--
CREATE TABLE PaymentKind (
  paymentkind_id          int(8) auto_increment,
  paymentkind_shortlabel  varchar(3) NOT NULL DEFAULT '',
  paymentkind_label       varchar(40) NOT NULL DEFAULT '',
  PRIMARY KEY (paymentkind_id)
);


--
-- New table 'PaymentInvoice'
--
CREATE TABLE PaymentInvoice (
  paymentinvoice_invoice_id  int(8) NOT NULL,
  paymentinvoice_payment_id  int(8) NOT NULL,
  paymentinvoice_timeupdate  timestamp(14),
  paymentinvoice_timecreate  timestamp(14),
  paymentinvoice_userupdate  int(8),
  paymentinvoice_usercreate  int(8),
  paymentinvoice_amount      double (10,2) NOT NULL DEFAULT '0',
  PRIMARY KEY (paymentinvoice_invoice_id,paymentinvoice_payment_id)
);


--
-- New table 'Account'
--
CREATE TABLE Account (
  account_id	      int(8) auto_increment,
  account_timeupdate  timestamp(14),
  account_timecreate  timestamp(14),
  account_userupdate  int(8),
  account_usercreate  int(8),
  account_bank	      varchar(60) DEFAULT '' NOT NULL,
  account_number      varchar(11) DEFAULT '0' NOT NULL,
  account_balance     double(15,2) DEFAULT '0.00' NOT NULL,
  account_today	      double(15,2) DEFAULT '0.00' NOT NULL,
  account_comment     varchar(100),
  account_label	      varchar(40) NOT NULL DEFAULT '',
  PRIMARY KEY (account_id)
);


-------------------------------------------------------------------------------
-- Group module tables
-------------------------------------------------------------------------------
--
-- Table structure for table 'UGroup' (cause Group is a reserved keyword)
--
CREATE TABLE UGroup (
  group_id          int(8) auto_increment,
  group_timeupdate  timestamp(14),
  group_timecreate  timestamp(14),
  group_userupdate  int(8),
  group_usercreate  int(8),
  group_system      int(1) DEFAULT 0,
  group_privacy     int(2) NULL DEFAULT 0, 
  group_local       int(1) DEFAULT 1,
  group_ext_id      int(8),
  group_name        varchar(32) NOT NULL,
  group_desc        varchar(128),
  group_email       varchar(128),
  PRIMARY KEY (group_id)
);


--
-- Table structure for table 'UserObmGroup'
--
CREATE TABLE UserObmGroup (
  userobmgroup_group_id    int(8) DEFAULT 0 NOT NULL,
  userobmgroup_userobm_id  int(8) DEFAULT 0 NOT NULL,
  PRIMARY KEY (userobmgroup_group_id, userobmgroup_userobm_id)
);


--
-- Table structure for table 'GroupGroup'
--
CREATE TABLE GroupGroup (
  groupgroup_parent_id  int(8) DEFAULT 0 NOT NULL,
  groupgroup_child_id   int(8) DEFAULT 0 NOT NULL,
  PRIMARY KEY (groupgroup_parent_id, groupgroup_child_id)
);


-------------------------------------------------------------------------------
-- Import module tables
-------------------------------------------------------------------------------
--
-- Table structure for table 'Import'
--
CREATE TABLE Import (
  import_id                   int(8) auto_increment,
  import_timeupdate           timestamp(14),
  import_timecreate           timestamp(14),
  import_userupdate           int(8),
  import_usercreate           int(8),
  import_name                 varchar(64) NOT NULL,
  import_datasource_id        int(8) DEFAULT 0,
  import_marketingmanager_id  int(8),
  import_separator            varchar(3),
  import_enclosed             char(1),
  import_desc                 text,
  PRIMARY KEY (import_id),
  UNIQUE (import_name)
);


-------------------------------------------------------------------------------
-- Tables needed for Connectors sync
-------------------------------------------------------------------------------
--
-- Table structure for the table 'DeletedCalendarEvent'
--
CREATE TABLE DeletedCalendarEvent (
  deletedcalendarevent_id         int(8) auto_increment,
  deletedcalendarevent_event_id   int(8),
  deletedcalendarevent_user_id    int(8),
  deletedcalendarevent_timestamp  timestamp(14),
  PRIMARY KEY (deletedcalendarevent_id),
  INDEX idx_dce_event (deletedcalendarevent_event_id),
  INDEX idx_dce_user (deletedcalendarevent_user_id)
);


--
-- Table structure for the table 'DeletedContact'
--
CREATE TABLE DeletedContact (
  deletedcontact_contact_id  int(8),
  deletedcontact_timestamp   timestamp(14),
  PRIMARY KEY (deletedcontact_contact_id)
);


--
-- Table structure for the table 'DeletedUser'
--
CREATE TABLE DeletedUser (
  deleteduser_user_id    int(8),
  deleteduser_timestamp  timestamp(14),
  PRIMARY KEY (deleteduser_user_id)
);


--
-- Table structure for the table 'DeletedTodo'
--
CREATE TABLE DeletedTodo (
  deletedtodo_todo_id    int(8),
  deletedtodo_timestamp  timestamp(14),
  PRIMARY KEY (deletedtodo_todo_id)
);


-------------------------------------------------------------------------------
-- Tables needed for Resource module
-------------------------------------------------------------------------------
--
-- Table structure for table 'Resource'
--
CREATE TABLE Resource (
  resource_id                int(8) auto_increment,
  resource_timeupdate        timestamp(14),
  resource_timecreate        timestamp(14),
  resource_userupdate        int(8),
  resource_usercreate        int(8),
  resource_name              varchar(32) DEFAULT '' NOT NULL,
  resource_description       varchar(255),
  resource_qty               int(8) DEFAULT 0,
  PRIMARY KEY (resource_id),
  UNIQUE k_label_resource (resource_name)
);

--
-- Table structure for table 'RGroup'
--
CREATE TABLE RGroup (
  rgroup_id          int(8) auto_increment,
  rgroup_timeupdate  timestamp(14),
  rgroup_timecreate  timestamp(14),
  rgroup_userupdate  int(8),
  rgroup_usercreate  int(8),
  rgroup_privacy     int(2) NULL DEFAULT 0,
  rgroup_name        varchar(32) NOT NULL,
  rgroup_desc        varchar(128),
  PRIMARY KEY (rgroup_id)
);

--
-- Table structure for table 'ResourceGroup'
--
CREATE TABLE ResourceGroup (
  resourcegroup_rgroup_id    int(8) DEFAULT 0 NOT NULL,
  resourcegroup_resource_id  int(8) DEFAULT 0 NOT NULL
);

