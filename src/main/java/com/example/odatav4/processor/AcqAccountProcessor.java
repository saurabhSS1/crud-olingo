package com.example.odatav4.processor;

import com.example.odatav4.entity.AcqAccount;
import com.example.odatav4.service.AcqAccountService;
import org.apache.olingo.commons.api.data.*;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.*;
import org.apache.olingo.server.api.deserializer.DeserializerResult;
import org.apache.olingo.server.api.deserializer.ODataDeserializer;
import org.apache.olingo.server.api.processor.EntityCollectionProcessor;
import org.apache.olingo.server.api.processor.EntityProcessor;
import org.apache.olingo.server.api.serializer.EntityCollectionSerializerOptions;
import org.apache.olingo.server.api.serializer.EntitySerializerOptions;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.queryoption.FilterOption;
import org.apache.olingo.server.api.uri.queryoption.OrderByOption;
import org.apache.olingo.server.api.uri.queryoption.SkipOption;
import org.apache.olingo.server.api.uri.queryoption.TopOption;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;

@Component
public class AcqAccountProcessor implements EntityCollectionProcessor, EntityProcessor {

    @Autowired
    private AcqAccountService service;

    private OData odata;
    private ServiceMetadata serviceMetadata;

    @Override
    public void init(OData odata, ServiceMetadata serviceMetadata) {
        this.odata = odata;
        this.serviceMetadata = serviceMetadata;
    }

    @Override
    public void readEntityCollection(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
        System.out.println("Entering readEntityCollection method");
        FilterOption filterOption = uriInfo.getFilterOption();
        OrderByOption orderByOption = uriInfo.getOrderByOption();
        SkipOption skipOption = uriInfo.getSkipOption();
        TopOption topOption = uriInfo.getTopOption();

        String filter = filterOption != null ? filterOption.getText() : null;
        String orderBy = orderByOption != null ? orderByOption.getText() : null;
        Integer skip = skipOption != null ? skipOption.getValue() : null;
        Integer top = topOption != null ? topOption.getValue() : null;

        List<AcqAccount> accounts = service.getAllAccounts(filter, orderBy, top, skip);

        EntityCollection entityCollection = new EntityCollection();
        for (AcqAccount account : accounts) {
            Entity entity = new Entity()
                    .addProperty(new Property(null, "id", ValueType.PRIMITIVE, account.getId()))
                    .addProperty(new Property(null, "accountName", ValueType.PRIMITIVE, account.getAccountName()))
                    .addProperty(new Property(null, "accountType", ValueType.PRIMITIVE, account.getAccountType()))
                    .addProperty(new Property(null, "recordLastModifiedDate", ValueType.PRIMITIVE, account.getRecordLastModifiedDate()));
            entityCollection.getEntities().add(entity);
        }

        EdmEntitySet edmEntitySet = ((UriResourceEntitySet) uriInfo.getUriResourceParts().get(0)).getEntitySet();
        ODataSerializer serializer = odata.createSerializer(responseFormat);
        SerializerResult serializedResponse = serializer.entityCollection(serviceMetadata, edmEntitySet.getEntityType(), entityCollection, EntityCollectionSerializerOptions.with().contextURL(ContextURL.with().entitySet(edmEntitySet).build()).build());

        response.setContent(serializedResponse.getContent());
        response.setStatusCode(200);
        response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
        System.out.println("Exiting readEntityCollection method");
    }

