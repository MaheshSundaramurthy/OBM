<?php
/******************************************************************************
Copyright (C) 2011-2014 Linagora

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

//Message automatique envoyé par OBM
------------------------------------------------------------------
RENDEZ-VOUS RÉCURRENT ANNULÉ !
------------------------------------------------------------------

Le rendez-vous suivant a été annulé

du              : <?php echo $startDate; ?>

au              : <?php echo $endDate; ?>

heure           : <?php echo $startTime." - ".$endTime ; ?>

récurrence      : <?php echo $repeat_kind; ?>

sujet           : <?php echo $title; ?>

lieu            : <?php echo $location; ?>

organisateur    : <?php echo $organizer; ?>

créé par        : <?php echo $creator; ?>

participant(s)  : <?php echo $attendees; ?>

::NB : Si vous êtes utilisateur du connecteur Thunderbird ou de la synchronisation ActiveSync, vous devez synchroniser pour visualiser cette annulation.
******************************************************************************/


?>
此邮件由OBM自动发送
------------------------------------------------------------------
周期性约会已取消！
------------------------------------------------------------------

以下约会被取消

从            : <?php echo $startDate; ?>

到            : <?php echo $endDate; ?>

时间          : <?php echo $startTime." - ".$endTime ; ?>

周期性        : <?php echo $repeat_kind; ?>

主题          : <?php echo $title; ?>

地点          : <?php echo $location; ?>

组织者        : <?php echo $organizer; ?>

创建者        : <?php echo $creator; ?>

参与者  	  : <?php echo $attendees; ?>

::NB : 若您正在使用Thunderbird扩展或ActiveSync, 您必须同步以查看取消操作。