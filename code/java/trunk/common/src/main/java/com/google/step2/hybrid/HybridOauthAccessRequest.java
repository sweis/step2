/**
 * Copyright 2008 Google Inc.
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
 * 
 */

package com.google.step2.hybrid;

import org.openid4java.message.ParameterList;

import java.util.Arrays;
import java.util.List;

/**
 * Hybrid OAuth Message Extension for Access Requests
 * 
 * @author steveweis@google.com (Steve Weis)
 */
public class HybridOauthAccessRequest extends HybridOauthMessage {
  
  protected final static List<String> requiredFields = 
    Arrays.asList(new String[] {ACCESS_TOKEN, ACCESS_TOKEN_SECRET});
  
  protected final static List<String> optionalFields = 
    Arrays.asList(new String[0]);
  
  public HybridOauthAccessRequest(ParameterList parameters) {
    this.parameters = parameters;
  }
  
  boolean isValid() {
    return isValid(requiredFields, optionalFields);
  }
}
