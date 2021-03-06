/**
 * Copyright 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.google.step2;

import com.google.step2.hybrid.HybridOauthRequest;
import com.google.step2.openid.ax2.FetchRequest2;
import com.google.step2.openid.ax2.ValidateRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openid4java.consumer.ConsumerException;
import org.openid4java.consumer.ConsumerManager;
import org.openid4java.discovery.DiscoveryException;
import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.message.AuthRequest;
import org.openid4java.message.MessageException;

import java.util.List;

/**
 * Class that helps build AuthRequest objects. It's usually used like this:
 *
 * <pre>
 * protected void doPost(HttpServletRequest req, HttpServletResponse resp)
 *     throws ServletException, IOException {
 *
 *   ConsumerHelper consumerHelper = ... // recommendation: inject with Guice
 *   String user_supplied_id = ... // id supplied by the user, or perhaps IdP
 *   String returnToUrl = ... // where should IdP send AuthResponse?
 *
 *   AuthRequestHelper helper =
 *       consumerHelper.getAuthRequestHelper(user_supplied_id, returnToUrl);
 *
 *   try {
 *     AuthRequest authReq = helper
 *           .requestAxAttribute("email", AX_EMAIL_SCHEMA, true)
 *           .generateRequest();
 *
 *     resp.sendRedirect(authReq.getDestinationUrl(true));
 *   } catch (DiscoveryException e) {
 *     ...
 *   } catch (MessageException e) {
 *     ...
 *   } catch (ConsumerException e) {
 *     ...
 *   }
 * }
 * </pre>
 *
 * @author Dirk Balfanz (dirk.balfanz@gmail.com)
 * @author Breno de Medeiros (breno.demedeiros@gmail.com)
 */
public class AuthRequestHelper {
  private static Log log = LogFactory.getLog(AuthRequestHelper.class);

  // For doing associations, and for generating requests that include
  // OpenID identity requests
  private final ConsumerManager consumerManager;

  // Request OAuth scope(s)
  private String oauthScope;

  // The user-supplied identifier
  private final String openId;

  // The URL that the auth result should be delivered to
  private final String returnToUrl;

  // If AX attributes are specified, this object will hold them.
  private FetchRequest2 axFetchRequest = null;

  // If AX validation attributes are specified, this object will hold them.
  private ValidateRequest axValidateRequest = null;

  // If a Hybrid Oauth extension is specified, this object will hold it
  private HybridOauthRequest hybridOauthRequest = null;

  // discovery information
  private DiscoveryInformation discovered = null;

  AuthRequestHelper(ConsumerManager consumerManager, String openId,
      String returnToUrl) {
    log.info("OpenId: " + openId + " ReturnToUrl: " + returnToUrl);
    this.consumerManager = consumerManager;
    this.openId = openId.trim();
    this.returnToUrl = returnToUrl;
  }

  /**
   * Returns discover information obtained by performing discovery on a
   * user-supplied identifier.
   *
   * @throws DiscoveryException if discovery cannot be performed.
   */
  public DiscoveryInformation getDiscoveryInformation()
      throws DiscoveryException {
    // if discovered == null, we'll try again
    if (discovered == null) {
      List<DiscoveryInformation> discoveries = consumerManager.discover(openId);
      discovered = consumerManager.associate(discoveries);
    }

    return discovered;
  }

  public AuthRequestHelper requestOauthAuthorization(String consumerKey,
      String scope) {
    log.info("Requesting OauthAuthorization");
    hybridOauthRequest = new HybridOauthRequest(consumerKey, scope);
    return this;
  }

  /**
   * Adds a request for an Attribute Exchange attribute to the outgoing auth
   * request.
   *
   * @param alias which alias should be used for this AX attribute in the
   *   outgoing auth request.
   * @param typeUri describes which attribute we're actually asking about
   *   (see www.axschema.org)
   * @param required whether or not we consider this attribute "required".
   * @return this very instance.
   */
  public AuthRequestHelper requestAxAttribute(String alias, String typeUri,
      boolean required) {
    log.info("Request AX Attribute Alias: " + alias);
    return requestAxAttribute(alias, typeUri, required, 1);
  }

  /**
   * Adds a request for an Attribute Exchange attribute to the outgoing auth
   * request.
   *
   * @param alias which alias should be used for this AX attribute in the
   *   outgoing auth request.
   * @param typeUri describes which attribute we're actually asking about
   *   (see www.axschema.org)
   * @param required whether or not we consider this attribute "required".
   * @param count how many values for this attribute we would like to receive
   *   in the response.
   * @return this very instance.
   */
  public AuthRequestHelper requestAxAttribute(String alias, String typeUri,
      boolean required, int count) {
    log.info("Request AX Attribute Alias: " + alias);
    if (axFetchRequest == null) {
      axFetchRequest = new FetchRequest2();
    }

    try {
      axFetchRequest.addAttribute(alias, typeUri, required, count);
    } catch (MessageException e) {
      log.warn("Unable to add attribute to AX fetch request.");
      axFetchRequest = null;
    }
    return this;
  }
  
  /**
   * Adds a request to validate an Attribute Exchange attribute to the outoing
   * auth reqeust.
   *  
   * @param alias wWhich alias should be used for this AX attribute in the
   *   outgoing auth request.
   * @param typeUri Describes which attribute we're actually asking about
   *   (see www.axschema.org)
   * @param value The value to validate
   * @return This instance
   */
  public AuthRequestHelper validateAxAttribute(String alias, String typeUri,
      String value) {
    log.info("Validate AX Attribute Alias: " + alias);
    if (axValidateRequest == null) {
      axValidateRequest = new ValidateRequest();
    }
    
    try {
      axValidateRequest.addAttribute(alias, typeUri, value);
    } catch (MessageException e) {
      log.warn("Unable to add attribute to AX fetch request.");
      axFetchRequest = null;
    }
    return this;
  }

  /**
   * Generates a new auth request, which can be queried for a redirect-URL
   * to send the user agent to (and which will point to he OP).
   */
  public AuthRequest generateRequest() throws DiscoveryException,
      MessageException, ConsumerException {
    DiscoveryInformation discovered = getDiscoveryInformation();

    // this a standard OpenID request
    AuthRequest authReq =
      consumerManager.authenticate(discovered, returnToUrl, null);
    if (axFetchRequest != null) {
      authReq.addExtension(axFetchRequest);
    }

    if (hybridOauthRequest != null) {
      authReq.addExtension(hybridOauthRequest);
    }

    if (axValidateRequest != null) {
      authReq.addExtension(axValidateRequest);
    }

    log.info(authReq);
    return authReq;
  }
}
