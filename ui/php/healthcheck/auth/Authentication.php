<?php

/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2011-2012  Linagora
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version, provided you comply with the Additional Terms applicable for OBM
 * software by Linagora pursuant to Section 7 of the GNU Affero General Public
 * License, subsections (b), (c), and (e), pursuant to which you must notably (i)
 * retain the displaying by the interactive user interfaces of the “OBM, Free
 * Communication by Linagora” Logo with the “You are using the Open Source and
 * free version of OBM developed and supported by Linagora. Contribute to OBM R&D
 * by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
 * links between OBM and obm.org, between Linagora and linagora.com, as well as
 * between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
 * from infringing Linagora intellectual property rights over its trademarks and
 * commercial brands. Other Additional Terms apply, see
 * <http://www.linagora.com/licenses/> for more details.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License and
 * its applicable Additional Terms for OBM along with this program. If not, see
 * <http://www.gnu.org/licenses/> for the GNU Affero General   Public License
 * version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
 * applicable to the OBM software.
 * ***** END LICENSE BLOCK ***** */

require_once dirname(__FILE__) . '/Sha1Hasher.php';

class Authentication {

  private static $hasher;

  public static function getHasher() {
    if (!isset(self::$hasher)) {
      self::$hasher = new Sha1Hasher();
    }

    return self::$hasher;
  }

  private static function parseFile() {
    $iniFile = dirname(__FILE__) . "/../../../conf/healthcheck.ini";
    if ( !is_file($iniFile) ) {
      return array();
    }
    return parse_ini_file($iniFile, true);
  }
  
  public static function isConfigured() {
    $ini = self::parseFile();
    
    if (array_key_exists('authentication', $ini)) {
      $auth = $ini['authentication'];
      
      return array_key_exists('login', $auth) && array_key_exists('password', $auth);
    }
  
    return false;
  }
  
  public static function verify() {
    $ini = self::parseFile();
    $auth = $ini['authentication'];
    
    return $_SERVER['PHP_AUTH_USER'] == $auth['login'] && self::getHasher()->hash($_SERVER['PHP_AUTH_PW']) == $auth['password'];
  }

  public static function unauthorized() {
    if (!isset($_SERVER['PHP_AUTH_USER'])) {
      header('WWW-Authenticate: Basic realm="OBM Health Check"');
    }
    
    header('HTTP/1.0 401 Unauthorized');
  }
  
}
