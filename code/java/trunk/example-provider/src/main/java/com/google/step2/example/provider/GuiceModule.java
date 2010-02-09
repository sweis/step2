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

package com.google.step2.example.provider;

import com.google.inject.AbstractModule;
import com.google.inject.CreationException;
import com.google.inject.Scopes;
import com.google.step2.hybrid.HybridOauthMessage;
import com.google.step2.openid.ax2.AxMessage2;

import org.apache.http.client.HttpClient;
import org.openid4java.consumer.ConsumerAssociationStore;
import org.openid4java.consumer.InMemoryConsumerAssociationStore;
import org.openid4java.message.Message;
import org.openid4java.message.MessageException;
import org.openid4java.util.HttpClientFactory;


/**
 *
 * @author Dirk Balfanz (dirk.balfanz@gmail.com)
 * @author Breno de Medeiros (breno.demedeiros@gmail.com)
 */
public class GuiceModule extends AbstractModule {
  @Override
  protected void configure() {

    try {
      Message.addExtensionFactory(AxMessage2.class);
    } catch (MessageException e) {
      throw new CreationException(null);
    }

    try {
      Message.addExtensionFactory(HybridOauthMessage.class);
    } catch (MessageException e) {
      throw new CreationException(null);
    }

    bind(ConsumerAssociationStore.class)
        .to(InMemoryConsumerAssociationStore.class)
        .in(Scopes.SINGLETON);

    bind(HttpClient.class).toInstance(HttpClientFactory.getInstance(0,
        Boolean.FALSE, 10000, 10000, null));
  }
}