    @Override
    public void readEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
        System.out.println("Entering readEntity method");
        UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) uriInfo.getUriResourceParts().get(0);
        String id = uriResourceEntitySet.getKeyPredicates().get(0).getText();

        AcqAccount account = service.getAccountById(id).orElseThrow(() -> new ODataApplicationException("Account not found", 404, Locale.ENGLISH));

        Entity entity = new Entity()
                .addProperty(new Property(null, "id", ValueType.PRIMITIVE, account.getId()))
                .addProperty(new Property(null, "accountName", ValueType.PRIMITIVE, account.getAccountName()))
                .addProperty(new Property(null, "accountType", ValueType.PRIMITIVE, account.getAccountType()))
                .addProperty(new Property(null, "recordLastModifiedDate", ValueType.PRIMITIVE, account.getRecordLastModifiedDate()));

        ODataSerializer serializer = odata.createSerializer(responseFormat);
        EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();
        SerializerResult serializedContent = serializer.entity(serviceMetadata, edmEntitySet.getEntityType(), entity, EntitySerializerOptions.with().contextURL(ContextURL.with().serviceRoot(URI.create(request.getRawBaseUri() + "/odata")).keyPath(id).build()).build());

        response.setContent(serializedContent.getContent());
        response.setStatusCode(200);
        response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
        System.out.println("Exiting readEntity method");
    }

    @Override
    public void createEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType requestFormat, ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
        System.out.println("Entering createEntity method");

        // Logging the raw payload
        InputStream requestBody = request.getBody();
        try {
            byte[] rawPayload = requestBody.readAllBytes();
            System.out.println("Raw Payload: " + new String(rawPayload));
            requestBody = new ByteArrayInputStream(rawPayload); // Reset stream
        } catch (Exception e) {
            System.out.println("Error reading raw payload: " + e.getMessage());
        }

        EdmEntitySet edmEntitySet = ((UriResourceEntitySet) uriInfo.getUriResourceParts().get(0)).getEntitySet();
        System.out.println("EntitySet: " + edmEntitySet.getName());

        // Deserialize the request payload into an Olingo entity
        ODataDeserializer deserializer = odata.createDeserializer(requestFormat);
        System.out.println("Deserializer created");

        DeserializerResult result = deserializer.entity(requestBody, edmEntitySet.getEntityType());
        System.out.println("Request payload deserialized");

        Entity requestEntity = result.getEntity();
        System.out.println("Request entity obtained: " + requestEntity);

        // Logging the requestEntity properties
        System.out.println("Request Entity Properties: ");
        for (Property property : requestEntity.getProperties()) {
            System.out.println("Property Name: " + property.getName() + ", Property Value: " + property.getValue());
        }

        // Convert Olingo entity to JPA AcqAccount entity
        Property idProperty = requestEntity.getProperty("id");
        Property accountNameProperty = requestEntity.getProperty("accountName");
        Property accountTypeProperty = requestEntity.getProperty("accountType");
        Property recordLastModifiedDateProperty = requestEntity.getProperty("recordLastModifiedDate");

        // Log each property and check for null values
        if (idProperty == null) {
            System.out.println("id property is null");
        } else {
            System.out.println("id property value: " + idProperty.getValue());
        }
        if (accountNameProperty == null) {
            System.out.println("accountName property is null");
        } else {
            System.out.println("accountName property value: " + accountNameProperty.getValue());
        }
        if (accountTypeProperty == null) {
            System.out.println("accountType property is null");
        } else {
            System.out.println("accountType property value: " + accountTypeProperty.getValue());
        }
        if (recordLastModifiedDateProperty == null) {
            System.out.println("recordLastModifiedDate property is null");
        } else {
            System.out.println("recordLastModifiedDate property value: " + recordLastModifiedDateProperty.getValue());
        }

        if (idProperty == null || accountNameProperty == null || accountTypeProperty == null || recordLastModifiedDateProperty == null) {
            throw new ODataApplicationException("Missing required properties", HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ENGLISH);
        }

        AcqAccount newAccount = new AcqAccount();
        newAccount.setId((String) idProperty.getValue());
        newAccount.setAccountName((String) accountNameProperty.getValue());
        newAccount.setAccountType((String) accountTypeProperty.getValue());
        newAccount.setRecordLastModifiedDate(OffsetDateTime.parse(recordLastModifiedDateProperty.getValue().toString()));

        // Save the entity
        System.out.println("Saving new account");
        AcqAccount createdAccount = service.createAccount(newAccount);

        // Convert JPA entity back to Olingo entity
        Entity responseEntity = new Entity()
                .addProperty(new Property(null, "id", ValueType.PRIMITIVE, createdAccount.getId()))
                .addProperty(new Property(null, "accountName", ValueType.PRIMITIVE, createdAccount.getAccountName()))
                .addProperty(new Property(null, "accountType", ValueType.PRIMITIVE, createdAccount.getAccountType()))
                .addProperty(new Property(null, "recordLastModifiedDate", ValueType.PRIMITIVE, createdAccount.getRecordLastModifiedDate().toString()));

        // Serialize the response
        System.out.println("Serializing response");
        ODataSerializer serializer = odata.createSerializer(responseFormat);
        SerializerResult serializedResponse = serializer.entity(serviceMetadata, edmEntitySet.getEntityType(), responseEntity, EntitySerializerOptions.with().contextURL(ContextURL.with().serviceRoot(URI.create(request.getRawBaseUri() + "/odata")).keyPath(createdAccount.getId()).build()).build());

        response.setContent(serializedResponse.getContent());
        response.setStatusCode(201);
        response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
        System.out.println("Exiting createEntity method");
    }

    @Override
    public void updateEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType requestFormat, ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
        System.out.println("Entering updateEntity method");
        EdmEntitySet edmEntitySet = ((UriResourceEntitySet) uriInfo.getUriResourceParts().get(0)).getEntitySet();
        String id = ((UriResourceEntitySet) uriInfo.getUriResourceParts().get(0)).getKeyPredicates().get(0).getText();

        // Deserialize the request payload into an Olingo entity
        ODataDeserializer deserializer = odata.createDeserializer(requestFormat);
        DeserializerResult result = deserializer.entity(request.getBody(), edmEntitySet.getEntityType());
        Entity requestEntity = result.getEntity();

        // Convert Olingo entity to JPA AcqAccount entity
        AcqAccount updatedAccount = new AcqAccount();
        updatedAccount.setId(id);
        updatedAccount.setAccountName((String) requestEntity.getProperty("accountName").getValue());
        updatedAccount.setAccountType((String) requestEntity.getProperty("accountType").getValue());
        updatedAccount.setRecordLastModifiedDate(OffsetDateTime.parse(requestEntity.getProperty("recordLastModifiedDate").getValue().toString()));

        // Update the entity
        AcqAccount updated = service.updateAccount(id, updatedAccount);

        // Convert JPA entity back to Olingo entity
        Entity responseEntity = new Entity()
                .addProperty(new Property(null, "id", ValueType.PRIMITIVE, updated.getId()))
                .addProperty(new Property(null, "accountName", ValueType.PRIMITIVE, updated.getAccountName()))
                .addProperty(new Property(null, "accountType", ValueType.PRIMITIVE, updated.getAccountType()))
                .addProperty(new Property(null, "recordLastModifiedDate", ValueType.PRIMITIVE, updated.getRecordLastModifiedDate().toString()));

        // Serialize the response
        ODataSerializer serializer = odata.createSerializer(responseFormat);
        SerializerResult serializedResponse = serializer.entity(serviceMetadata, edmEntitySet.getEntityType(), responseEntity, EntitySerializerOptions.with().contextURL(ContextURL.with().serviceRoot(URI.create(request.getRawBaseUri() + "/odata")).keyPath(id).build()).build());

        response.setContent(serializedResponse.getContent());
        response.setStatusCode(200);
        response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
        System.out.println("Exiting updateEntity method");
    }
    @Override
    public void deleteEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo) throws ODataApplicationException, ODataLibraryException {
        UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) uriInfo.getUriResourceParts().get(0);
        String id = uriResourceEntitySet.getKeyPredicates().get(0).getText();

        // Delete the entity
        service.deleteAccount(id);

        // Ensure no content in response
        response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());
        response.setHeader(HttpHeader.CONTENT_TYPE, "text/plain");
        response.setContent(new ByteArrayInputStream(new byte[0]));
        response.setHeader(HttpHeader.CONTENT_LENGTH, "0");
    }

}
