/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.portal.vulcan.internal.graphql.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.exception.NoSuchModelException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.HashMapDictionary;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.ProxyUtil;
import com.liferay.portal.kernel.util.SetUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.vulcan.graphql.annotation.GraphQLField;
import com.liferay.portal.vulcan.graphql.annotation.GraphQLTypeExtension;
import com.liferay.portal.vulcan.graphql.servlet.ServletData;
import com.liferay.portal.vulcan.internal.configuration.VulcanConfiguration;
import com.liferay.portal.vulcan.internal.configuration.util.ConfigurationUtil;
import com.liferay.portal.vulcan.internal.graphql.data.fetcher.LiferayMethodDataFetcher;
import com.liferay.portal.vulcan.internal.graphql.data.processor.LiferayMethodDataFetchingProcessor;
import com.liferay.portal.vulcan.internal.graphql.util.GraphQLUtil;
import com.liferay.portal.vulcan.multipart.MultipartBody;
import com.liferay.portal.vulcan.util.TransformUtil;

import graphql.ExceptionWhileDataFetching;
import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.Scalars;
import graphql.TypeResolutionEnvironment;

import graphql.annotations.annotationTypes.GraphQLTypeResolver;
import graphql.annotations.annotationTypes.GraphQLUnion;
import graphql.annotations.processor.ProcessingElementsContainer;
import graphql.annotations.processor.exceptions.CannotCastMemberException;
import graphql.annotations.processor.exceptions.GraphQLAnnotationsException;
import graphql.annotations.processor.graphQLProcessors.GraphQLInputProcessor;
import graphql.annotations.processor.graphQLProcessors.GraphQLOutputProcessor;
import graphql.annotations.processor.retrievers.GraphQLExtensionsHandler;
import graphql.annotations.processor.retrievers.GraphQLFieldRetriever;
import graphql.annotations.processor.retrievers.GraphQLInterfaceRetriever;
import graphql.annotations.processor.retrievers.GraphQLObjectInfoRetriever;
import graphql.annotations.processor.retrievers.GraphQLTypeRetriever;
import graphql.annotations.processor.retrievers.fieldBuilders.ArgumentBuilder;
import graphql.annotations.processor.retrievers.fieldBuilders.DeprecateBuilder;
import graphql.annotations.processor.retrievers.fieldBuilders.DirectivesBuilder;
import graphql.annotations.processor.retrievers.fieldBuilders.field.FieldNameBuilder;
import graphql.annotations.processor.retrievers.fieldBuilders.method.MethodNameBuilder;
import graphql.annotations.processor.retrievers.fieldBuilders.method.MethodTypeBuilder;
import graphql.annotations.processor.searchAlgorithms.BreadthFirstSearch;
import graphql.annotations.processor.searchAlgorithms.ParentalSearch;
import graphql.annotations.processor.typeBuilders.EnumBuilder;
import graphql.annotations.processor.typeBuilders.InputObjectBuilder;
import graphql.annotations.processor.typeBuilders.InterfaceBuilder;
import graphql.annotations.processor.typeBuilders.OutputObjectBuilder;
import graphql.annotations.processor.typeBuilders.UnionBuilder;
import graphql.annotations.processor.typeFunctions.DefaultTypeFunction;
import graphql.annotations.processor.typeFunctions.TypeFunction;
import graphql.annotations.processor.util.NamingKit;

import graphql.execution.AsyncExecutionStrategy;
import graphql.execution.DataFetcherExceptionHandler;
import graphql.execution.DataFetcherExceptionHandlerParameters;
import graphql.execution.DataFetcherExceptionHandlerResult;
import graphql.execution.ExecutionStrategy;

import graphql.kickstart.execution.GraphQLObjectMapper;
import graphql.kickstart.execution.GraphQLQueryInvoker;
import graphql.kickstart.execution.config.DefaultExecutionStrategyProvider;
import graphql.kickstart.execution.config.ExecutionStrategyProvider;
import graphql.kickstart.execution.error.GraphQLErrorHandler;
import graphql.kickstart.servlet.GraphQLConfiguration;
import graphql.kickstart.servlet.GraphQLHttpServlet;
import graphql.kickstart.servlet.apollo.ApolloScalars;

import graphql.language.ArrayValue;
import graphql.language.BooleanValue;
import graphql.language.EnumValue;
import graphql.language.FloatValue;
import graphql.language.IntValue;
import graphql.language.NullValue;
import graphql.language.ObjectField;
import graphql.language.ObjectValue;
import graphql.language.StringValue;
import graphql.language.Value;

import graphql.scalars.ExtendedScalars;

import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.DataFetchingEnvironmentImpl;
import graphql.schema.FieldCoordinates;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLFieldsContainer;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLInterfaceType;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLNamedType;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLSchemaElement;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeReference;
import graphql.schema.GraphQLTypeUtil;
import graphql.schema.GraphQLTypeVisitorStub;
import graphql.schema.PropertyDataFetcher;
import graphql.schema.SchemaTransformer;
import graphql.schema.TypeResolver;

import graphql.util.TraversalControl;
import graphql.util.TraverserContext;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import javax.ws.rs.core.Response;

import javax.xml.bind.DatatypeConverter;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.context.ServletContextHelper;
import org.osgi.service.http.whiteboard.HttpWhiteboardConstants;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

/**
 * @author Preston Crary
 */
@Component(immediate = true, service = {})
public class GraphQLServletExtender {

