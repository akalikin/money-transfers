package com.revolut.akalikin.controller;

import com.google.inject.Inject;
import com.revolut.akalikin.exception.AccountAlreadyExistsException;
import com.revolut.akalikin.exception.AccountInsufficientFundsException;
import com.revolut.akalikin.exception.AccountNotFoundException;
import com.revolut.akalikin.exception.InvalidRequestException;
import com.revolut.akalikin.exception.PermanentException;
import com.revolut.akalikin.exception.TransientException;
import com.revolut.akalikin.model.Account;
import com.revolut.akalikin.operation.ReadOperation;
import com.revolut.akalikin.operation.TransferOperation;
import com.revolut.akalikin.operation.WriteOperation;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PATCH;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.Optional;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.CONFLICT;
import static javax.ws.rs.core.Response.Status.CREATED;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.OK;
import static javax.ws.rs.core.Response.Status.PRECONDITION_FAILED;

/**
 * Controller for the available APIs.
 * Very useful additions would be Logging, such as request logging;
 * and Metrics for response counts, operation latencies etc.
 */
@Path("v1")
public class AccountTransferServiceController {

    private final TransferOperation transferOperation;
    private final ReadOperation readOperation;
    private final WriteOperation writeOperation;

    @Inject
    public AccountTransferServiceController(
            ReadOperation readOperation, TransferOperation transferOperation, WriteOperation writeOperation) {
        this.readOperation = readOperation;
        this.transferOperation = transferOperation;
        this.writeOperation = writeOperation;
    }

    @GET
    @Path("accounts")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAccounts() {
        Collection<Account> accounts = readOperation.readAccounts();
        return response(OK, accounts);
    }

    @GET
    @Path("accounts/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAccount(@PathParam("id") String id) {
        try {
            Account account = readOperation.readAccount(id);
            return response(OK, account);
        } catch (AccountNotFoundException e) {
            return response(NOT_FOUND, e.toString());
        } catch (InvalidRequestException e) {
            return response(BAD_REQUEST, e.toString());
        } catch (Exception e) {
            return response(INTERNAL_SERVER_ERROR, e.toString());
        }
    }

    @POST
    @Path("accounts/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createAccount(@PathParam("id") String id, Long initialBalance) {
        try {
            Account createdAccount = writeOperation.createAccount(id, Optional.ofNullable(initialBalance));
            return response(CREATED, createdAccount);
        } catch (AccountAlreadyExistsException e) {
            return response(CONFLICT, e.toString());
        } catch (TransientException | PermanentException | Exception e) {
            return response(INTERNAL_SERVER_ERROR, e.toString());
        }
    }

    @PATCH
    @Path("accounts/from/{from}/to/{to}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response transfer(@PathParam("from") String fromId, @PathParam("to") String toId, Long amount) {
        try {
            transferOperation.executeMoneyTransfer(fromId, toId, amount);
            return response(OK, null);
        } catch (AccountNotFoundException e) {
            return response(NOT_FOUND, e.toString());
        } catch (AccountInsufficientFundsException e) {
            // Not a true Precondition failed response,
            // as it's not in response to a condition specified in the request
            return response(PRECONDITION_FAILED, e.toString());
        } catch (InvalidRequestException e) {
            return response(BAD_REQUEST, e.toString());
        } catch (TransientException | PermanentException | Exception e) {
            return response(INTERNAL_SERVER_ERROR, e.toString());
        }
    }

    private Response response(Response.Status status, Object entity) {
        Response.ResponseBuilder responseBuilder = Response.status(status);
        if (entity != null) {
            responseBuilder.entity(entity);
        }
        return responseBuilder.build();
    }

}
