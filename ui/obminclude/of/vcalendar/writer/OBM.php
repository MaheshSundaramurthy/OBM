<?php

include_once('obminclude/of/Vcalendar.php');

class Vcalendar_Writer_OBM {

  var $db;

  var $lazyRead;

  var $lazyWrite;

  var $ids;

  var $frequency = array("daily","weekly","monthly","yearly");

  var $repeat = array("byday","bymonthday","byyearday","byweekno","bymonth","bysetpos","wkst");

  var $rights;

  function Vcalendar_Writer_OBM() {
    $this->db = new DB_OBM;
    $this->lazyRead = true;
    $rights = of_right_entity_for_user('calendar', $GLOBALS['obm']['uid'], 'write', '', 'userobm');
    $this->rights = $rights['ids'];
  }

  function writeDocument(&$document) {
    $vevents = &$document->getVevents();
    for($i=0; $i < count($vevents); $i++ ) {
      $this->writeVevent($vevents[$i]);
    }
    $valarms = &$document->getValarms();
    for($i=0; $i < count($valarms); $i++) {
      $this->writeValarm($valarms[$i]);
    }

  }

  function writeVevent(&$vevent) {
    $eventData = $this->getOBMEvent($vevent);

    if(is_null($eventData)) {
      $this->insertEvent($vevent);

    } else {

      if($this->haveAccess($eventData->f('calendarevent_owner'))) {
        $this->updateEvent($eventData->f('calendarevent_id'), $vevent);
      } else {
        // FIXME WHAT TO DO??
        $this->updateAttendees($eventData->f('calendarevent_id'),$vevent);
      }
    }
  }

  function & getEventByData(&$vevent) {
    $eventData = NULL;
    if($this->lazyRead) {
      if(($organizer = $vevent->get('organizer'))) {
        $owner = "OR calendarevent_owner = '".$organizer."'";
      }
      $query = "SELECT calendarevent_id as id
      FROM CalendarEvent WHERE calendarevent_title =  '".addslashes($vevent->get('summary'))."'
      AND calendarevent_date = '".$vevent->get('dtstart')."' AND
      (calendarevent_owner = ".$GLOBALS['obm']['uid']." $owner)";
      $this->db->query($query);
      if($this->db->nf() > 0) {
        $this->db->next_record();
        $eventData = $this->getEventById($this->db->f('id'));
      }
    } else {
      //TODO Hard working query.
    }
    return $eventData;
  }

  function getEventById($id) {
    $eventData = run_query_calendar_detail($id);
    if($eventData->nf() == 0) {
      return null;
    }
    return $eventData;    
  }

  function parseEventData(&$vevent) {
    $entities = array();
    $states = array();
    $attendees = $vevent->get('attendee');
    if(!is_null($attendees)) {
      if(is_array($attendees) && !array_key_exists('entity',$attendees)) {
        foreach($attendees as $attendee) {
          $entities[$attendee['entity']][] = $attendee['id'];
          $states[$attendee['entity']][$attendee['id']] = $attendee['state'];
        }
      } elseif(!is_null($attendees)) {
        $entities[$attendees['entity']][] = $attendees['id'];
        $states[$attendees['entity']][$attendees['id']] = $attendees['state'];
      }
    }
    $entities['user'][] = $GLOBALS['obm']['uid'];
    $entities['user'] = array_unique($entities['user']);
    $event['date_exception'] = $vevent->get('exdate');
    $event['owner'] = $this->parseOrganizer($vevent->get('organizer'));
    $event['title'] = addslashes($vevent->get('summary'));
    $event['date_begin'] = $vevent->get('dtstart');
    $event['date_end'] = $vevent->get('dtend');
    $event['event_duration'] = $vevent->get('duration');
    $event['duration'] = $vevent->get('duration');
    $event['description'] = addslashes($vevent->get('description'));
    $event['location'] = addslashes($vevent->get('location'));
    $event['category1'] = $this->parseCategories($vevent->get('categories'));
    $event['priority'] = $this->parsePriority($vevent->get('priority'));
    $event['privacy'] = $this->parsePrivacy($vevent->get('class')) ;
    $event = array_merge($event, $this->parseRrule($vevent->get('rrule'), $vevent));
    $event['all_day'] = $vevent->isAllDay();
    $event['color'] = $vevent->get('x-obm-color');
    $event['properties'] = $vevent->get('x-obm-properties');
    return array('event' => $event, 'entities' => $entities, 'states' => $states);
  }

  function parseAttendee() {
    
  }
  
