package com.fulfilment.application.monolith.fulfillment.adapters.restapi;

import com.fulfilment.application.monolith.fulfillment.domain.ports.AssociateFulfillmentOperation;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
@Path("/warehouse")
public class FulfillmentResourceImpl {

  @Inject
  private AssociateFulfillmentOperation associateFulfillmentOperation;

  @POST
  @Path("/{businessUnitCode}/fulfillment")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Transactional
  public Response addFulfillment(
      @PathParam("businessUnitCode") String businessUnitCode,
      @NotNull FulfillmentRequest request) {
    Object result = associateFulfillmentOperation.associate(
        businessUnitCode, request.productId, request.storeId);
    return Response.status(201).entity(result).build();
  }
}
