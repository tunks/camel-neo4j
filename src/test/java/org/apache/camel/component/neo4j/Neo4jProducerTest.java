package org.apache.camel.component.neo4j;

import static org.mockito.Matchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.springframework.data.neo4j.support.DelegatingGraphDatabase;
import org.springframework.data.neo4j.support.Neo4jTemplate;

/**
 * @author Stephen K Samuel samspade79@gmail.com
 * 
 */
public class Neo4jProducerTest {

	private EmbeddedGraphDatabase	db;

	@Mock
	private Neo4jEndpoint		endpoint;

	@Mock
	private Exchange			exchange;

	private Neo4jProducer		producer;

	@Mock
	private Message			msg;

	@Mock
	private Neo4jTemplate		template;

	@Before
	public void before() throws IOException {
		initMocks(this);
		when(exchange.getIn()).thenReturn(msg);
		db = new EmbeddedGraphDatabase(getRandomStore());
		producer = new Neo4jProducer(endpoint, new DelegatingGraphDatabase(db), template);
	}

	String getRandomStore() throws IOException {
		File file = File.createTempFile("prefix", "suffix").getParentFile();
		File dir = new File(file.getAbsolutePath() + "/neo4j-test-" + UUID.randomUUID());
		dir.mkdir();
		return dir.getAbsolutePath();
	}

	@Test
	public void testCreateNodeEmptyBody() throws Exception {
		when(msg.getHeader(Neo4jEndpoint.HEADER_OPERATION)).thenReturn(Neo4jOperation.CREATE_NODE);
		Node node = mock(Node.class);
		when(node.getId()).thenReturn(14L);
		when(template.createNode()).thenReturn(node);
		producer.process(exchange);
		verify(template).createNode();
		verify(msg).setHeader(Neo4jEndpoint.HEADER_NODE_ID, 14L);
	}

	@Test
	public void testCreateNodePropertiesBody() throws Exception {
		when(msg.getHeader(Neo4jEndpoint.HEADER_OPERATION)).thenReturn(Neo4jOperation.CREATE_NODE);
		when(msg.getBody()).thenReturn(new HashMap<String, Object>());
		Node node = mock(Node.class);
		when(node.getId()).thenReturn(14L);
		when(template.createNode(anyMap())).thenReturn(node);
		producer.process(exchange);
		verify(template).createNode(anyMap());
		verify(msg).setHeader(Neo4jEndpoint.HEADER_NODE_ID, 14L);
	}

	@Test
	public void testCreateRelationshipWithBasicBody() throws Exception {

		when(msg.getHeader(Neo4jEndpoint.HEADER_OPERATION)).thenReturn(Neo4jOperation.CREATE_RELATIONSHIP);

		Node start = mock(Node.class);
		Node end = mock(Node.class);
		String type = "friendswith";

		BasicRelationship br = new BasicRelationship(start, end, type);
		when(msg.getBody()).thenReturn(br);

		Relationship r = mock(Relationship.class);
		when(r.getId()).thenReturn(99L);

		when(template.createRelationshipBetween(start, end, type, null)).thenReturn(r);

		producer.process(exchange);
		verify(template).createRelationshipBetween(start, end, type, null);

		verify(msg).setHeader(Neo4jEndpoint.HEADER_RELATIONSHIP_ID, 99L);
	}

	@Test
	public void testCreateRelationshipWithSpringBody() throws Exception {

		when(msg.getHeader(Neo4jEndpoint.HEADER_OPERATION)).thenReturn(Neo4jOperation.CREATE_RELATIONSHIP);

		Object start = new Object();
		Object end = new Object();
		Class entityClass = String.class;
		String type = "friendswith";

		SpringDataRelationship spring = new SpringDataRelationship(start, end, entityClass, type, true);
		when(msg.getBody()).thenReturn(spring);

		Relationship r = mock(Relationship.class);
		when(r.getId()).thenReturn(55L);

		when(template.createRelationshipBetween(start, end, entityClass, type, true)).thenReturn(r);

		producer.process(exchange);
		verify(template).createRelationshipBetween(start, end, entityClass, type, true);

		verify(msg).setHeader(Neo4jEndpoint.HEADER_RELATIONSHIP_ID, 55L);
	}

	@Test(expected = Neo4jException.class)
	public void testNullOperationFails() throws Exception {
		producer.process(exchange);
	}
}