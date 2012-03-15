/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.nju.artemis.aejb.client;

import java.lang.reflect.InvocationTargetException;

import javax.ejb.Remote;
import javax.ejb.Singleton;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * @author <a href="wangjue1199@gmail.com">Jason</a>
 */
@Singleton
@Remote(AEjbClient.class)
public class AEjbClientBean implements AEjbClient {

	static Object client;
	
	private void getOrCreateAEjbClient() {
		if (client == null)
			try {
				client = new InitialContext().lookup("java:global/aejb/client");
			} catch (NamingException e) {
				e.printStackTrace();
			}
	}

	@Override
	public Object getAEjbClient() {
		getOrCreateAEjbClient();
		return client;
	}

	@Override
	public boolean blockAEjb(String aejbName) {
		getOrCreateAEjbClient();
		try {
			return (Boolean) client.getClass().getDeclaredMethod("blockAEjb", String.class).invoke(client, aejbName);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean resumeAEjb(String aejbName) {
		getOrCreateAEjbClient();
		try {
			return (Boolean) client.getClass().getDeclaredMethod("resumeAEjb", String.class).invoke(client, aejbName);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		return false;
	}
}
