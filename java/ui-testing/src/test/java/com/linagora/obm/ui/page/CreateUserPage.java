/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2014  Linagora
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
package com.linagora.obm.ui.page;

import java.text.SimpleDateFormat;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.google.common.collect.Iterables;
import com.linagora.obm.ui.bean.UIUser;

public class CreateUserPage extends RootPage {
	
	private WebElement userKind;
	private WebElement userFirstname;
	private WebElement userLastname;
	private WebElement userLogin;
	private WebElement createUserSubmit;
	private WebElement userCommonname;
	private WebElement passwd;
	private WebElement cba_hidden;
	private WebElement cba_archive;
	private WebElement sel_profile;
	private WebElement delegationTargetField; 
	private WebElement userTitle; 
	private WebElement userDatebegin; 
	private WebElement noexperie; 
	private WebElement userDateexp; 
	private WebElement userPhone; 
	private WebElement userPhone2; 
	private WebElement userMobile; 
	private WebElement userFax; 
	private WebElement userFax2; 
	private WebElement userCompany; 
	private WebElement userDirection; 
	private WebElement userService; 
	private WebElement userAd1;
	private WebElement userAd2;
	private WebElement userAd3; 
	private WebElement userZip; 
	private WebElement userTown; 
	private WebElement userCdx; 
	private WebElement userDesc;
	@FindBy(id="userMailActive")
	private WebElement userMailActive; 
	private WebElement externalEmailField;
	@FindBy(name="tf_email[]")
	private List<WebElement> internalEmailFields;
	
	public CreateUserPage(WebDriver driver) {
		super(driver);
	}
	
	@Override
	public void open() {
		driver.get(mapping.lookup(CreateUserPage.class).toExternalForm());
	}

	public CreateUserPage createUserAsExpectingError(UIUser userToCreate) {
		doCreateUser(userToCreate);
		return this;
	}
	
	public CreateUserSummaryPage createUser(UIUser userToCreate) {
		doCreateUser(userToCreate);
		return pageFactory.create(driver, CreateUserSummaryPage.class);
	}

	private void doCreateUser(UIUser userToCreate) {
		if (userToCreate.hasKindDefined()) {
			userKind.sendKeys(userToCreate.getKind().getUiFrenchText());
		}
		userLogin.sendKeys(userToCreate.getLogin());
		userFirstname.sendKeys(userToCreate.getFirstName());
		userLastname.sendKeys(userToCreate.getLastName());
		userCommonname.sendKeys(userToCreate.getCommonName());
		passwd.sendKeys(userToCreate.getPassword());
		userTitle.sendKeys(userToCreate.getTitle());
		clickCheckbox(cba_hidden, userToCreate.isMailboxHidden());
		clickCheckbox(cba_archive, userToCreate.isMailboxArchive());
		delegationTargetField.sendKeys(userToCreate.getDelegation());
		clickCheckbox(noexperie, !userToCreate.isNoExpire());
		userPhone.sendKeys(userToCreate.getPhone()); 
		userPhone2.sendKeys(userToCreate.getPhone2());
		userMobile.sendKeys(userToCreate.getPhoneMobile());
		userFax.sendKeys(userToCreate.getPhoneFax()); 
		userFax2.sendKeys(userToCreate.getPhoneFax2());
		userCompany.sendKeys(userToCreate.getCompany()); 
		userDirection.sendKeys(userToCreate.getDirection()); 
		userService.sendKeys(userToCreate.getService());
		userAd1.sendKeys(userToCreate.getAddress1());
		userAd2.sendKeys(userToCreate.getAddress2());
		userAd3.sendKeys(userToCreate.getAddress3());
		userZip.sendKeys(userToCreate.getAddressZip());
		userTown.sendKeys(userToCreate.getAddressTown());
		userCdx.sendKeys(userToCreate.getAddressCedex());
		userDesc.sendKeys(userToCreate.getDescription());

		if (clickCheckbox(userMailActive, userToCreate.isEmailInternalEnabled())) {
			WebElement firstInternalEmail = Iterables.getLast(internalEmailFields);
			firstInternalEmail.sendKeys(userToCreate.getEmailAddress());
		} else {
			externalEmailField.sendKeys(userToCreate.getEmailAddress());
		}

		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
		if (userToCreate.getDateBegin() != null) {
			userDatebegin.sendKeys(sdf.format(userToCreate.getDateBegin()));
		}
		if (userToCreate.getDateExpire() != null) {
			userDateexp.sendKeys(sdf.format(userToCreate.getDateExpire()));
		}
		
		for (WebElement domainOption : sel_profile.findElements(By.tagName("option"))) {
			if (domainOption.getAttribute("value").equals(String.valueOf(userToCreate.getProfile().getUiValue()))) {
				domainOption.click();
			}
		}
		
		createUserSubmit.click();
	}

}
