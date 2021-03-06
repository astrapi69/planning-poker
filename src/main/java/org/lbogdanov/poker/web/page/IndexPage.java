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
package org.lbogdanov.poker.web.page;

import static org.apache.wicket.AttributeModifier.append;
import static org.apache.wicket.validation.validator.StringValidator.maximumLength;
import static org.lbogdanov.poker.core.Constants.SESSION_CODE_MAX_LENGTH;
import static org.lbogdanov.poker.core.Constants.SESSION_DESCRIPTION_MAX_LENGTH;
import static org.lbogdanov.poker.core.Constants.SESSION_ESTIMATES_MAX_LENGTH;
import static org.lbogdanov.poker.core.Constants.SESSION_NAME_MAX_LENGTH;

import javax.inject.Inject;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxFallbackButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.io.IClusterable;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;
import org.lbogdanov.poker.core.Duration;
import org.lbogdanov.poker.core.SessionService;
import org.lbogdanov.poker.core.UserService;
import org.lbogdanov.poker.web.markup.BootstrapFeedbackPanel;
import org.lbogdanov.poker.web.markup.ControlGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Represents an index page.
 * 
 * @author Leonid Bogdanov
 */
public class IndexPage extends AbstractPage {

    /**
     * Simple POJO for internal login form.
     */
    private static final class Credentials implements IClusterable {

        private String username;
        private String password;
        private boolean rememberme;

    }

    /**
     * Simple POJO for session create and join forms.
     */
    private static final class Game implements IClusterable {

        private String code;
        private String name;
        private String description;
        private String estimates;

    }

    private static final Logger LOGGER = LoggerFactory.getLogger(IndexPage.class);
    private static final ResourceReference JS = new PageScriptResourceReference(IndexPage.class, "index.js");

    @Inject
    private SessionService sessionService;
    @Inject
    private UserService userService;

    /**
     * Creates a new instance of <code>Index</code> page.
     */
    @SuppressWarnings("unchecked")
    public IndexPage() {
        WebMarkupContainer session = new WebMarkupContainer("session");
        WebMarkupContainer login = new WebMarkupContainer("login") {

            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(userService.getCurrentUser() == null);
            }

        };

        Form<?> internal = new StatelessForm<Credentials>("internal", new CompoundPropertyModel<Credentials>(new Credentials()));
        MarkupContainer usernameGroup = new ControlGroup("usernameGroup");
        MarkupContainer passwordGroup = new ControlGroup("passwordGroup");
        MarkupContainer rememberGroup = new ControlGroup("rememberGroup");
        internal.add(new BootstrapFeedbackPanel("feedback"),
                     usernameGroup.add(new RequiredTextField<String>("username").setLabel(new ResourceModel("login.internal.username"))),
                     passwordGroup.add(new PasswordTextField("password").setLabel(new ResourceModel("login.internal.password"))),
                     rememberGroup.add(new CheckBox("rememberme"), new AjaxFallbackButton("submit", internal) {

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                Credentials credentials = (Credentials) form.getModelObject();
                // TODO Atmosphere issue
                // getSession().replaceSession();
                try {
                    userService.login(credentials.username, credentials.password, credentials.rememberme);
                    if (target != null) {
                        target.appendJavaScript("$('#crsl').carousel({interval: false}).carousel('next');");
                        target.add(getNavBar());
                    }
                } catch (RuntimeException re) {
                    LOGGER.info("Authentication error", re);
                    form.error(IndexPage.this.getString("login.internal.authError"));
                    if (target != null) {
                        target.add(form);
                    }
                }
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                if (target != null) {
                    target.add(form);
                }
            }

        }));

        IModel<Game> gameModel = new CompoundPropertyModel<Game>(new Game());
        IValidator<String> codeValidator = new IValidator<String>() {

            @Override
            public void validate(IValidatable<String> validatable) {
                String code = validatable.getValue();
                if (!sessionService.exists(code)) {
                    ValidationError error = new ValidationError();
                    error.addKey("session.join.invalidCode").setVariable("code", code);
                    validatable.error(error);
                }
            }

        };
        Form<?> join = new Form<Game>("join", gameModel);
        MarkupContainer codeGroup = new ControlGroup("codeGroup").add(new AjaxFallbackButton("submit", join) {

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                Game game = (Game) form.getModelObject();
                setResponsePage(SessionPage.class, new PageParameters().add("code", game.code));
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                if (target != null) {
                    target.add(form);
                }
            }

        });
        join.add(new BootstrapFeedbackPanel("feedback"),
                 codeGroup.add(new RequiredTextField<String>("code").setLabel(new ResourceModel("session.join.code"))
                                                                    .add(maximumLength(SESSION_CODE_MAX_LENGTH), codeValidator)));

        IValidator<String> estimatesValidator = new IValidator<String>() {

            @Override
            public void validate(IValidatable<String> validatable) {
                try {
                    Duration.parse(validatable.getValue());
                } catch (IllegalArgumentException e) {
                    ValidationError error = new ValidationError();
                    error.addKey("session.create.estimates.invalidEstimate").setVariable("estimate", e.getMessage());
                    validatable.error(error);
                }
            }

        };
        Form<?> create = new Form<Game>("create", gameModel);
        MarkupContainer nameGroup = new ControlGroup("nameGroup");
        MarkupContainer estimatesGroup = new ControlGroup("estimatesGroup");
        MarkupContainer descriptionGroup = new ControlGroup("descriptionGroup");
        create.add(new BootstrapFeedbackPanel("feedback"),
                   nameGroup.add(new RequiredTextField<String>("name").setLabel(new ResourceModel("session.create.name"))
                                                                      .add(maximumLength(SESSION_NAME_MAX_LENGTH))),
                   estimatesGroup.add(new RequiredTextField<String>("estimates").setLabel(new ResourceModel("session.create.estimates"))
                                                                                .add(maximumLength(SESSION_ESTIMATES_MAX_LENGTH), estimatesValidator)),
                   descriptionGroup.add(new TextArea<String>("description").setLabel(new ResourceModel("session.create.description"))
                                                                           .add(maximumLength(SESSION_DESCRIPTION_MAX_LENGTH))),
                   new AjaxFallbackButton("submit", create) {

            @Override
            public void onSubmit(AjaxRequestTarget target, Form<?> form) {
                Game game = (Game) form.getModelObject();
                String code = sessionService.create(game.name, game.description, game.estimates).getCode();
                setResponsePage(SessionPage.class, new PageParameters().add("code", code));
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                if (target != null) {
                    target.add(form);
                }
            }

        });

        login.add(internal.setOutputMarkupId(true));
        session.add(join.setOutputMarkupId(true), create.setOutputMarkupId(true));
        session.add(append("class", new AbstractReadOnlyModel<String>() {

            @Override
            public String getObject() {
                return userService.getCurrentUser() != null ? "active" : null;
            }

        }));
        add(login, session);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ResourceReference getJavaScript() {
        return JS;
    }

}