	@Activate
	protected void activate(BundleContext bundleContext) {
		_bundleContext = bundleContext;

		_graphQLFieldRetriever = new LiferayGraphQLFieldRetriever();

		GraphQLInterfaceRetriever graphQLInterfaceRetriever =
			new GraphQLInterfaceRetriever();

		GraphQLObjectInfoRetriever graphQLObjectInfoRetriever =
			new GraphQLObjectInfoRetriever() {

				@Override
				public String getTypeName(Class<?> objectClass) {
					String graphQLName = GraphQLUtil.getGraphQLNameValue(
						objectClass);

					if (graphQLName == null) {
						return NamingKit.toGraphqlName(objectClass.getName());
					}

					return NamingKit.toGraphqlName(graphQLName);
				}

				public Boolean isGraphQLField(AnnotatedElement element) {
					GraphQLField graphQLField = element.getAnnotation(
						GraphQLField.class);

					if (graphQLField == null) {
						return false;
					}

					return graphQLField.value();
				}

			};

		BreadthFirstSearch breadthFirstSearch = new BreadthFirstSearch(
			graphQLObjectInfoRetriever);
		ParentalSearch parentalSearch = new ParentalSearch(
			graphQLObjectInfoRetriever);

		GraphQLExtensionsHandler graphQLExtensionsHandler =
			new GraphQLExtensionsHandler() {
				{
					setFieldRetriever(_graphQLFieldRetriever);
					setFieldSearchAlgorithm(parentalSearch);
					setGraphQLObjectInfoRetriever(graphQLObjectInfoRetriever);
					setMethodSearchAlgorithm(breadthFirstSearch);
				}
			};

		GraphQLTypeRetriever graphQLTypeRetriever = new GraphQLTypeRetriever() {
			{
				setExtensionsHandler(graphQLExtensionsHandler);
				setFieldSearchAlgorithm(parentalSearch);
				setGraphQLFieldRetriever(_graphQLFieldRetriever);
				setGraphQLInterfaceRetriever(graphQLInterfaceRetriever);
				setGraphQLObjectInfoRetriever(graphQLObjectInfoRetriever);
				setMethodSearchAlgorithm(breadthFirstSearch);
			}

			public GraphQLType getGraphQLType(
					Class<?> clazz,
					ProcessingElementsContainer processingElementsContainer,
					boolean input)
				throws CannotCastMemberException, GraphQLAnnotationsException {

				Map<String, GraphQLType> graphQLTypes =
					processingElementsContainer.getTypeRegistry();

				String typeName = _getTypeName(
					input, processingElementsContainer,
					graphQLObjectInfoRetriever.getTypeName(clazz));

				GraphQLType graphQLType = graphQLTypes.get(typeName);

				String registeredClassNamesKey = clazz.getName() + "_" + input;

				if (_registeredClassNames.containsKey(
						registeredClassNamesKey)) {

					typeName = _registeredClassNames.get(
						registeredClassNamesKey);
				}
				else if (graphQLType != null) {
					String name = clazz.getName();

					name = name.replaceAll("\\.", "_");

					typeName = _getTypeName(
						input, processingElementsContainer,
						StringUtil.replace(name, '$', '_'));
				}

				Stack<String> processingStack =
					processingElementsContainer.getProcessing();

				if (processingStack.contains(typeName)) {
					return new GraphQLTypeReference(typeName);
				}

				graphQLType = graphQLTypes.get(typeName);

				if (graphQLType != null) {
					return graphQLType;
				}

				processingStack.push(typeName);

				_registeredClassNames.put(registeredClassNamesKey, typeName);

				if (clazz.getAnnotation(GraphQLUnion.class) != null) {
					graphQLType = new UnionBuilder(
						graphQLObjectInfoRetriever
					).getUnionBuilder(
						clazz, processingElementsContainer
					).build();
				}
				else if (clazz.isAnnotationPresent(GraphQLTypeResolver.class)) {
					graphQLType = new InterfaceBuilder(
						graphQLObjectInfoRetriever, _graphQLFieldRetriever,
						graphQLExtensionsHandler
					).getInterfaceBuilder(
						clazz, processingElementsContainer
					).build();
				}
				else if (Enum.class.isAssignableFrom(clazz)) {
					graphQLType = new EnumBuilder(
						graphQLObjectInfoRetriever
					).getEnumBuilder(
						clazz
					).build();
				}
				else {
					if (input) {
						graphQLType = new InputObjectBuilder(
							graphQLObjectInfoRetriever, parentalSearch,
							breadthFirstSearch, _graphQLFieldRetriever
						).getInputObjectBuilder(
							clazz, processingElementsContainer
						).build();
					}
					else {
						graphQLType = new OutputObjectBuilder(
							graphQLObjectInfoRetriever, parentalSearch,
							breadthFirstSearch, _graphQLFieldRetriever,
							graphQLInterfaceRetriever, graphQLExtensionsHandler
						).getOutputObjectBuilder(
							clazz, processingElementsContainer
						).build();
					}
				}

				if (!StringUtil.equals(
						GraphQLTypeUtil.simplePrint(graphQLType), typeName)) {

					if (!_equals(
							graphQLTypes.get(
								GraphQLTypeUtil.simplePrint(graphQLType)),
							graphQLType)) {

						try {
							Class<? extends GraphQLType> graphQLTypeClass =
								graphQLType.getClass();

							Field field = graphQLTypeClass.getDeclaredField(
								"name");

							field.setAccessible(true);

							field.set(graphQLType, typeName);
						}
						catch (Exception exception) {
							if (_log.isDebugEnabled()) {
								_log.debug(exception, exception);
							}
						}
					}
					else {
						graphQLType = graphQLTypes.get(
							GraphQLTypeUtil.simplePrint(graphQLType));
					}
				}

				graphQLTypes.put(
					GraphQLTypeUtil.simplePrint(graphQLType), graphQLType);

				processingStack.pop();

				return graphQLType;
			}

			private boolean _equals(
				GraphQLSchemaElement graphQLSchemaElement1,
				GraphQLSchemaElement graphQLSchemaElement2) {

				List<GraphQLSchemaElement> childrenGraphQLSchemaElement1 =
					graphQLSchemaElement1.getChildren();
				List<GraphQLSchemaElement> childrenGraphQLSchemaElement2 =
					graphQLSchemaElement2.getChildren();

				for (GraphQLSchemaElement childGraphQLSchemaElement1 :
						childrenGraphQLSchemaElement1) {

					boolean found = false;

					for (GraphQLSchemaElement childGraphQLSchemaElement2 :
							childrenGraphQLSchemaElement2) {

						if (StringUtil.equals(
								GraphQLTypeUtil.simplePrint(
									childGraphQLSchemaElement1),
								GraphQLTypeUtil.simplePrint(
									childGraphQLSchemaElement2)) &&
							_equals(
								childGraphQLSchemaElement1,
								childGraphQLSchemaElement2)) {

							found = true;

							break;
						}
					}

					if (!found) {
						return false;
					}
				}

				if (childrenGraphQLSchemaElement1.size() ==
						childrenGraphQLSchemaElement2.size()) {

					return true;
				}

				return false;
			}

			private String _getTypeName(
				boolean input,
				ProcessingElementsContainer processingElementsContainer,
				String typeName) {

				if (input) {
					typeName =
						processingElementsContainer.getInputPrefix() +
							typeName +
								processingElementsContainer.getInputSuffix();
				}

				return typeName;
			}

		};

		// Handle Circular reference between GraphQLInterfaceRetriever and
		// GraphQLTypeRetriever

		graphQLInterfaceRetriever.setGraphQLTypeRetriever(graphQLTypeRetriever);

		_defaultTypeFunction = new DefaultTypeFunction(
			new GraphQLInputProcessor() {
				{
					setGraphQLTypeRetriever(graphQLTypeRetriever);
				}
			},
			new GraphQLOutputProcessor() {
				{
					setGraphQLTypeRetriever(graphQLTypeRetriever);
				}
			}) {

			@Override
			public GraphQLType buildType(
				boolean input, Class<?> clazz, AnnotatedType annotatedType,
				ProcessingElementsContainer processingElementsContainer) {

				GraphQLType graphQLType = super.buildType(
					input, clazz, annotatedType, processingElementsContainer);

				if ((annotatedType != null) &&
					(annotatedType.isAnnotationPresent(NotEmpty.class) ||
					 annotatedType.isAnnotationPresent(NotNull.class))) {

					graphQLType = new GraphQLNonNull(graphQLType);
				}

				return graphQLType;
			}

		};

		_defaultTypeFunction.register(new DateTypeFunction());
		_defaultTypeFunction.register(new MapTypeFunction());
		_defaultTypeFunction.register(new ObjectTypeFunction());

		Dictionary<String, Object> properties = new HashMapDictionary<>();

		properties.put(
			HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_NAME, "GraphQL");
		properties.put(
			HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_PATH, "/graphql");
		properties.put(
			HttpWhiteboardConstants.HTTP_WHITEBOARD_FILTER_SERVLET, "GraphQL");

		_servletContextHelperServiceRegistration =
			bundleContext.registerService(
				ServletContextHelper.class,
				new ServletContextHelper(bundleContext.getBundle()) {
				},
				properties);

		_servletDataServiceTracker = new ServiceTracker<>(
			bundleContext, ServletData.class,
			new ServletDataServiceTrackerCustomizer());

		_servletDataServiceTracker.open();

		properties = new HashMapDictionary<>();

		properties.put(
			HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_SELECT, "GraphQL");
		properties.put(
			HttpWhiteboardConstants.HTTP_WHITEBOARD_SERVLET_NAME, "GraphQL");
		properties.put(
			HttpWhiteboardConstants.HTTP_WHITEBOARD_SERVLET_PATTERN, "/*");

		_servletServiceRegistration = _bundleContext.registerService(
			Servlet.class,
			(Servlet)ProxyUtil.newProxyInstance(
				GraphQLServletExtender.class.getClassLoader(),
				new Class<?>[] {Servlet.class},
				new InvocationHandler() {

					@Override
					public Object invoke(
							Object proxy, Method method, Object[] arguments)
						throws Throwable {

						String methodName = method.getName();

						if (methodName.equals("destroy")) {
							return null;
						}

						if (methodName.equals("getServletConfig")) {
							return _servletConfig;
						}

						if (methodName.equals("getServletInfo")) {
							return StringPool.BLANK;
						}

						if (methodName.equals("hashCode")) {
							return hashCode();
						}

						if (methodName.equals("init") &&
							(arguments.length > 0)) {

							_servletConfig = (ServletConfig)arguments[0];

							return null;
						}

						HttpServletRequest httpServletRequest =
							(HttpServletRequest)arguments[0];

						arguments[0] = new HttpServletRequestWrapper(
							httpServletRequest) {

							@Override
							public boolean isAsyncSupported() {
								return false;
							}

						};

						Servlet servlet = _createServlet();

						servlet.init(_servletConfig);

						try {
							return method.invoke(servlet, arguments);
						}
						catch (InvocationTargetException
									invocationTargetException) {

							throw invocationTargetException.getCause();
						}
					}

					private ServletConfig _servletConfig;

				}),
			properties);
	}