  function insertEvent(&$vevent) {
    $data = $this->parseEventData($vevent);
    $data['event']['owner'] = $GLOBALS['obm']['uid'];
    run_query_calendar_add_event($data['event'], $data['entities'], $id);
    $this->updateStates($data['states'], $id);
  }

  function updateStates($states, $id) {
    foreach($states as $entity => $stateInfo) {
      foreach($stateInfo as $entityId => $state) {
        if(!is_null($state)) {
          run_query_calendar_update_occurrence_state($id,$entity,$entityId,$state);
        }
      }
    }
  }

  function updateAttendees($id, &$vevent) {
    $data = $this->parseEventData($vevent);
    $this->updateStates($data['states'], $id);
  }
  
  function updateEvent($id, &$vevent) {

    $data = $this->parseEventData($vevent);
    if(!$this->lazyWrite) {
      //TODO : Hard working update.
    }
    $data['event']['calendar_id'] = $id;
    run_query_calendar_event_update($data['event'], $data['entities'], $id, true);
    $this->updateStates($data['states'], $id);
  }

  function & getOBMEvent(&$vevent) {
    $eventData = NULL;
    if(($id = $this->getOBMId($vevent->get('uid')))) {
      $eventData = $this->getEventById($id);
    }
    if(is_null($eventData)) {
      $eventData = $this->getEventByData($vevent);
    }
    return $eventData;
  }

  function getOBMId($id) {
    if(is_null($id)) {
      return NULL;
    }
    //FIXME : Have a unique id over all installed obm in the world
    if(preg_match('/^obm@([0-9]+)$/',$id,$match)) {
      return $match[1];
    }
    return NULL;
  }

  function addAttendee($id, &$vevent) {
    echo "AJOUT DES PARTICIPANTS => $id";
  }

  function parseCategories($categories) {
    if(is_null($categories)) {
      return NULL;
    }
    $name = addslashes(array_shift($categories));
    $query = "SELECT calendarcategory1_id as id FROM CalendarCategory1 WHERE
                     calendarcategory1_label = '$name' AND 
                     calendarcategory1_domain_id = ".$GLOBALS['obm']['domain_id'];
    $this->db->query($query);
    if($this->db->next_record()) {
      return $this->db->f('id');
    }
    return NULL;
  }

  function parsePrivacy($value) {
    if(strtolower($value) == 'private') {
      return 1;
    }
    return 0;
  }

  function parseOrganizer($organizer) {
    if($this->haveAccess($organizer)) {
      return $organizer;
    } else {
      return $GLOBALS['obm']['uid'];
    }

  }

  function parsePriority($value) {
    if($value > 5 ) {
      return 1;
    } elseif ($value < 5) {
      return 3;
    }
    return 2;
  }

  function & parseRrule($rrule, &$vevent) {
    $event = array();
    if(!isset($rrule['kind']) || is_null($rrule)||!in_array($rrule['kind'],$this->frequency) ) {
      //FIXME Error Handler.
      $event['repeat_kind'] = 'none';
      $event['repeatfrequency'] =1;
      return $event;
    }
    $event['repeatfrequency'] = $rrule['interval'];
    $countFactor = $rrule['interval'];
    $event['repeat_kind'] = $rrule['kind'];
    switch ($rrule['kind'])  {
      case 'daily' :
        $countUnit = 'day';
        break;
      case 'yearly' :
        $countUnit = 'year';
        break;
      case 'weekly' :
        $countUnit = 'week';
        $days = '0000000';
        if(!is_null($rrule['byday'])) {
          $countFactor = $countFactor / count($rrule['byday']);
          foreach($rrule['byday'] as $day) {
            $index = date('w', strtotime($day)) - date('w', strtotime($GLOBALS['ccalendar_weekstart']));
            $days[$index] = '1';
          }
        } else {
          $days[0] = '1';
        }
        $event['repeat_days'] = $days;
        break;
      case 'monthly' :
        $countUnit = 'month';
        if(!is_null($rrule['byday'])) {
          $event['repeat_kind'] = 'monthlybyday';
        } else {
          $event['repeat_kind'] = 'monthlybydate';
        }
        break;
    }
    if(!is_null($rrule['until'])) {
      $event['repeat_end'] = $rrule['until'];
    }elseif(!is_null($rrule['count'])) {
      $countFactor = ceil($countFactor * $rrule['count']);
      $event['repeat_end'] = strtotime("+$countFactor $countUnit", strtotime($vevent->get('dtstart')));
    }else {
      $event['repeat_end'] = NULL;
    }
    return $event;
  }

  function haveAccess($organizer) {
    if(in_array($organizer,$this->rights)) {
      return true;
    }
    return false;
  }
   
  function writeValarm(&$valarm) {

  }

}

?>
