/**
 * Copyright 2012 Leonid Bogdanov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.lbogdanov.poker.web;

import static org.lbogdanov.poker.core.Constants.DEFAULT_ASYNC_TRANSPORT;
import static org.lbogdanov.poker.util.Settings.ASYNC_TRANSPORT;

import java.util.Locale;

import javax.inject.Singleton;

import org.apache.shiro.SecurityUtils;
import org.apache.wicket.ConverterLocator;
import org.apache.wicket.IConverterLocator;
import org.apache.wicket.Page;
import org.apache.wicket.Session;
import org.apache.wicket.atmosphere.EventBus;
import org.apache.wicket.atmosphere.config.AtmosphereTransport;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WebSession;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.settings.IApplicationSettings;
import org.lbogdanov.poker.core.User;
import org.lbogdanov.poker.web.page.*;
import org.lbogdanov.poker.web.page.SessionPage.Subscriber;
import org.lbogdanov.poker.web.util.UserSerializer;

import fiftyfive.wicket.shiro.ShiroWicketPlugin;


/**
 * A subclass of <code>WebApplication</code> which defines Planning Poker web application.
 * 
 * @author Leonid Bogdanov
 */
@Singleton
public class PokerWebApplication extends WebApplication {

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<? extends Page> getHomePage() {
        return IndexPage.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Session newSession(Request request, Response response) {
        return new WebSession(request) {

            @Override
            public void replaceSession() {
                SecurityUtils.getSubject().getSession().stop();
                super.replaceSession();
            }

        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected IConverterLocator newConverterLocator() {
        ConverterLocator locator = (ConverterLocator) super.newConverterLocator();
        locator.set(User.class, UserSerializer.get());
        return locator;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void init() {
        super.init();
        String asyncTransport = ASYNC_TRANSPORT.asString().or(DEFAULT_ASYNC_TRANSPORT);
        EventBus eventBus = new EventBus(this);
        eventBus.getParameters().setTransport(AtmosphereTransport.valueOf(asyncTransport.toUpperCase(Locale.ENGLISH)));
        eventBus.addRegistrationListener(Subscriber.get());
        new ShiroWicketPlugin() {

            @Override
            public void onLoggedOut() {}

            @Override
            public void onLoginRequired() {}

        }.mountLoginPage(null, getHomePage()).install(this);
        if (usesDeploymentConfig()) {
            IApplicationSettings appSettings = getApplicationSettings();
            appSettings.setInternalErrorPage(ErrorPage.class);
            appSettings.setPageExpiredErrorPage(ErrorPage.class);
            // appSettings.setAccessDeniedPage(ErrorPage.class);
            // TODO Atmosphere issue
            // setRootRequestMapper(new CryptoMapper(getRootRequestMapper(), this));
        }
        mountResource("logo.png", new PackageResourceReference(getHomePage(), "images/logo.png"));
        mountPage("/session/${code}", SessionPage.class);
        mountPage("/profile/", ProfilePage.class);
        mountPage("/sessions/", MySessionsPage.class);
    }

}