	@Deactivate
	protected void deactivate() {
		_servletDataServiceTracker.close();

		_servletServiceRegistration.unregister();

		_servletContextHelperServiceRegistration.unregister();
	}

	private static boolean _isMultipartBody(Parameter parameter) {
		Class<?> clazz = parameter.getType();

		String typeName = clazz.getTypeName();

		return typeName.contains("MultipartBody");
	}

	private void _collectObjectFields(
		GraphQLObjectType.Builder builder,
		Map<String, Configuration> configurations,
		Function<ServletData, Object> function,
		ProcessingElementsContainer processingElementsContainer,
		List<ServletData> servletDatas) {

		Stream<ServletData> stream = servletDatas.stream();

		Map<String, Optional<Method>> methods = stream.filter(
			servletData -> servletData.getGraphQLNamespace() == null
		).flatMap(
			servletData -> Stream.of(
				function.apply(servletData)
			).filter(
				Objects::nonNull
			).map(
				Object::getClass
			).map(
				Class::getMethods
			).flatMap(
				Arrays::stream
			).filter(
				method -> _isMethodEnabled(
					configurations, method, servletData.getPath())
			)
		).collect(
			Collectors.groupingBy(
				Method::getName,
				Collectors.maxBy(Comparator.comparingInt(this::_getVersion)))
		);

		for (Optional<Method> methodOptional : methods.values()) {
			if (methodOptional.isPresent()) {
				Method method = methodOptional.get();

				Class<?> clazz = method.getDeclaringClass();

				builder.field(
					_graphQLFieldRetriever.getField(
						clazz.getSimpleName(), method,
						processingElementsContainer));
			}
		}
	}

	private GraphQLFieldDefinition _createNodeGraphQLFieldDefinition(
		GraphQLOutputType graphQLOutputType) {

		GraphQLFieldDefinition.Builder graphQLFieldDefinitionbuilder =
			GraphQLFieldDefinition.newFieldDefinition();

		GraphQLArgument.Builder graphQLArgumentBuilder =
			GraphQLArgument.newArgument();

		graphQLFieldDefinitionbuilder.argument(
			graphQLArgumentBuilder.name(
				"dataType"
			).type(
				Scalars.GraphQLString
			).build());

		graphQLArgumentBuilder = GraphQLArgument.newArgument();

		graphQLFieldDefinitionbuilder.argument(
			graphQLArgumentBuilder.name(
				"id"
			).type(
				ExtendedScalars.GraphQLLong
			).build());

		graphQLFieldDefinitionbuilder.name("graphQLNode");
		graphQLFieldDefinitionbuilder.type(graphQLOutputType);

		return graphQLFieldDefinitionbuilder.build();
	}

	private GraphQLInterfaceType _createNodeGraphQLInterfaceType() {
		GraphQLInterfaceType.Builder interfaceBuilder =
			GraphQLInterfaceType.newInterface();

		GraphQLFieldDefinition.Builder fieldBuilder =
			GraphQLFieldDefinition.newFieldDefinition();

		interfaceBuilder.field(
			fieldBuilder.name(
				"id"
			).type(
				ExtendedScalars.GraphQLLong
			).build());

		interfaceBuilder.name("GraphQLNode");

		return interfaceBuilder.build();
	}

