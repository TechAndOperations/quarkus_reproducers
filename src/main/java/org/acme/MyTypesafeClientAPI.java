package org.acme;

import java.util.List;

import org.eclipse.microprofile.graphql.NonNull;
import org.eclipse.microprofile.graphql.Query;

import io.smallrye.graphql.client.typesafe.api.GraphQLClientApi;

/**
 * Typesafe GraphQL client for the My Gateway.
 *
 * <p>The configuration key {@code foo_bar} maps to the
 * {@code quarkus.smallrye-graphql-client.foo_bar.*} properties.
 */
@GraphQLClientApi(configKey = "foo_bar")
public interface MyTypesafeClientAPI {

    @Query(value = "objects")
    List<MyDTO> fetchObjectByRef(@NonNull List<@NonNull String> objectRefs);

}
