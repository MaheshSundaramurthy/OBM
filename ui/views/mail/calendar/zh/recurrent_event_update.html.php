<?php
/******************************************************************************
Copyright (C) 2011-2012 Linagora

This program is free software: you can redistribute it and/or modify it under
the terms of the GNU Affero General Public License as published by the Free
Software Foundation, either version 3 of the License, or (at your option) any
later version, provided you comply with the Additional Terms applicable for OBM
software by Linagora pursuant to Section 7 of the GNU Affero General Public
License, subsections (b), (c), and (e), pursuant to which you must notably (i)
retain the displaying by the interactive user interfaces of the “OBM, Free
Communication by Linagora” Logo with the “You are using the Open Source and
free version of OBM developed and supported by Linagora. Contribute to OBM R&D
by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
links between OBM and obm.org, between Linagora and linagora.com, as well as
between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
from infringing Linagora intellectual property rights over its trademarks and
commercial brands. Other Additional Terms apply, see
<http://www.linagora.com/licenses/> for more details.

This program is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License and
its applicable Additional Terms for OBM along with this program. If not, see
<http://www.gnu.org/licenses/> for the GNU Affero General   Public License
version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
applicable to the OBM software.
//Recurrent appointment updated
//The appointment <?php echo $title; ?>, initially scheduled from <?php echo $old_startDate; ?> to <?php echo $old_endDate; ?> at <?php echo $old_startTime." - ".$old_endTime ; ?> (location : <?php echo $old_location; ?>),
was updated : 
//Subject
//From
//To
//Time
//Recurrence kind
//Location
//Author
//Created by
//Participants
//Accept
//Decline
//More informations
******************************************************************************/


?>
<table style="width:80%; border:1px solid #000; border-collapse:collapse;background:#EFF0F2;font-size:12px;">
    <tr>
        <th style="text-align:center; background-color: #509CBC; color:#FFF; font-size:14px" colspan="2">
          周期性约会已更新
        </th>
    </tr>
    <tr>
        <td colspan="2">
初步计划从<?php echo $old_startDate; ?>到<?php echo $old_endDate; ?>的<?php echo $old_startTime." - ".$old_endTime ; ?> (地点: <?php echo $old_location; ?>)的约会<?php echo $title; ?>
已被更新: </td>
    </tr>
        <tr>
        <td style="text-align:right;width:20%;padding-right:1em;">主题</td><td style="font-weight:bold;"><?php echo $title; ?></td>
    </tr>
    <tr>
        <td style="text-align:right;padding-right:1em;">从</td><td style="font-weight:bold;"><?php echo $startDate; ?></td>
    </tr>
    <tr>
        <td style="text-align:right;padding-right:1em;">到</td><td style="font-weight:bold;"><?php echo $endDate; ?></td>
    </tr>
    <tr>
        <td style="text-align:right;padding-right:1em;">时间</td><td style="font-weight:bold;"><?php echo $startTime." - ".$endTime ; ?></td>
    </tr>    
    <tr>
        <td style="text-align:right;padding-right:1em;">周期类型</td><td style="font-weight:bold;"><?php echo $repeat_kind; ?></td>
    </tr>
    <tr>
        <td style="text-align:right;padding-right:1em;">地点</td><td style="font-weight:bold;"><?php echo $location; ?></td>
    </tr>
    <tr>
        <td style="text-align:right;padding-right:1em;">组织者</td><td style="font-weight:bold;"><?php echo $organizer; ?></td>
    </tr>
    <tr>
        <td style="text-align:right;padding-right:1em;">创建者</td><td style="font-weight:bold;"><?php echo $creator; ?></td>
    </tr>
    <tr>
        <td style="text-align:right;padding-right:1em;">参与者</td><td style="font-weight:bold;"><?php echo $attendees; ?></td>
    </tr>
    <tr>
        <td style="text-align:right;" colspan="2">
          <a href="<?php echo $host; ?>calendar/calendar_index.php?action=update_decision&calendar_id=<?php echo $id; ?>&entity_kind=user&rd_decision_event=ACCEPTED">接受</a>
          <a href="<?php echo $host; ?>calendar/calendar_index.php?action=update_decision&calendar_id=<?php echo $id; ?>&entity_kind=user&rd_decision_event=DECLINED">拒绝</a>
          <a href="<?php echo $host; ?>calendar/calendar_index.php?action=detailconsult&calendar_id=<?php echo $id; ?>">更多信息</a>
        </td>
    </tr>
</table>