	private Servlet _createServlet() throws Exception {
		Servlet servlet = _servlet;

		if (servlet != null) {
			return servlet;
		}

		synchronized (_servletDataList) {
			if (_servlet != null) {
				return _servlet;
			}

			PropertyDataFetcher.clearReflectionCache();

			_registeredClassNames.clear();

			ProcessingElementsContainer processingElementsContainer =
				new ProcessingElementsContainer(_defaultTypeFunction);

			Map<Class<?>, Set<Class<?>>> classesMap =
				processingElementsContainer.getExtensionsTypeRegistry();

			List<ServletData> servletDatas = new ArrayList<>();

			for (ServletData servletData : _servletDataList) {
				if (_isGraphQLEnabled(servletData.getPath())) {
					servletDatas.add(servletData);
				}

				Object query = servletData.getQuery();

				Class<?> queryClass = query.getClass();

				for (Class<?> innerClasses : queryClass.getClasses()) {
					if (innerClasses.isAnnotationPresent(
							GraphQLTypeExtension.class)) {

						GraphQLTypeExtension graphQLTypeExtension =
							innerClasses.getAnnotation(
								GraphQLTypeExtension.class);

						Class<?> clazz = graphQLTypeExtension.value();

						if (!classesMap.containsKey(clazz)) {
							classesMap.put(clazz, new HashSet<>());
						}

						Set<Class<?>> classes = classesMap.get(clazz);

						classes.add(innerClasses);
					}
				}
			}

			GraphQLSchema.Builder graphQLSchemaBuilder =
				GraphQLSchema.newSchema();

			GraphQLObjectType.Builder mutationBuilder =
				GraphQLObjectType.newObject();

			mutationBuilder.name("mutation");

			GraphQLObjectType.Builder graphQLObjectTypeBuilder =
				GraphQLObjectType.newObject();

			graphQLObjectTypeBuilder.name("query");

			Map<String, Configuration> configurations =
				ConfigurationUtil.getConfigurations(_configurationAdmin);

			_collectObjectFields(
				mutationBuilder, configurations, ServletData::getMutation,
				processingElementsContainer, servletDatas);
			_collectObjectFields(
				graphQLObjectTypeBuilder, configurations, ServletData::getQuery,
				processingElementsContainer, servletDatas);

			GraphQLInterfaceType graphQLInterfaceType = _registerInterfaces(
				processingElementsContainer, graphQLObjectTypeBuilder,
				graphQLSchemaBuilder);

			_registerNamespace(
				configurations, ServletData::getQuery, graphQLObjectTypeBuilder,
				graphQLSchemaBuilder, false, processingElementsContainer,
				servletDatas);
			_registerNamespace(
				configurations, ServletData::getMutation, mutationBuilder,
				graphQLSchemaBuilder, true, processingElementsContainer,
				servletDatas);

			graphQLSchemaBuilder.mutation(mutationBuilder.build());
			graphQLSchemaBuilder.query(graphQLObjectTypeBuilder.build());

			GraphQLConfiguration.Builder graphQLConfigurationBuilder =
				GraphQLConfiguration.with(
					SchemaTransformer.transformSchema(
						graphQLSchemaBuilder.build(),
						new LiferayGraphQLTypeVisitor(graphQLInterfaceType)));

			ExecutionStrategy executionStrategy = new AsyncExecutionStrategy(
				new SanitizedDataFetcherExceptionHandler());

			ExecutionStrategyProvider executionStrategyProvider =
				new DefaultExecutionStrategyProvider(executionStrategy);

			GraphQLQueryInvoker graphQLQueryInvoker =
				GraphQLQueryInvoker.newBuilder(
				).withExecutionStrategyProvider(
					executionStrategyProvider
				).build();

			graphQLConfigurationBuilder.with(graphQLQueryInvoker);

			GraphQLObjectMapper.Builder objectMapperBuilder =
				GraphQLObjectMapper.newBuilder();

			objectMapperBuilder.withGraphQLErrorHandler(
				new LiferayGraphQLErrorHandler());
			objectMapperBuilder.withObjectMapperProvider(
				() -> {
					ObjectMapper objectMapper = new ObjectMapper();

					objectMapper.setFilterProvider(
						new SimpleFilterProvider() {
							{
								addFilter(
									"Liferay.Vulcan",
									SimpleBeanPropertyFilter.serializeAll());
							}
						});

					return objectMapper;
				});

			graphQLConfigurationBuilder.with(objectMapperBuilder.build());

			_servlet = GraphQLHttpServlet.with(
				graphQLConfigurationBuilder.build());

			return _servlet;
		}
	}

	private Integer _getVersion(Method method) {
		Class<?> clazz = method.getDeclaringClass();

		String packageString = String.valueOf(clazz.getPackage());

		String[] packageNames = packageString.split("\\.");

		String version = packageNames[packageNames.length - 1];

		return Integer.valueOf(version.replaceAll("\\D", ""));
	}

	private boolean _isGraphQLEnabled(String path) throws Exception {
		String filterString = String.format(
			"(&(path=%s)(service.factoryPid=%s))",
			path.substring(0, path.indexOf("-graphql")),
			VulcanConfiguration.class.getName());

		Configuration[] configurations = _configurationAdmin.listConfigurations(
			filterString);

		if (!ArrayUtil.isEmpty(configurations)) {
			Dictionary<String, Object> dictionary =
				configurations[0].getProperties();

			return (Boolean)dictionary.get("graphQLEnabled");
		}

		return true;
	}

	private boolean _isMethodEnabled(
		Map<String, Configuration> configurations, Method method, String path) {

		String substring = path.substring(0, path.indexOf("-graphql"));

		if (configurations.containsKey(substring)) {
			Configuration configuration = configurations.get(substring);

			Dictionary<String, Object> properties =
				configuration.getProperties();

			String excludedOperationIds = GetterUtil.getString(
				properties.get("excludedOperationIds"));

			Set<String> excludedOperationIdsList = SetUtil.fromArray(
				excludedOperationIds.split(","));

			if (excludedOperationIdsList.contains(method.getName())) {
				return false;
			}
		}

		return GraphQLUtil.isGraphQLFieldValue(method);
	}

	private GraphQLInterfaceType _registerInterfaces(
		ProcessingElementsContainer processingElementsContainer,
		GraphQLObjectType.Builder graphQLObjectTypeBuilder,
		GraphQLSchema.Builder graphQLSchemaBuilder) {

		try {
			Map<String, GraphQLType> graphQLTypes =
				processingElementsContainer.getTypeRegistry();

			GraphQLInterfaceType graphQLInterfaceType =
				_createNodeGraphQLInterfaceType();

			graphQLTypes.put("GraphQLNode", graphQLInterfaceType);

			graphQLObjectTypeBuilder.field(
				_createNodeGraphQLFieldDefinition(graphQLInterfaceType));

			GraphQLCodeRegistry.Builder builder =
				processingElementsContainer.getCodeRegistryBuilder();

			graphQLSchemaBuilder.codeRegistry(
				builder.dataFetcher(
					FieldCoordinates.coordinates("query", "graphQLNode"),
					new NodeDataFetcher()
				).typeResolver(
					"GraphQLNode", new GraphQLNodeTypeResolver()
				).build());

			return graphQLInterfaceType;
		}
		catch (Exception exception) {
			throw new RuntimeException(exception);
		}
	}

