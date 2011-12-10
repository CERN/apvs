/*
 * Copyright 2011 Daniel Kurka
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.googlecode.mgwt.ui.client.widget.tabbar;

import com.googlecode.mgwt.ui.client.MGWTStyle;
import com.googlecode.mgwt.ui.client.theme.base.tabbar.ContactsTabBarButtonCss;

/**
 * <p>ContactsTabBarButton class.</p>
 *
 * @author kurt
 * @version $Id: $
 */
public class ContactsTabBarButton extends TabBarButtonBase {

	/**
	 * <p>Constructor for ContactsTabBarButton.</p>
	 */
	public ContactsTabBarButton() {
		this(MGWTStyle.getTheme().getMGWTClientBundle().getContactsTabBarButtonCss());
	}

	/**
	 * <p>Constructor for ContactsTabBarButton.</p>
	 *
	 * @param contactsTabBarButtonCss a {@link com.googlecode.mgwt.ui.client.theme.base.tabbar.ContactsTabBarButtonCss} object.
	 */
	public ContactsTabBarButton(ContactsTabBarButtonCss contactsTabBarButtonCss) {
		super(contactsTabBarButtonCss);
		addStyleName(contactsTabBarButtonCss.contacts());
		setText("Contacts");
	}

}
