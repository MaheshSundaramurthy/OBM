package fr.aliacom.obm.common;

import static org.fest.assertions.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.junit.Test;

public class SQLUtilsTest {

	@Test
		public void testSelectCalendarsConditionNullCollection() {
			Collection<String> emails = null;
	
			assertThat(SQLUtils.selectCalendarsCondition(emails)).isEqualTo("");
		}

	@Test
		public void testSelectCalendarsConditionEmptyCollection() {
			Collection<String> emails = Collections.emptySet();
	
			assertThat(SQLUtils.selectCalendarsCondition(emails)).isEqualTo("");
		}

	@Test
		public void testSelectCalendarsConditionSingleElementCollection() {
			Collection<String> emails = Collections.singleton("test@test.com");
	
			assertThat(SQLUtils.selectCalendarsCondition(emails)).isEqualTo("AND (u.userobm_email LIKE ?) ");
		}
	
	@Test
		public void testSelectCalendarsCondition() {
			Collection<String> emails = new ArrayList<String>();
			
			emails.add("test@test.com");
			emails.add("test2@test.com");
			emails.add("test3@test.com");
	
			assertThat(SQLUtils.selectCalendarsCondition(emails)).isEqualTo("AND (u.userobm_email LIKE ? OR u.userobm_email LIKE ? OR u.userobm_email LIKE ?) ");
		}
	
	@Test
		public void testSelectCalendarsConditionMoreElements() {
			Collection<String> emails = new ArrayList<String>();
			
			emails.add("test@test.com");
			emails.add("test2@test.com");
			emails.add("test3@test.com");
			emails.add("test4@test.com");
			emails.add("test5@test.com");
			emails.add("test6@test.com");
	
			assertThat(SQLUtils.selectCalendarsCondition(emails)).isEqualTo("AND (u.userobm_email LIKE ? OR u.userobm_email LIKE ? OR u.userobm_email LIKE ? OR u.userobm_email LIKE ? OR u.userobm_email LIKE ? OR u.userobm_email LIKE ?) ");
		}

}