	private void _registerNamespace(
		Map<String, Configuration> configurations,
		Function<ServletData, Object> function,
		GraphQLObjectType.Builder graphQLObjectTypeBuilder,
		GraphQLSchema.Builder graphQLSchemaBuilder, boolean mutation,
		ProcessingElementsContainer processingElementsContainer,
		List<ServletData> servletDatas) {

		for (ServletData servletData : servletDatas) {
			String graphQLNamespace = servletData.getGraphQLNamespace();

			if (graphQLNamespace == null) {
				continue;
			}

			Object query = function.apply(servletData);

			Class<?> clazz = query.getClass();

			List<Method> methods = TransformUtil.transformToList(
				clazz.getMethods(),
				method -> {
					if (_isMethodEnabled(
							configurations, method, servletData.getPath())) {

						return method;
					}

					return null;
				});

			if (ListUtil.isEmpty(methods)) {
				continue;
			}

			GraphQLObjectType.Builder builder = new GraphQLObjectType.Builder();

			String prefix = "";

			if (mutation) {
				prefix = "Mutation";
			}

			builder.name(
				prefix + StringUtil.upperCaseFirstLetter(graphQLNamespace));

			GraphQLCodeRegistry.Builder graphQLCodeRegistryBuilder =
				processingElementsContainer.getCodeRegistryBuilder();

			for (Method method : methods) {
				builder.field(
					_graphQLFieldRetriever.getField(
						clazz.getSimpleName(), method,
						processingElementsContainer));

				graphQLSchemaBuilder.codeRegistry(
					graphQLCodeRegistryBuilder.dataFetcher(
						FieldCoordinates.coordinates(
							graphQLNamespace, method.getName()),
						new LiferayMethodDataFetcher(
							_liferayMethodDataFetchingProcessor, method)
					).build());
			}

			GraphQLFieldDefinition.Builder graphQLFieldDefinitionBuilder =
				GraphQLFieldDefinition.newFieldDefinition();

			graphQLObjectTypeBuilder.field(
				graphQLFieldDefinitionBuilder.name(
					graphQLNamespace
				).type(
					builder.build()
				));

			String parentField = "query";

			if (mutation) {
				parentField = "mutation";
			}

			graphQLSchemaBuilder.codeRegistry(
				graphQLCodeRegistryBuilder.dataFetcher(
					FieldCoordinates.coordinates(parentField, graphQLNamespace),
					(DataFetcher<Object>)dataFetcher -> new Object()
				).build());
		}
	}

	private void _replaceFieldDefinition(
		GraphQLInterfaceType graphQLInterfaceType,
		GraphQLObjectType graphQLObjectType,
		GraphQLObjectType.Builder graphQLObjectTypeBuilder) {

		for (GraphQLFieldDefinition graphQLFieldDefinition :
				graphQLObjectType.getFieldDefinitions()) {

			GraphQLOutputType graphQLOutputType =
				graphQLFieldDefinition.getType();

			String typeName = GraphQLTypeUtil.simplePrint(graphQLOutputType);

			if ((typeName != null) && typeName.equals("Object") &&
				StringUtil.endsWith(graphQLFieldDefinition.getName(), "Id")) {

				GraphQLFieldDefinition.Builder
					newGraphQLFieldDefinitionBuilder =
						GraphQLFieldDefinition.newFieldDefinition(
							graphQLFieldDefinition);

				graphQLObjectTypeBuilder.field(
					newGraphQLFieldDefinitionBuilder.type(
						graphQLInterfaceType
					).build());
			}
		}
	}

	private void _replaceFieldNodes(
		GraphQLCodeRegistry.Builder graphQLCodeRegistryBuilder,
		GraphQLInterfaceType graphQLInterfaceType,
		GraphQLObjectType graphQLObjectType,
		GraphQLObjectType.Builder graphQLObjectTypeBuilder) {

		GraphQLFieldDefinition graphQLFieldDefinition =
			graphQLObjectType.getFieldDefinition("contentType");

		if (graphQLFieldDefinition == null) {
			return;
		}

		GraphQLFieldDefinition.Builder graphQLFieldDefinitionBuilder =
			GraphQLFieldDefinition.newFieldDefinition(
			).name(
				"graphQLNode"
			).type(
				graphQLInterfaceType
			);

		graphQLObjectTypeBuilder.field(graphQLFieldDefinitionBuilder.build());

		graphQLCodeRegistryBuilder.dataFetcher(
			FieldCoordinates.coordinates(
				graphQLObjectType.getName(), "graphQLNode"),
			new GraphQLNodePropertyDataFetcher()
		).typeResolver(
			"GraphQLNode", new GraphQLNodeTypeResolver()
		).build();
	}

	private static final Log _log = LogFactoryUtil.getLog(
		GraphQLServletExtender.class);

	private static final GraphQLScalarType _dateGraphQLScalarType;
	private static final GraphQLType _mapGraphQLType;
	private static final GraphQLScalarType _objectGraphQLScalarType;

	static {
		GraphQLScalarType.Builder dateBuilder = new GraphQLScalarType.Builder();

		_dateGraphQLScalarType = dateBuilder.name(
			"Date"
		).description(
			"An RFC-3339 compliant date time scalar"
		).coercing(
			new Coercing<Date, String>() {

				@Override
				public Date parseLiteral(Object value)
					throws CoercingParseLiteralException {

					return _toDate(value);
				}

				@Override
				public Date parseValue(Object value)
					throws CoercingParseValueException {

					return _toDate(value);
				}

				@Override
				public String serialize(Object value)
					throws CoercingSerializeException {

					SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
						"yyyy-MM-dd'T'HH:mm:ss'Z'");

					return simpleDateFormat.format((Date)value);
				}

				private Date _toDate(Object value) {
					if (value instanceof Date) {
						return (Date)value;
					}

					if (value instanceof StringValue) {
						StringValue stringValue = (StringValue)value;

						Calendar calendar = DatatypeConverter.parseDateTime(
							stringValue.getValue());

						return calendar.getTime();
					}

					Calendar calendar = DatatypeConverter.parseDateTime(
						value.toString());

					return calendar.getTime();
				}

			}
		).build();

		GraphQLScalarType.Builder objectBuilder =
			new GraphQLScalarType.Builder();

		_mapGraphQLType = objectBuilder.name(
			"Map"
		).description(
			"Any kind of object supported by a Map"
		).coercing(
			new Coercing<Object, Object>() {

				@Override
				public Object parseLiteral(Object value)
					throws CoercingParseLiteralException {

					return value;
				}

				@Override
				public Object parseValue(Object value)
					throws CoercingParseValueException {

					return value;
				}

				@Override
				public Object serialize(Object value)
					throws CoercingSerializeException {

					return value;
				}

			}
		).build();

