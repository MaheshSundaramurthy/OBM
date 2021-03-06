<?php
include_once(dirname(__FILE__) . '/obmSyncAuth.php');
include_once(dirname(__FILE__) . '/abstractAuth.class.php');

class doLoginAuth extends abstractAuth {

  const         LOGIN_PATH              = "/obm-sync/services/login/doLogin";

  protected function getLoginUrl($requester){
    $method = self::LOGIN_PATH;
    $origin = self::$origin;

    if($this->authKind == "standalone"){
      if(!$this->login){
        throw new Exception("For a standalone authentication, you must set the login and password in obmSyncRequester", 500);
      }
    }

    if ( $this->login ) {
      $login = $this->login;
    }

    if($this->authKind != "LemonLDAP" && $this->password ){
      $password = $this->password;
    }

    return $requester->getRootPath() . $method . $this->formatLoginParams($origin, $login, $password);
  }

}