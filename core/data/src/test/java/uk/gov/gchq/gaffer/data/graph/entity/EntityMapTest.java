/*
 * Copyright 2017-2020 Crown Copyright
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.gchq.gaffer.data.graph.entity;

import com.google.common.collect.Sets;
import org.junit.jupiter.api.Test;

import uk.gov.gchq.gaffer.commonutil.TestGroups;
import uk.gov.gchq.gaffer.data.element.Entity;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class EntityMapTest {

    @Test
    public void shouldGetEntities() {
        final EntityMap entityMap = getEntityMap();

        final Set<Object> results = entityMap.getVertices();

        assertThat(results).contains(1);
    }

    @Test
    public void shouldGetEmptyEntitySet() {
        final EntityMap entityMap = new EntityMap();

        final Set<Object> results = entityMap.getVertices();

        assertThat(results).isEmpty();
    }

    @Test
    public void shouldGetVertices() {
        final EntityMap entityMap = getEntityMap();

        final Set<Object> results = entityMap.getVertices();

        assertThat(results).contains(1, 2, 3, 4, 5, 6);
    }

    @Test
    public void shouldContainVertex() {
        final EntityMap entityMap = getEntityMap();

        final boolean results = entityMap.containsVertex(6);

        assertThat(results).isTrue();
    }

    @Test
    public void shouldNotContainVertex() {
        final EntityMap entityMap = getEntityMap();

        final boolean results = entityMap.containsVertex(7);

        assertThat(results).isFalse();
    }

    @Test
    public void shouldPutSingleEntity() {
        // Given
        final EntityMap entityMap = new EntityMap();

        // When
        entityMap.putEntity(1, new Entity(TestGroups.ENTITY, 1));

        // Then
        assertThat(entityMap.containsVertex(1)).isTrue();
        assertThat(entityMap.get(1)).contains(new Entity(TestGroups.ENTITY, 1));
        assertThat(entityMap.getVertices()).contains(1);
    }

    @Test
    public void shouldPutMultipleEntities() {
        // Given
        final EntityMap entityMap = new EntityMap();

        // When
        entityMap.putEntities(1, Sets.newHashSet(new Entity(TestGroups.ENTITY, 1), new Entity(TestGroups.ENTITY, 2), new Entity(TestGroups.ENTITY, 3)));

        // Then
        assertThat(entityMap.containsVertex(1)).isTrue();
        assertThat(entityMap.get(1)).contains(new Entity(TestGroups.ENTITY, 1), new Entity(TestGroups.ENTITY, 2), new Entity(TestGroups.ENTITY, 3));
        assertThat(entityMap.getVertices()).contains(1);
    }

    @Test
    public void shouldPutMultipleEntities_2() {
        // Given
        final EntityMap entityMap = new EntityMap();

        // When
        entityMap.putEntities(1, Sets.newHashSet(new Entity(TestGroups.ENTITY, 1), new Entity(TestGroups.ENTITY, 2), new Entity(TestGroups.ENTITY, 3)));
        entityMap.putEntities(1, Sets.newHashSet(new Entity(TestGroups.ENTITY, 4), new Entity(TestGroups.ENTITY, 5), new Entity(TestGroups.ENTITY, 6)));

        // Then
        assertThat(entityMap.containsVertex(1)).isTrue();
        assertThat(entityMap.get(1)).contains(new Entity(TestGroups.ENTITY, 1), new Entity(TestGroups.ENTITY, 2), new Entity(TestGroups.ENTITY, 3), new Entity(TestGroups.ENTITY, 4), new Entity(TestGroups.ENTITY, 5), new Entity(TestGroups.ENTITY, 6));
        assertThat(entityMap.getVertices()).contains(1);
    }

    @Test
    public void shouldPutMultipleEntities_3() {
        // Given
        final EntityMap entityMap = new EntityMap();

        // When
        entityMap.putEntities(1, Sets.newHashSet(new Entity(TestGroups.ENTITY, 1), new Entity(TestGroups.ENTITY, 2), new Entity(TestGroups.ENTITY, 3)));
        entityMap.putEntities(2, Sets.newHashSet(new Entity(TestGroups.ENTITY, 1), new Entity(TestGroups.ENTITY, 2), new Entity(TestGroups.ENTITY, 3)));

        // Then
        assertThat(entityMap.containsVertex(1)).isTrue();
        assertThat(entityMap.get(1)).contains(new Entity(TestGroups.ENTITY, 1), new Entity(TestGroups.ENTITY, 2), new Entity(TestGroups.ENTITY, 3));
        assertThat(entityMap.get(2)).contains(new Entity(TestGroups.ENTITY, 1), new Entity(TestGroups.ENTITY, 2), new Entity(TestGroups.ENTITY, 3));
        assertThat(entityMap.getVertices()).contains(1, 2);
    }

    private EntityMap getEntityMap() {
        final EntityMap entityMap = new EntityMap();

        entityMap.putEntity(1, new Entity(TestGroups.ENTITY, 1));
        entityMap.putEntity(2, new Entity(TestGroups.ENTITY, 2));
        entityMap.putEntity(3, new Entity(TestGroups.ENTITY, 3));
        entityMap.putEntity(4, new Entity(TestGroups.ENTITY, 4));
        entityMap.putEntity(5, new Entity(TestGroups.ENTITY, 5));
        entityMap.putEntity(6, new Entity(TestGroups.ENTITY, 6));

        return entityMap;
    }
}
