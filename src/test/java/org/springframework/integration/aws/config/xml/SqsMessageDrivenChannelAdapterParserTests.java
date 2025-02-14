/*
 * Copyright 2016-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.integration.aws.config.xml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.willThrow;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.TaskExecutor;
import org.springframework.integration.aws.inbound.SqsMessageDrivenChannelAdapter;
import org.springframework.integration.channel.NullChannel;
import org.springframework.integration.test.util.TestUtils;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.core.DestinationResolutionException;
import org.springframework.messaging.core.DestinationResolver;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.amazonaws.services.sqs.AmazonSQS;
import io.awspring.cloud.core.env.ResourceIdResolver;
import io.awspring.cloud.messaging.listener.SimpleMessageListenerContainer;
import io.awspring.cloud.messaging.listener.SqsMessageDeletionPolicy;

/**
 * @author Artem Bilan
 * @author Patrick Fitzsimons
 */
@SpringJUnitConfig
@DirtiesContext
public class SqsMessageDrivenChannelAdapterParserTests {

	@Autowired
	private AmazonSQS amazonSqs;

	@Autowired
	private ResourceIdResolver resourceIdResolver;

	@Autowired
	private DestinationResolver<?> destinationResolver;

	@Autowired
	private TaskExecutor taskExecutor;

	@Autowired
	private MessageChannel errorChannel;

	@Autowired
	private NullChannel nullChannel;

	@Autowired
	private SqsMessageDrivenChannelAdapter sqsMessageDrivenChannelAdapter;

	@Bean
	DestinationResolver<?> destinationResolver() {
		DestinationResolver<?> destinationResolver = Mockito.mock(DestinationResolver.class);
		willThrow(DestinationResolutionException.class).given(destinationResolver).resolveDestination(anyString());
		return destinationResolver;
	}

	@Test
	void testSqsMessageDrivenChannelAdapterParser() {
		SimpleMessageListenerContainer listenerContainer = TestUtils.getPropertyValue(
				this.sqsMessageDrivenChannelAdapter, "listenerContainer", SimpleMessageListenerContainer.class);
		assertThat(TestUtils.getPropertyValue(listenerContainer, "amazonSqs")).isSameAs(this.amazonSqs);
		assertThat(TestUtils.getPropertyValue(listenerContainer, "resourceIdResolver"))
				.isSameAs(this.resourceIdResolver);
		assertThat(TestUtils.getPropertyValue(listenerContainer, "taskExecutor")).isSameAs(this.taskExecutor);
		assertThat(TestUtils.getPropertyValue(listenerContainer, "destinationResolver"))
				.isSameAs(this.destinationResolver);
		assertThat(listenerContainer.isRunning()).isFalse();
		assertThat(TestUtils.getPropertyValue(listenerContainer, "maxNumberOfMessages")).isEqualTo(5);
		assertThat(TestUtils.getPropertyValue(listenerContainer, "visibilityTimeout")).isEqualTo(200);
		assertThat(TestUtils.getPropertyValue(listenerContainer, "waitTimeOut")).isEqualTo(40);
		assertThat(TestUtils.getPropertyValue(listenerContainer, "queueStopTimeout")).isEqualTo(11000L);
		assertThat(TestUtils.getPropertyValue(listenerContainer, "autoStartup")).isEqualTo(false);

		assertThat(this.sqsMessageDrivenChannelAdapter.getPhase()).isEqualTo(100);
		assertThat(this.sqsMessageDrivenChannelAdapter.isAutoStartup()).isFalse();
		assertThat(this.sqsMessageDrivenChannelAdapter.isRunning()).isFalse();
		assertThat(TestUtils.getPropertyValue(this.sqsMessageDrivenChannelAdapter, "outputChannel"))
				.isSameAs(this.errorChannel);
		assertThat(TestUtils.getPropertyValue(this.sqsMessageDrivenChannelAdapter, "errorChannel"))
				.isSameAs(this.nullChannel);
		assertThat(TestUtils.getPropertyValue(this.sqsMessageDrivenChannelAdapter, "messagingTemplate.sendTimeout"))
				.isEqualTo(2000L);
		assertThat(TestUtils.getPropertyValue(this.sqsMessageDrivenChannelAdapter, "messageDeletionPolicy",
				SqsMessageDeletionPolicy.class)).isEqualTo(SqsMessageDeletionPolicy.NEVER);
	}

}
