/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2012  Linagora
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Affero General Public License as 
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version, provided you comply 
 * with the Additional Terms applicable for OBM connector by Linagora 
 * pursuant to Section 7 of the GNU Affero General Public License, 
 * subsections (b), (c), and (e), pursuant to which you must notably (i) retain 
 * the “Message sent thanks to OBM, Free Communication by Linagora” 
 * signature notice appended to any and all outbound messages 
 * (notably e-mail and meeting requests), (ii) retain all hypertext links between 
 * OBM and obm.org, as well as between Linagora and linagora.com, and (iii) refrain 
 * from infringing Linagora intellectual property rights over its trademarks 
 * and commercial brands. Other Additional Terms apply, 
 * see <http://www.linagora.com/licenses/> for more details. 
 *
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License 
 * for more details. 
 *
 * You should have received a copy of the GNU Affero General Public License 
 * and its applicable Additional Terms for OBM along with this program. If not, 
 * see <http://www.gnu.org/licenses/> for the GNU Affero General Public License version 3 
 * and <http://www.linagora.com/licenses/> for the Additional Terms applicable to 
 * OBM connectors. 
 * 
 * ***** END LICENSE BLOCK ***** */
package com.linagora.obm.ui.tests;

import static org.fest.assertions.api.Assertions.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.test.GuiceModule;
import org.obm.test.SlowGuiceRunner;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;

import com.google.inject.Inject;
import com.linagora.obm.ui.bean.UIDomain;
import com.linagora.obm.ui.bean.UIUser;
import com.linagora.obm.ui.ioc.Module;
import com.linagora.obm.ui.page.HomePage;
import com.linagora.obm.ui.page.LoginPage;
import com.linagora.obm.ui.page.PageFactory;

@GuiceModule(Module.class)
@RunWith(SlowGuiceRunner.class)
public class UILoginTest {

	@Inject PageFactory pageFactory;
	
	private WebDriver driver;

	@Before
	public void setUp() {
		this.driver = new FirefoxDriver();
	}

	@After
	public void tearDown() {
		driver.quit();
	}

	@Test
	public void titleContainsOBM() {
		LoginPage loginPage = pageFactory.create(driver, LoginPage.class);
		loginPage.open();
		assertThat(loginPage.currentTitle()).contains("OBM");
	}

	@Test
	public void loginFails() {
		LoginPage loginPage = pageFactory.create(driver, LoginPage.class);
		loginPage.open();
		LoginPage newPage = loginPage.loginAsExpectingError(
				UIUser.builder().login("doNotExist").password("neither").build(),
				UIDomain.globalDomain());

		WebElement deniedLoginForm = newPage.elLoginForm();
		assertThat(deniedLoginForm).isNotNull();

		WebElement errorLegend = newPage.elErrorLegend();
		assertThat(errorLegend).isNotNull();
		assertThat(errorLegend.getText()).isEqualTo("Login ou mot de passe invalide.");
	}

	@Test
	public void login() {
		UIUser uiUser = UIUser.admin0();
		UIDomain uiDomain = UIDomain.globalDomain();

		LoginPage loginPage = pageFactory.create(driver, LoginPage.class);
		loginPage.open();
		HomePage homePage = loginPage.login(uiUser, uiDomain);

		System.out.println(homePage.currentTitle());
		assertThat(homePage.currentTitle()).contains("OBM");
		assertThat(homePage.elInformationUser().getText()).startsWith(uiUser.getLogin()).contains(uiDomain.getName());
		assertThat(homePage.elInformationProfile().getText()).contains(uiUser.getProfile().getUiValue());
	}
}
