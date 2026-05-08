/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ai.hub.internal.model;

import com.google.api.gax.grpc.InstantiatingGrpcChannelProvider;
import com.google.api.gax.rpc.FixedHeaderProvider;
import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.GenerateContentRequest;
import com.google.cloud.vertexai.api.GenerationConfig;
import com.google.cloud.vertexai.api.PredictionServiceClient;
import com.google.cloud.vertexai.api.PredictionServiceSettings;
import com.google.cloud.vertexai.generativeai.GenerativeModel;

import com.liferay.ai.hub.internal.configuration.VertexAIConfiguration;
import com.liferay.portal.configuration.module.configuration.ConfigurationProviderUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.module.configuration.ConfigurationException;
import com.liferay.portal.kernel.util.HashMapBuilder;

import dev.langchain4j.model.vertexai.gemini.VertexAiGeminiChatModel;
import dev.langchain4j.model.vertexai.gemini.VertexAiGeminiStreamingChatModel;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall;
import io.grpc.MethodDescriptor;

import java.io.IOException;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * @author Feliphe Marinho
 * @author Iliyan Peychev
 */
public class VertexAiGeminiUtil {

	public static VertexAiGeminiChatModel createVertexAiGeminiChatModel(
			long companyId, long userId)
		throws ConfigurationException {

		VertexAIConfiguration vertexAIConfiguration =
			ConfigurationProviderUtil.getCompanyConfiguration(
				VertexAIConfiguration.class, companyId);

		VertexAI vertexAI = _createVertexAI(
			_createLabels(companyId, userId), vertexAIConfiguration);

		GenerativeModel generativeModel = new GenerativeModel(
			vertexAIConfiguration.modelName(), vertexAI);

		return new VertexAiGeminiChatModel(
			generativeModel, GenerationConfig.getDefaultInstance()) {

			@Override
			public void close() {
				try {
					super.close();
				}
				finally {
					vertexAI.close();
				}
			}

		};
	}

	public static VertexAiGeminiStreamingChatModel
			createVertexAiGeminiStreamingChatModel(long companyId, long userId)
		throws ConfigurationException {

		VertexAIConfiguration vertexAIConfiguration =
			ConfigurationProviderUtil.getCompanyConfiguration(
				VertexAIConfiguration.class, companyId);

		VertexAI vertexAI = _createVertexAI(
			_createLabels(companyId, userId), vertexAIConfiguration);

		GenerativeModel generativeModel = new GenerativeModel(
			vertexAIConfiguration.modelName(), vertexAI);

		return new VertexAiGeminiStreamingChatModel(
			generativeModel, GenerationConfig.getDefaultInstance()) {

			@Override
			public void close() {
				try {
					super.close();
				}
				finally {
					vertexAI.close();
				}
			}

		};
	}

	private static Map<String, String> _createLabels(
		long companyId, long userId) {

		return HashMapBuilder.put(
			"company_id", String.valueOf(companyId)
		).put(
			"user_id", String.valueOf(userId)
		).build();
	}

	private static PredictionServiceClient _createPredictionServiceClient(
		String apiEndpoint, Map<String, String> labels) {

		try {
			InstantiatingGrpcChannelProvider instantiatingGrpcChannelProvider =
				InstantiatingGrpcChannelProvider.newBuilder(
				).setEndpoint(
					apiEndpoint + ":443"
				).setInterceptorProvider(
					() -> Collections.singletonList(
						new LabelsClientInterceptor(labels))
				).build();

			PredictionServiceSettings predictionServiceSettings =
				PredictionServiceSettings.newBuilder(
				).setEndpoint(
					apiEndpoint + ":443"
				).setHeaderProvider(
					FixedHeaderProvider.create("user-agent", "LangChain4j")
				).setTransportChannelProvider(
					instantiatingGrpcChannelProvider
				).build();

			return PredictionServiceClient.create(predictionServiceSettings);
		}
		catch (IOException ioException) {
			throw new IllegalStateException(
				"Failed to create the prediction service client", ioException);
		}
	}

	private static VertexAI _createVertexAI(
		Map<String, String> labels,
		VertexAIConfiguration vertexAIConfiguration) {

		String apiEndpoint;

		if (Objects.equals(vertexAIConfiguration.location(), "global")) {
			apiEndpoint = "aiplatform.googleapis.com";
		}
		else {
			apiEndpoint =
				vertexAIConfiguration.location() + "-aiplatform.googleapis.com";
		}

		return new VertexAI.Builder(
		).setApiEndpoint(
			apiEndpoint
		).setLocation(
			vertexAIConfiguration.location()
		).setPredictionClientSupplier(
			() -> _createPredictionServiceClient(apiEndpoint, labels)
		).setProjectId(
			vertexAIConfiguration.projectId()
		).build();
	}

	private static final Log _log = LogFactoryUtil.getLog(
		VertexAiGeminiUtil.class);

	private static class LabelsClientInterceptor implements ClientInterceptor {

		@Override
		public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
			MethodDescriptor<ReqT, RespT> methodDescriptor,
			CallOptions callOptions, Channel channel) {

			return new ForwardingClientCall.SimpleForwardingClientCall
				<ReqT, RespT>(channel.newCall(methodDescriptor, callOptions)) {

				@Override
				@SuppressWarnings("unchecked")
				public void sendMessage(ReqT message) {
					if (message instanceof GenerateContentRequest) {
						GenerateContentRequest generateContentRequest =
							(GenerateContentRequest)message;

						if (_log.isDebugEnabled()) {
							_log.debug(
								"Injecting Vertex AI labels " + _labels +
									" into GenerateContentRequest");
						}

						super.sendMessage(
							(ReqT)generateContentRequest.toBuilder(
							).putAllLabels(
								_labels
							).build());
					}
					else {
						super.sendMessage(message);
					}
				}

			};
		}

		private LabelsClientInterceptor(Map<String, String> labels) {
			_labels = labels;
		}

		private final Map<String, String> _labels;

	}

}