		_objectGraphQLScalarType = objectBuilder.name(
			"Object"
		).description(
			"Any kind of object supported by basic scalar types"
		).coercing(
			new Coercing<Object, Object>() {

				@Override
				public Object parseLiteral(Object value)
					throws CoercingParseLiteralException {

					if (value instanceof ArrayValue) {
						ArrayValue arrayValue = (ArrayValue)value;

						List<Value> values = arrayValue.getValues();

						Stream<Value> stream = values.stream();

						return stream.map(
							this::parseLiteral
						).collect(
							Collectors.toList()
						);
					}
					else if (value instanceof BooleanValue) {
						BooleanValue booleanValue = (BooleanValue)value;

						return booleanValue.isValue();
					}
					else if (value instanceof EnumValue) {
						EnumValue enumValue = (EnumValue)value;

						return enumValue.getName();
					}
					else if (value instanceof FloatValue) {
						FloatValue floatValue = (FloatValue)value;

						return floatValue.getValue();
					}
					else if (value instanceof IntValue) {
						IntValue intValue = (IntValue)value;

						return intValue.getValue();
					}
					else if (value instanceof NullValue) {
						return null;
					}
					else if (value instanceof ObjectValue) {
						ObjectValue objectValue = (ObjectValue)value;

						List<ObjectField> objectFields =
							objectValue.getObjectFields();

						Stream<ObjectField> stream = objectFields.stream();

						return stream.collect(
							Collectors.toMap(
								ObjectField::getName,
								objectField -> parseLiteral(
									objectField.getValue())));
					}
					else if (value instanceof StringValue) {
						StringValue stringValue = (StringValue)value;

						return stringValue.getValue();
					}

					throw new CoercingSerializeException(
						"Unable to parse " + value);
				}

				@Override
				public Object parseValue(Object value)
					throws CoercingParseValueException {

					return value;
				}

				@Override
				public Object serialize(Object value)
					throws CoercingSerializeException {

					return value;
				}

			}
		).build();
	}

	private BundleContext _bundleContext;

	@Reference
	private ConfigurationAdmin _configurationAdmin;

	private DefaultTypeFunction _defaultTypeFunction;
	private GraphQLFieldRetriever _graphQLFieldRetriever;

	@Reference
	private LiferayMethodDataFetchingProcessor
		_liferayMethodDataFetchingProcessor;

	private final Map<String, String> _registeredClassNames = new HashMap<>();
	private volatile Servlet _servlet;
	private ServiceRegistration<ServletContextHelper>
		_servletContextHelperServiceRegistration;
	private final List<ServletData> _servletDataList = new ArrayList<>();
	private ServiceTracker<ServletData, ServletData> _servletDataServiceTracker;
	private ServiceRegistration<Servlet> _servletServiceRegistration;

	private static class DateTypeFunction implements TypeFunction {

		@Override
		public GraphQLType buildType(
			boolean input, Class<?> clazz, AnnotatedType annotatedType,
			ProcessingElementsContainer processingElementsContainer) {

			return _dateGraphQLScalarType;
		}

		@Override
		public boolean canBuildType(
			Class<?> clazz, AnnotatedType annotatedType) {

			if (clazz == Date.class) {
				return true;
			}

			return false;
		}

	}

	private static class GraphQLNodePropertyDataFetcher
		implements DataFetcher<Object> {

		@Override
		public Object get(DataFetchingEnvironment dataFetchingEnvironment)
			throws Exception {

			GraphQLSchema graphQLSchema =
				dataFetchingEnvironment.getGraphQLSchema();

			GraphQLCodeRegistry graphQLCodeRegistry =
				graphQLSchema.getCodeRegistry();

			Map<String, GraphQLNamedType> graphQLNamedTypes =
				graphQLSchema.getTypeMap();

			GraphQLFieldDefinition.Builder builder =
				GraphQLFieldDefinition.newFieldDefinition();

			Object source = dataFetchingEnvironment.getSource();

			Class<?> clazz = source.getClass();

			Method getContentTypeMethod = clazz.getMethod("getContentType");

			String fieldName = StringUtil.lowerCaseFirstLetter(
				(String)getContentTypeMethod.invoke(source));

			GraphQLFieldDefinition graphQLFieldDefinition =
				dataFetchingEnvironment.getFieldDefinition();

			DataFetcher<?> dataFetcher = graphQLCodeRegistry.getDataFetcher(
				(GraphQLFieldsContainer)graphQLNamedTypes.get("query"),
				builder.name(
					fieldName
				).type(
					graphQLFieldDefinition.getType()
				).build());

			DataFetchingEnvironmentImpl.Builder dataFetchingEnvironmentBuilder =
				DataFetchingEnvironmentImpl.newDataFetchingEnvironment(
					dataFetchingEnvironment);

			Method method = clazz.getMethod("getId");

			Method[] methods = clazz.getMethods();

			for (Method existingMethod : methods) {
				if (StringUtil.equals(
						existingMethod.getName(), "getContentId")) {

					method = existingMethod;
				}
			}

			return dataFetcher.get(
				dataFetchingEnvironmentBuilder.arguments(
					Collections.singletonMap(
						fieldName + "Id", method.invoke(source))
				).build());
		}

	}

	private static class GraphQLNodeTypeResolver implements TypeResolver {

		@Override
		public GraphQLObjectType getType(
			TypeResolutionEnvironment typeResolutionEnvironment) {

			GraphQLSchema graphQLSchema = typeResolutionEnvironment.getSchema();

			Map<String, GraphQLNamedType> graphQLNamedTypes =
				graphQLSchema.getTypeMap();

			GraphQLNamedType graphQLNamedType = graphQLNamedTypes.get(
				_getClassName(typeResolutionEnvironment.getObject()));

			return (GraphQLObjectType)graphQLNamedType;
		}

		private String _getClassName(Object object) {
			Class<?> clazz = object.getClass();

			String name = clazz.getName();

			if (!name.contains("$")) {
				return clazz.getSimpleName();
			}

			Class<?> parentClass = clazz.getSuperclass();

			return parentClass.getSimpleName();
		}

	}

	private static class LiferayArgumentBuilder extends ArgumentBuilder {

		public LiferayArgumentBuilder(
			Method method, TypeFunction typeFunction,
			GraphQLFieldDefinition.Builder builder,
			ProcessingElementsContainer processingElementsContainer,
			GraphQLOutputType graphQLOutputType) {

			super(
				method, typeFunction, builder, processingElementsContainer,
				graphQLOutputType);

			_method = method;
			_typeFunction = typeFunction;
			_processingElementsContainer = processingElementsContainer;
		}

		@Override
		public List<GraphQLArgument> build() {
			Stream<Parameter> stream = Arrays.stream(_method.getParameters());

			return stream.filter(
				parameter -> !DataFetchingEnvironment.class.isAssignableFrom(
					parameter.getType())
			).map(
				parameter -> {
					if (_isMultipartBody(parameter)) {
						GraphQLArgument.Builder builder =
							new GraphQLArgument.Builder();

						return builder.type(
							new GraphQLList(ApolloScalars.Upload)
						).name(
							"multipartBody"
						).build();
					}

					return _createGraphQLArgument(
						parameter,
						(GraphQLInputType)_typeFunction.buildType(
							true, parameter.getType(),
							parameter.getAnnotatedType(),
							_processingElementsContainer));
				}
			).collect(
				Collectors.toList()
			);
		}

		private GraphQLArgument _createGraphQLArgument(
				Parameter parameter, GraphQLInputType graphQLInputType)
			throws GraphQLAnnotationsException {

			GraphQLArgument.Builder builder = GraphQLArgument.newArgument();

			String graphQLName = GraphQLUtil.getGraphQLNameValue(parameter);

			if (graphQLName != null) {
				builder.name(NamingKit.toGraphqlName(graphQLName));
			}
			else {
				builder.name(NamingKit.toGraphqlName(parameter.getName()));
			}

			builder.type(graphQLInputType);

			DirectivesBuilder directivesBuilder = new DirectivesBuilder(
				parameter, _processingElementsContainer);

			builder.withDirectives(directivesBuilder.build());

			return builder.build();
		}

		private final Method _method;
		private final ProcessingElementsContainer _processingElementsContainer;
		private final TypeFunction _typeFunction;

	}

	private static class LiferayDeprecateBuilder extends DeprecateBuilder {

		public LiferayDeprecateBuilder(AccessibleObject accessibleObject) {
			super(accessibleObject);

			_accessibleObject = accessibleObject;
		}

		public String build() {
			Deprecated deprecated = _accessibleObject.getAnnotation(
				Deprecated.class);

			if (deprecated != null) {
				return "Deprecated";
			}

			return null;
		}

		private final AccessibleObject _accessibleObject;

	}

	private static class LiferayGraphQLErrorHandler
		implements GraphQLErrorHandler {

		@Override
		public List<GraphQLError> processErrors(
			List<GraphQLError> graphQLErrors) {

			Stream<GraphQLError> stream = graphQLErrors.stream();

			return stream.filter(
				graphQLError ->
					!_isNoSuchModelException(graphQLError) ||
					_isRequiredField(graphQLError)
			).map(
				graphQLError -> {
					String message = graphQLError.getMessage();

					if (message.contains("SecurityException")) {
						return _getExtendedGraphQLError(
							graphQLError, Response.Status.UNAUTHORIZED);
					}
					else if (_isNoSuchModelException(graphQLError)) {
						return _getExtendedGraphQLError(
							graphQLError, Response.Status.NOT_FOUND);
					}

					if (!_isClientErrorException(graphQLError)) {
						return _getExtendedGraphQLError(
							graphQLError,
							Response.Status.INTERNAL_SERVER_ERROR);
					}

					return _getExtendedGraphQLError(
						graphQLError, Response.Status.BAD_REQUEST);
				}
			).collect(
				Collectors.toList()
			);
		}

		private GraphQLError _getExtendedGraphQLError(
			GraphQLError graphQLError, Response.Status status) {

			GraphqlErrorBuilder graphqlErrorBuilder =
				GraphqlErrorBuilder.newError();

			return graphqlErrorBuilder.message(
				StringUtil.removeSubstring(graphQLError.getMessage(), "%")
			).extensions(
				HashMapBuilder.put(
					"code", (Object)status.getReasonPhrase()
				).put(
					"exception",
					HashMapBuilder.put(
						"errno", status.getStatusCode()
					).build()
				).build()
			).build();
		}

		private boolean _isClientErrorException(GraphQLError graphQLError) {
			if (graphQLError instanceof ExceptionWhileDataFetching) {
				ExceptionWhileDataFetching exceptionWhileDataFetching =
					(ExceptionWhileDataFetching)graphQLError;

				return exceptionWhileDataFetching.getException() instanceof
					GraphQLError;
			}

			String message = graphQLError.getMessage();

			if (!(graphQLError instanceof Throwable) ||
				message.contains("ClientErrorException")) {

				return true;
			}

			return false;
		}

		private boolean _isNoSuchModelException(GraphQLError graphQLError) {
			if (!(graphQLError instanceof ExceptionWhileDataFetching)) {
				return false;
			}

			ExceptionWhileDataFetching exceptionWhileDataFetching =
				(ExceptionWhileDataFetching)graphQLError;

			Throwable throwable = exceptionWhileDataFetching.getException();

			if ((throwable != null) &&
				(throwable.getCause() instanceof NoSuchModelException)) {

				return true;
			}

			return false;
		}

		private boolean _isRequiredField(GraphQLError graphQLError) {
			List<Object> path = Optional.ofNullable(
				graphQLError.getPath()
			).orElse(
				Collections.emptyList()
			);

			if (path.size() <= 1) {
				return true;
			}

			return StringUtil.containsIgnoreCase(
				(String)path.get(path.size() - 1), "parent");
		}

	}

	private static class MapTypeFunction implements TypeFunction {

		@Override
		public GraphQLType buildType(
			boolean input, Class<?> clazz, AnnotatedType annotatedType,
			ProcessingElementsContainer processingElementsContainer) {

			return _mapGraphQLType;
		}

		@Override
		public boolean canBuildType(
			Class<?> clazz, AnnotatedType annotatedType) {

			if (clazz == Map.class) {
				return true;
			}

			return false;
		}

	}

	private static class NodeDataFetcher implements DataFetcher<Object> {

		@Override
		public Object get(DataFetchingEnvironment dataFetchingEnvironment)
			throws Exception {

			GraphQLSchema graphQLSchema =
				dataFetchingEnvironment.getGraphQLSchema();

			GraphQLCodeRegistry graphQLCodeRegistry =
				graphQLSchema.getCodeRegistry();

			GraphQLFieldDefinition.Builder builder =
				GraphQLFieldDefinition.newFieldDefinition();

			GraphQLFieldDefinition graphQLFieldDefinition =
				dataFetchingEnvironment.getFieldDefinition();

			String fieldName = _getFieldName(
				dataFetchingEnvironment, graphQLSchema);

			DataFetcher<?> dataFetcher = graphQLCodeRegistry.getDataFetcher(
				(GraphQLFieldsContainer)dataFetchingEnvironment.getParentType(),
				builder.argument(
					graphQLFieldDefinition.getArgument("id")
				).name(
					fieldName
				).type(
					graphQLFieldDefinition.getType()
				).build());

			DataFetchingEnvironmentImpl.Builder dataFetchingEnvironmentBuilder =
				DataFetchingEnvironmentImpl.newDataFetchingEnvironment(
					dataFetchingEnvironment);

			return dataFetcher.get(
				dataFetchingEnvironmentBuilder.arguments(
					Collections.singletonMap(
						fieldName + "Id",
						dataFetchingEnvironment.getArgument("id"))
				).build());
		}

		private String _getFieldName(
			DataFetchingEnvironment dataFetchingEnvironment,
			GraphQLSchema graphQLSchema) {

			Map<String, GraphQLNamedType> graphQLNamedTypes =
				graphQLSchema.getTypeMap();

			GraphQLNamedType graphQLNamedType = graphQLNamedTypes.get(
				dataFetchingEnvironment.getArgument("dataType"));

			return StringUtil.lowerCaseFirstLetter(graphQLNamedType.getName());
		}

	}

	private static class ObjectTypeFunction implements TypeFunction {

		@Override
		public GraphQLType buildType(
			boolean input, Class<?> clazz, AnnotatedType annotatedType,
			ProcessingElementsContainer processingElementsContainer) {

			return _objectGraphQLScalarType;
		}

		@Override
		public boolean canBuildType(
			Class<?> clazz, AnnotatedType annotatedType) {

			if ((clazz == Float.class) || (clazz == MultipartBody.class) ||
				(clazz == Number.class) || (clazz == Object.class) ||
				(clazz == Response.class)) {

				return true;
			}

			return false;
		}

	}

	private static class SanitizedDataFetcherExceptionHandler
		implements DataFetcherExceptionHandler {

		@Override
		public DataFetcherExceptionHandlerResult onException(
			DataFetcherExceptionHandlerParameters
				dataFetcherExceptionHandlerParameters) {

			DataFetcherExceptionHandlerResult.Builder builder =
				DataFetcherExceptionHandlerResult.newResult();

			return builder.error(
				new ExceptionWhileDataFetching(
					dataFetcherExceptionHandlerParameters.getPath(),
					dataFetcherExceptionHandlerParameters.getException(),
					dataFetcherExceptionHandlerParameters.getSourceLocation())
			).build();
		}

	}

	private class LiferayGraphQLFieldRetriever extends GraphQLFieldRetriever {

		@Override
		public GraphQLFieldDefinition getField(
				String parentName, Field field,
				ProcessingElementsContainer processingElementsContainer)
			throws GraphQLAnnotationsException {

			GraphQLFieldDefinition.Builder builder =
				GraphQLFieldDefinition.newFieldDefinition();

			builder.deprecate(
				new DeprecateBuilder(
					field
				).build());

			GraphQLField graphQLField = field.getAnnotation(GraphQLField.class);

			if (graphQLField != null) {
				builder.description(graphQLField.description());
			}

			builder.name(
				new FieldNameBuilder(
					field
				).build());
			builder.type(
				(GraphQLOutputType)_defaultTypeFunction.buildType(
					field.getType(), field.getAnnotatedType(),
					processingElementsContainer));

			return builder.build();
		}

		@Override
		public GraphQLFieldDefinition getField(
			String parentName, Method method,
			ProcessingElementsContainer processingElementsContainer) {

			GraphQLFieldDefinition.Builder builder =
				GraphQLFieldDefinition.newFieldDefinition();

			MethodTypeBuilder methodTypeBuilder = new MethodTypeBuilder(
				method, processingElementsContainer.getDefaultTypeFunction(),
				processingElementsContainer, false);

			GraphQLOutputType graphQLOutputType =
				(GraphQLOutputType)methodTypeBuilder.build();

			ArgumentBuilder argumentBuilder = new LiferayArgumentBuilder(
				method, processingElementsContainer.getDefaultTypeFunction(),
				builder, processingElementsContainer, graphQLOutputType);

			builder.arguments(argumentBuilder.build());

			builder.dataFetcher(
				new LiferayMethodDataFetcher(
					_liferayMethodDataFetchingProcessor, method));

			DeprecateBuilder deprecateBuilder = new LiferayDeprecateBuilder(
				method);

			builder.deprecate(deprecateBuilder.build());

			GraphQLField graphQLField = method.getAnnotation(
				GraphQLField.class);

			if (graphQLField != null) {
				builder.description(graphQLField.description());
			}

			MethodNameBuilder methodNameBuilder = new MethodNameBuilder(method);

			builder.name(methodNameBuilder.build());

			builder.type(graphQLOutputType);

			return builder.build();
		}

	}

	private class LiferayGraphQLTypeVisitor extends GraphQLTypeVisitorStub {

		public LiferayGraphQLTypeVisitor(
			GraphQLInterfaceType graphQLInterfaceType) {

			_graphQLInterfaceType = graphQLInterfaceType;
		}

		@Override
		public TraversalControl visitGraphQLObjectType(
			GraphQLObjectType graphQLObjectType,
			TraverserContext<GraphQLSchemaElement> traverserContext) {

			GraphQLFieldDefinition idGraphQLFieldDefinition =
				graphQLObjectType.getFieldDefinition("id");

			if ((idGraphQLFieldDefinition == null) ||
				(idGraphQLFieldDefinition.getType() !=
					ExtendedScalars.GraphQLLong)) {

				return TraversalControl.CONTINUE;
			}

			return changeNode(
				traverserContext,
				graphQLObjectType.transform(
					graphQLObjectTypeBuilder -> {
						_replaceFieldDefinition(
							_graphQLInterfaceType, graphQLObjectType,
							graphQLObjectTypeBuilder);
						_replaceFieldNodes(
							traverserContext.getVarFromParents(
								GraphQLCodeRegistry.Builder.class),
							_graphQLInterfaceType, graphQLObjectType,
							graphQLObjectTypeBuilder);

						graphQLObjectTypeBuilder.withInterface(
							_graphQLInterfaceType);
					}));
		}

		private final GraphQLInterfaceType _graphQLInterfaceType;

	}

	private class ServletDataServiceTrackerCustomizer
		implements ServiceTrackerCustomizer<ServletData, ServletData> {

		@Override
		public ServletData addingService(
			ServiceReference<ServletData> serviceReference) {

			ServletData servletData = _bundleContext.getService(
				serviceReference);

			synchronized (_servletDataList) {
				_servletDataList.add(servletData);

				_servlet = null;
			}

			return servletData;
		}

		@Override
		public void modifiedService(
			ServiceReference<ServletData> serviceReference,
			ServletData servletData) {
		}

		@Override
		public void removedService(
			ServiceReference<ServletData> serviceReference,
			ServletData servletData) {

			synchronized (_servletDataList) {
				_servletDataList.remove(servletData);

				_servlet = null;
			}

			_bundleContext.ungetService(serviceReference);
		}

	}

}