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
package org.lbogdanov.poker.web.oauth;

import javax.inject.Inject;
import javax.inject.Named;

import org.scribe.up.provider.OAuthProvider;

import io.buji.oauth.OAuthFilter;


/**
 * A subclass of <code>InjectableOAuthFilter</code> merely to add injection support.
 * 
 * @author Leonid Bogdanov
 */
public class InjectableOAuthFilter extends OAuthFilter {

    public static final String FAILURE_URL_PARAM = "failureURL";

    /**
     * {@inheritDoc}
     */
    @Override @Inject
    public void setFailureUrl(@Named(FAILURE_URL_PARAM) String failureUrl) {
        super.setFailureUrl(failureUrl);
    }

    /**
     * {@inheritDoc}
     */
    @Override @Inject
    public void setProvider(OAuthProvider provider) {
        super.setProvider(provider);
    }

}
