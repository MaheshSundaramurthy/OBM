/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2011-2013  Linagora
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
package fr.aliacom.obm.common.profile;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.filter.SlowFilterRunner;
import org.obm.sync.Right;

import com.google.common.collect.ImmutableMap;
@RunWith(SlowFilterRunner.class)
public class ModuleCheckBoxStatesTest {

	@Test
	public void testBuild() {
		ModuleCheckBoxStates states = ModuleCheckBoxStates
				.builder()
				.module(Module.MAILBOX)
				.checkBoxState(Right.ACCESS, CheckBoxState.CHECKED)
				.build();

		assertThat(states.getModule()).isEqualTo(Module.MAILBOX);
		assertThat(states.getCheckBoxStates()).isEqualTo(ImmutableMap.of(Right.ACCESS, CheckBoxState.CHECKED));
	}

	@Test(expected = IllegalStateException.class)
	public void testBuildWhenNoModule() {
		ModuleCheckBoxStates
				.builder()
				.checkBoxState(Right.ACCESS, CheckBoxState.UNCHECKED)
				.build();
	}

	@Test
	public void testGetCheckBoxState() {
		ModuleCheckBoxStates states = ModuleCheckBoxStates
				.builder()
				.module(Module.MAILBOX)
				.checkBoxState(Right.ACCESS, CheckBoxState.CHECKED)
				.build();

		assertThat(states.getCheckBoxState(Right.ACCESS)).isEqualTo(CheckBoxState.CHECKED);
	}

	@Test
	public void testGetCheckBoxStateWhenNoValue() {
		ModuleCheckBoxStates states = ModuleCheckBoxStates
				.builder()
				.module(Module.MAILBOX)
				.checkBoxState(Right.ACCESS, CheckBoxState.CHECKED)
				.build();

		assertThat(states.getCheckBoxState(Right.READ)).isNull();
	}

	@Test
	public void testGetCheckBoxStateWhenNoStatesAtAll() {
		ModuleCheckBoxStates states = ModuleCheckBoxStates
				.builder()
				.module(Module.MAILBOX)
				.build();

		assertThat(states.getCheckBoxState(Right.READ)).isNull();
	}

}